/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFastInFastOut;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.bank.FastInFastOutRecord;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * <h1> 战法快进快出 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FastInFastOutImpl implements IFastInFastOut {

    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // es group by 分组 数量
    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    // 快进快出生成结果条数(数值排序有6中规则(降序和升序),流入金额、流出金额、流出日期)
    // 每种规则生成10W数据,比如调单卡号作为来源情况满的话,会有60w数据,可能有重复,需要去重
    // 那么调单卡号作为中转、作为沉淀 数据满的情况下,3种情况共计180万数据(数据足够)
    // 不可能不停的取,直至取满 (快进快出这个逻辑太奇葩 eg. 调单卡号作为中转卡号, 进账有1000条, 出账有1000条, 那就是1000 * 1000 条.....)
    // 你需要从中筛选出符合时间间隔、特征比的记录数...
    // 实际数据量可能更奇葩(可能导入文件数据才20W,全部查询的时候会出现300多万的快进快出数据量(如果算满的话)
    @Value("${fastInout.result_chunkSize}")
    private int resultChunkSize;

    // 按照进或者出排序后需要取出的数据量
    @Value("${fastInout.order_chunkSize}")
    private int orderChunkSize;

    // 每次查询涉及的卡数量
    private final int perQueryCardNum = 20;

    // 每次批量查询的条数
    private final int perQueryNum = 5000;

    public ServerResponse fastInFastOutAnalysis(FastInFastOutRequest request) throws ExecutionException, InterruptedException {


        List<String> adjustCards = request.getCardNum();
        String caseId = request.getCaseId();
        int interceptCardPosition = 0;
        // TODO 需要判断
        // TODO 等待调单卡号作为来源、中转、沉淀的快进快出记录生成完毕之后 (再根据 FastInFastOutRequest 筛选符合的快进快出结果)
        // 根据配置值生成固定数量的快进快出记录
        String tradeAmount = FundTacticsAnalysisField.CHANGE_MONEY;
        String tradingTime = FundTacticsAnalysisField.TRADING_TIME;
        // 来源的

        // 中转的
        saveResultViaTransit(adjustCards, caseId, orderChunkSize, interceptCardPosition, true, tradeAmount);
//        saveResultViaTransit(adjustCards, caseId, orderChunkSize, interceptCardPosition, false, tradeAmount);
//        saveResultViaTransit(adjustCards, caseId, orderChunkSize, interceptCardPosition, false, tradingTime);

        // 沉淀的

        if (request.getType() == 1) {
            // 当快进快出数据统计表完成后, 开始统计
            return ServerResponse.createBySuccess(queryFastInFastOutResult(request));
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * <h2> 生成按流入金额/流出金额/流出日期 排序的快进快出记录 </h2>
     * <p>
     * 按调单卡号进账金额排序
     */
    private Map<String, Map<String, Object>> saveResultViaTransit(List<String> adjustCards, String caseId, int orderChunkSize, int interceptPosition,
                                                                  boolean isInflow, String property) {

        Map<String, Map<String, Object>> inflowAmountMap = new HashMap<>();
        CompletableFuture<Map<String, Object>> inflowAmountDescFutures = CompletableFuture.supplyAsync(() -> {
            Map<String, Object> inflowAmountDescMap = new HashMap<>();
            // 当前排序分页取的前多少条
            inflowAmountDescMap.put("orderChunkSize", orderChunkSize);
            // 从上面的结果中取部分记录(截取记录的位置)
            inflowAmountDescMap.put("interceptPosition", interceptPosition);
            Map<String, Object> map = saveFastInOutFromTransit(adjustCards, caseId, orderChunkSize, interceptPosition, inflowAmountDescMap,
                    isInflow, property, Sort.Direction.DESC);
            inflowAmountMap.put("inflowAmountDesc", map);
            return map;
        });
        // 查看这些卡号的进账的数量
        // 如果 count <= orderChunkSize, 那么降序 和 升序 的数据只需要入一次
        long count = getInOutCountViaQueryCards(adjustCards, caseId, isInflow);
        if (count > orderChunkSize) {
            CompletableFuture<Map<String, Object>> inflowAmounAscFutures = CompletableFuture.supplyAsync(() -> {
                // 流入金额升序排序生成快进快出记录
                Map<String, Object> inflowAmountAscMap = new HashMap<>();
                // 当前排序分页取的前多少条
                inflowAmountAscMap.put("orderChunkSize", orderChunkSize);
                // 从上面的结果中取部分记录(截取记录的位置)
                inflowAmountAscMap.put("interceptPosition", interceptPosition);
                Map<String, Object> map = saveFastInOutFromTransit(adjustCards, caseId, orderChunkSize, interceptPosition, inflowAmountAscMap,
                        isInflow, property, Sort.Direction.ASC);
                inflowAmountMap.put("inflowAmountAsc", map);
                return map;
            });
        }
        return inflowAmountMap;
    }

    /**
     * <h2> 调单卡号作为中转卡号情况 保存快进快出记录 </h2>
     */
    private Map<String, Object> saveFastInOutFromTransit(List<String> adjustCards, String caseId, int orderChunkSize, int interceptPosition,
                                                         Map<String, Object> transitMap, boolean isInflow, String sortProperty, Sort.Direction direction) {

        // 流入金额/流出金额/流出日期 降序排序生成快进快出记录
        List<BankTransactionRecord> inOutflowOrder = getInOutRecordOrderViaQueryCards(adjustCards, caseId, isInflow, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(inOutflowOrder)) {
            return transitMap;
        }
        // 去重 inOutflowOrder 中的查询卡号,查询这些卡号的进账/出账记录数
        List<String> distinctQueryCard = inOutflowOrder.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
        Map<String, Integer> queryCardInOutTimes = getInOutTotalTimesViaQueryCards(distinctQueryCard, caseId, !isInflow);
        if (CollectionUtils.isEmpty(queryCardInOutTimes)) {
            return transitMap;
        }
        // 查看 inOutflowOrder 前 interceptCardPosition 到 perQueryCardNum 的数量的查询卡号的出账总次数是否 == resultChunkSize
        // 当大于 resultChunkSize 的时候截止,或者 queryCardOutTimes的size 扫描结束为止
        int resultTotal = 0;
        // 需要查询出账的卡集合
        List<String> requireQueryInOutCards = new ArrayList<>();
        List<BankTransactionRecord> inOutflowOrderInRecords = new ArrayList<>();
        int recordNextInterceptPosition = interceptPosition;
        for (BankTransactionRecord record : inOutflowOrder) {
            if (resultTotal >= resultChunkSize) {
                break;
            }
            Integer outTimes = queryCardInOutTimes.get(record.getQueryCard());
            if (outTimes > 0) {
                inOutflowOrderInRecords.add(record);
                recordNextInterceptPosition += 1;
                resultTotal += outTimes;
                requireQueryInOutCards.add(record.getQueryCard());
            }
        }
        // 记录 下一次的interceptPosition
        transitMap.put("interceptPosition", recordNextInterceptPosition);
        List<BankTransactionRecord> inOutflowOrderInOutRecords = asyncQueryInOutRecord(resultTotal, requireQueryInOutCards, caseId, isInflow);
        if (CollectionUtils.isEmpty(inOutflowOrderInOutRecords)) {
            return transitMap;
        }
        // 生成快进快出记录(调单作为中转卡号-流入金额/流出金额/流出日期 降序/升序情况)
        Map<Integer, Integer> hashValueMaps = generateFastInOutFromTransit(inOutflowOrderInRecords, inOutflowOrderInOutRecords, caseId, isInflow);
        transitMap.put("hashValueMaps", hashValueMaps);
        return transitMap;
    }

    /**
     * <h2> 批量查询卡号的进出记录 </h2>
     */
    @SuppressWarnings("all")
    private List<BankTransactionRecord> asyncQueryInOutRecord(int resultTotal, List<String> requireQueryInOutCards, String caseId, boolean isInflow) {

        int position = 0;
        List<CompletableFuture<List<BankTransactionRecord>>> futures = new ArrayList<>();
        while (position < resultTotal) {
            // 查询这些卡号的入账(批量查询,每次查询 perQueryNum)
            int next = Math.min(position + perQueryNum, resultTotal);

            // 批量查询这些卡号的出账记录
            int finalPosition = position;
            CompletableFuture<List<BankTransactionRecord>> future = CompletableFuture.supplyAsync(() ->
                    getInOutRecordViaQueryCards(requireQueryInOutCards, caseId, !isInflow, finalPosition, next));
            futures.add(future);
            position = next;
        }
        List<BankTransactionRecord> inOutflowOrderInOutRecords = new ArrayList<>();
        futures.forEach(record -> {
            try {
                inOutflowOrderInOutRecords.addAll(record.get());
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("async query card out records exception from adjust card!");
            }
        });
        return inOutflowOrderInOutRecords;
    }

    /**
     * <h2> 生成快进快出记录 入统计表 {@link FastInFastOutRecord} </h2>
     * <p>
     * 调单卡号作为中转卡号的情况
     *
     * @return 返回一条快进快出记录的hash值
     */
    private Map<Integer, Integer> generateFastInOutFromTransit(List<BankTransactionRecord> first, List<BankTransactionRecord> second,
                                                               String caseId, boolean isInflow) {

        // 将second 转成map,避免 n * n, 改成 2 * n
        Map<String, List<BankTransactionRecord>> maps = new HashMap<>();
        second.forEach(e -> {

            if (maps.containsKey(e.getQueryCard())) {
                maps.get(e.getQueryCard()).add(e);
            } else {
                List<BankTransactionRecord> bankTransactionRecords = new ArrayList<>();
                bankTransactionRecords.add(e);
                maps.put(e.getQueryCard(), bankTransactionRecords);
            }
        });
        // 快进快出记录
        List<Map<String, ?>> fastInFastOutRecords = new ArrayList<>();
        Map<Integer, Integer> hashValueMaps = new HashMap<>();
        // 快进快出每一条记录的hash值
        for (BankTransactionRecord orderRecord : first) {

            List<BankTransactionRecord> otherRecords = maps.get(orderRecord.getQueryCard());
            if (CollectionUtils.isEmpty(otherRecords)) {
                continue;
            }
            for (BankTransactionRecord otherRecord : otherRecords) {
                Map<String, Object> fastInFastOutRecord = convertFromDataTransit(orderRecord, otherRecord, caseId, isInflow);
                if (!CollectionUtils.isEmpty(fastInFastOutRecord)) {
                    fastInFastOutRecords.add(fastInFastOutRecord);
                    hashValueMaps.put(Integer.parseInt(fastInFastOutRecord.get("id").toString()), 0);
                }
            }
        }
        // 录入记录
        entranceRepository.saveAll(fastInFastOutRecords, caseId, FastInFastOutRecord.class);
        return hashValueMaps;
    }

    /**
     * <h2> 通过查询卡号获取进出的总次数 </h2>
     */
    private long getInOutCountViaQueryCards(List<String> cards, String caseId, boolean isIn) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, isIn);
        return entranceRepository.count(caseId, BankTransactionRecord.class, query);
    }

    /**
     * <h2> 通过查询卡号获取进出记录(根据排序) </h2>
     */
    private List<BankTransactionRecord> getInOutRecordOrderViaQueryCards(List<String> cards, String caseId, boolean isIn, String property, Sort.Direction direction, int orderChunkSize) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, isIn);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(0, orderChunkSize, direction, property), caseId, BankTransactionRecord.class, query);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 通过查询卡号获取进出记录(不排序) </h2>
     */
    private List<BankTransactionRecord> getInOutRecordViaQueryCards(List<String> cards, String caseId, boolean isIn, int from, int orderChunkSize) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, isIn);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(from, orderChunkSize), caseId, BankTransactionRecord.class, query);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 通过查询卡号获取进账/出账的总次数 </h2>
     */
    private Map<String, Integer> getInOutTotalTimesViaQueryCards(List<String> cards, String caseId, boolean isInFlow) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, isInFlow);
        AggregationParams agg = aggregationRequestParamFactory.groupByAndCountField(FundTacticsAnalysisField.QUERY_CARD, cards.size(), new Pagination(0, cards.size()));
        agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggDocCountMapping(FundTacticsAnalysisField.QUERY_CARD));
        agg.setResultName("getQueryCardOutTimes");
        Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultMaps) || CollectionUtils.isEmpty(resultMaps.get(agg.getResultName()))) {
            return null;
        }
        List<List<Object>> results = resultMaps.get(agg.getResultName());
        return results.stream().collect(Collectors.toMap(e -> e.get(0).toString(), e -> Integer.parseInt(e.get(1).toString()), (v1, v2) -> v1));
    }


    /**
     * <h2>
     * 查询卡号(调单卡号) 、对方卡号(排除这些调单)得到交易记录(去重对方卡号)
     * 然后以这些对方卡号 查询 出账的记录(按照querySize来取)
     * </h2>
     *
     * @param request       快进快出请求
     * @param next          聚合分页起始位置
     * @param size          聚合分页返回条数
     * @param loanFlagValue 借贷标志值
     * @param isCredits     查询的是进账还是出账
     */
    private List<BankTransactionRecord> getQueryAsAdjustOppositeNoSuchAdjust(FastInFastOutRequest request, int next, int size, String loanFlagValue, boolean isCredits,
                                                                             com.zqykj.domain.PageRequest pageRequest) {

        QuerySpecialParams query = queryRequestParamFactory.queryAsAdjustOppositeNoSuchAdjust(request, loanFlagValue);
        Pagination pagination = new Pagination(next, size);
        AggregationParams agg = aggregationRequestParamFactory.groupByField(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, initGroupSize, pagination);
        agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD));
        agg.setResultName("oppositeCardGroup");
        Map<String, List<List<Object>>> resultsMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, request.getCaseId());
        List<List<Object>> results = resultsMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        List<String> oppositeCards = results.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(oppositeCards)) {
            return null;
        }
        return getTradeRecordsViaCards(request.getCaseId(), request.getSingleQuota(), oppositeCards, isCredits,
                pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
    }


    /**
     * <h2> 获取给定一组查询卡号和对方卡号 入账/出账的数据 </h2>
     * <p>
     * 分页获取数据(返回的数据量大小未知)
     * 排序: 流入金额排序, 流出时间日期与流出金额
     */
    @SuppressWarnings("all")
    private List<BankTransactionRecord> getCreditAndPayOutViaLocalAndOpposite(String caseId, int singleQuota, List<String> queryCards,
                                                                              List<String> oppositeCards, int size,
                                                                              boolean isCredits, SortRequest sortRequest, String... includeFields) {
        QuerySpecialParams query = queryRequestParamFactory.buildCreditAndPayOutViaLocalAndOpposite(caseId, queryCards, oppositeCards,
                singleQuota, isCredits, includeFields);
        // 分页与排序
        com.zqykj.domain.PageRequest pageRequest = com.zqykj.domain.PageRequest.of(0, size, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty());
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(pageRequest, caseId, BankTransactionRecord.class, query);
        if (null == recordPage) {
            return new ArrayList<>();
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 查询出  入账/出账记录 (不在给定的这一组卡号之内的) </h2>
     */
    private List<BankTransactionRecord> getCreditAndPayoutRecordsNoSuchCards(FastInFastOutRequest request, int size,
                                                                             boolean isCredits, String... includeFields) {
        QuerySpecialParams query = queryRequestParamFactory.buildCreditAndPayoutRecordsNoSuchCards(request, size, isCredits, includeFields);
        // 分页与排序
        SortRequest sortRequest = request.getSortRequest();
        com.zqykj.domain.PageRequest pageRequest = com.zqykj.domain.PageRequest.of(0, size, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty());
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(pageRequest, request.getCaseId(), BankTransactionRecord.class, query);
        return recordPage.getContent();
    }

    /**
     * <h2> 过滤调单卡号的情况(查询卡号是调单、对方卡号也是调单卡号的情况,借贷标志为进或者出) </h2>
     */
    private List<String> getLocalAndOppositeAsAdjust(FastInFastOutRequest request, boolean loanFlagValue) {

        AggregationParams agg = aggregationRequestParamFactory.buildFastInFastOutOppositeCardGroup(request);
        Map<String, String> mapping = aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);
        agg.setResultName("oppositeCardGroup");
        agg.setMapping(mapping);
        QuerySpecialParams query = queryRequestParamFactory.buildCreditAndPayOutViaLocalAndOpposite(request.getCaseId(), request.getCardNum(), request.getCardNum(), request.getSingleQuota(),
                loanFlagValue);
        Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, request.getCaseId());
        List<List<Object>> oppositeCardResults = resultMaps.get(agg.getResultName());
        if (CollectionUtils.isEmpty(oppositeCardResults)) {
            return new ArrayList<>();
        }
        // 对方卡号
        return oppositeCardResults.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
    }

    /**
     * <h2>  计算 调单卡号(作为查询卡号)、对方卡号(不在这些调单卡号之内的),借贷标志为进或者出的交易记录,然后对对方卡号去重(获取去重后的总数量) </h2>
     */
    private long getDistinctLocalAsAdjustAndOppositeNoSuchAdjust(FastInFastOutRequest request, String loanFlagValue) {

        // 计算 调单卡号(作为查询卡号)、对方卡号(不在这些调单卡号之内的),借贷标志为出的交易记录,然后对对方卡号去重(获取去重后的总数量)
        QuerySpecialParams distinctQuery = queryRequestParamFactory.queryAsAdjustOppositeNoSuchAdjust(request, loanFlagValue);
        AggregationParams distinctAgg = aggregationRequestParamFactory.buildDistinctViaField(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);
        distinctAgg.setResultName("distinctOppositeCard");
        distinctAgg.setMapping(aggregationEntityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD));
        Map<String, List<List<Object>>> distinctResultMaps = entranceRepository.compoundQueryAndAgg(distinctQuery, distinctAgg, BankTransactionRecord.class, request.getCaseId());
        List<List<Object>> distinctTotal = distinctResultMaps.get(distinctAgg.getResultName());
        return (long) distinctTotal.get(0).get(0);
    }

    /**
     * <h2> 获取一批查询卡号的进或者出账记录(日期条件参与过滤) </h2>
     * <p>
     * 限定本方查询卡号
     */
    private List<BankTransactionRecord> getFastInOutTradeRecords(String caseId, int singleQuota, List<String> queryCards,
                                                                 boolean isQueryCredits, Date tradeDate, QueryOperator operator,
                                                                 com.zqykj.domain.PageRequest pageRequest, String... includeFields) {
        QuerySpecialParams filterQuery = queryRequestParamFactory.getFastInOutTradeRecordsByCondition(caseId, singleQuota, queryCards, isQueryCredits, tradeDate, operator, includeFields);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(pageRequest, caseId, BankTransactionRecord.class, filterQuery);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 获取查询卡号与对方卡号的进或者出账记录 </h2>
     * <p>
     * 限定本方和对方卡号
     */
    private List<BankTransactionRecord> getFastInOutTradeRecordsViaLocalOpposite(String caseId, int singleQuota, List<String> queryCards, List<String> oppositeAdjustCards,
                                                                                 boolean isQueryCredits, Date tradeDate, QueryOperator operator,
                                                                                 com.zqykj.domain.PageRequest pageRequest, String... includeFields) {
        QuerySpecialParams filterQuery = queryRequestParamFactory.getFastInOutTradeRecordsByLocalOpposite(caseId, singleQuota, queryCards, oppositeAdjustCards,
                isQueryCredits, tradeDate, operator, includeFields);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(pageRequest, caseId, BankTransactionRecord.class, filterQuery);
        if (null == recordPage) {
            return new ArrayList<>();
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 获取一批查询卡号的进或者出账记录(日期条件不参与过滤) </h2>
     * <p>
     * 限定本方查询卡号
     */
    private List<BankTransactionRecord> getTradeRecordsViaCards(String caseId, int singleQuota, List<String> queryCards,
                                                                boolean isQueryCredits, com.zqykj.domain.PageRequest pageRequest, String... includeFields) {
        QuerySpecialParams filterQuery = queryRequestParamFactory.getFastInOutTradeRecordsByCondition(caseId, singleQuota, queryCards, isQueryCredits, null, null, includeFields);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(pageRequest, caseId, BankTransactionRecord.class, filterQuery);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return new ArrayList<>();
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为来源情况
     */
    @SuppressWarnings("all")
    private FastInFastOutRecord convertFromDataSource(BankTransactionRecord first, BankTransactionRecord second, String sortType) {

        FastInFastOutRecord fastInFastOutResult = new FastInFastOutRecord();
        // 入账金额
        double creditAmount;
        // 按照入账金额排序
        if ("inflowAmount".equals(sortType)) {
            // 资金来源卡号
            fastInFastOutResult.setFundSourceCard(first.getQueryCard());
            // 资金来源户名
            fastInFastOutResult.setFundSourceAccountName(first.getCustomerName());
            // 流入时间日期
            fastInFastOutResult.setInflowDate(first.getTradingTime());
            // 流入金额
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            creditAmount = first.getChangeAmount();
            // 资金中转卡号
            fastInFastOutResult.setFundTransitCard(first.getTransactionOppositeCard());
            // 资金中转户名
            fastInFastOutResult.setFundTransitAccountName(first.getTransactionOppositeName());
            // 流出时间日期
            fastInFastOutResult.setOutflowDate(second.getTradingTime());
            // 流出金额
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            // 资金沉淀卡号
            fastInFastOutResult.setFundDepositCard(second.getTransactionOppositeCard());
            // 资金沉淀户名
            fastInFastOutResult.setFundDepositAccountName(second.getTransactionOppositeName());
        } else {

            // 按照流出金额、流出日期排序
            fastInFastOutResult.setFundSourceCard(second.getQueryCard());
            fastInFastOutResult.setFundSourceAccountName(second.getCustomerName());
            fastInFastOutResult.setInflowDate(second.getTradingTime());
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            creditAmount = second.getChangeAmount();
            fastInFastOutResult.setFundTransitCard(first.getQueryCard());
            fastInFastOutResult.setFundTransitAccountName(first.getCustomerName());
            fastInFastOutResult.setOutflowDate(first.getTradingTime());
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            fastInFastOutResult.setFundDepositCard(first.getTransactionOppositeCard());
            fastInFastOutResult.setFundDepositAccountName(first.getTransactionOppositeName());
        }
        // 特征比(入账金额-出账金额) / 入账金额
        computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), creditAmount);
        return fastInFastOutResult;
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为中转情况
     */
    @SuppressWarnings("all")
    private Map<String, Object> convertFromDataTransit(BankTransactionRecord first, BankTransactionRecord second, String caseId, boolean isInflow) {

        Map<String, Object> map = new HashMap<>();
        double inflowAmount;
        if (isInflow) {
            // 资金来源卡号
            map.put("source_card", first.getTransactionOppositeCard());
            // 资金来源户名
            map.put("source_account_name", first.getTransactionOppositeName());
            // 流入时间日期
            map.put("inflow_date", first.getTradingTime());
            // 流入金额
            map.put("inflow_amount", BigDecimalUtil.value(first.getChangeAmount().toString()));
            inflowAmount = first.getChangeAmount();
            // 资金中转卡号
            map.put("transit_card", first.getQueryCard());
            // 资金中转户名
            map.put("transit_account_name", first.getCustomerName());
            // 流出时间日期
            map.put("outflow_date", second.getTradingTime());
            // 流出金额
            map.put("outflow_amount", second.getChangeAmount());
            // 资金沉淀卡号
            map.put("deposit_card", second.getTransactionOppositeCard());
            // 资金沉淀户名
            map.put("deposit_account_name", second.getTransactionOppositeName());
            // 时间间隔
            long timeInterval = computeTimeInterval(first.getTradingTime(), second.getTradingTime());
            if (timeInterval == -1L) {
                return null;
            }
            map.put("time_interval", timeInterval);
            // 调单卡号
            map.put("adjust_card", first.getQueryCard());
        } else {
            map.put("source_card", second.getTransactionOppositeCard());
            map.put("source_account_name", second.getTransactionOppositeName());
            map.put("inflow_date", second.getTradingTime());
            map.put("inflow_amount", BigDecimalUtil.value(second.getChangeAmount().toString()));
            inflowAmount = second.getChangeAmount();
            map.put("transit_card", second.getQueryCard());
            map.put("transit_account_name", second.getCustomerName());
            map.put("outflow_date", first.getTradingTime());
            map.put("outflow_amount", first.getChangeAmount());
            map.put("deposit_card", first.getTransactionOppositeCard());
            map.put("deposit_account_name", first.getTransactionOppositeName());
            // 时间间隔
            long timeInterval = computeTimeInterval(second.getTradingTime(), first.getTradingTime());
            if (timeInterval == -1L) {
                return null;
            }
            map.put("time_interval", timeInterval);
            // 调单卡号
            map.put("adjust_card", second.getQueryCard());
        }
        // 特征比(入账金额-出账金额) / 入账金额
        int characteristicRatio = computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), inflowAmount);
        map.put("feature_ratio", characteristicRatio);
        int fastInFastOutRecordHash = computeFastInFastOutRecordHash(map);
        map.put("id", fastInFastOutRecordHash);
        map.put("case_id", caseId);
        map.put("case_id_hash", caseId.hashCode());
        // hash 值Id
        return map;
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为沉淀情况
     */
    @SuppressWarnings("all")
    private FastInFastOutRecord convertFromDataDeposit(BankTransactionRecord first, BankTransactionRecord second, String sortType) {

        FastInFastOutRecord fastInFastOutResult = new FastInFastOutRecord();
        double inflowAmount;
        if (sortType.equals("inflowAmount")) {
            // 资金来源卡号
            fastInFastOutResult.setFundSourceCard(first.getTransactionOppositeCard());
            // 资金来源户名
            fastInFastOutResult.setFundSourceAccountName(first.getTransactionOppositeName());
            // 流入时间日期
            fastInFastOutResult.setInflowDate(first.getTradingTime());
            // 流入金额
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            inflowAmount = first.getChangeAmount();
            // 资金中转卡号
            fastInFastOutResult.setFundTransitCard(first.getQueryCard());
            // 资金中转户名
            fastInFastOutResult.setFundTransitAccountName(first.getCustomerName());
            // 流出时间日期
            fastInFastOutResult.setOutflowDate(second.getTradingTime());
            // 流出金额
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            // 资金沉淀卡号
            fastInFastOutResult.setFundDepositCard(second.getQueryCard());
            // 资金沉淀户名
            fastInFastOutResult.setFundDepositAccountName(second.getCustomerName());
        } else {
            fastInFastOutResult.setFundSourceCard(second.getTransactionOppositeCard());
            fastInFastOutResult.setFundSourceAccountName(second.getTransactionOppositeName());
            fastInFastOutResult.setInflowDate(second.getTradingTime());
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            inflowAmount = second.getChangeAmount();
            fastInFastOutResult.setFundTransitCard(second.getQueryCard());
            fastInFastOutResult.setFundTransitAccountName(second.getCustomerName());
            fastInFastOutResult.setOutflowDate(first.getTradingTime());
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            fastInFastOutResult.setFundDepositCard(first.getQueryCard());
            fastInFastOutResult.setFundDepositAccountName(first.getCustomerName());
        }
        // 特征比(入账金额-出账金额) / 入账金额
        int characteristicRatio = computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), inflowAmount);

        return fastInFastOutResult;
    }

    /**
     * <h2> 计算快进快出结果的特征比 </h2>
     */
    private int computeFastInFastOutCharacteristicRatio(double firstChangeAmount, double secondChangeAmount, double inflowAmount) {
        BigDecimal sub;
        if (firstChangeAmount < secondChangeAmount) {
            sub = BigDecimalUtil.sub(secondChangeAmount, firstChangeAmount);
        } else {
            sub = BigDecimalUtil.sub(firstChangeAmount, secondChangeAmount);
        }
        BigDecimal div = BigDecimalUtil.divReserveFour(sub.doubleValue(), inflowAmount);
        return BigDecimalUtil.mul(div.doubleValue(), 100).intValue();
    }

    /**
     * <h2> 检查满足特征比与时间间隔的快进快出结果 </h2>
     */
    private boolean checkTimeIntervalAndFeatureRatio(String sortType, int characteristicRatio, long timeInterval, BankTransactionRecord first, BankTransactionRecord second) {
        // 检查特征比
        boolean checkFeatureRatio;
        boolean checkTimeInterval;
        if (sortType.equals("inflowAmount")) {
            checkFeatureRatio = checkFeatureRatio(characteristicRatio, first.getChangeAmount(), second.getChangeAmount());
            // 检查时间间隔
            checkTimeInterval = checkTimeInterval(checkFeatureRatio, timeInterval, first.getTradingTime(), second.getTradingTime());
        } else {
            checkFeatureRatio = checkFeatureRatio(characteristicRatio, second.getChangeAmount(), first.getChangeAmount());
            // 检查时间间隔
            checkTimeInterval = checkTimeInterval(checkFeatureRatio, timeInterval, second.getTradingTime(), first.getTradingTime());
        }
        return checkTimeInterval;
    }

    /**
     * <h2> 检查特征比 </h2>
     */
    private boolean checkFeatureRatio(int characteristicRatio, double creditAmount, double payoutAmount) {

        // 特征比(入账金额-出账金额) / 入账金额
        BigDecimal sub;
        if (creditAmount < payoutAmount) {
            sub = BigDecimalUtil.sub(payoutAmount, creditAmount);
        } else {
            sub = BigDecimalUtil.sub(creditAmount, payoutAmount);
        }
        BigDecimal div = BigDecimalUtil.divReserveFour(sub.doubleValue(), creditAmount);
        return div.doubleValue() <= BigDecimalUtil.div(characteristicRatio, 100).doubleValue();
    }


    /**
     * <h2> 检查时间间隔 </h2>
     * <p>
     * 查看流入时间日期 与 流出时间日期之前必须 小于等于 timeInterval
     */
    private boolean checkTimeInterval(boolean checkFeatureRatio, long timeInterval, Date inflowDate, Date outflowDate) {
        if (!checkFeatureRatio) {
            return false;
        }
        // 如果 流出时间日期 早于 流入时间日期(过滤此数据)
        if (outflowDate.before(inflowDate)) {
            return false;
        }
        // 查看时间间隔
        long minutes = ChronoUnit.MINUTES.between(Instant.ofEpochMilli(inflowDate.getTime()), Instant.ofEpochMilli(outflowDate.getTime()));
        return minutes <= timeInterval;
    }


    private long computeTimeInterval(Date inflowDate, Date outflowDate) {
        long minutes = ChronoUnit.MINUTES.between(Instant.ofEpochMilli(outflowDate.getTime()), Instant.ofEpochMilli(inflowDate.getTime()));
        if (minutes < 0) {
            return -1L;
        }
        return minutes;
    }

    private int computeFastInFastOutRecordHash(Map<String, Object> fastInFastOutRecord) {

        StringBuilder sb = new StringBuilder();
        sb.append(fastInFastOutRecord.get("source_card"));
        sb.append(fastInFastOutRecord.get("inflow_date"));
        sb.append(fastInFastOutRecord.get("inflow_amount"));
        sb.append(fastInFastOutRecord.get("transit_card"));
        sb.append(fastInFastOutRecord.get("outflow_date"));
        sb.append(fastInFastOutRecord.get("outflow_amount"));
        sb.append(fastInFastOutRecord.get("deposit_card"));
        return FastInFastOutRecord.hash(sb.toString());
    }

    private List<FastInFastOutResult> queryFastInFastOutResult(FastInFastOutRequest request) {

        QuerySpecialParams fastInFastOutRecord = queryRequestParamFactory.getFastInFastOutRecord(request);
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        SortRequest sortRequest = request.getSortRequest();
        Direction order = sortRequest.getOrder();
        Page<FastInFastOutRecord> page = entranceRepository.findAll(PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize(),
                Sort.Direction.valueOf(order.name()), sortRequest.getProperty()), request.getCaseId(), FastInFastOutRecord.class, fastInFastOutRecord);
        if (null == page) {
            return new ArrayList<>();
        }
        List<FastInFastOutRecord> content = page.getContent();
        if (CollectionUtils.isEmpty(content)) {
            return new ArrayList<>();
        }
        return convertFromFastInFastOutResult(content);
    }

    private List<FastInFastOutResult> convertFromFastInFastOutResult(List<FastInFastOutRecord> records) {

        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();

        for (FastInFastOutRecord record : records) {

            FastInFastOutResult fastInFastOutResult = new FastInFastOutResult();
            BeanUtils.copyProperties(record, fastInFastOutResult);
            fastInFastOutResult.setInflowDate(format.format(record.getInflowDate()));
            fastInFastOutResult.setOutflowDate(format.format(record.getOutflowDate()));
            fastInFastOutResults.add(fastInFastOutResult);
        }
        return fastInFastOutResults;
    }
}
