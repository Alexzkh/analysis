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
import com.zqykj.common.core.ServerResponse;
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


        // TODO 需要判断
        // TODO 等待调单卡号作为来源、中转、沉淀的快进快出记录生成完毕之后 (再根据 FastInFastOutRequest 筛选符合的快进快出结果)
        return null;
    }

    /**
     * <h2> 批量处理调单卡号作为资金来源卡号 </h2>
     * <p>
     * 方案一
     * 处理的是 调单卡号 ----(出) ----(出)
     * 调单卡号作为来源首先过滤出出账的数据(借贷标志为出), 以这批数据的对方卡号(可能是调单或者非调单) 继续过滤出账记录(借贷标志为出)
     */
    private void batchBuildFastInoutFromSource(List<String> adjustCards) throws ExecutionException, InterruptedException {

        // 根据配置值生成固定数量的快进快出记录

    }

    /**
     * <h2> 批量处理调单卡号为中转卡号情况 </h2>
     */
    private void batchProcessTransit(List<String> adjustCards, String caseId) {

        // 根据配置值生成固定数量的快进快出记录

        generateInflowAmountResultViaTransit(adjustCards, caseId);

        generateOutFlowViaTransit(adjustCards);
    }


    /**
     * <h2> 批量处理沉淀数据(调单卡号为沉淀的情况) </h2>
     */
    private void batchBuildFastInoutFromDeposit(List<String> adjustCards) throws ExecutionException, InterruptedException {

        // 根据配置值生成固定数量的快进快出记录
    }


    /**
     * <h2> 生成按流入金额排序的快进快出记录 </h2>
     * <p>
     * 调单卡号的来源(即它的进账记录排序)
     */
    private void generateInflowAmountResultViaTransit(List<String> adjustCards, String caseId) {

        CompletableFuture<List<BankTransactionRecord>> inflowAmountDescFutures =
                CompletableFuture.supplyAsync(() -> {
                    // 流入金额降序排序生成快进快出记录
                    List<BankTransactionRecord> inflowAmountDesc = getInOutRecordViaCards(adjustCards, caseId, true, FundTacticsAnalysisField.CHANGE_MONEY, Sort.Direction.DESC);
                    if (CollectionUtils.isEmpty(inflowAmountDesc)) {
                        return null;
                    }
                    // 去重 inflowAmountDesc 记录中的查询卡号(以这批卡号作为查询卡号,筛选 出账的记录)
                    List<String> distinctQueryCard = inflowAmountDesc.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
                    Map<String, BankTransactionRecord> inflowAmountDescMap = inflowAmountDesc.stream().collect(Collectors.toMap(BankTransactionRecord::getQueryCard, e -> e, (v1, v2) -> v1));
                    // 每次查询 perQueryCardNum 张卡,然后生成快进快出记录,一旦超过  resultChunkSize 条就停止
                    int position = 0;
                    int size = distinctQueryCard.size();
                    while (position < size) {
                        int next = Math.min(position + perQueryCardNum, size);
                        // 查询这 perQueryCardNum 张卡的出账记录,然后生成快进快出记录

                    }
                    return null;
                });
        CompletableFuture<List<BankTransactionRecord>> inflowAmounAscFutures =
                CompletableFuture.supplyAsync(() -> {
                    // 流入金额升序排序生成快进快出记录
                    List<BankTransactionRecord> inflowAmountAsc = getInOutRecordViaCards(adjustCards, caseId, true, FundTacticsAnalysisField.CHANGE_MONEY, Sort.Direction.ASC);
                    if (CollectionUtils.isEmpty(inflowAmountAsc)) {
                        return null;
                    }
                    // 去重inflowAmountAsc 记录中的查询卡号(以这批卡号作为查询卡号, 筛选出账的记录)
                    List<String> distinctQueryCard = inflowAmountAsc.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
                    Map<String, BankTransactionRecord> inflowAmountDescMap = inflowAmountAsc.stream().collect(Collectors.toMap(BankTransactionRecord::getQueryCard, e -> e, (v1, v2) -> v1));
                    // 每次查询 perQueryCardNum 张卡,然后生成快进快出记录,一旦超过 resultChunkSize 就停止
                    int position = 0;

                    return null;
                });
    }


    private boolean generateFastInoutFromTransit(List<String> cards, String caseId) {

        // 查询这批卡号的总量


        return true;
    }

    /**
     * <h2> 生成按流出金额、流出日期排序的快进快出记录 </h2>
     */
    private void generateOutFlowViaTransit(List<String> adjustCards) {

    }


    /**
     * <h2> 通过卡号获取进出记录(根据排序) </h2>
     */
    private List<BankTransactionRecord> getInOutRecordViaCards(List<String> cards, String caseId, boolean isIn, String property, Sort.Direction direction) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, isIn);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(0, orderChunkSize, direction, property), caseId, BankTransactionRecord.class, query);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
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
     * <h2> 计算快进快出结果 </h2>
     * <p>
     * 适用于调单卡号作为来源的情况
     */
    private void computeResultFromSource(String sortType, int characteristicRatio, long timeInterval,
                                         BankTransactionRecord first, List<BankTransactionRecord> seconds,
                                         List<FastInFastOutRecord> results, int limit) {
        for (BankTransactionRecord second : seconds) {
            // 如果满足limit也停止计算
            if (results.size() >= limit) {
                break;
            }
            boolean checkTimeInterval = checkTimeIntervalAndFeatureRatio(sortType, characteristicRatio, timeInterval, first, second);
            if (checkTimeInterval) {
                FastInFastOutRecord fastInFastOutResult = convertFromDataSource(first, second, sortType);
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
                                          List<FastInFastOutRecord> results, int limit) {
        for (BankTransactionRecord second : seconds) {
            // 如果满足limit也停止计算
            if (results.size() >= limit) {
                break;
            }
            // 检查特征比
            boolean checkTimeInterval = checkTimeIntervalAndFeatureRatio(sortType, characteristicRatio, timeInterval, first, second);
            if (checkTimeInterval) {
                FastInFastOutRecord fastInFastOutResult = convertFromDataTransit(first, second, sortType);
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
                                          List<FastInFastOutRecord> results, int limit) {
        for (BankTransactionRecord second : seconds) {
            // 如果满足limit也停止计算
            if (results.size() >= limit) {
                break;
            }
            // 检查特征比
            boolean checkTimeInterval = checkTimeIntervalAndFeatureRatio(sortType, characteristicRatio, timeInterval, first, second);
            if (checkTimeInterval) {
                FastInFastOutRecord fastInFastOutResult = convertFromDataDeposit(first, second, sortType);
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
        computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), fastInFastOutResult, creditAmount);
        return fastInFastOutResult;
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为中转情况
     */
    @SuppressWarnings("all")
    private FastInFastOutRecord convertFromDataTransit(BankTransactionRecord first, BankTransactionRecord second, String sortType) {

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
            fastInFastOutResult.setFundDepositCard(second.getTransactionOppositeCard());
            // 资金沉淀户名
            fastInFastOutResult.setFundDepositAccountName(second.getTransactionOppositeName());
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
        computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), fastInFastOutResult, inflowAmount);
        return fastInFastOutResult;
    }

    /**
     * <h2> 计算快进快出结果的特征比 </h2>
     */
    private void computeFastInFastOutCharacteristicRatio(double firstChangeAmount, double secondChangeAmount,
                                                         FastInFastOutRecord fastInFastOutResult, double inflowAmount) {
        BigDecimal sub;
        if (firstChangeAmount < secondChangeAmount) {
            sub = BigDecimalUtil.sub(secondChangeAmount, firstChangeAmount);
        } else {
            sub = BigDecimalUtil.sub(firstChangeAmount, secondChangeAmount);
        }
        BigDecimal div = BigDecimalUtil.divReserveFour(sub.doubleValue(), inflowAmount);
        BigDecimal mul = BigDecimalUtil.mul(div.doubleValue(), 100);
        fastInFastOutResult.setCharacteristicRatio(mul.intValue());
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
