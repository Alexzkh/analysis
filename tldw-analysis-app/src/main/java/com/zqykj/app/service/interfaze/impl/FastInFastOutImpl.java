/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFastInFastOut;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
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

    // 对方卡号数量
    private static final int OPPOSITE_CARD_COUNT = 10000;
    // 每次查询入账/出账的分页size
    private static final int QUERY_SIZE = 2000;

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    public ServerResponse fastInFastOutAnalysis(FastInFastOutRequest request) throws ExecutionException, InterruptedException {

        PageRequest pageRequest = request.getPageRequest();
        int limit = pageRequest.getPage() * pageRequest.getPageSize() + pageRequest.getPageSize();
        // 来源、中转、沉淀 数据可能会重复(比如来源的5条和中转的5条是重复的,那必然会少5条,因此一开始寻找的时候就需要增加一倍)
        int limitNew = limit * 2;
        List<FastInFastOutResult> results = batchProcessFastInFastOutSelectIndividuals(request, limit);

        // 然后排序
        Direction order = request.getSortRequest().getOrder();
        Comparator<BigDecimal> bigDecimalComparator = Comparator.reverseOrder();
        Comparator<Long> dateComparator = Comparator.reverseOrder();
        if (order.isAscending()) {
            bigDecimalComparator = Comparator.naturalOrder();
            dateComparator = Comparator.naturalOrder();
        }
        // 最终排序后的返回结果
        List<FastInFastOutResult> orderResults;
        if (request.getSortRequest().getSortType().equals("inflowAmount")) {
            // 流入金额排序
            orderResults = results.stream().sorted(Comparator.comparing(FastInFastOutResult::getInflowAmount, bigDecimalComparator)).collect(Collectors.toList());
        } else if (request.getSortRequest().getSortType().equals("outflowAmount")) {
            // 流出金额排序
            orderResults = results.stream().sorted(Comparator.comparing(FastInFastOutResult::getOutflowAmount, bigDecimalComparator)).collect(Collectors.toList());
        } else {
            // 流出日期排序
            orderResults = results.stream().sorted(Comparator.comparing(e -> {
                try {
                    return format.parse(e.getOutflowDate()).getTime();
                } catch (ParseException ex) {
                    return 0L;
                }
            }, dateComparator)).collect(Collectors.toList());
        }
        return ServerResponse.createBySuccess(orderResults);
    }

    /**
     * <h2> 处理快进快出结果(选择个体) </h2>
     */
    private List<FastInFastOutResult> batchProcessFastInFastOutSelectIndividuals(FastInFastOutRequest request, int limit)
            throws ExecutionException, InterruptedException {

        // 异步操作
        List<CompletableFuture<List<FastInFastOutResult>>> futures = new ArrayList<>();
        // 返回调单卡号为来源卡号情况的结果数据
        CompletableFuture<List<FastInFastOutResult>> asFundSourceCardFuture = CompletableFuture.supplyAsync(() ->
        {
            try {
                return processAdjustCardAsFundSourceCard(request, limit);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }, ThreadPoolConfig.getExecutor());
        futures.add(asFundSourceCardFuture);
        // 返回调单卡号为中转卡号情况的结果数据
        CompletableFuture<List<FastInFastOutResult>> asFundTransitFuture = CompletableFuture.supplyAsync(() ->
                processAdjustCardAsFundTransitCard(request, limit), ThreadPoolConfig.getExecutor());
        futures.add(asFundTransitFuture);
        // 返回调单卡号为沉淀卡号情况的结果数据
        CompletableFuture<List<FastInFastOutResult>> asFundDepositFuture = CompletableFuture.supplyAsync(() ->
        {
            try {
                return processAdjustCardAsFundDepositCard(request, limit);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        });
        futures.add(asFundDepositFuture);
        // 最终结果
        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();
        // 汇总调单卡号作为来源、中转、沉淀的三组结果数据
        for (CompletableFuture<List<FastInFastOutResult>> future : futures) {

            fastInFastOutResults.addAll(future.get());
        }
        // 对汇总的数据去重
        return fastInFastOutResults.stream().collect(
                Collectors.collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(o -> FastInFastOutResult.hash(o.toString())))), ArrayList::new));
    }

    /**
     * <h2> 去重方式二: 将元素值给Set(看Set种是否存在) </h2>
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * <h2> 处理调单卡号为资金来源卡号的情况 </h2>
     *
     * @param request 快进快出请求
     * @param limit   需要返回的数量限制
     */
    private List<FastInFastOutResult> processAdjustCardAsFundSourceCard(FastInFastOutRequest request, int limit) throws ExecutionException, InterruptedException {

        // 按照排序需要将分页的足够数据筛选出来
        // 需要筛选出2部分数据, 调单卡号出账数据(这部分作为来源-中转)、 调单卡后出账数据的出账数据(这部分作为中转-沉淀)
        List<FastInFastOutResult> results = new ArrayList<>();
        // 若大于10000 (暂定),则使用方案一,否则使用方案二
        batchProcessSource(request, QUERY_SIZE, limit, results);
        if (results.size() == 0) {
            return null;
        }
        // 外部需要一个总数量来限制while循环
//        while (results.size() < limit) {
//            // TODO
//            // 如果 results的size 还是小于需要返回的limit,那么继续处理这一批固定调单的下一批数据
//
//        }
        return results;
    }

    /**
     * <h2> 处理调单卡号为资金沉淀卡号的情况 </h2>
     *
     * @param request 快进快出请求
     * @param limit   需要返回的数量
     */
    private List<FastInFastOutResult> processAdjustCardAsFundDepositCard(FastInFastOutRequest request, int limit) throws ExecutionException, InterruptedException {

        // 按照排序需要将分页的足够数据筛选出来
        List<FastInFastOutResult> results = new ArrayList<>();
        batchProcessDeposit(request, QUERY_SIZE, limit, results);
        if (results.size() == 0) {
            return null;
        }
//        while (results.size() < limit) {
//
//            // TODO
//            // 如果 results的size 还是小于需要返回的limit,那么继续处理这一批固定调单的下一批数据
//        }
        return results;
    }

    /**
     * <h2> 处理调单卡号为资金中转卡号的情况 </h2>
     *
     * @param request 快进快出请求
     * @param limit   需要返回的数量
     */
    private List<FastInFastOutResult> processAdjustCardAsFundTransitCard(FastInFastOutRequest request, int limit) {


        List<FastInFastOutResult> results = new ArrayList<>();
        batchProcessTransit(request, QUERY_SIZE, limit, results);
        if (results.size() == 0) {
            return null;
        }
//        while (results.size() < limit) {
//            // TODO
//            // 如果 results的size 还是小于需要返回的limit,那么继续处理这一批固定调单的下一批数据
//        }
        return results;
    }

    /**
     * <h2> 批量处理沉淀数据(调单卡号为沉淀的情况) </h2>
     */
    private void batchProcessDeposit(FastInFastOutRequest request, int querySize, int limit, List<FastInFastOutResult> results) throws ExecutionException, InterruptedException {

        int singleQuota = request.getSingleQuota();
        String caseId = request.getCaseId();
        // 排序请求
        SortRequest sortRequest = request.getSortRequest();
        String sortProperty = FundTacticsAnalysisField.CHANGE_MONEY;
        if (sortRequest.getSortType().equals("outflowTime")) {
            sortProperty = FundTacticsAnalysisField.TRADING_TIME;
        }
        // 查询记录的起始位置
        int from = 0;
        com.zqykj.domain.PageRequest pageRequest =
                com.zqykj.domain.PageRequest.of(from, querySize, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortProperty);
        List<BankTransactionRecord> bankTransactionRecords = getOrderRecordsViaDeposit(request, pageRequest, querySize);
        if (CollectionUtils.isEmpty(bankTransactionRecords)) {
            return;
        }
        // 过滤一部分数据,避免下面查询次数太多
        if (!sortRequest.getSortType().equals("inflowAmount")) {
            // 对方卡号集合 作为对方卡号,查询卡号作为调单卡号,借贷标志为进
            List<String> oppositeAdjustCards = bankTransactionRecords.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
            QuerySpecialParams filterQuery = queryRequestParamFactory.getFastInOutTradeRecordsByLocalOpposite(caseId, singleQuota, request.getCardNum(),
                    oppositeAdjustCards, true, null, null);
            AggregationParams agg = aggregationRequestParamFactory.groupByField(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, oppositeAdjustCards.size(),
                    new Pagination(0, oppositeAdjustCards.size()));
            agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD));
            agg.setResultName("distinctOppositeCard");
            Map<String, List<List<Object>>> maps = entranceRepository.compoundQueryAndAgg(filterQuery, agg, BankTransactionRecord.class, caseId);
            if (CollectionUtils.isEmpty(maps) || CollectionUtils.isEmpty(maps.get(agg.getResultName()))) {
                return;
            }
            Map<String, String> cardsMap = maps.get(agg.getResultName()).stream().collect(Collectors.toMap(e -> e.get(0).toString(), e1 -> e1.get(0).toString(), (v1, v2) -> v1));
            bankTransactionRecords = bankTransactionRecords.stream().filter(e -> cardsMap.containsKey(e.getQueryCard())).collect(Collectors.toList());
        } else {
            // 对方卡号集合 作为查询卡号过滤,且借贷标志为进
            List<String> queryCards = bankTransactionRecords.stream().map(BankTransactionRecord::getTransactionOppositeCard).distinct().collect(Collectors.toList());
            QuerySpecialParams filterQuery = queryRequestParamFactory.getFastInOutTradeRecordsByCondition(caseId, singleQuota, queryCards, true, null, null);
            AggregationParams agg = aggregationRequestParamFactory.groupByField(FundTacticsAnalysisField.QUERY_CARD, queryCards.size(),
                    new Pagination(0, queryCards.size()));
            agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD));
            agg.setResultName("distinctQueryCard");
            Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(filterQuery, agg, BankTransactionRecord.class, caseId);
            if (CollectionUtils.isEmpty(resultMaps) || CollectionUtils.isEmpty(resultMaps.get(agg.getResultName()))) {
                return;
            }
            Map<String, String> cardsMap = resultMaps.get(agg.getResultName()).stream().collect(Collectors.toMap(e -> e.get(0).toString(), e1 -> e1.get(0).toString(), (v1, v2) -> v1));
            bankTransactionRecords = bankTransactionRecords.stream().filter(e -> cardsMap.containsKey(e.getTransactionOppositeCard())).collect(Collectors.toList());
        }
        // 依次计算每一条记录
        for (BankTransactionRecord first : bankTransactionRecords) {
            if (results.size() >= limit) {
                return;
            }
            List<BankTransactionRecord> second;
            if (!request.getSortRequest().getSortType().equals("inflowAmount")) {
                second = getFastInOutTradeRecords(request.getCaseId(), singleQuota, Collections.singletonList(first.getTransactionOppositeCard()), true,
                        first.getTradingTime(), QueryOperator.lte, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
            } else {
                second = getFastInOutTradeRecordsViaLocalOpposite(request.getCaseId(), singleQuota, request.getCardNum(), Collections.singletonList(first.getQueryCard()),
                        true, first.getTradingTime(), QueryOperator.gte, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
            }
            if (CollectionUtils.isEmpty(second)) {
                continue;
            }
            // 获取快进快出结果(处理调单卡号作为来源的情况)
            computeResultFromDeposit(sortRequest.getSortType(), request.getCharacteristicRatio(), request.getTimeInterval(), first, second, results, limit);
            if (second.size() < querySize) {
                continue;
            }
            // 若limit 不够,继续下钻 second 记录(调整分页参数)
            while (results.size() < limit) {

                from = from + querySize;
                List<BankTransactionRecord> secondNew;
                if (!request.getSortRequest().getSortType().equals("inflowAmount")) {
                    secondNew = getFastInOutTradeRecords(request.getCaseId(), singleQuota, Collections.singletonList(first.getTransactionOppositeCard()), true,
                            first.getTradingTime(), QueryOperator.lte, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
                } else {
                    secondNew = getFastInOutTradeRecordsViaLocalOpposite(request.getCaseId(), singleQuota, request.getCardNum(),
                            Collections.singletonList(first.getQueryCard()), false, first.getTradingTime(), QueryOperator.gte, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
                }
                if (CollectionUtils.isEmpty(secondNew)) {
                    // 结束while,进行下一轮的 batchProcessSource()
                    break;
                }
                computeResultFromDeposit(sortRequest.getSortType(), request.getCharacteristicRatio(), request.getTimeInterval(), first, secondNew, results, limit);
                if (secondNew.size() < querySize) {
                    break;
                }
            }
        }
    }

    /**
     * <h2> 批量处理调单卡号作为资金来源卡号 </h2>
     * <p>
     * 方案一
     * 处理的是 调单卡号 ----(出) ----(出)
     * 调单卡号作为来源首先过滤出出账的数据(借贷标志为出), 以这批数据的对方卡号(可能是调单或者非调单) 继续过滤出账记录(借贷标志为出)
     */
    private void batchProcessSource(FastInFastOutRequest request, int querySize, int limit, List<FastInFastOutResult> results) throws ExecutionException, InterruptedException {

        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        // 排序请求
        SortRequest sortRequest = request.getSortRequest();
        String sortProperty = FundTacticsAnalysisField.CHANGE_MONEY;
        if (sortRequest.getSortType().equals("outflowTime")) {
            sortProperty = FundTacticsAnalysisField.TRADING_TIME;
        }
        // 查询记录的起始位置
        int from = 0;
        com.zqykj.domain.PageRequest pageRequest =
                com.zqykj.domain.PageRequest.of(from, querySize, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortProperty);
        List<BankTransactionRecord> bankTransactionRecords = getOrderRecordsViaSource(request, querySize, pageRequest);
        if (CollectionUtils.isEmpty(bankTransactionRecords)) {
            return;
        }
        // 过滤一部分数据,避免下面查询次数太多
        if (!sortRequest.getSortType().equals("inflowAmount")) {
            List<String> oppositeAdjustCards = bankTransactionRecords.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
            QuerySpecialParams filterQuery = queryRequestParamFactory.getFastInOutTradeRecordsByLocalOpposite(caseId, singleQuota, request.getCardNum(),
                    oppositeAdjustCards, false, null, null);
            AggregationParams agg = aggregationRequestParamFactory.groupByField(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, oppositeAdjustCards.size(),
                    new Pagination(0, oppositeAdjustCards.size()));
            agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD));
            agg.setResultName("distinctOppositeCard");
            Map<String, List<List<Object>>> maps = entranceRepository.compoundQueryAndAgg(filterQuery, agg, BankTransactionRecord.class, caseId);
            if (CollectionUtils.isEmpty(maps) || CollectionUtils.isEmpty(maps.get(agg.getResultName()))) {
                return;
            }
            Map<String, String> cardsMap = maps.get(agg.getResultName()).stream().collect(Collectors.toMap(e -> e.get(0).toString(), e1 -> e1.get(0).toString(), (v1, v2) -> v1));
            bankTransactionRecords = bankTransactionRecords.stream().filter(e -> cardsMap.containsKey(e.getQueryCard())).collect(Collectors.toList());
        } else {
            List<String> queryCards = bankTransactionRecords.stream().map(BankTransactionRecord::getTransactionOppositeCard).distinct().collect(Collectors.toList());
            QuerySpecialParams filterQuery = queryRequestParamFactory.getFastInOutTradeRecordsByCondition(caseId, singleQuota, queryCards, false, null, null);
            AggregationParams agg = aggregationRequestParamFactory.groupByField(FundTacticsAnalysisField.QUERY_CARD, queryCards.size(),
                    new Pagination(0, queryCards.size()));
            agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD));
            agg.setResultName("distinctQueryCard");
            Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(filterQuery, agg, BankTransactionRecord.class, caseId);
            if (CollectionUtils.isEmpty(resultMaps) || CollectionUtils.isEmpty(resultMaps.get(agg.getResultName()))) {
                return;
            }
            Map<String, String> cardsMap = resultMaps.get(agg.getResultName()).stream().collect(Collectors.toMap(e -> e.get(0).toString(), e1 -> e1.get(0).toString(), (v1, v2) -> v1));
            bankTransactionRecords = bankTransactionRecords.stream().filter(e -> cardsMap.containsKey(e.getTransactionOppositeCard())).collect(Collectors.toList());
        }
        // 依次计算每一条记录
        for (BankTransactionRecord first : bankTransactionRecords) {
            if (results.size() >= limit) {
                return;
            }
            List<BankTransactionRecord> second;
            if (!sortRequest.getSortType().equals("inflowAmount")) {
                second = getFastInOutTradeRecordsViaLocalOpposite(caseId, singleQuota, request.getCardNum(),
                        Collections.singletonList(first.getQueryCard()), false, first.getTradingTime(), QueryOperator.lte, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
            } else {
                second = getFastInOutTradeRecords(caseId, singleQuota, Collections.singletonList(first.getTransactionOppositeCard()),
                        false, first.getTradingTime(), QueryOperator.gte, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
            }
            if (CollectionUtils.isEmpty(second)) {
                continue;
            }
            // 获取快进快出结果(处理调单卡号作为来源的情况)i
            computeResultFromSource(sortProperty, request.getCharacteristicRatio(), request.getTimeInterval(), first, second, results, limit);
            if (second.size() < querySize) {
                continue;
            }
            // 若limit 不够,继续下钻 second 记录(调整分页参数)
            int fromNew = 0;
            while (results.size() < limit) {

                fromNew++;
                com.zqykj.domain.PageRequest pageRequestNew =
                        com.zqykj.domain.PageRequest.of(fromNew, querySize, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortProperty);
                List<BankTransactionRecord> secondNew;
                if (!sortRequest.getSortType().equals("inflowAmount")) {
                    // 这个数据需要它的对端卡号一定是调单的才行
                    secondNew = getFastInOutTradeRecordsViaLocalOpposite(caseId, singleQuota, Collections.singletonList(first.getQueryCard()),
                            request.getCardNum(), true, first.getTradingTime(), QueryOperator.lte, pageRequestNew, FundTacticsAnalysisField.fastInFastOutFields());
                } else {
                    secondNew = getFastInOutTradeRecords(caseId, singleQuota, Collections.singletonList(first.getTransactionOppositeCard()),
                            false, first.getTradingTime(), QueryOperator.gte, pageRequestNew, FundTacticsAnalysisField.fastInFastOutFields());
                }
                if (CollectionUtils.isEmpty(secondNew)) {
                    // 结束while,进行下一轮的 batchProcessSource()
                    break;
                }
                computeResultFromSource(sortProperty, request.getCharacteristicRatio(), request.getTimeInterval(), first, secondNew, results, limit);
                if (secondNew.size() < querySize) {
                    break;
                }
            }
        }
    }

    /**
     * <h2> 批量处理调单卡号为中转卡号情况 </h2>
     */
    @SuppressWarnings("all")
    private void batchProcessTransit(FastInFastOutRequest request, int querySize, int limit, List<FastInFastOutResult> results) {

        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        // 排序请求
        SortRequest sortRequest = request.getSortRequest();
        // 计算单卡 查询的入账 还是 出账的数据(排序的进出 和 单卡查询的进出相反)
        // eg. 调单卡为中转卡情况下, 若按流入金额排序(查询的是这些调单进账的数据,假设拿到一个最高的一条,需要与沉淀数据匹配,必然查询的是这个调单的出账数据)
        String sortProperty = FundTacticsAnalysisField.CHANGE_MONEY;
        // 流出金额、流出日期排序
        boolean isCredits = false;
        QueryOperator operator = QueryOperator.lte;
        if (sortRequest.getSortType().equals("outflowTime")) {
            sortProperty = FundTacticsAnalysisField.TRADING_TIME;
        } else if (sortRequest.getSortType().equals("inflowAmount")) {
            // 流入金额排序
            isCredits = true;
            operator = QueryOperator.gte;
        }
        // 查询的起始位置
        int from = 0;
        com.zqykj.domain.PageRequest pageRequest =
                com.zqykj.domain.PageRequest.of(from, querySize, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortProperty);
        List<BankTransactionRecord> bankTransactionRecords = getTradeRecordsViaCards(caseId, singleQuota, request.getCardNum(),
                isCredits, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
        if (CollectionUtils.isEmpty(bankTransactionRecords)) {
            return;
        }
        // 看看这些调单卡号中哪些有出账的(这些调单查询卡号是根据排序得来的)
        List<String> queryCards = bankTransactionRecords.stream().map(e -> e.getQueryCard()).distinct().collect(Collectors.toList());
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.getFastInOutTradeRecordsByCondition(caseId, singleQuota, queryCards, !isCredits, null, null);
        AggregationParams aggregationParams = aggregationRequestParamFactory.groupByField(FundTacticsAnalysisField.QUERY_CARD, queryCards.size(), new Pagination(0, queryCards.size()));
        aggregationParams.setMapping(aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        aggregationParams.setResultName("queryCardGroupBy");
        Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultMaps)) {
            return;
        }
        List<List<Object>> result = resultMaps.get(aggregationParams.getResultName());
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        Map<String, String> queryCardMaps = result.stream().map(e -> e.get(0).toString()).collect(Collectors.toMap(e -> e.toString(), e -> e.toString(), (v1, v2) -> v1));
        // 筛选出符合的查询卡号
        List<BankTransactionRecord> bankTransactionRecordsNew = bankTransactionRecords.stream().filter(e -> queryCardMaps.containsKey(e.getQueryCard()))
                .collect(Collectors.toList());
        // 依次计算每一条记录
        for (BankTransactionRecord first : bankTransactionRecordsNew) {
            if (results.size() >= limit) {
                return;
            }
            // 根据排序查询入账和出账的数据
            List<BankTransactionRecord> second = getFastInOutTradeRecords(caseId, singleQuota, Collections.singletonList(first.getQueryCard()),
                    !isCredits, first.getTradingTime(), operator, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
            if (CollectionUtils.isEmpty(second)) {
                continue;
            }
            // 处理多个来源/多个沉淀情况
            computeResultFromTransit(sortRequest.getSortType(), request.getCharacteristicRatio(), request.getTimeInterval(), first, second, results, limit);
            if (second.size() < querySize) {
                continue;
            }
            //  当limit 不够, 继续下钻 second 数据
            int fromNew = 0;
            while (results.size() < limit) {
                fromNew++;
                com.zqykj.domain.PageRequest pageRequestNew =
                        com.zqykj.domain.PageRequest.of(fromNew, querySize, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortProperty);
                List<BankTransactionRecord> secondNew = getFastInOutTradeRecords(caseId, singleQuota, Collections.singletonList(first.getQueryCard()),
                        !isCredits, first.getTradingTime(), operator, pageRequestNew, FundTacticsAnalysisField.fastInFastOutFields());
                if (CollectionUtils.isEmpty(secondNew)) {
                    // 结束while,进行下一轮的 batchProcessTransit()
                    break;
                }
                computeResultFromTransit(sortRequest.getSortType(), request.getCharacteristicRatio(), request.getTimeInterval(), first, second, results, limit);
                if (secondNew.size() < querySize) {
                    break;
                }
            }
        }
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
     * <h2> 返回的数据是根据排序来的(调单卡号作为来源卡号的情况) </h2>
     */
    private List<BankTransactionRecord> getOrderRecordsViaSource(FastInFastOutRequest request, int querySize,
                                                                 com.zqykj.domain.PageRequest pageRequest) throws ExecutionException, InterruptedException {

        List<String> cardNum = request.getCardNum();
        String sortType = request.getSortRequest().getSortType();
        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        String[] fields = FundTacticsAnalysisField.fastInFastOutFields();
        if (sortType.equals("inflowAmount")) {
            // 按流入金额排序
            return getTradeRecordsViaCards(caseId, singleQuota, cardNum, false, pageRequest, fields);
        } else {
            // 分为2种情况处理
            // 过滤调单卡号的情况(查询卡号是调单、对方卡号也是调单卡号的情况,借贷标志为出)
            List<String> oppositeCards = getLocalAndOppositeAsAdjust(request, false);
            // 计算 调单卡号(作为查询卡号)、对方卡号(不在这些调单卡号之内的),借贷标志为出的交易记录,然后对对方卡号去重(获取去重后的总数量)
            long distinct = getDistinctLocalAsAdjustAndOppositeNoSuchAdjust(request, FundTacticsAnalysisField.LOAN_FLAG_OUT);
            if (CollectionUtils.isEmpty(oppositeCards) && distinct == 0L) {
                return null;
            }
            // 若 oppositeCards size 大于10000 (暂定),则使用方案一,否则使用方案二
            if (distinct > OPPOSITE_CARD_COUNT) {

                return getOutFlowOrderResultFromSourceOne(request, oppositeCards, querySize, pageRequest, fields);
            } else {
                return getOutFlowOrderResultFromSourceTwo(request, pageRequest, oppositeCards, Integer.parseInt(String.valueOf(distinct)), querySize);
            }
        }
    }


    /**
     * <h2> 返回以流出金额、流出日期排序的结果(调单卡号作为来源的情况) </h2>
     * <p>
     * 方案一
     */
    private List<BankTransactionRecord> getOutFlowOrderResultFromSourceOne(FastInFastOutRequest request, List<String> oppositeAdjustCards,
                                                                           int querySize, com.zqykj.domain.PageRequest pageRequest,
                                                                           String... fields) {
        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        // 调单卡号(原调单卡号出账的对方卡号) 出账记录的排序
        List<BankTransactionRecord> adjustPayoutRecords =
                getTradeRecordsViaCards(caseId, singleQuota, oppositeAdjustCards, false, pageRequest, fields);
        // 非调单(原调单卡号出账的对方卡号)  出账记录的排序
        List<BankTransactionRecord> unadjustedRecords = getOrderRecordsUnadjusted(request, false, querySize);
        // 合并排序
        unadjustedRecords.addAll(adjustPayoutRecords);
        // TODO 将 unadjustedRecords 分散开来,多线程异步去查
        // 检查这些卡号是否是调单卡号出账记录的对方卡号
        List<String> oppositeCards = unadjustedRecords.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
        Map<String, String> filterOppositeCards = getCreditAndPayOutViaLocalAndOpposite(caseId, singleQuota, request.getCardNum(),
                oppositeCards, querySize, false, request.getSortRequest(), FundTacticsAnalysisField.fastInFastOutFields())
                .stream().collect(Collectors.toMap(BankTransactionRecord::getTransactionOppositeCard, BankTransactionRecord::getTransactionOppositeCard, (v1, v2) -> v1));
        // 过滤对方卡号集合
        if (!CollectionUtils.isEmpty(filterOppositeCards)) {
            return unadjustedRecords.stream().filter(e -> oppositeCards.contains(e.getQueryCard())).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * <h2> 返回以流出金额、流出日期排序的结果(调单卡号作为来源的情况) </h2>
     * <p>
     * 方案二
     */
    @SuppressWarnings("all")
    private List<BankTransactionRecord> getOutFlowOrderResultFromSourceTwo(FastInFastOutRequest request, com.zqykj.domain.PageRequest pageRequest,
                                                                           List<String> oppositeAdjustCards, int total, int querySize) throws ExecutionException, InterruptedException {
        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        // total 要适当加一些(因为es的去重数量算的是近似值,所以可能会比实际的值少)
        if (total == 0) {
            return null;
        }
        total = total + total / 100;
        // 异步任务执行
        List<BankTransactionRecord> finalTransactionRecords = new ArrayList<>();
        List<CompletableFuture<List<BankTransactionRecord>>> futures = new ArrayList<>();
        int position = 0;
        // TODO 暂定
        int chunkSize = 1000;
        while (position < total) {
            int next = Math.min(position + chunkSize, total);
            int finalPosition = position;
            CompletableFuture<List<BankTransactionRecord>> future = CompletableFuture.supplyAsync(() -> getQueryAsAdjustOppositeNoSuchAdjust(request, finalPosition, next,
                    FundTacticsAnalysisField.LOAN_FLAG_OUT, false, pageRequest), ThreadPoolConfig.getExecutor());
            futures.add(future);
            position = next;
        }
        for (CompletableFuture<List<BankTransactionRecord>> future : futures) {
            List<BankTransactionRecord> bankTransactionRecords = future.get();
            if (!CollectionUtils.isEmpty(bankTransactionRecords)) {
                finalTransactionRecords.addAll(bankTransactionRecords);
            }
        }
        // 再次对 finalTransactionRecords 排序,取前 querySize
        List<BankTransactionRecord> unadjustedRecords = finalTransactionRecords.stream().sorted(Comparator.comparing(BankTransactionRecord::getChangeAmount, Comparator.reverseOrder()))
                .skip(0).limit(querySize).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(finalTransactionRecords) && CollectionUtils.isEmpty(oppositeAdjustCards)) {
            return null;
        }
        if (!CollectionUtils.isEmpty(oppositeAdjustCards)) {
            // 调单卡号出账记录的排序
            List<BankTransactionRecord> adjustPayoutRecords = getTradeRecordsViaCards(caseId, singleQuota, oppositeAdjustCards, false, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
            // 合并继续排序
            unadjustedRecords.addAll(adjustPayoutRecords);
        }
        return unadjustedRecords.stream().sorted(Comparator.comparing(BankTransactionRecord::getChangeAmount, Comparator.reverseOrder())).
                skip(0).limit(querySize).collect(Collectors.toList());
    }

    /**
     * <h2> 根据排序获取沉淀数据(调单卡号作为沉淀情况) </h2>
     */
    private List<BankTransactionRecord> getOrderRecordsViaDeposit(FastInFastOutRequest request, com.zqykj.domain.PageRequest pageRequest, int querySize) throws ExecutionException, InterruptedException {

        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        List<String> cardNum = request.getCardNum();
        String[] fields = FundTacticsAnalysisField.fastInFastOutFields();
        if (!request.getSortRequest().getSortType().equals("inflowAmount")) {
            // 按流出金额、流出日期排序(调单卡号作为查询卡号,找出进的)
            return getTradeRecordsViaCards(caseId, singleQuota, cardNum, true, pageRequest, fields);
        } else {
            // 过滤调单卡号的情况(查询卡号是调单、对方卡号也是调单卡号的情况,借贷标志为进)
            List<String> oppositeCards = getLocalAndOppositeAsAdjust(request, true);
            // 计算 调单卡号(作为查询卡号)、对方卡号(不在这些调单卡号之内的),借贷标志为进的交易记录,然后对对方卡号去重(获取去重后的总数量)
            long distinct = getDistinctLocalAsAdjustAndOppositeNoSuchAdjust(request, FundTacticsAnalysisField.LOAN_FLAG_IN);
            // 若 oppositeCards size 大于10000 (暂定),则使用方案一,否则使用方案二
            if (distinct > OPPOSITE_CARD_COUNT) {

                return getInFlowOrderResultFromDepositOne(request, oppositeCards, pageRequest, querySize, fields);
            } else {

                return getInFlowOrderResultFromDepositTwo(request, pageRequest, oppositeCards, Integer.parseInt(String.valueOf(distinct)), querySize);
            }
        }

    }


    /**
     * <h2> 获取流入金额排序的结果(调单卡号作为沉淀的情况)  </h2>
     * <p>
     * 方案一
     */
    private List<BankTransactionRecord> getInFlowOrderResultFromDepositOne(FastInFastOutRequest request, List<String> oppositeAdjustCards,
                                                                           com.zqykj.domain.PageRequest pageRequest, int querySize, String... fields) {

        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        SortRequest sortRequest = request.getSortRequest();
        // 调单卡号(原调单卡号进账的对方卡号) 进账记录的排序
        List<BankTransactionRecord> adjustPayoutRecords = getTradeRecordsViaCards(caseId, singleQuota, oppositeAdjustCards, false, pageRequest, fields);
        // 未调单卡号(原调单卡号进账的对方卡号) 进账记录的排序
        List<BankTransactionRecord> unadjustedRecords = getOrderRecordsUnadjusted(request, true, querySize);
        // 合并排序
        unadjustedRecords.addAll(adjustPayoutRecords);
        // TODO 将 unadjustedRecords 分散开来,多线程异步去查
        // 检查这些卡号是否调单卡号进账记录的对方卡号
        List<String> oppositeCards = unadjustedRecords.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
        Map<String, String> filterOppositeCards = getCreditAndPayOutViaLocalAndOpposite(caseId, singleQuota, request.getCardNum(), oppositeCards, querySize, true, sortRequest,
                FundTacticsAnalysisField.fastInFastOutFields()).stream().collect(Collectors.toMap(BankTransactionRecord::getTransactionOppositeCard,
                BankTransactionRecord::getTransactionOppositeCard, (v1, v2) -> v1));
        // 过滤对方卡号集合
        if (!CollectionUtils.isEmpty(filterOppositeCards)) {

            return unadjustedRecords.stream().filter(e -> oppositeCards.contains(e.getQueryCard())).collect(Collectors.toList());
        } else {
            // 结束,进行下一次的批量查询batchProcessDeposit
            return null;
        }
    }

    /**
     * <h2> 获取流入金额排序的结果(调单卡号作为沉淀的情况)  </h2>
     * <p>
     * 方案二
     */
    @SuppressWarnings("all")
    private List<BankTransactionRecord> getInFlowOrderResultFromDepositTwo(FastInFastOutRequest request, com.zqykj.domain.PageRequest pageRequest,
                                                                           List<String> oppositeAdjustCards, int total, int querySize) throws ExecutionException, InterruptedException {
        List<String> cardNum = request.getCardNum();
        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        if (!request.getSortRequest().getSortType().equals("inflowAmount")) {
            // 流出金额、流出时间日期 排序(借贷标志为进)
            return getTradeRecordsViaCards(caseId, singleQuota, cardNum, true, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
        } else {
            // total 要适当加一些(因为es的去重数量算的是近似值,所以可能会比实际的值少)
            if (total == 0) {
                return null;
            }
            total = total + total / 100;
            // 异步任务执行
            List<BankTransactionRecord> finalTransactionRecords = new ArrayList<>();
            List<CompletableFuture<List<BankTransactionRecord>>> futures = new ArrayList<>();
            int position = 0;
            int chunkSize = 1000;
            while (position < total) {
                int next = Math.min(position + chunkSize, total);
                int finalPosition = position;
                CompletableFuture<List<BankTransactionRecord>> future = CompletableFuture.supplyAsync(() ->
                        getQueryAsAdjustOppositeNoSuchAdjust(request, finalPosition, next, FundTacticsAnalysisField.LOAN_FLAG_IN, true, pageRequest), ThreadPoolConfig.getExecutor());
                futures.add(future);
                position = next;
            }
            for (CompletableFuture<List<BankTransactionRecord>> future : futures) {
                finalTransactionRecords.addAll(future.get());
            }
            // 再次对 finalTransactionRecords 排序,取前 querySize
            List<BankTransactionRecord> unadjustedRecords = finalTransactionRecords.stream().sorted(Comparator.comparing(BankTransactionRecord::getChangeAmount, Comparator.reverseOrder()))
                    .skip(0).limit(querySize).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(finalTransactionRecords) && CollectionUtils.isEmpty(oppositeAdjustCards)) {
                return null;
            }
            // 调单卡号进账记录的排序
            if (!CollectionUtils.isEmpty(oppositeAdjustCards)) {
                List<BankTransactionRecord> adjustPayoutRecords = getTradeRecordsViaCards(caseId, singleQuota, oppositeAdjustCards, true, pageRequest, FundTacticsAnalysisField.fastInFastOutFields());
                // 合并继续排序
                unadjustedRecords.addAll(adjustPayoutRecords);
            }
            return unadjustedRecords.stream().sorted(Comparator.comparing(BankTransactionRecord::getChangeAmount, Comparator.reverseOrder())).
                    skip(0).limit(querySize).collect(Collectors.toList());
        }
    }

    /**
     * <h2> 返回的数据是根据排序来的(查询卡号为非调单的情况) </h2>
     */
    private List<BankTransactionRecord> getOrderRecordsUnadjusted(FastInFastOutRequest request, boolean isCredits, int size) {

        // 排序请求
        return getCreditAndPayoutRecordsNoSuchCards(request, size, isCredits, FundTacticsAnalysisField.fastInFastOutFields());
    }


    /**
     * <h2> 计算快进快出结果 </h2>
     * <p>
     * 适用于调单卡号作为来源的情况
     */
    private void computeResultFromSource(String sortType, int characteristicRatio, long timeInterval,
                                         BankTransactionRecord first, List<BankTransactionRecord> seconds,
                                         List<FastInFastOutResult> results, int limit) {
        for (BankTransactionRecord second : seconds) {
            // 如果满足limit也停止计算
            if (results.size() >= limit) {
                break;
            }
            boolean checkTimeInterval = checkTimeIntervalAndFeatureRatio(sortType, characteristicRatio, timeInterval, first, second);
            if (checkTimeInterval) {
                FastInFastOutResult fastInFastOutResult = convertFromDataSource(first, second, sortType);
                results.add(fastInFastOutResult);
            }
        }
    }

    /**
     * <h2> 计算快进快出结果 </h2>
     * <p>
     * 适用于调单卡号作为中转情况
     */
    private void computeResultFromTransit(String sortType, int characteristicRatio, long timeInterval,
                                          BankTransactionRecord first, List<BankTransactionRecord> seconds,
                                          List<FastInFastOutResult> results, int limit) {
        for (BankTransactionRecord second : seconds) {
            // 如果满足limit也停止计算
            if (results.size() >= limit) {
                break;
            }
            // 检查特征比
            boolean checkTimeInterval = checkTimeIntervalAndFeatureRatio(sortType, characteristicRatio, timeInterval, first, second);
            if (checkTimeInterval) {
                FastInFastOutResult fastInFastOutResult = convertFromDataTransit(first, second, sortType);
                results.add(fastInFastOutResult);
            }
        }
    }


    /**
     * <h2> 计算快进快出结果 </h2>
     * <p>
     * 适用于调单卡号作为沉淀情况
     */
    private void computeResultFromDeposit(String sortType, int characteristicRatio, long timeInterval,
                                          BankTransactionRecord first, List<BankTransactionRecord> seconds,
                                          List<FastInFastOutResult> results, int limit) {
        for (BankTransactionRecord second : seconds) {
            // 如果满足limit也停止计算
            if (results.size() >= limit) {
                break;
            }
            // 检查特征比
            boolean checkTimeInterval = checkTimeIntervalAndFeatureRatio(sortType, characteristicRatio, timeInterval, first, second);
            if (checkTimeInterval) {
                FastInFastOutResult fastInFastOutResult = convertFromDataDeposit(first, second, sortType);
                results.add(fastInFastOutResult);
            }
        }
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
    private FastInFastOutResult convertFromDataSource(BankTransactionRecord first, BankTransactionRecord second, String sortType) {

        FastInFastOutResult fastInFastOutResult = new FastInFastOutResult();
        // 入账金额
        double creditAmount;
        // 按照入账金额排序
        if ("inflowAmount".equals(sortType)) {
            // 资金来源卡号
            fastInFastOutResult.setFundSourceCard(first.getQueryCard());
            // 资金来源户名
            fastInFastOutResult.setFundSourceAccountName(first.getCustomerName());
            // 流入时间日期
            fastInFastOutResult.setInflowDate(format.format(first.getTradingTime()));
            // 流入金额
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            creditAmount = first.getChangeAmount();
            // 资金中转卡号
            fastInFastOutResult.setFundTransitCard(first.getTransactionOppositeCard());
            // 资金中转户名
            fastInFastOutResult.setFundTransitAccountName(first.getTransactionOppositeName());
            // 流出时间日期
            fastInFastOutResult.setOutflowDate(format.format(second.getTradingTime()));
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
            fastInFastOutResult.setInflowDate(format.format(second.getTradingTime()));
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            creditAmount = second.getChangeAmount();
            fastInFastOutResult.setFundTransitCard(first.getQueryCard());
            fastInFastOutResult.setFundTransitAccountName(first.getCustomerName());
            fastInFastOutResult.setOutflowDate(format.format(first.getTradingTime()));
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            fastInFastOutResult.setFundDepositCard(first.getTransactionOppositeCard());
            fastInFastOutResult.setFundDepositAccountName(first.getTransactionOppositeName());
        }
        // 特征比(入账金额-出账金额) / 入账金额
        computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), fastInFastOutResult, creditAmount);
        return fastInFastOutResult;
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为中转情况
     */
    @SuppressWarnings("all")
    private FastInFastOutResult convertFromDataTransit(BankTransactionRecord first, BankTransactionRecord second, String sortType) {

        FastInFastOutResult fastInFastOutResult = new FastInFastOutResult();
        double inflowAmount;
        if (sortType.equals("inflowAmount")) {
            // 资金来源卡号
            fastInFastOutResult.setFundSourceCard(first.getTransactionOppositeCard());
            // 资金来源户名
            fastInFastOutResult.setFundSourceAccountName(first.getTransactionOppositeName());
            // 流入时间日期
            fastInFastOutResult.setInflowDate(format.format(first.getTradingTime()));
            // 流入金额
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            inflowAmount = first.getChangeAmount();
            // 资金中转卡号
            fastInFastOutResult.setFundTransitCard(first.getQueryCard());
            // 资金中转户名
            fastInFastOutResult.setFundTransitAccountName(first.getCustomerName());
            // 流出时间日期
            fastInFastOutResult.setOutflowDate(format.format(second.getTradingTime()));
            // 流出金额
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            // 资金沉淀卡号
            fastInFastOutResult.setFundDepositCard(second.getTransactionOppositeCard());
            // 资金沉淀户名
            fastInFastOutResult.setFundDepositAccountName(second.getTransactionOppositeName());
        } else {
            fastInFastOutResult.setFundSourceCard(second.getTransactionOppositeCard());
            fastInFastOutResult.setFundSourceAccountName(second.getTransactionOppositeName());
            fastInFastOutResult.setInflowDate(format.format(second.getTradingTime()));
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            inflowAmount = second.getChangeAmount();
            fastInFastOutResult.setFundTransitCard(second.getQueryCard());
            fastInFastOutResult.setFundTransitAccountName(second.getCustomerName());
            fastInFastOutResult.setOutflowDate(format.format(first.getTradingTime()));
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            fastInFastOutResult.setFundDepositCard(first.getTransactionOppositeCard());
            fastInFastOutResult.setFundDepositAccountName(first.getTransactionOppositeName());
        }
        // 特征比(入账金额-出账金额) / 入账金额
        computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), fastInFastOutResult, inflowAmount);
        return fastInFastOutResult;
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为沉淀情况
     */
    @SuppressWarnings("all")
    private FastInFastOutResult convertFromDataDeposit(BankTransactionRecord first, BankTransactionRecord second, String sortType) {

        FastInFastOutResult fastInFastOutResult = new FastInFastOutResult();
        double inflowAmount;
        if (sortType.equals("inflowAmount")) {
            // 资金来源卡号
            fastInFastOutResult.setFundSourceCard(first.getTransactionOppositeCard());
            // 资金来源户名
            fastInFastOutResult.setFundSourceAccountName(first.getTransactionOppositeName());
            // 流入时间日期
            fastInFastOutResult.setInflowDate(format.format(first.getTradingTime()));
            // 流入金额
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            inflowAmount = first.getChangeAmount();
            // 资金中转卡号
            fastInFastOutResult.setFundTransitCard(first.getQueryCard());
            // 资金中转户名
            fastInFastOutResult.setFundTransitAccountName(first.getCustomerName());
            // 流出时间日期
            fastInFastOutResult.setOutflowDate(format.format(second.getTradingTime()));
            // 流出金额
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            // 资金沉淀卡号
            fastInFastOutResult.setFundDepositCard(second.getQueryCard());
            // 资金沉淀户名
            fastInFastOutResult.setFundDepositAccountName(second.getCustomerName());
        } else {
            fastInFastOutResult.setFundSourceCard(second.getTransactionOppositeCard());
            fastInFastOutResult.setFundSourceAccountName(second.getTransactionOppositeName());
            fastInFastOutResult.setInflowDate(format.format(second.getTradingTime()));
            fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            inflowAmount = second.getChangeAmount();
            fastInFastOutResult.setFundTransitCard(second.getQueryCard());
            fastInFastOutResult.setFundTransitAccountName(second.getCustomerName());
            fastInFastOutResult.setOutflowDate(format.format(first.getTradingTime()));
            fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            fastInFastOutResult.setFundDepositCard(first.getQueryCard());
            fastInFastOutResult.setFundDepositAccountName(first.getCustomerName());
        }
        // 特征比(入账金额-出账金额) / 入账金额
        computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), fastInFastOutResult, inflowAmount);
        return fastInFastOutResult;
    }

    /**
     * <h2> 计算快进快出结果的特征比 </h2>
     */
    private void computeFastInFastOutCharacteristicRatio(double firstChangeAmount, double secondChangeAmount,
                                                         FastInFastOutResult fastInFastOutResult, double inflowAmount) {
        BigDecimal sub;
        if (firstChangeAmount < secondChangeAmount) {
            sub = BigDecimalUtil.sub(secondChangeAmount, firstChangeAmount);
        } else {
            sub = BigDecimalUtil.sub(firstChangeAmount, secondChangeAmount);
        }
        BigDecimal div = BigDecimalUtil.divReserveFour(sub.doubleValue(), inflowAmount);
        BigDecimal mul = BigDecimalUtil.mul(div.doubleValue(), 100);
        fastInFastOutResult.setCharacteristicRatio(mul.toString() + "%");
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
}
