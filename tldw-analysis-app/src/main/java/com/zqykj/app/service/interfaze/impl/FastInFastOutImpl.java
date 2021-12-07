/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFastInFastOut;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.app.service.vo.fund.middle.CreditAdjustCards;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.CombinationQueryParams;
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
import java.text.SimpleDateFormat;
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

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 对方卡号数量
    private static final int OPPOSITE_CARD_COUNT = 10000;
    // 每次查询入账/出账的分页size
    private static final int QUERY_SIZE = 2000;

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    public ServerResponse fastInFastOutAnalysis(FastInFastOutRequest request) throws ExecutionException, InterruptedException {

        // TODO 暂时先算选择个体的
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        // 需要返回的总数据量
        int limit = pageRequest.getPage() * pageRequest.getPageSize();
        // 异步操作
        List<CompletableFuture<List<FastInFastOutResult>>> futures = new ArrayList<>();
        // 返回调单卡号为来源卡号情况的结果数据
        CompletableFuture<List<FastInFastOutResult>> asFundSourceCardFuture = CompletableFuture.supplyAsync(() ->
        {
            try {
                return processAdjustCardAsFundSourceCard(request, QUERY_SIZE, limit);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }, ThreadPoolConfig.getExecutor());
        futures.add(asFundSourceCardFuture);
        // 返回调单卡号为中转卡号情况的结果数据
        CompletableFuture<List<FastInFastOutResult>> asFundTransitFuture = CompletableFuture.supplyAsync(() ->
                processAdjustCardAsFundTransitCard(request, QUERY_SIZE, limit), ThreadPoolConfig.getExecutor());
        futures.add(asFundTransitFuture);
        // 返回调单卡号为沉淀卡号情况的结果数据
        CompletableFuture<List<FastInFastOutResult>> asFundDepositFuture = CompletableFuture.supplyAsync(() ->
                processAdjustCardAsFundDepositCard(request, QUERY_SIZE, limit));
        futures.add(asFundDepositFuture);
        // 最终结果
        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();
        // 汇总调单卡号作为来源、中转、沉淀的三组结果数据
        for (CompletableFuture<List<FastInFastOutResult>> future : futures) {

            fastInFastOutResults.addAll(future.get());
        }
        // 对汇总后的数据进行分页与排序
        return ServerResponse.createBySuccess();
    }


    /**
     * <h2> 处理调单卡号为资金来源卡号的情况 </h2>
     *
     * @param request   快进快出请求
     * @param querySize 每次查询需要返回的数量
     * @param limit     需要返回的数量限制
     */
    private List<FastInFastOutResult> processAdjustCardAsFundSourceCard(FastInFastOutRequest request, int querySize, int limit) throws ExecutionException, InterruptedException {

        // 按照排序需要将分页的足够数据筛选出来
        // 需要筛选出2部分数据, 调单卡号出账数据(这部分作为来源-中转)、 调单卡后出账数据的出账数据(这部分作为中转-沉淀)
        List<FastInFastOutResult> results = new ArrayList<>();
        // 过滤调单卡号的情况(查询卡号是调单、对方卡号也是对方卡号的情况)
        AggregationParams agg = aggregationRequestParamFactory.buildFastInFastOutOppositeCardGroup(request);
        Map<String, String> mapping = aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);
        agg.setResultName("oppositeCardGroup");
        agg.setMapping(mapping);
        QuerySpecialParams query = queryRequestParamFactory.buildFastInFastOutAdjustQuery(request);
        Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, request.getCaseId());
        List<List<Object>> oppositeCardResults = resultMaps.get(agg.getResultName());
        if (CollectionUtils.isEmpty(oppositeCardResults)) {
            return new ArrayList<>();
        }
        // 对方卡号
        List<String> oppositeCards = oppositeCardResults.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
        // 计算 调单卡号(作为查询卡号)、对方卡号(不在这些调单卡号之内的),借贷标志为出的交易记录,然后对对方卡号去重(获取去重后的总数量)
        QuerySpecialParams distinctQuery = queryRequestParamFactory.oppositeCardDistinctFromFastInFastOut(request, FundTacticsAnalysisField.LOAN_FLAG_OUT);
        AggregationParams distinctAgg = aggregationRequestParamFactory.buildDistinctViaField(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);
        distinctAgg.setResultName("distinctOppositeCard");
        distinctAgg.setMapping(aggregationEntityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD));
        Map<String, List<List<Object>>> distinctResultMaps = entranceRepository.compoundQueryAndAgg(distinctQuery, distinctAgg, BankTransactionRecord.class, request.getCaseId());
        List<List<Object>> distinctTotal = distinctResultMaps.get(distinctAgg.getResultName());
        long total = (long) distinctTotal.get(0).get(0) + 1000L;
        // 若大于15000(暂定),则使用方案一,否则使用方案二
        batchProcessSource(request, oppositeCards, querySize, limit, total, results);
        while (results.size() < limit) {
            // TODO
            // 如果 results的size 还是小于需要返回的limit,那么继续处理这一批固定调单的下一批数据

        }
        return results;
    }

    /**
     * <h2> 批量处理调单卡号作为资金来源卡号 </h2>
     * <p>
     * 方案一
     * 处理的是 调单卡号 ----(出) ----(出)
     * 调单卡号作为来源首先过滤出出账的数据(借贷标志为出), 以这批数据的对方卡号(可能是调单或者非调单) 继续过滤出账记录(借贷标志为出)
     */
    @SuppressWarnings("all")
    private void batchProcessSource(FastInFastOutRequest request, List<String> oppositeCards, int querySize, int limit, long total,
                                    List<FastInFastOutResult> results) throws ExecutionException, InterruptedException {

        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        // 排序请求
        SortRequest sortRequest = request.getSortRequest();
        String property = sortRequest.getProperty();
        List<BankTransactionRecord> bankTransactionRecords;
        if (total > OPPOSITE_CARD_COUNT) {
            bankTransactionRecords = getOrderRecordsViaSource(request, oppositeCards, querySize);
        } else {
            bankTransactionRecords = getOrderRecordsViaSourceSecondOption(request, oppositeCards, Integer.parseInt(String.valueOf(total)), querySize);
        }
        if (CollectionUtils.isEmpty(bankTransactionRecords)) {
            return;
        }
        // 根据排序查询入账和出账的数据
        boolean isCredits = true;
        if (property.equals("flow_out_money") || property.equals("flow_out_time")) {
            isCredits = false;
        }
        // 查询记录的起始位置
        int from = 0;
        // 依次计算每一条记录
        for (BankTransactionRecord first : bankTransactionRecords) {
            List<BankTransactionRecord> second = getCreditAndPayOutRecordsViaCards(
                    caseId, singleQuota, Collections.singletonList(first.getQueryCard()), from, querySize, isCredits, sortRequest, FundTacticsAnalysisField.fastInFastOutFields());
            if (CollectionUtils.isEmpty(second)) {
                continue;
            }
            // 获取快进快出结果(处理调单卡号作为来源的情况)
            computeMultiSourceAndMultiDepositResult(request, results, first, second, limit);
            // 若limit 不够,继续下钻 second 记录(调整分页参数)
            while (results.size() < limit) {

                from = from + querySize;
                List<BankTransactionRecord> secondNew = getCreditAndPayOutRecordsViaCards(
                        caseId, singleQuota, Collections.singletonList(first.getQueryCard()), from, querySize, isCredits, sortRequest, FundTacticsAnalysisField.fastInFastOutFields());
                if (CollectionUtils.isEmpty(second)) {
                    // 结束while,进行下一轮的 batchProcessSource()
                    break;
                }
                computeMultiSourceAndMultiDepositResult(request, results, first, second, limit);
            }
        }
    }

    /**
     * <h2> 返回的数据是根据排序来的(调单卡号作为来源卡号的情况) </h2>
     * <p>
     * 方案二
     */
    private List<BankTransactionRecord> getOrderRecordsViaSourceSecondOption(FastInFastOutRequest request, List<String> oppositeAdjustCards,
                                                                             int total, int querySize) throws ExecutionException, InterruptedException {

        List<String> cardNum = request.getCardNum();
        SortRequest sortRequest = request.getSortRequest();
        String property = sortRequest.getProperty();
        // 查询记录的起始位置
        int from = 0;
        if (property.equals("flow_in_money")) {
            // 按入账金额排序
            return getOrderRecordsViaAdjust(request, cardNum, from, querySize);

        } else if (property.equals("flow_out_money") || property.equals("flow_out_time")) {
            // 异步任务执行
            List<BankTransactionRecord> finalTransactionRecords = new ArrayList<>();
            List<CompletableFuture<List<BankTransactionRecord>>> futures = new ArrayList<>();
            int position = 0;
            int chunkSize = 1000;
            while (position < total) {
                int next = Math.min(position + chunkSize, total);
                int finalPosition = position;
                CompletableFuture<List<BankTransactionRecord>> future = CompletableFuture.supplyAsync(() -> getPayoutRecordsFromAdjustDeposit(request, finalPosition, next, querySize), ThreadPoolConfig.getExecutor());
                futures.add(future);
                position = next;
            }
            for (CompletableFuture<List<BankTransactionRecord>> future : futures) {
                finalTransactionRecords.addAll(future.get());
            }
            // 再次对 finalTransactionRecords 排序,取前 querySize
            List<BankTransactionRecord> unadjustedRecords = finalTransactionRecords.stream().sorted(Comparator.comparing(BankTransactionRecord::getChangeAmount, Comparator.reverseOrder()))
                    .skip(0).limit(querySize).collect(Collectors.toList());
            // 调单卡号出账记录的排序
            List<BankTransactionRecord> adjustPayoutRecords = getOrderRecordsViaAdjust(request, oppositeAdjustCards, from, querySize);
            // 合并继续排序
            unadjustedRecords.addAll(adjustPayoutRecords);
            return unadjustedRecords.stream().sorted(Comparator.comparing(BankTransactionRecord::getChangeAmount)).
                    skip(0).limit(querySize).collect(Collectors.toList());
        } else {
            // 按入账金额排序(默认)
            return getOrderRecordsViaAdjust(request, cardNum, from, querySize);
        }
    }

    /**
     * <h2> 获取调单卡号作为来源沉淀的交易记录 </h2>
     *
     * @param request 快进快出请求
     * @param next    聚合分页起始位置
     * @param size    聚合分页返回条数
     */
    private List<BankTransactionRecord> getPayoutRecordsFromAdjustDeposit(FastInFastOutRequest request, int next, int size, int querySize) {

        QuerySpecialParams query = queryRequestParamFactory.oppositeCardDistinctFromFastInFastOut(request, FundTacticsAnalysisField.LOAN_FLAG_OUT);
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
        // 查询记录的起始位置
        int from = 0;
        // 用这些卡号查询出账的记录
        return getCreditAndPayOutRecordsViaCards(request.getCaseId(), request.getSingleQuota(), oppositeCards, from, querySize,
                false, request.getSortRequest(), FundTacticsAnalysisField.fastInFastOutFields());
    }


    /**
     * <h2> 返回的数据是根据排序来的(调单卡号作为来源卡号的情况) </h2>
     */
    private List<BankTransactionRecord> getOrderRecordsViaSource(FastInFastOutRequest request, List<String> oppositeAdjustCards, int size) {

        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        List<String> cardNum = request.getCardNum();
        SortRequest sortRequest = request.getSortRequest();
        String property = sortRequest.getProperty();
        // 查询数据的起始位置
        int from = 0;
        if (property.equals("flow_in_money")) {
            // 按入账金额排序
            return getOrderRecordsViaAdjust(request, cardNum, from, size);

        } else if (property.equals("flow_out_money") || property.equals("flow_out_time")) {

            // 调单卡号出账记录的排序
            List<BankTransactionRecord> adjustPayoutRecords = getOrderRecordsViaAdjust(request, oppositeAdjustCards, from, size);
            // 非调单出账记录的排序
            List<BankTransactionRecord> unadjustedRecords = getOrderRecordsUnadjusted(request, size);
            // 合并排序
            unadjustedRecords.addAll(adjustPayoutRecords);
            // TODO 将 unadjustedRecords 分散开来,多线程异步去查
            // 检查这些卡号是否调单卡号出账记录的对方卡号
            List<String> oppositeCards = unadjustedRecords.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
            Map<String, String> filterOppositeCards = getCreditAndPayOutViaQueryAndOpposite(caseId, singleQuota, cardNum, oppositeCards, size, false, sortRequest,
                    FundTacticsAnalysisField.fastInFastOutFields()).stream().collect(Collectors.toMap(BankTransactionRecord::getTransactionOppositeCard,
                    BankTransactionRecord::getTransactionOppositeCard, (v1, v2) -> v1));
            // 过滤对方卡号集合
            if (!CollectionUtils.isEmpty(filterOppositeCards)) {

                return unadjustedRecords.stream().filter(e -> oppositeCards.contains(e.getQueryCard())).collect(Collectors.toList());
            } else {
                return new ArrayList<>();
            }
        } else {

            // 按入账金额排序(默认)
            return getOrderRecordsViaAdjust(request, cardNum, from, size);
        }
    }


    /**
     * <h2> 处理调单卡号为资金中转卡号的情况 </h2>
     *
     * @param request   快进快出请求
     * @param chunkSize 下一次查询的起始位置
     * @param limit     需要返回的数量
     */
    private List<FastInFastOutResult> processAdjustCardAsFundTransitCard(FastInFastOutRequest request, int chunkSize, int limit) {

        // 首先看排序,先筛选来源的数据,还是沉淀的数据(后续2个for的时候要决定谁在第一层)
        // 然后去重获取需要的调单卡号,调单卡作为中转卡情况比较特殊(多了一个步骤就是,要查询这些调单卡的来源是几个)
        // 若走一个(走一个来源、多个沉淀)、反之走单笔逻辑(eg. 3条来源、一条沉淀 / 3条来源、3条沉淀 需要算9次, 即2层for循环)
        List<FastInFastOutResult> results = new ArrayList<>();
        batchProcessTransit(request, chunkSize, limit, results);
        while (results.size() < limit) {
            // TODO
            // 如果 results的size 还是小于需要返回的limit,那么继续处理这一批固定调单的下一批数据
        }
        return results;
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
        String property = sortRequest.getProperty();
        // 查询的起始位置
        int from = 0;
        List<BankTransactionRecord> bankTransactionRecords = getOrderRecordsViaAdjust(request, request.getCardNum(), from, querySize);
        if (CollectionUtils.isEmpty(bankTransactionRecords)) {
            return;
        }
        // 查看这一批调单卡号的来源情况
        List<String> adjustCards = bankTransactionRecords.stream().map(BankTransactionRecord::getQueryCard).collect(Collectors.toList());
        Map<String, CreditAdjustCards> creditsAdjustCards =
                getCreditsAdjustCards(request.getCaseId(), adjustCards, request.getSingleQuota(), adjustCards.size());
        // 根据计算查询入账 还是 出账数据
        boolean isCredits = true;
        if (property.equals("flow_out_money") || property.equals("flow_out_time")) {
            isCredits = false;
        }
        // 依次计算每一条记录
        for (BankTransactionRecord first : bankTransactionRecords) {
            // 根据排序查询入账和出账的数据
            List<BankTransactionRecord> second = getCreditAndPayOutRecordsViaCards(
                    caseId, singleQuota, Collections.singletonList(first.getQueryCard()), from, querySize, isCredits, sortRequest, FundTacticsAnalysisField.fastInFastOutFields()
            );
            if (CollectionUtils.isEmpty(second)) {
                continue;
            }
            // 检查来源的情况
            CreditAdjustCards creditAdjustCards = creditsAdjustCards.get(first.getQueryCard());
            if (null == creditAdjustCards || creditAdjustCards.getCreditsTimes() > 1) {
                // 处理多个来源/多个沉淀情况
                computeMultiSourceAndMultiDepositResult(request, results, first, second, limit);
                //  当limit 不够, 继续下钻 second 数据
                while (results.size() < limit) {
                    from = from + querySize;
                    List<BankTransactionRecord> secondNew = getCreditAndPayOutRecordsViaCards(
                            caseId, singleQuota, Collections.singletonList(first.getQueryCard()), from, querySize, isCredits, sortRequest, FundTacticsAnalysisField.fastInFastOutFields()
                    );
                    if (CollectionUtils.isEmpty(secondNew)) {
                        // 结束while,进行下一轮的 batchProcessTransit()
                        break;
                    }
                    computeMultiSourceAndMultiDepositResult(request, results, first, second, limit);
                }
            } else {
                // 处理单个来源/多个沉淀情况
                computeMultiDepositFromSingleSourceResult(creditAdjustCards, request, first, second, limit, results);
                // 当limit 不够, 继续下钻 second 数据
                while (results.size() < limit) {
                    from = from + querySize;
                    List<BankTransactionRecord> secondNew = getCreditAndPayOutRecordsViaCards(
                            caseId, singleQuota, Collections.singletonList(first.getQueryCard()), from, querySize, isCredits, sortRequest, FundTacticsAnalysisField.fastInFastOutFields()
                    );
                    if (CollectionUtils.isEmpty(secondNew)) {
                        // 结束while,进行下一轮的 batchProcessTransit()
                        break;
                    }
                    computeMultiSourceAndMultiDepositResult(request, results, first, second, limit);
                }
            }
        }
    }

    /**
     * <h2> 返回的数据是根据排序来的(调单卡号作为查询卡号情况) </h2>
     */
    private List<BankTransactionRecord> getOrderRecordsViaAdjust(FastInFastOutRequest request, List<String> cards, int from, int size) {

        // 排序请求
        SortRequest sortRequest = request.getSortRequest();
        String property = sortRequest.getProperty();
        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        // 根据计算查询入账 还是 出账数据
        boolean isCredits = true;
        if (property.equals("flow_out_money") || property.equals("flow_out_time")) {
            isCredits = false;
        }
        return getCreditAndPayOutRecordsViaCards(caseId, singleQuota, cards, from, size, isCredits, sortRequest, FundTacticsAnalysisField.fastInFastOutFields());
    }

    /**
     * <h2> 返回的数据是根据排序来的(查询卡号为非调单的情况) </h2>
     */
    private List<BankTransactionRecord> getOrderRecordsUnadjusted(FastInFastOutRequest request, int size) {

        // 排序请求
        SortRequest sortRequest = request.getSortRequest();
        String property = sortRequest.getProperty();
        // 根据计算查询入账 还是 出账数据
        boolean isCredits = true;
        if (property.equals("flow_out_money") || property.equals("flow_out_time")) {
            isCredits = false;
        }
        return getCreditAndPayoutRecordsNoSuchCards(request, size, isCredits, FundTacticsAnalysisField.fastInFastOutFields());
    }


    /**
     * <h2> 处理调单卡号为资金沉淀卡号的情况 </h2>
     *
     * @param request   快进快出请求
     * @param querySize 查询的数量
     * @param limit     需要返回的数量
     */
    private List<FastInFastOutResult> processAdjustCardAsFundDepositCard(FastInFastOutRequest request, int querySize, int limit) {

        // 按照排序需要将分页的足够数据筛选出来
        List<FastInFastOutResult> results = new ArrayList<>();
        // 批量处理沉淀数据
        batchProcessDeposit(request, querySize, limit, results);
        while (results.size() < limit) {
            // TODO
            // 如果 results的size 还是小于需要返回的limit,那么继续处理这一批固定调单的下一批数据
        }
        return results;
    }

    /**
     * <h2> 批量处理沉淀数据(调单卡号为沉淀的情况) </h2>
     */
    private void batchProcessDeposit(FastInFastOutRequest request, int chunkSize, int limit, List<FastInFastOutResult> results) {

        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        // 排序请求
        SortRequest sortRequest = request.getSortRequest();
        String property = sortRequest.getProperty();
        //

//        getOrderRecordsViaDeposit(request, queryCards, 0, chunkSize);
    }

    /**
     * <h2>  </h2>
     */
    private List<BankTransactionRecord> getOrderRecordsViaDeposit(FastInFastOutRequest request, List<String> queryCards, int from, int size) {

        return null;
    }

    /**
     * <h2> 计算一个来源到多个沉淀结果数据 </h2>
     */
    private void computeMultiDepositFromSingleSourceResult(CreditAdjustCards creditCard, FastInFastOutRequest request,
                                                           BankTransactionRecord creditRecord, List<BankTransactionRecord> payoutRecords,
                                                           int limit, List<FastInFastOutResult> results) {
        // 特征比
        int characteristicRatio = request.getCharacteristicRatio();
        // 时间间隔(分钟)
        int timeInterval = request.getTimeInterval();
        double accumulatePayoutAmount = 0;
        for (BankTransactionRecord payoutRecord : payoutRecords) {
            // 如果满足limit也停止计算
            if (results.size() >= limit) {
                break;
            }
            // 检查特征比
            boolean checkFeatureRatio = checkFeatureRatio(characteristicRatio, creditRecord, payoutRecord);
            if (checkFeatureRatio) {
                // 检查时间间隔
                boolean checkTimeInterval = checkTimeInterval(timeInterval, creditRecord, payoutRecord);
                if (checkTimeInterval) {
                    accumulatePayoutAmount = BigDecimalUtil.add(accumulatePayoutAmount, payoutRecord.getChangeAmount()).doubleValue();
                    // 如果出账累加金额 大于 来源的进账金额(停止计算)
                    if (accumulatePayoutAmount > creditCard.getCreditsAmount().doubleValue()) {
                        break;
                    }
                    // 生成FastInFastOutResult记录
                    FastInFastOutResult fastInFastOutResult = convertFromData(creditRecord, payoutRecord);
                    results.add(fastInFastOutResult);
                }
            }
        }
    }

    /**
     * <h2> 计算多个来源 或者 多个沉淀(除了一个来源,多个沉淀需要特殊结算,其他情况目前都按照这种计算-算单笔) </h2>
     */
    private void computeMultiSourceAndMultiDepositResult(FastInFastOutRequest request, List<FastInFastOutResult> results,
                                                         BankTransactionRecord first, List<BankTransactionRecord> second,
                                                         int limit) {
        // 特征比
        int characteristicRatio = request.getCharacteristicRatio();
        // 时间间隔(分钟)
        int timeInterval = request.getTimeInterval();
        for (BankTransactionRecord payoutRecord : second) {
            // 如果满足limit也停止计算
            if (results.size() >= limit) {
                break;
            }
            // 检查特征比
            boolean checkFeatureRatio = checkFeatureRatio(characteristicRatio, first, payoutRecord);
            if (checkFeatureRatio) {
                // 检查时间间隔
                boolean checkTimeInterval = checkTimeInterval(timeInterval, first, payoutRecord);
                if (checkTimeInterval) {
                    // 生成FastInFastOutResult记录
                    FastInFastOutResult fastInFastOutResult = convertFromData(first, payoutRecord);
                    results.add(fastInFastOutResult);
                }
            }
        }
    }


    /**
     * <h2> 获取入账的调单卡号集合(调单卡号为中转卡号时适用) </h2>
     * <p>
     * 因为需要处理一种特殊情况 (调单卡号只有一个来源,但是有多个沉淀)
     *
     * @param caseId      案件Id
     * @param adjustCards 给定的一组调单卡号集合
     * @param singleQuota 单笔限额(指的是交易金额)
     */
    private Map<String, CreditAdjustCards> getCreditsAdjustCards(String caseId, List<String> adjustCards, int singleQuota, int size) {

        // 构建查询入账调单卡号查询请求参数
        QuerySpecialParams query = queryRequestParamFactory.buildCreditsAdjustCards(caseId, adjustCards, singleQuota);
        // 构建查询入账调单卡号聚合请求参数
        AggregationParams agg = aggregationRequestParamFactory.buildCreditsAdjustCardsAgg(initGroupSize, 0, size);
        // 构建聚合名称属性映射(获取聚合值)
        Map<String, String> aggNameKeyMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggNameKeyMapping, CreditAdjustCards.class);
        // 设置此聚合功能名称
        agg.setResultName("getCreditsAdjustCards");
        Map<String, List<List<Object>>> resultsMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        List<List<Object>> results = resultsMap.get(agg.getResultName());
        return results.stream().collect(Collectors.toMap(e -> e.get(0).toString(),
                e1 -> new CreditAdjustCards(e1.get(0).toString(), Integer.parseInt(e1.get(1).toString()), BigDecimalUtil.value(e1.get(2).toString())),
                (v1, v2) -> v1
        ));
    }

    /**
     * <h2> 获取给定一组查询卡号入账/出账的数据 </h2>
     * <p>
     * 分页获取数据(返回的数据量大小未知)
     * 排序: 流入金额排序, 流出时间日期与流出金额
     *
     * @param caseId        案件Id
     * @param singleQuota   单笔限额(交易金额)
     * @param cards         卡号
     * @param size          分页需要返回的条数
     * @param isCredits     计算入账/出账 (true-入账,false-出账)
     * @param sortRequest   排序
     * @param includeFields 查询包含的字段
     */
    @SuppressWarnings("all")
    private List<BankTransactionRecord> getCreditAndPayOutRecordsViaCards(String caseId, int singleQuota, List<String> cards,
                                                                          int from, int size, boolean isCredits, SortRequest sortRequest,
                                                                          String... includeFields) {
        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 调单卡号集合过滤
        if (!CollectionUtils.isEmpty(cards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, cards));
        }
        // 单笔限额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        if (isCredits) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        querySpecialParams.addCombiningQueryParams(filter);
        // 设置查询包含的字段
        querySpecialParams.setIncludeFields(includeFields);
        // 分页与排序
        PageRequest pageRequest = PageRequest.of(0, size, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty());
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(pageRequest, caseId, BankTransactionRecord.class, querySpecialParams);
        if (null == recordPage) {
            return new ArrayList<>();
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 获取给定一组查询卡号和对方卡号 入账/出账的数据 </h2>
     * <p>
     * 分页获取数据(返回的数据量大小未知)
     * 排序: 流入金额排序, 流出时间日期与流出金额
     *
     * @param caseId        案件Id
     * @param singleQuota   单笔限额(交易金额)
     * @param queryCards    查询卡号
     * @param oppositeCards 对方卡号
     * @param page          分页页码
     * @param size          分页需要返回的条数
     * @param isCredits     计算入账/出账 (true-入账,false-出账)
     * @param sortRequest   排序
     * @param includeFields 查询包含的字段
     */
    @SuppressWarnings("all")
    private List<BankTransactionRecord> getCreditAndPayOutViaQueryAndOpposite(String caseId, int singleQuota, List<String> queryCards,
                                                                              List<String> oppositeCards, int size,
                                                                              boolean isCredits, SortRequest sortRequest, String... includeFields) {
        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 调单卡号(查询卡号)集合过滤
        if (!CollectionUtils.isEmpty(queryCards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, queryCards));
        }
        // 对方卡号过滤
        if (!CollectionUtils.isEmpty(oppositeCards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, oppositeCards));
        }
        // 单笔限额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        if (isCredits) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        querySpecialParams.addCombiningQueryParams(filter);
        // 设置查询包含的字段
        querySpecialParams.setIncludeFields(includeFields);
        // 分页与排序
        PageRequest pageRequest = PageRequest.of(0, size, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty());
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(pageRequest, caseId, BankTransactionRecord.class, querySpecialParams);
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
        // 卡号集合
        // 案件Id
        String caseId = request.getCaseId();
        // 单笔限额(交易金额)
        int singleQuota = request.getSingleQuota();
        // 构建查询参数
        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 单笔限额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        if (isCredits) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        // not in 给定的一组卡号
        CombinationQueryParams noIn = new CombinationQueryParams(ConditionType.must_not);
        noIn.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNum()));
        // 添加一个单个组合查询
        filter.addCombinationQueryParams(noIn);
        // 包装查询参数
        query.addCombiningQueryParams(filter);
        // 设置查询包含的字段
        query.setIncludeFields(includeFields);
        // 分页与排序
        SortRequest sortRequest = request.getSortRequest();
        PageRequest pageRequest = PageRequest.of(0, size, Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty());
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(pageRequest, caseId, BankTransactionRecord.class, query);
        return recordPage.getContent();
    }

    /**
     * <h2> 检查特征比 </h2>
     */
    private boolean checkFeatureRatio(int characteristicRatio, BankTransactionRecord first, BankTransactionRecord second) {

        // 特征比(入账金额-出账金额) / 入账金额
        BigDecimal sub = BigDecimalUtil.sub(first.getChangeAmount(), first.getChangeAmount());
        BigDecimal div = BigDecimalUtil.div(sub.doubleValue(), first.getChangeAmount());
        return div.intValue() <= characteristicRatio;
    }


    /**
     * <h2> 检查时间间隔 </h2>
     * <p>
     * 查看流入时间日期 与 流出时间日期之前必须 小于等于 timeInterval
     */
    private boolean checkTimeInterval(int timeInterval, BankTransactionRecord first, BankTransactionRecord second) {
        // 如果 流出时间日期 早于 流入时间日期(过滤此数据)
        if (second.getTradingTime().before(first.getTradingTime())) {
            return false;
        }
        // 查看时间间隔
        Calendar firstCal = Calendar.getInstance();
        firstCal.setTime(first.getTradingTime());
        Calendar secondCal = Calendar.getInstance();
        secondCal.setTime(first.getTradingTime());
        int firstMinute = firstCal.get(Calendar.MINUTE);
        int secondMinute = secondCal.get(Calendar.MINUTE);
        return (secondMinute - firstMinute) <= timeInterval;
    }

    /**
     * <h2> 将来源-中转 、中转-沉淀 2条交易记录合并成一条快进快出结果 </h2>
     */
    private FastInFastOutResult convertFromData(BankTransactionRecord sourceToTransit, BankTransactionRecord transitToDeposit) {

        FastInFastOutResult fastInFastOutResult = new FastInFastOutResult();
        // 资源来源卡号
        fastInFastOutResult.setFundSourceCard(sourceToTransit.getQueryCard());
        // 资金来源户名
        fastInFastOutResult.setFundSourceAccountName(sourceToTransit.getCustomerName());
        // 流入时间日期
        fastInFastOutResult.setInflowDate(format.format(sourceToTransit.getTradingTime()));
        // 流入金额
        fastInFastOutResult.setInflowAmount(BigDecimalUtil.value(sourceToTransit.getChangeAmount().toString()));
        // 资金中转卡号
        fastInFastOutResult.setFundTransitCard(transitToDeposit.getQueryCard());
        // 资金中转户名
        fastInFastOutResult.setFundTransitAccountName(transitToDeposit.getCustomerName());
        // 流出时间日期
        fastInFastOutResult.setOutflowDate(format.format(transitToDeposit.getTradingTime()));
        // 流出金额
        fastInFastOutResult.setOutflowAmount(BigDecimalUtil.value(transitToDeposit.getChangeAmount().toString()));
        // 资金沉淀卡号
        fastInFastOutResult.setFundDepositCard(transitToDeposit.getTransactionOppositeCard());
        // 资金沉淀户名
        fastInFastOutResult.setFundDepositAccountName(transitToDeposit.getTransactionOppositeName());
        // 特征比(入账金额-出账金额) / 入账金额
        BigDecimal sub = BigDecimalUtil.sub(sourceToTransit.getChangeAmount(), transitToDeposit.getChangeAmount());
        BigDecimal div = BigDecimalUtil.div(sub.doubleValue(), sourceToTransit.getChangeAmount());
        fastInFastOutResult.setCharacteristicRatio(div.toString() + "%");
        return fastInFastOutResult;
    }
}
