package com.zqykj.app.service.interfaze.impl;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.agg.TradeStatisticalAnalysisAggParamFactory;
import com.zqykj.app.service.factory.param.query.TradeStatisticalAnalysisQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.app.service.transform.NumericalConversion;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.enums.HistogramStatistic;
import com.zqykj.common.response.*;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.common.request.TransactionStatisticsAggs;
import com.zqykj.common.util.EasyExcelUtils;
import com.zqykj.domain.*;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionStatisticsImpl extends FundTacticsCommonImpl implements ITransactionStatistics {

    private final TradeStatisticalAnalysisAggParamFactory tradeStatisticalAnalysisAggParamFactory;

    private final TradeStatisticalAnalysisQueryParamFactory tradeStatisticalAnalysisQueryParamFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    @Override
    public HistogramStatisticResponse getHistogramStatistics(String caseId, FundTacticsPartGeneralPreRequest request, TransactionStatisticsAggs transactionStatisticsAggs) {
        List<HistogramStatistic> responseList = new ArrayList<>();
        HistogramStatisticResponse histogramStatisticResponse = new HistogramStatisticResponse();

        // ??????????????????
        QuerySpecialParams query = tradeStatisticalAnalysisQueryParamFactory.createTradeAmountByTimeQuery(request, caseId);
        /**
         * ???????????????????????????????????????????????????.
         * */
        Map<String, ParsedStats> map = entranceRepository.statsAggs(query, Constants.Individual.FOURTH_AGGREGATE_NAME,
                caseId, BankTransactionFlow.class);
        ParsedStats parsedStats = map.get(Constants.BucketName.STATS);
        Double max = parsedStats.getMax();

        /**
         * ??????????????????????????????????????????????????????range??????,?????????????????????????????????range?????????.
         * */
        List<Range> ranges = NumericalConversion.intervalConversion(max, transactionStatisticsAggs.getHistorgramNumbers());

        /**
         * ??????range?????????????????????queryParams??????????????????????????????????????????.
         * */
        Map queryResultMap = entranceRepository.rangeAggs(query, Constants.Individual.FOURTH_AGGREGATE_NAME
                , caseId,
                ranges, BankTransactionFlow.class);
        /**
         * ??????????????????????????????????????????.
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
    public TradeStatisticalAnalysisFundSumByDate getSummaryOfTradeAmountGroupedByTime(FundDateRequest request) {

        String caseId = request.getCaseId();
        // ??????????????????
        QuerySpecialParams query = tradeStatisticalAnalysisQueryParamFactory.createTradeAmountByTimeQuery(request, caseId);

        // ????????????????????????
        AggregationParams dateAgg = tradeStatisticalAnalysisAggParamFactory.buildTradeStatisticsAnalysisFundByTimeType(request);

        Map<String, String> mapping = new LinkedHashMap<>();

        entityMappingFactory.buildTradeAnalysisResultAggMapping(mapping, TradeStatisticalAnalysisFundSumByDate.class);

        dateAgg.setMapping(mapping);

        dateAgg.setResultName("fundSumByDate");

        // ??????  DateSpecificFormat??????
        List<List<Object>> result = entranceRepository.dateGroupAgg(query, dateAgg, BankTransactionRecord.class, caseId);

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
                        String oldTradeAmount = map.get(date).toString();
                        map.put(date, BigDecimalUtil.add(oldTradeAmount, curTradeAmount));
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

    public ServerResponse<FundAnalysisResultResponse<TradeStatisticalAnalysisResult>> tradeStatisticsAnalysisResult(TradeStatisticalAnalysisQueryRequest request, int from, int size,
                                                                                                                    boolean isComputeTotal) throws Exception {

        Map<String, Object> map;
        if (request.getAnalysisType() == 2 || request.getAnalysisType() == 3) {

            request.setGroupInitSize(fundThresholdConfig.getGroupByThreshold());
            map = statisticsAnalysisResultViaChosenMainCards(request, from, size, request.getCaseId(), isComputeTotal);
        } else {
            // ????????????
            if (request.getPageRequest().getPage() > fundThresholdConfig.getPaginationThreshold()) {
                return ServerResponse.createBySuccess("??????????????? " + fundThresholdConfig.getPaginationThreshold() + "???", FundAnalysisResultResponse.empty());
            }
            map = statisticsAnalysisResultViaAllMainCards(request, from, size, isComputeTotal);
        }
        if (CollectionUtils.isEmpty(map)) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        List<TradeStatisticalAnalysisResult> results = (List<TradeStatisticalAnalysisResult>) map.get("result");
        long total = Long.parseLong(String.valueOf(map.get("total")));
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(results, total, size));
    }

    public ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> getDetail(FundTacticsPartGeneralRequest request) {

        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        return detail(request, pageRequest.getPage(), pageRequest.getPageSize(), FundTacticsFuzzyQueryField.detailFuzzyFields);
    }

    public ServerResponse<String> detailExport(ExcelWriter excelWriter, FundTacticsPartGeneralRequest request) throws Exception {

        int total = Integer.parseInt(String.valueOf(detailTotal(request)));
        // ???????????????????????????,????????????sheet?????????
        writeSheet(excelWriter, total, request);
        return ServerResponse.createBySuccess();
    }

    public ServerResponse<String> transactionStatisticsAnalysisResultExport(ExcelWriter excelWriter, TradeStatisticalAnalysisQueryRequest request) throws Exception {
        int total;
        if (request.getTopRange() != null) {
            total = request.getTopRange();
        } else {
            total = Integer.parseInt(String.valueOf(getTradeStatisticsAnalysisResultTotal(request)));
            if (request.getPercentageOfAccountNumber() != null) {
                total = BigDecimalUtil.mul(String.valueOf(total), BigDecimalUtil.div(request.getPercentageOfAccountNumber(), 100).toString()).intValue();
            }
        }
        if (total == 0) {
            // ????????????sheet
            WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
            excelWriter.write(new ArrayList<>(), sheet);
            return ServerResponse.createBySuccess();
        }
        int perRowSheetCount = exportThresholdConfig.getPerSheetRowCount();
        if (total <= perRowSheetCount) {
            // ??????sheet?????????
            WriteSheet sheet = EasyExcelUtils.generateWriteSheet(request.getExportFileName());
            writeSheet(0, total, excelWriter, sheet, request);
        } else {
            // ??????sheet?????????
            int limit = total;
            if (total > exportThresholdConfig.getExcelExportThreshold()) {
                limit = exportThresholdConfig.getExcelExportThreshold();
            }
            int position = 0;
            int perSheetRowCount = exportThresholdConfig.getPerSheetRowCount();
            // ?????????????????????????????????(??????sheet?????????50W,?????????????????????,????????????1W,????????????50???,?????????????????????????????????,ThreadPoolConfig.getExecutor()
            // ???????????????????????????,?????????????????????????????????)
            int sheetNo = 0;
            while (position < limit) {
                int next = Math.min(position + perSheetRowCount, limit);
                WriteSheet sheet;
                if (sheetNo == 0) {
                    sheet = EasyExcelUtils.generateWriteSheet(sheetNo, request.getExportFileName());
                } else {
                    sheet = EasyExcelUtils.generateWriteSheet(sheetNo, request.getExportFileName() + "_" + sheetNo);
                }
                writeSheet(position, next, excelWriter, sheet, request);
                position = next;
                sheetNo++;
            }
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * <h2> ??????????????????????????????????????????Excel??? </h2>
     */
    private void writeSheet(int position, int limit, ExcelWriter writer, WriteSheet sheet, TradeStatisticalAnalysisQueryRequest request) throws Exception {

        if (limit == 0) {
            return;
        }
        int chunkSize = exportThresholdConfig.getPerWriteRowCount();
        List<Future<List<TradeStatisticalAnalysisResult>>> futures = new ArrayList<>();
        while (position < limit) {
            int next = Math.min(position + chunkSize, limit);
            int finalPosition = position;
            Future<List<TradeStatisticalAnalysisResult>> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            return tradeStatisticsAnalysisResult(request, finalPosition, next - finalPosition, false).getData().getContent();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    },
                    ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
        }
        for (Future<List<TradeStatisticalAnalysisResult>> future : futures) {
            List<TradeStatisticalAnalysisResult> dataList = future.get();
            // ??????sheet
            if (!CollectionUtils.isEmpty(dataList)) {
                writer.write(dataList, sheet);
            }
        }
    }

    /**
     * <h2> ?????????????????????????????? </h2>
     */
    private long getTradeStatisticsAnalysisResultTotal(TradeStatisticalAnalysisQueryRequest request) {

        AggregationParams totalAgg = total(request);
        String caseId = request.getCaseId();
        // ????????????
        // ?????????????????????,????????????????????????????????? ???????????????????????????
        if (CollectionUtils.isEmpty(request.getCardNum())) {
            QueryOperator operator = FundTacticsPartGeneralPreRequest.getOperator(request.getOperator());
            List<String> adjustCards = queryMaxAdjustCardsBySingleAmountDate(request.getCaseId(), request.getFund(), operator, FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()));
            if (!CollectionUtils.isEmpty(adjustCards)) {
                request.setCardNum(adjustCards);
            }
        }
        QuerySpecialParams totalQuery = tradeStatisticalAnalysisQueryParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId);
        Map<String, List<List<Object>>> totalMap = entranceRepository.compoundQueryAndAgg(totalQuery, totalAgg, BankTransactionFlow.class, caseId);
        if (CollectionUtils.isEmpty(totalMap) || CollectionUtils.isEmpty(totalMap.get(CARDINALITY_TOTAL))) {
            return 0;
        }
        return (long) totalMap.get(CARDINALITY_TOTAL).get(0).get(0);
    }

    /**
     * <h2> ???????????? / ?????????????????????????????? </h2>
     */
    @SuppressWarnings("all")
    private Map<String, Object> statisticsAnalysisResultViaChosenMainCards(TradeStatisticalAnalysisQueryRequest request, int from, int size, String caseId, boolean isComputeTotal) {

        // ?????? ??????????????????????????????
        QuerySpecialParams tradeStatisticsQuery = tradeStatisticalAnalysisQueryParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId);
        // ?????? ????????????????????????????????????
        AggregationParams tradeStatisticsAgg = tradeStatisticalAnalysisAggParamFactory.buildTradeStatisticsAnalysisByMainCards(request, from, size);

        // ?????? mapping (???????????? -> ????????????)  , (???????????? -> ????????????)
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        entityMappingFactory.buildTradeAnalysisResultAggMapping(aggMapping, entityMapping, TradeStatisticalAnalysisResult.class);
        tradeStatisticsAgg.setMapping(aggMapping);
        tradeStatisticsAgg.setEntityAggColMapping(entityMapping);
        tradeStatisticsAgg.setResultName("chosen_main_cards");

        // ??????????????????(??????????????????)
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, List<List<Object>>> totalResults = null;
        if (isComputeTotal) {
            AggregationParams totalAgg = total(request);
            // ????????????????????????????????????
            QuerySpecialParams totalQuery = tradeStatisticalAnalysisQueryParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId);
            totalResults = entranceRepository.compoundQueryAndAgg(totalQuery, totalAgg, BankTransactionFlow.class, caseId);
        }
        // ??????????????????????????????
        Map<String, List<List<Object>>> results = entranceRepository.compoundQueryAndAgg(tradeStatisticsQuery, tradeStatisticsAgg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(results)) {
            resultMap.put("total", 0);
            resultMap.put("result", new ArrayList<>());
            return resultMap;
        }
        // ??????????????????
        List<List<Object>> returnResults = results.get(tradeStatisticsAgg.getResultName());

        // ???????????????????????? ??? ????????????????????????????????????( ???????????????????????????????????????????????? ??? ???????????????????????????, ??????????????????????????????????????????)
        List<String> entityTitles = new ArrayList<>(entityMapping.keySet());

        // ?????????????????????
        List<Map<String, Object>> entityPropertyMapping = parseFactory.convertEntity(returnResults, entityTitles, TradeStatisticalAnalysisResult.class);

        // ??????????????????
        List<TradeStatisticalAnalysisResult> tradeStatisticalAnalysisResults = JacksonUtils.parse(entityPropertyMapping, new TypeReference<List<TradeStatisticalAnalysisResult>>() {
        });

        // ???????????????2?????????
        tradeStatisticalAnalysisResults.forEach(TradeStatisticalAnalysisResult::amountReservedTwo);

        // ?????????????????????????????????
        List<String> queryCards = tradeStatisticalAnalysisResults.stream().map(e -> e.getQueryCardKey()).collect(Collectors.toList());
        List<TradeStatisticalAnalysisResult> tradeStatisticalAnalysisHits = getTradeStatisticalAnalysisHits(queryCards, caseId);
        addTradeStatisticalAnalysisShowFields(tradeStatisticalAnalysisResults, tradeStatisticalAnalysisHits);

        if (CollectionUtils.isEmpty(totalResults)) {
            resultMap.put("total", 0);
        } else {
            List<List<Object>> total = totalResults.get(CARDINALITY_TOTAL);
            if (CollectionUtils.isEmpty(total)) {
                resultMap.put("total", 0);
            } else {
                resultMap.put("total", Integer.parseInt(String.valueOf(total.get(0).get(0))));
            }
        }
        resultMap.put("result", tradeStatisticalAnalysisResults);
        return resultMap;
    }

    /**
     * <h2> ?????????????????????????????????????????? </h2>
     */
    private AggregationParams total(TradeStatisticalAnalysisQueryRequest request) {

        if (null == request) {
            return null;
        }
        AggregationParams totalAgg = tradeStatisticalAnalysisAggParamFactory.buildTradeStatisticsAnalysisTotalAgg(request);
        totalAgg.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        totalAgg.setResultName(CARDINALITY_TOTAL);
        return totalAgg;
    }

    /**
     * <h2> ????????????????????????(????????????????????????????????????) </h2>
     * <p>
     * ???????????????: ?????????????????????????????????????????????(???????????????????????????????????????)
     */
    @SuppressWarnings("all")
    protected Map<String, Object> statisticsAnalysisResultViaAllMainCards(TradeStatisticalAnalysisQueryRequest request, int from, int limit, boolean isComputeTotal) throws Exception {

        // ????????????
        String caseId = request.getCaseId();
        Map<String, Object> resultMap = new HashMap<>();
        QueryOperator operator = FundTacticsPartGeneralPreRequest.getOperator(request.getOperator());
        // ?????????????????????????????????????????????,??????????????????????????????????????????????????????
        if (checkAdjustCardCountBySingleAmountDate(request.getCaseId(), request.getFund(), operator, FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()))) {
            List<String> adjustCards = queryMaxAdjustCardsBySingleAmountDate(request.getCaseId(), request.getFund(), operator, FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()));
            if (CollectionUtils.isEmpty(adjustCards)) {
                resultMap.put("total", 0);
                resultMap.put("result", new ArrayList<>());
                return resultMap;
            }
            request.setCardNum(adjustCards);
            return statisticsAnalysisResultViaChosenMainCards(request, from, limit, request.getCaseId(), isComputeTotal);
        }
        // ???????????? ??????????????????
        // ???????????????????????????
        AggregationParams totalAgg = total(request);
        // ?????? ??????????????????????????????
        QuerySpecialParams statisticsQuery = tradeStatisticalAnalysisQueryParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId);
        Map<String, List<List<Object>>> totalResults = entranceRepository.compoundQueryAndAgg(statisticsQuery, totalAgg, BankTransactionFlow.class, caseId);
        // ??????????????????????????????????????????????????????????????????????????????????????????
        long total = 0;
        if (!CollectionUtils.isEmpty(totalResults)) {
            List<List<Object>> list = totalResults.get(CARDINALITY_TOTAL);
            if (!CollectionUtils.isEmpty(list)) {
                total = (long) list.get(0).get(0);
            }
        } else {
            resultMap.put("total", 0);
            resultMap.put("result", new ArrayList<>());
            return resultMap;
        }
        // ??????????????????
        request.setGroupInitSize(fundThresholdConfig.getGroupByThreshold());
        // ??????????????????????????????
        int position = 0;
        // ????????????????????????
        int size = Integer.parseInt(String.valueOf(total));
        // ?????????????????????????????????
        int chunkSize = fundThresholdConfig.getPerTotalSplitCount();
        ThreadPoolTaskExecutor executor = ThreadPoolConfig.getExecutor();
        // ?????????????????????
        List<String> queryCards = new ArrayList<>();
        StopWatch stopWatch = StopWatch.createStarted();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            Future<List<String>> future = executor.submit(new StatisticalFutureTask(position,
                    next, from, limit, caseId, request));
            List<String> results = future.get();
            if (!CollectionUtils.isEmpty(results)) {
                queryCards.addAll(results);
            }
            if (queryCards.size() == limit) {
                break;
            } else {
                if (queryCards.size() > 0) {
                    from = 0;
                    limit = limit - queryCards.size();
                }
                position = next;
            }
        }
        List<TradeStatisticalAnalysisResult> statisticalAnalysisResults = new ArrayList<>();
        if (!CollectionUtils.isEmpty(queryCards)) {
            request.setCardNum(queryCards);
            request.setGroupInitSize(queryCards.size());
            Map<String, Object> resultsMap = statisticsAnalysisResultViaChosenMainCards(request, 0, queryCards.size(), caseId, false);
            statisticalAnalysisResults = (List<TradeStatisticalAnalysisResult>) resultsMap.get("result");
        }
        stopWatch.stop();
        log.info("async compute statistical analysis results cost time = {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        resultMap.put("total", total);
        resultMap.put("result", statisticalAnalysisResults);
        return resultMap;
    }

    /**
     * <h2> ?????????????????????????????????????????????????????????????????? </h2>
     */
    private List<String> getQueryCards(TradeStatisticalAnalysisQueryRequest request, int from, int size, String caseId) {

        // ????????????????????????
        QuerySpecialParams query = tradeStatisticalAnalysisQueryParamFactory.createTradeStatisticalAnalysisQueryRequestByMainCards(request, caseId);
        // ?????? ??????????????????????????????
        AggregationParams agg = tradeStatisticalAnalysisAggParamFactory.buildTradeStatisticalQueryCardsAgg(request, from, size);
        // ?????? mapping (???????????? -> ????????????)
        Map<String, String> mapping = entityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD);
        agg.setMapping(mapping);
        agg.setResultName("queryCards");
        Map<String, List<List<Object>>> results = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        List<List<Object>> cards = results.get(agg.getResultName());
        return cards.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
    }

    /**
     * <h2> ???????????????????????????????????????(??????????????????) </h2>
     */
    class StatisticalFutureTask implements Callable<List<String>> {

        private int position;

        private int next;

        private String caseId;

        private int skip;

        private int limit;

        private TradeStatisticalAnalysisQueryRequest request;

        public StatisticalFutureTask(int position, int next, int skip, int limit,
                                     String caseId, TradeStatisticalAnalysisQueryRequest request) {
            this.position = position;
            this.next = next;
            this.skip = skip;
            this.limit = limit;
            this.caseId = caseId;
            this.request = request;
        }

        @Override
        public List<String> call() throws ExecutionException, InterruptedException {

            StopWatch stopWatch = StopWatch.createStarted();
            List<String> cards = asyncQueryStatisticalResult(position, next, request, caseId);
            if (CollectionUtils.isEmpty(cards)) {
                return null;
            }
            // ??????????????????????????????
            Map<String, String> filterMainCards = fundTacticsAnalysis.asyncFilterMainCards(caseId, cards);
            // ???????????????????????????
            List<String> finalCards = cards.stream().filter(filterMainCards::containsKey)
                    .skip(skip).limit(limit).collect(Collectors.toList());
            log.info("Current Thread  = {} ,filter mainCards  cost time = {} ms", Thread.currentThread().getName(), stopWatch.getTime(TimeUnit.MILLISECONDS));
            return finalCards;
        }
    }

    /**
     * <h2> ??????????????????????????????????????????(????????????????????????) </h2>
     */
    private List<String> asyncQueryStatisticalResult(int from, int size, TradeStatisticalAnalysisQueryRequest request,
                                                     String caseId) throws ExecutionException, InterruptedException {
        StopWatch stopWatch = StopWatch.createStarted();
        int position = from;
        List<String> cards = new ArrayList<>(size);
        List<CompletableFuture<List<String>>> futures = new ArrayList<>();
        int chunkSize = fundThresholdConfig.getPerTotalSplitQueryCount();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            int finalPosition = position;
            CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() ->
                    getCardsViaQueryStatisticalResult(request, finalPosition, chunkSize, caseId), ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
        }
        for (CompletableFuture<List<String>> future : futures) {
            List<String> card = future.get();
            if (!CollectionUtils.isEmpty(card)) {
                cards.addAll(card);
            }
        }
        stopWatch.stop();
        log.info("async batch query adjust cards cost time = {}", stopWatch.getTime(TimeUnit.MILLISECONDS));
        return cards;
    }

    /**
     * <h2> ????????????????????????(??????????????????????????????????????????) ???????????????????????????????????? </h2>
     * <p>
     * ??????????????? {@link BankTransactionRecord}
     */
    private List<String> getCardsViaQueryStatisticalResult(TradeStatisticalAnalysisQueryRequest request, int position, int next,
                                                           String caseId) {

        List<String> queryCards = getQueryCards(request, position, next, caseId);
        if (CollectionUtils.isEmpty(queryCards)) {
            return null;
        }

        return queryCards;
    }

    /**
     * <h2> ?????????????????????????????????????????????????????? </h2>
     * <p>
     * ????????????????????????????????????????????????????????????????????????
     */
    private List<TradeStatisticalAnalysisResult> getTradeStatisticalAnalysisHits(List<String> queryCards, String caseId) {

        if (CollectionUtils.isEmpty(queryCards)) {
            return new ArrayList<>();
        }
        // ??????????????????
        QuerySpecialParams condition = tradeStatisticalAnalysisQueryParamFactory.buildTradeStatisticalAnalysisHitsQuery(queryCards, caseId);
        // ??????????????????
        AggregationParams agg = tradeStatisticalAnalysisAggParamFactory.buildTradeStatisticalAnalysisHitsAgg(queryCards.size());
        Map<String, String> map = entityMappingFactory.buildShowFieldsAggMapping();
        agg.setMapping(map);
        agg.setResultName("hits");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(condition, agg, BankTransactionRecord.class, caseId);
        List<Map<String, Object>> results = parseFactory.convertEntity(resultMap.get(agg.getResultName()), new ArrayList<>(), TradeStatisticalAnalysisResult.class);
        return JacksonUtils.parse(results, new TypeReference<List<TradeStatisticalAnalysisResult>>() {
        });
    }

    /**
     * <h2> ???????????????????????????????????????????????? </h2>
     */
    private void addTradeStatisticalAnalysisShowFields(List<TradeStatisticalAnalysisResult> statisticalAnalysisResults,
                                                       List<TradeStatisticalAnalysisResult> hits) {

        // ???????????????map(????????????for??????,?????????????????????)
        Map<String, TradeStatisticalAnalysisResult> hitsMap = hits.stream().collect(Collectors.toMap(TradeStatisticalAnalysisResult::getTradeCard, e -> e, (v1, v2) -> v1));
        for (TradeStatisticalAnalysisResult statisticalAnalysisResult : statisticalAnalysisResults) {

            TradeStatisticalAnalysisResult hit = hitsMap.get(statisticalAnalysisResult.getQueryCardKey());
            if (null != hit) {
                // ????????????
                statisticalAnalysisResult.setCustomerName(hit.getCustomerName());
                // ??????????????????
                statisticalAnalysisResult.setCustomerIdentityCard(hit.getCustomerIdentityCard());
                // ????????????
                statisticalAnalysisResult.setBank(hit.getBank());
                // ??????
                statisticalAnalysisResult.setQueryAccount(hit.getQueryAccount());
                // ????????????
                statisticalAnalysisResult.setTradeCard(hit.getTradeCard());
            }
        }
    }
}
