package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.app.service.transform.NumericalConversion;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.enums.HistogramStatistic;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.request.TransactionStatisticsDetailRequest;
import com.zqykj.common.response.*;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.domain.*;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.app.service.interfaze.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.interfaze.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.interfaze.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.interfaze.factory.QueryRequestParamFactory;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 交易统计实现类
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionStatisticsImpl implements ITransactionStatistics {


    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    private static final String CARDINALITY_TOTAL = "cardinality_total";

    private static final int MAIN_CARD_SIZE = 5000;


    @Override
    public TransactionStatisticsResponse calculateStatisticalResults(String caseId, TransactionStatisticsRequest transactionStatisticsRequest) {
        TradeStatisticalAnalysisPreRequest tradeStatisticalAnalysisPreRequest = transactionStatisticsRequest.getTradeStatisticalAnalysisPreRequest();
        TransactionStatisticsAggs transactionStatisticsAggs = transactionStatisticsRequest.getTransactionStatisticsAggs();
        TimeTypeRequest timeTypeRequest = transactionStatisticsAggs.getDateType();

        /**
         * 获取交易金额聚合统计直方图结果.
         * */
        FundTacticsPartGeneralPreRequest tacticsPartGeneralPreRequest = new FundTacticsPartGeneralPreRequest();
        BeanUtils.copyProperties(tradeStatisticalAnalysisPreRequest, tacticsPartGeneralPreRequest);
        HistogramStatisticResponse histogramStatisticResponse = this.getHistogramStatistics(caseId, tacticsPartGeneralPreRequest, transactionStatisticsAggs);

        /**
         * 获取日期折现图聚合统计结果.
         * */
        FundDateRequest fundAnalysisDateRequest = new FundDateRequest();
        BeanUtils.copyProperties(tradeStatisticalAnalysisPreRequest, fundAnalysisDateRequest);
        fundAnalysisDateRequest.setTimeType(timeTypeRequest);
        TradeStatisticalAnalysisFundSumByDate tradeAmountByTime = this.getSummaryOfTradeAmountGroupedByTime(caseId, fundAnalysisDateRequest);

        TimeGroupTradeAmountSum timeGroupTradeAmountSum = new TimeGroupTradeAmountSum();

        timeGroupTradeAmountSum.setDates(tradeAmountByTime.getDates());
        timeGroupTradeAmountSum.setTradeAmounts(tradeAmountByTime.getTradeAmounts());

        // todo 获取卡聚合统计列表

        /**
         * 交易统计返回结果封装.
         * */
        TransactionStatisticsResponse transactionStatisticsResponse = new TransactionStatisticsResponse(histogramStatisticResponse, timeGroupTradeAmountSum, null);


        return transactionStatisticsResponse;
    }


    @Override
    public HistogramStatisticResponse getHistogramStatistics(String caseId, FundTacticsPartGeneralPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs) {
        List<HistogramStatistic> responseList = new ArrayList<>();
        HistogramStatisticResponse histogramStatisticResponse = new HistogramStatisticResponse();

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);
        /**
         * 根据查询条件计算出当前数据中最大值.
         * */
        Map<String, ParsedStats> map = entranceRepository.statsAggs(query, Constants.Individual.FOURTH_AGGREGATE_NAME,
                caseId, BankTransactionFlow.class);
        ParsedStats parsedStats = map.get(Constants.BucketName.STATS);
        Double max = parsedStats.getMax();

        /**
         * 然后根据最大值和传入的区间个数来获取range范围,从而作为直方图聚合参数range的入参.
         * */
        List<Range> ranges = NumericalConversion.intervalConversion(max, transactionStatisticsAggs.getHistorgramNumbers());

        /**
         * 根据range参数和构建好的queryParams做聚合统计算出日志直方图结果.
         * */
        Map queryResultMap = entranceRepository.rangeAggs(query, Constants.Individual.FOURTH_AGGREGATE_NAME
                , caseId,
                ranges, BankTransactionFlow.class);
        /**
         * 转换结果封装业务层数据给前台.
         * */
        queryResultMap.forEach((key, value) -> {
            HistogramStatistic histogramStatistic = HistogramStatistic.builder()
                    .abscissa((String) key)
                    .ordinate((Long) value)
                    .build();
            responseList.add(histogramStatistic);
        });
        histogramStatisticResponse.setHistogramStatisticList(responseList);

        return histogramStatisticResponse;

    }

    @Override
    public QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, FundTacticsPartGeneralPreRequest request) {
        // 构建查询参数
        return queryRequestParamFactory.createTradeAmountByTimeQuery(request, caseId);
    }

    @Override
    public Page<BankTransactionFlow> accessTransactionStatisticDetail(String caseId, TransactionStatisticsDetailRequest transactionStatisticsDetailRequest) throws Exception {


        PageRequest pageRequest = PageRequest.of(transactionStatisticsDetailRequest.getQueryRequest().getPaging().getPage(),
                transactionStatisticsDetailRequest.getQueryRequest().getPaging().getPageSize(),
                transactionStatisticsDetailRequest.getQueryRequest().getSorting().getOrder().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                transactionStatisticsDetailRequest.getQueryRequest().getSorting().getProperty());
        Page<BankTransactionFlow> page = entranceRepository.findByCondition(pageRequest, caseId, BankTransactionFlow.class
                , transactionStatisticsDetailRequest.getCardNumber(), caseId, transactionStatisticsDetailRequest.getQueryRequest().getKeyword());
        return page;

    }

    @Override
    public TradeStatisticalAnalysisFundSumByDate getSummaryOfTradeAmountGroupedByTime(String caseId, FundDateRequest request) {

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);

        // 构建日期聚合参数
        AggregationParams dateAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisFundByTimeType(request);

        Map<String, String> mapping = new LinkedHashMap<>();

        aggregationEntityMappingFactory.buildTradeStatisticsFundTimeAggMapping(mapping, TradeStatisticalAnalysisFundSumByDate.class);

        dateAgg.setMapping(mapping);

        dateAgg.setResultName("fundSumByDate");

        // 构建  DateSpecificFormat对象
        List<List<Object>> result = entranceRepository.dateGroupAgg(query, dateAgg, BankTransactionFlow.class, caseId);

        TradeStatisticalAnalysisFundSumByDate groupTradeAmountSum = new TradeStatisticalAnalysisFundSumByDate();

        List<Object> dates = result.get(0);
        List<Object> tradeAmounts = result.get(1);

        if (!CollectionUtils.isEmpty(result)) {
            if (TimeTypeRequest.h == request.getTimeType()) {

                Map<String, BigDecimal> map = new HashMap<>();

                for (int i = 0; i < dates.size(); i++) {

                    String date = dates.get(i).toString();
                    String curTradeAmount = tradeAmounts.get(i).toString();
                    if (map.containsKey(date)) {
                        String OldTradeAmount = map.get(date).toString();
                        map.put(date, BigDecimalUtil.add(OldTradeAmount, curTradeAmount));
                    } else {
                        map.put(date, BigDecimalUtil.value(curTradeAmount));
                    }
                }
                LinkedHashSet<String> sortDates = map.keySet().stream().sorted(Comparator.comparing(Integer::valueOf)).collect(Collectors.toCollection(LinkedHashSet::new));
                groupTradeAmountSum.setDates(sortDates);
                List<BigDecimal> sortTradeAmounts = new ArrayList<>();
                sortDates.forEach(e -> sortTradeAmounts.add(map.get(e)));
                groupTradeAmountSum.setTradeAmounts(sortTradeAmounts);
            } else {
                Set<String> dateSets = dates.stream().map(Object::toString).collect(Collectors.toCollection(LinkedHashSet::new));
                groupTradeAmountSum.setDates(dateSets);
                List<BigDecimal> funds = tradeAmounts.stream().map(x -> BigDecimalUtil.value(x.toString()))
                        .collect(Collectors.toList());
                groupTradeAmountSum.setTradeAmounts(funds);
            }
        }

        return groupTradeAmountSum;
    }

    public ServerResponse getTransactionStatisticsAnalysisResult(String caseId, TradeStatisticalAnalysisQueryRequest request) {

        TradeStatisticalAnalysisResultResponse resultResponse = new TradeStatisticalAnalysisResultResponse();

        List<TradeStatisticalAnalysisResult> results;
        long total;
        if (request.getSearchType() == 0 && !CollectionUtils.isEmpty(request.getCardNums())) {

            Map<String, Object> map = statisticsAnalysisResultViaChosenMainCards(request, caseId);

            results = (List<TradeStatisticalAnalysisResult>) map.get("result");
            total = (long) map.get("total");
        } else {

            // TODO 全部条件
            Map<String, Object> map = statisticsAnalysisResultViaAllMainCards(request, caseId);
            results = (List<TradeStatisticalAnalysisResult>) map.get("result");
            total = (long) map.get("total");
        }
        Integer pageSize = request.getPageRequest().getPageSize();
        // 结果集
        resultResponse.setContent(results);
        // 每页显示条数
        resultResponse.setSize(pageSize);
        // 总数据量
        resultResponse.setTotal(total);
        // 总页数
        resultResponse.setTotalPages(PageRequest.getTotalPages(total, pageSize));
        return ServerResponse.createBySuccess(resultResponse);
    }

    /**
     * <h2> 选择个体 / 选择部分调单卡号集合 </h2>
     */
    @SuppressWarnings("all")
    private Map<String, Object> statisticsAnalysisResultViaChosenMainCards(TradeStatisticalAnalysisQueryRequest request, String caseId) {

        // 设置分组桶的大小
        request.setGroupInitSize(initGroupSize);

        // 构建 交易统计分析查询请求
        QuerySpecialParams tradeStatisticsQuery = queryRequestParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId);

        // 构建 交易统计分析聚合查询请求
        AggregationParams tradeStatisticsAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisByMainCards(request);

        // 构建 mapping (聚合名称 -> 聚合属性)  , (实体属性 -> 聚合名称)
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggMapping, entityMapping, TradeStatisticalAnalysisResult.class);
        tradeStatisticsAgg.setMapping(aggMapping);
        tradeStatisticsAgg.setEntityAggColMapping(entityMapping);
        tradeStatisticsAgg.setResultName("chosen_main_cards");

        // 设置同级聚合(计算总数据量)
        AggregationParams totalAgg = total(request);
        if (null != totalAgg) {
            tradeStatisticsAgg.addSiblingAggregation(totalAgg);
        }
        //
        Map<String, List<List<Object>>> results = entranceRepository.compoundQueryAndAgg(tradeStatisticsQuery, tradeStatisticsAgg, BankTransactionRecord.class, caseId);

        // 聚合返回结果
        List<List<Object>> returnResults = results.get(tradeStatisticsAgg.getResultName());

        // 一组实体属性集合 与 聚合名称顺序是一一对应的( 所以聚合返回的结果每一列值的属性 与 实体属性也是对应的, 处理聚合展示字段需要特殊处理)
        List<String> entityTitles = new ArrayList<>(entityMapping.keySet());

        // 实体属性值映射
        List<Map<String, Object>> entityPropertyMapping = aggregationResultEntityParseFactory.convertEntity(returnResults, entityTitles, TradeStatisticalAnalysisResult.class);

        // 反序列化实体
        List<TradeStatisticalAnalysisResult> tradeStatisticalAnalysisResults = JacksonUtils.parse(JacksonUtils.toJson(entityPropertyMapping), new TypeReference<List<TradeStatisticalAnalysisResult>>() {
        });

        // 将金额保留2位小数
        tradeStatisticalAnalysisResults.forEach(TradeStatisticalAnalysisResult::amountReservedTwo);

        Map<String, Object> map = new HashMap<>();

        List<List<Object>> total = results.get(CARDINALITY_TOTAL);

        if (CollectionUtils.isEmpty(total)) {
            map.put("total", 0);
        } else {
            map.put("total", total.get(0).get(0));
        }
        map.put("result", tradeStatisticalAnalysisResults);
        return map;
    }

    /**
     * <h2> 按照本方查询卡号(即是全部调单卡号进行分析) </h2>
     * <p>
     * 分析的结果: 其中交易卡号出现的必须是调单的(无论它原来是在本方还是对方)
     */
    private Map<String, Object> statisticsAnalysisResultViaAllMainCards(TradeStatisticalAnalysisQueryRequest request, String caseId) {

        // 由于无法一次性获取全部调单卡号, 因此批量获取调单卡号集合 // TODO 暂定为每次5000-10000
        boolean flag = true;
        List<TradeStatisticalAnalysisResult> allResults = new ArrayList<>();
        int page = request.getPageRequest().getPage();
        int size = request.getPageRequest().getPageSize();
        int from = 0;
        while (flag) {

            // TODO 当allResults 大于等于 一定量的时候,为了避免内存溢出(或者需要调正JVM的话),需要设置一个阈值
            // 循环获取调单卡号集合
            request.getPageRequest().setPage(from);
            request.getPageRequest().setPageSize(MAIN_CARD_SIZE);
            List<String> mainCards = fundTacticsAnalysis.getAllMainCardsViaPageable(request, caseId);
            if (mainCards.size() < MAIN_CARD_SIZE) {
                flag = false;
            }
            // 调用 statisticsAnalysisResultViaChosenMainCards 方法获取结果
            // 设置调单卡号集合
            request.setCardNums(mainCards);
            Map<String, Object> map = statisticsAnalysisResultViaChosenMainCards(request, caseId);
            List<TradeStatisticalAnalysisResult> results = (List<TradeStatisticalAnalysisResult>) map.get("result");
            allResults.addAll(results);
            from += MAIN_CARD_SIZE;
        }
        // 重新设置page 和 size
        request.getPageRequest().setPage(page);
        request.getPageRequest().setPageSize(size);
        // 进行内存的合并与排序
        Map<String, List<TradeStatisticalAnalysisResult>> groupResult = allResults.stream().collect(Collectors.groupingBy(TradeStatisticalAnalysisResult::getTradeCard));
        // 最终合并的结果
        List<TradeStatisticalAnalysisResult> mergeResult = new ArrayList<>(groupResult.size());
        groupResult.forEach((key, value) -> {

            if (value.size() > 1) {
                //
                mergeResult.add(TradeStatisticalAnalysisResult.mergeTradeStatisticalAnalysisBankFlow(value));
            } else {
                TradeStatisticalAnalysisResult tradeStatisticalAnalysisResult = value.get(0);
                // 保留2位小数
                TradeStatisticalAnalysisResult.amountReservedTwo(tradeStatisticalAnalysisResult);
                mergeResult.add(tradeStatisticalAnalysisResult);
            }
        });
        // 排序与分页
        List<TradeStatisticalAnalysisResult> results = TradeStatisticalAnalysisResult.tradeStatisticalAnalysisResultSortAndPage(mergeResult, request.getPageRequest(), request.getSortRequest());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", results);
        resultMap.put("total", Long.parseLong(String.valueOf(mergeResult.size())));
        return resultMap;
    }

    /**
     * <h2> 获取交易统计分析结果总数据量 </h2>
     */
    private AggregationParams total(TradeStatisticalAnalysisQueryRequest request) {

        if (null == request) {
            return null;
        }
        AggregationParams totalAgg = aggregationRequestParamFactory.buildTradeStatisticsAnalysisTotalAgg(request);
        totalAgg.setMapping(aggregationEntityMappingFactory.buildFundTacticsAnalysisResultTotalAggMapping());
        totalAgg.setResultName(CARDINALITY_TOTAL);
        return totalAgg;
    }
}
