/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import cn.hutool.core.util.HashUtil;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.query.FastInFastOutQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFastInFastOut;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.middle.FastInFastOutDetailRequest;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.util.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
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
public class FastInFastOutImpl extends FundTacticsCommonImpl implements IFastInFastOut {

    private final FastInFastOutQueryParamFactory fastInFastOutQueryParamFactory;

    // 快进快出生成结果条数(数值排序有6中规则(降序和升序),流入金额、流出金额、流出日期)
    // 每种规则生成5W数据,比如调单卡号作为来源情况满的话,会有30w数据,可能有重复,需要去重
    // 那么调单卡号作为中转、作为沉淀 数据满的情况下,3种情况共计90万数据(数据足够)
    // 不可能不停的取,直至取满 (快进快出这个逻辑太奇葩 eg. 调单卡号作为中转卡号, 进账有1000条, 出账有1000条, 那就是1000 * 1000 条.....)
    // 你需要从中筛选出符合时间间隔、特征比的记录数...
    // 实际数据量可能更奇葩(可能导入文件数据才20W,全部查询的时候会出现300多万的快进快出数据量(如果算满的话)
    // 因此只能设置 阈值取出排序的一部分数据构建快进快出记录
    @Value("${fastInout.result_chunkSize}")
    private int resultChunkSize;
    // 按照进或者出排序后需要取出的数据量
    @Value("${fastInout.order_chunkSize}")
    private int orderChunkSize;
    // 每次批量查询数量
    @Value("${fastInout.per_query_count}")
    private int perQueryCount;
    // 中转卡号最大基数
    @Value("${fastInout.transit_card_count}")
    private int transitCardCount;

    public ServerResponse<FundAnalysisResultResponse<FastInFastOutResult>> fastInFastOutAnalysis(FastInFastOutRequest request) throws Exception {

        if (CollectionUtils.isEmpty(request.getCardNum())) {
            // 全部查询
            return fastInFastOutViaAllQuery(request);
        } else {
            // 查询个体
            return fastInFastOutViaChosenIndividual(request);
        }
    }

    @Override
    public ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> detailResult(FastInFastOutDetailRequest request) {

        // 详情中的交易卡号必须是调单卡号(即交易流水表中的左端的查询卡号)
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        SortRequest sortRequest = request.getSortRequest();
        String name = sortRequest.getOrder().name();
        QuerySpecialParams detailQuery = fastInFastOutQueryParamFactory.getResultDetail(request);
        Page<BankTransactionFlow> page = entranceRepository.findAll(PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize(), Sort.Direction.valueOf(name), sortRequest.getProperty())
                , request.getCaseId(), BankTransactionFlow.class, detailQuery);
        if (page == null) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        List<TradeAnalysisDetailResult> detailResults = getTradeAnalysisDetailResultsByFlow(page);
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(detailResults, page.getTotalElements(), page.getSize()));
    }

    /**
     * <h2> 全部查询 </h2>
     */
    private ServerResponse<FundAnalysisResultResponse<FastInFastOutResult>> fastInFastOutViaAllQuery(FastInFastOutRequest request) throws Exception {

        // 直接查询调单卡号的数量,批量获取 maxAdjustCardQueryCount 数量的调单卡号
        // 查询出前 maxAdjustCardQueryCount 个调单卡号
        double startAmount = Double.parseDouble(String.valueOf(request.getSingleQuota()));
        if (checkAdjustCardCountBySingleAmountDate(request.getCaseId(), startAmount, QueryOperator.gte, null)) {
            List<String> adjustCards = queryMaxAdjustCardsBySingleAmountDate(request.getCaseId(), startAmount, QueryOperator.gte, null);
            if (CollectionUtils.isEmpty(adjustCards)) {
                return null;
            }
            request.setCardNum(adjustCards);
            return fastInFastOutViaChosenIndividual(request);
        } else {
            // TODO 处理超过 maxAdjustCardQueryCount 数量的调单卡号逻辑
            return null;
        }
    }

    public static void main(String[] args) {

    }

    /**
     * <h2> 选择个体查询 </h2>
     */
    private ServerResponse<FundAnalysisResultResponse<FastInFastOutResult>> fastInFastOutViaChosenIndividual(FastInFastOutRequest request) throws Exception {
        // 需要返回的数量
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        String property = request.getSortRequest().getProperty();
        Direction order = request.getSortRequest().getOrder();
        int limit = pageRequest.getPage() == 0 ? pageRequest.getPageSize() : (pageRequest.getPage() + 1) * pageRequest.getPageSize();
        int skip = pageRequest.getPage() * pageRequest.getPageSize();
        int size = pageRequest.getPageSize();
        // 来源的
        CompletableFuture<? extends Map<String, ?>> sourceOrderFuture = CompletableFuture.supplyAsync(() ->
                getSourceOrder(request, limit), ThreadPoolConfig.getExecutor());
        // 中转的
        CompletableFuture<? extends Map<String, ?>> transitOrderFuture = CompletableFuture.supplyAsync(() ->
                getTransitOrder(request, limit), ThreadPoolConfig.getExecutor());
        // 沉淀的
        CompletableFuture<? extends Map<String, ?>> depositOrderFuture = CompletableFuture.supplyAsync(() ->
                getDepositOrder(request, limit), ThreadPoolConfig.getExecutor());

        List<FastInFastOutResult> results = new ArrayList<>();
        Set<String> hashTotal = new HashSet<>();
        // 包含limit数量 的快进快出结果 与 满足条件的总数据量
        Map<String, ?> sourceOrderMap = sourceOrderFuture.get();
        Map<String, ?> transitOrderMap = transitOrderFuture.get();
        Map<String, ?> depositOrderMap = depositOrderFuture.get();
        int computeResultCount = 0;
        if (!CollectionUtils.isEmpty(sourceOrderMap)) {
            List<FastInFastOutResult> sourceOrderRecords = (List<FastInFastOutResult>) sourceOrderMap.get("records");
            Set<String> sourceOrderHashes = (Set<String>) sourceOrderMap.get("hashes");
            results.addAll(sourceOrderRecords);
            hashTotal.addAll(sourceOrderHashes);
            computeResultCount++;
        }
        if (!CollectionUtils.isEmpty(transitOrderMap)) {
            List<FastInFastOutResult> transitOrderRecords = (List<FastInFastOutResult>) transitOrderMap.get("records");
            Set<String> transitOrderHashes = (Set<String>) transitOrderMap.get("hashes");
            results.addAll(transitOrderRecords);
            hashTotal.addAll(transitOrderHashes);
            computeResultCount++;
        }
        if (!CollectionUtils.isEmpty(depositOrderMap)) {
            List<FastInFastOutResult> depositOrderRecords = (List<FastInFastOutResult>) depositOrderMap.get("records");
            results.addAll(depositOrderRecords);
            Set<String> depositOrderHashes = (Set<String>) depositOrderMap.get("hashes");
            hashTotal.addAll(depositOrderHashes);
            computeResultCount++;
        }
        if (computeResultCount <= 1) {
            results = results.stream().skip(skip).limit(limit).collect(Collectors.toList());
        } else {
            // 结果合并去重
            Comparator<BigDecimal> amountComparator = Comparator.reverseOrder();
            Comparator<Long> dateComparator = Comparator.reverseOrder();
            if (order.isAscending()) {
                amountComparator = Comparator.naturalOrder();
                dateComparator = Comparator.naturalOrder();
            }
            if (property.equals(FundTacticsAnalysisField.FastInoutSort.INFLOW_AMOUNT)) {

                results = results.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(FastInFastOutResult::getHashId)
                )), ArrayList::new)).stream().sorted(Comparator.comparing(FastInFastOutResult::getInflowAmount, amountComparator))
                        .skip(skip).limit(size).collect(Collectors.toList());
            } else if (property.equals(FundTacticsAnalysisField.FastInoutSort.OUTFLOW_AMOUNT)) {

                results = results.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(FastInFastOutResult::getHashId)
                )), ArrayList::new)).stream().sorted(Comparator.comparing(FastInFastOutResult::getOutflowAmount, amountComparator))
                        .skip(skip).limit(size).collect(Collectors.toList());
            } else {
                results = results.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(FastInFastOutResult::getHashId)
                )), ArrayList::new)).stream().sorted(Comparator.comparing(e -> {
                    try {
                        return DATE_PARSER.parse(e.getOutflowDate()).getTime();
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }, dateComparator)).skip(skip).limit(size).collect(Collectors.toList());
            }
        }
        FundAnalysisResultResponse<FastInFastOutResult> resultResponse = new FundAnalysisResultResponse<>();
        resultResponse.setTotal(hashTotal.size());
        resultResponse.setTotalPages(PageRequest.getTotalPages(hashTotal.size(), pageRequest.getPageSize()));
        resultResponse.setSize(pageRequest.getPageSize());
        resultResponse.setContent(results);
        return ServerResponse.createBySuccess(resultResponse);
    }

    /**
     * <h2> 处理调单卡号为中转卡号的快进快出记录 </h2>
     */
    private Map<String, ?> getTransitOrder(FastInFastOutRequest request, int limit) {
        // 需要排序的字段
        String sortProperty = requireSortProperty(request.getSortRequest());
        // 是根据流入 还是 流出的排序
        boolean isInflow = isInFlow(request.getSortRequest());
        // 排序方向
        Sort.Direction direction = requireSortDirection(request.getSortRequest());
        // 根据配置值生成固定数量的快进快出记录
        return generateResultViaTransit(request, isInflow, sortProperty, direction, limit);
    }


    /**
     * <h2> 处理调单卡号为来源卡号的快进快出记录 </h2>
     */
    private Map<String, ?> getSourceOrder(FastInFastOutRequest request, int limit) {
        // 排序方向
        Sort.Direction direction = requireSortDirection(request.getSortRequest());
        String sortProperty = requireSortProperty(request.getSortRequest());
        boolean inFlow = isInFlow(request.getSortRequest());
        // 按照流入金额排序
        if (inFlow) {
            return generateResultViaSourceInflowOrDepositOutFlow(request, sortProperty, direction, limit);
        } else {
            // 按照流出金额、流出日期排序
            return generateResultViaSourceOutflowOrDepositInFlow(request, sortProperty, direction, limit);
        }
    }

    /**
     * <h2> 处理并保存调单卡号为沉淀卡号的快进快出记录 </h2>
     */
    private Map<String, ?> getDepositOrder(FastInFastOutRequest request, int limit) {

        // 排序方向
        Sort.Direction direction = requireSortDirection(request.getSortRequest());
        String sortProperty = requireSortProperty(request.getSortRequest());
        boolean inFlow = isInFlow(request.getSortRequest());
        if (inFlow) {
            return generateResultViaSourceOutflowOrDepositInFlow(request, sortProperty, direction, limit);
        } else {
            return generateResultViaSourceInflowOrDepositOutFlow(request, sortProperty, direction, limit);
        }
    }

    /**
     * <h2> 生成按流入金额排序(调单卡号作为来源卡号) 或者 生成按流出金额、流出日期排序(调单卡号作为沉淀卡号) 快进快出记录 </h2>
     * <p>
     * 调单卡号作为来源卡号 或者 沉淀卡号
     */
    private Map<String, ?> generateResultViaSourceInflowOrDepositOutFlow(FastInFastOutRequest request, String sortProperty, Sort.Direction direction, int limit) {

        List<String> adjustCards = request.getCardNum();
        int singleQuota = request.getSingleQuota();
        long timeInterval = request.getTimeInterval();
        int characteristicRatio = request.getCharacteristicRatio();
        boolean isInFlow = isInFlow(request.getSortRequest());
        // 获取调单卡号(来源卡号)查询的是出帐、调单卡号(沉淀卡号)查询的是进账
        List<BankTransactionRecord> sortRecords = getInOutRecordOrderViaQueryCards(adjustCards, request.getCaseId(), singleQuota, !isInFlow, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(sortRecords)) {
            return null;
        }
        // 去重 sortRecords 中的对方卡号,查询这些卡号的进账/出账记录
        List<String> distinctOppositeCard = sortRecords.stream().map(BankTransactionRecord::getTransactionOppositeCard).distinct().collect(Collectors.toList());
        // 获取这些卡号的进账/出账次数
        Map<String, Integer> oppositeCardInOutTimes = getInOutTotalTimesViaLocalAndOppositeCards(distinctOppositeCard, null, request.getCaseId(), singleQuota, !isInFlow);
        // 取出满足 resultChunkSize 数量的对方卡号
        if (CollectionUtils.isEmpty(oppositeCardInOutTimes)) {
            return null;
        }
        int computeTotal = 0;
        Map<String, Integer> requireQueryInOutCards = new HashMap<>();
        List<BankTransactionRecord> orderRecords = new ArrayList<>();
        for (BankTransactionRecord record : sortRecords) {
            if (computeTotal >= resultChunkSize) {
                break;
            }
            Integer inOutTimes = oppositeCardInOutTimes.get(record.getTransactionOppositeCard());
            if (null != inOutTimes && inOutTimes > 0) {
                orderRecords.add(record);
                if (!requireQueryInOutCards.containsKey(record.getTransactionOppositeCard())) {
                    // 检查需要查询的卡号
                    requireQueryInOutCards.put(record.getTransactionOppositeCard(), 0);
                    computeTotal += inOutTimes;
                }
            }
        }
        // 需要查询的卡号
        List<String> queryCards = new ArrayList<>(requireQueryInOutCards.keySet());
        List<BankTransactionRecord> unsortedRecords = asyncQueryInOutRecord(computeTotal, queryCards, null, request.getCaseId(), singleQuota, isInFlow);
        if (CollectionUtils.isEmpty(unsortedRecords)) {
            return null;
        }
        // 生成快进快出记录
        if (isInFlow) {
            return generateFastInOutFromSource(characteristicRatio, timeInterval, limit, orderRecords, unsortedRecords, true);
        }
        return generateFastInOutFromDeposit(characteristicRatio, timeInterval, limit, orderRecords, unsortedRecords, false);
    }

    /**
     * <h2> 生成按流出金额排序(调单卡号作为来源卡号)、流出日期 / 流入金额排序(调单卡号作为沉淀卡号) 快进快出记录 </h2>
     * <p>
     * 来源 - 中转 - 沉淀 (按照流出金额/流出日期排序,这种属于 2 跳排序, 实际上是拿来源卡号筛选出来的 中转卡号 去 查出账然后排序,
     * 关键问题在于 中转卡号很大的时候无法一次性取出,这就导致没办法进行流出金额与流出日期排序,因此只能随机取出一定量的卡号数量 暂定 transitCardCount
     */
    private Map<String, ?> generateResultViaSourceOutflowOrDepositInFlow(FastInFastOutRequest request, String sortProperty, Sort.Direction direction, int limit) {

        int singleQuota = request.getSingleQuota();
        boolean isInFlow = isInFlow(request.getSortRequest());
        // 获取二跳排序的查询卡号
        List<String> twoHopSortQueryCards = getTwoHopSortQueryCards(request);
        if (CollectionUtils.isEmpty(twoHopSortQueryCards)) {
            return null;
        }
        //  以twoHopSortQueryCards 作为查询卡号, 查询排序的记录
        List<BankTransactionRecord> sortRecords = getInOutRecordOrderViaQueryCards(twoHopSortQueryCards, request.getCaseId(), singleQuota, !isInFlow, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(sortRecords)) {
            return null;
        }
        // 去重sortRecords记录中的查询卡号,查询它的进账/出账记录
        List<String> distinctQueryCards = sortRecords.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
        // 获取这些卡号的进账/出账次数(对方卡号必须是调单卡号)
        Map<String, Integer> queryCardInOutTimes = getInOutTotalTimesViaLocalAndOppositeCards(distinctQueryCards, request.getCardNum(), request.getCaseId(), singleQuota, isInFlow);
        if (CollectionUtils.isEmpty(queryCardInOutTimes)) {
            return null;
        }
        // 取出满足 resultChunkSize 数量的查询卡号
        Map<String, Integer> requireQueryInOutCards = new HashMap<>();
        List<BankTransactionRecord> orderRecords = new ArrayList<>();
        // 满足条件的卡号总量
        int computeTotal = getRequireQueryCardsAndOrderRecordsAndTotal(sortRecords, requireQueryInOutCards, queryCardInOutTimes, orderRecords);
        // 需要查询的卡号
        List<String> queryCards = new ArrayList<>(requireQueryInOutCards.keySet());
        List<BankTransactionRecord> unsortedRecords = asyncQueryInOutRecord(computeTotal, queryCards, request.getCardNum(), request.getCaseId(), singleQuota, isInFlow);
        if (CollectionUtils.isEmpty(unsortedRecords)) {
            return null;
        }
        // 生成快进快出记录
        if (isInFlow) {
            return generateFastInOutFromDeposit(request.getCharacteristicRatio(), request.getTimeInterval(), limit, orderRecords, unsortedRecords, true);
        }
        return generateFastInOutFromSource(request.getCharacteristicRatio(), request.getTimeInterval(), limit, orderRecords, unsortedRecords, false);
    }

    private int getRequireQueryCardsAndOrderRecordsAndTotal(List<BankTransactionRecord> sortRecords, Map<String, Integer> requireQueryInOutCards,
                                                            Map<String, Integer> queryCardInOutTimes, List<BankTransactionRecord> orderRecords) {
        int computeTotal = 0;
        for (BankTransactionRecord record : sortRecords) {
            if (computeTotal >= resultChunkSize) {
                break;
            }
            Integer inOutTimes = queryCardInOutTimes.get(record.getQueryCard());
            if (null != inOutTimes && inOutTimes > 0) {
                orderRecords.add(record);
                if (!requireQueryInOutCards.containsKey(record.getQueryCard())) {
                    // 检查需要查询的卡号
                    requireQueryInOutCards.put(record.getQueryCard(), 0);
                    computeTotal += inOutTimes;
                }
            }
        }
        return computeTotal;
    }

    /**
     * <h2> 获取2跳排序的查询卡号 </h2>
     * <p>
     * 来源: 调单卡号出账的卡号 - 基于对方卡号继续查询出账的卡号(留下符合的对方卡号)
     * 沉淀: 调单卡号进账的卡号 - 基于对方卡号继续查询进账的卡号(留下符合的对方卡号)
     */
    private List<String> getTwoHopSortQueryCards(FastInFastOutRequest request) {

        List<String> adjustCards = request.getCardNum();
        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        boolean inFlow = isInFlow(request.getSortRequest());
        // 查询固定数量的调单卡号的进账或者出账(获取对方卡号去重)
        try {
            List<String> oppositeCards = asyncQueryOppositeAndLocalCards(adjustCards, caseId, singleQuota, inFlow, false);
            if (!CollectionUtils.isEmpty(oppositeCards)) {
                // 继续检查这些对方卡号(以它作为查询卡号) 是否有进账或者出账(有的话,记录当前的查询卡号)
                List<String> conditionCards = asyncQueryOppositeAndLocalCards(oppositeCards, caseId, singleQuota, !inFlow, true);
                // 最多取一部分卡号
                return conditionCards.stream().limit(maxAdjustCardQueryCount).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("async query two hop sort opposite cards error!");
            return null;
        }
        return null;
    }

    /**
     * <h2> 生成按流入金额/流出金额/流出日期 排序的快进快出记录 </h2>
     * <p>
     * 调单卡号作为中转卡号
     */
    private Map<String, ?> generateResultViaTransit(FastInFastOutRequest request, boolean isInflow, String sortProperty, Sort.Direction direction, int limit) {

        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota(); // 单笔限额
        long timeInterval = request.getTimeInterval(); // 时间间隔
        int characteristicRatio = request.getCharacteristicRatio();
        List<BankTransactionRecord> sortRecords = getInOutRecordOrderViaQueryCards(request.getCardNum(), caseId, singleQuota, isInflow, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(sortRecords)) {
            return new HashMap<>();
        }
        // 去重 inOutflowOrder 中的查询卡号,查询这些卡号的进账/出账记录数
        List<String> distinctQueryCard = sortRecords.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
        Map<String, Integer> queryCardInOutTimes = getInOutTotalTimesViaLocalAndOppositeCards(distinctQueryCard, null, caseId, singleQuota, !isInflow);
        if (CollectionUtils.isEmpty(queryCardInOutTimes)) {
            return new HashMap<>();
        }
        // 计算 inOutflowOrder 记录 的查询卡号的进账/出账总次数是否 == resultChunkSize
        // 当大于 resultChunkSize 的时候截止,或者 queryCardOutTimes 的 size 扫描结束为止
        // 为了取出满足 resultChunkSize 数量的 中转 到 沉淀数据
        // 需要查询出账的卡集合
        Map<String, Integer> requireQueryInOutCards = new HashMap<>();
        List<BankTransactionRecord> orderRecords = new ArrayList<>();
        // 满足条件的卡号总量
        int computeTotal = getRequireQueryCardsAndOrderRecordsAndTotal(sortRecords, requireQueryInOutCards, queryCardInOutTimes, orderRecords);
        // 需要查询的卡号
        List<String> queryCards = new ArrayList<>(requireQueryInOutCards.keySet());
        List<BankTransactionRecord> unsortedRecords = asyncQueryInOutRecord(computeTotal, queryCards, null, caseId, singleQuota, isInflow);
        if (CollectionUtils.isEmpty(unsortedRecords)) {
            return null;
        }
        // 生成快进快出记录
        return generateFastInOutFromTransit(characteristicRatio, timeInterval, limit, orderRecords, unsortedRecords, isInflow);
    }

    /**
     * <h2> 批量查询卡号的进/出记录,获取对方卡号,然后再查询 </h2>
     */
    private List<String> asyncQueryOppositeAndLocalCards(List<String> cards, String caseId, int singleQuota, boolean isInflow, boolean isLocal) throws ExecutionException, InterruptedException {

        int position = 0;
        List<CompletableFuture<List<String>>> futures = new ArrayList<>();
        while (position < transitCardCount) {
            int next = Math.min(position + perQueryCount, transitCardCount);
            int finalPosition = position;
            CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() ->
                    getDistinctOppositeCardOrQueryCard(cards, caseId, singleQuota, isInflow, isLocal, finalPosition, next), ThreadPoolConfig.getExecutor());
            position = next;
            futures.add(future);
        }
        List<String> oppositeCards = new ArrayList<>();
        for (CompletableFuture<List<String>> future : futures) {
            List<String> oppositeCardsFuture = future.get();
            if (!CollectionUtils.isEmpty(oppositeCardsFuture)) {
                oppositeCards.addAll(oppositeCardsFuture);
            }
        }
        return oppositeCards;
    }

    /**
     * <h2> 批量查询卡号的进/出记录 </h2>
     */
    private List<BankTransactionRecord> asyncQueryInOutRecord(int computeTotal, List<String> requireQueryInOutCards, @Nullable List<String> oppositeCards,
                                                              String caseId, int singleQuota, boolean isInflow) {

        int position = 0;
        List<CompletableFuture<List<BankTransactionRecord>>> futures = new ArrayList<>();
        int page = 0;
        while (position < computeTotal) {
            // 查询这些卡号的进账/出账(批量查询,每次查询 perQueryCount)
            int next = Math.min(position + perQueryCount, computeTotal);
            // 批量查询这些卡号的进账/出账记录
            int finalPage = page;
            CompletableFuture<List<BankTransactionRecord>> future = CompletableFuture.supplyAsync(() ->
                    getInOutRecordViaQueryCards(requireQueryInOutCards, oppositeCards, caseId, singleQuota, !isInflow, finalPage, perQueryCount));
            futures.add(future);
            position = next;
            page++;
        }
        List<BankTransactionRecord> inOutflowOrderInOutRecords = new ArrayList<>();
        futures.forEach(record -> {
            try {
                List<BankTransactionRecord> bankTransactionRecords = record.get();
                if (!CollectionUtils.isEmpty(bankTransactionRecords)) {
                    inOutflowOrderInOutRecords.addAll(bankTransactionRecords);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("async query card in / out records exception from adjust card!");
            }
        });
        return inOutflowOrderInOutRecords;
    }

    /**
     * <h2> 生成快进快出结果  </h2>
     * <p>
     * 调单卡号作为中转卡号的情况 (流入和流出排序不同,生成的结果也不同)
     *
     * @return 返回符合limit 数量的结果,返回数据总量
     */
    private Map<String, ?> generateFastInOutFromTransit(int characteristicRatio, long timeInterval, int limit,
                                                        List<BankTransactionRecord> orderRecords, List<BankTransactionRecord> unsortedRecords, boolean isInflow) {

        // 将second 转成map,避免 n * n, 改成 2 * n
        Map<String, List<BankTransactionRecord>> maps = getBankTransactionRecordMap(unsortedRecords);
        // 快进快出记录
        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();
        // 记录总量(一个不重复得hash 值代表一个)
        Set<Integer> hashes = new HashSet<>();
        for (BankTransactionRecord orderRecord : orderRecords) {

            List<BankTransactionRecord> mergeRecords = maps.get(orderRecord.getQueryCard());
            if (CollectionUtils.isEmpty(mergeRecords)) {
                continue;
            }
            for (BankTransactionRecord unsortedRecord : mergeRecords) {
                // 获取limit数量 所需要的结果
                if (fastInFastOutResults.size() < limit) {
                    FastInFastOutResult fastInFastOutResult = convertFromDataTransit(characteristicRatio, timeInterval, orderRecord, unsortedRecord, isInflow);
                    if (null != fastInFastOutResult) {
                        fastInFastOutResults.add(fastInFastOutResult);
                    }
                }
                // 记录总量
                Integer hash = computeResultHashFromTransit(characteristicRatio, timeInterval, orderRecord, unsortedRecord, isInflow);
                if (null != hash) {
                    hashes.add(hash);
                }
            }
        }
        Map<String, Object> resultsMap = new HashMap<>();
        resultsMap.put("records", fastInFastOutResults);
        resultsMap.put("hashes", hashes);
        return resultsMap;
    }

    /**
     * <h2> 生成快进快出结果 {@link FastInFastOutResult} </h2>
     * <p>
     * 调单卡号作为来源卡号的情况 (流入和流出排序不同,生成的结果也不同)
     *
     * @return 返回符合limit 数量的结果,返回数据总量
     */
    private Map<String, ?> generateFastInOutFromSource(int characteristicRatio, long timeInterval, int limit,
                                                       List<BankTransactionRecord> orderRecords, List<BankTransactionRecord> unsortedRecords, boolean isInflow) {
        Map<String, List<BankTransactionRecord>> maps = getBankTransactionRecordMap(unsortedRecords);
        // 快进快出记录
        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();
        // 记录总量(一个不重复得hash 值代表一个)
        Set<Integer> hashes = new HashSet<>();
        for (BankTransactionRecord orderRecord : orderRecords) {
            List<BankTransactionRecord> mergeRecords;
            if (isInflow) {
                mergeRecords = maps.get(orderRecord.getTransactionOppositeCard());
            } else {
                mergeRecords = maps.get(orderRecord.getQueryCard());
            }
            if (CollectionUtils.isEmpty(mergeRecords)) {
                continue;
            }
            for (BankTransactionRecord unsortedRecord : mergeRecords) {
                if (fastInFastOutResults.size() < limit) {
                    FastInFastOutResult fastInFastOutRecord = convertFromDataSource(characteristicRatio, timeInterval, orderRecord, unsortedRecord, isInflow);
                    if (null != fastInFastOutRecord) {
                        fastInFastOutResults.add(fastInFastOutRecord);
                    }
                }
                // 记录总量
                Integer hash = computeResultHashFromSource(characteristicRatio, timeInterval, orderRecord, unsortedRecord, isInflow);
                if (null != hash) {
                    hashes.add(hash);
                }
            }
        }
        Map<String, Object> resultsMap = new HashMap<>();
        resultsMap.put("records", fastInFastOutResults);
        resultsMap.put("hashes", hashes);
        return resultsMap;
    }

    /**
     * <h2> 生成快进快出结果 {@link FastInFastOutResult} </h2>
     * <p>
     * 调单卡号作为沉淀卡号的情况 (流入和流出排序不同,生成的结果也不同)
     * 需要排除 来源卡号 和 沉淀卡号相同的情况
     *
     * @return 返回符合limit 数量的结果,返回数据总量
     */
    private Map<String, ?> generateFastInOutFromDeposit(int characteristicRatio, long timeInterval, int limit,
                                                        List<BankTransactionRecord> orderRecords, List<BankTransactionRecord> unsortedRecords, boolean isInflow) {

        Map<String, List<BankTransactionRecord>> maps = getBankTransactionRecordMap(unsortedRecords);
        // 快进快出记录
        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();
        // 记录总量(一个不重复得hash 值代表一个)
        Set<Integer> hashes = new HashSet<>();
        for (BankTransactionRecord orderRecord : orderRecords) {
            List<BankTransactionRecord> mergeRecords;
            if (!isInflow) {
                mergeRecords = maps.get(orderRecord.getTransactionOppositeCard());
            } else {
                mergeRecords = maps.get(orderRecord.getQueryCard());
            }
            if (CollectionUtils.isEmpty(mergeRecords)) {
                continue;
            }
            for (BankTransactionRecord unsortedRecord : mergeRecords) {
                if (fastInFastOutResults.size() < limit) {
                    FastInFastOutResult fastInFastOutRecord = convertFromDataDeposit(characteristicRatio, timeInterval, orderRecord, unsortedRecord, isInflow);
                    if (null != fastInFastOutRecord) {
                        fastInFastOutResults.add(fastInFastOutRecord);
                    }
                }
                // 记录总量
                Integer hash = computeResultHashFromDeposit(characteristicRatio, timeInterval, orderRecord, unsortedRecord, isInflow);
                if (null != hash) {
                    hashes.add(hash);
                }
            }
        }
        Map<String, Object> resultsMap = new HashMap<>();
        resultsMap.put("records", fastInFastOutResults);
        resultsMap.put("hashes", hashes);
        return resultsMap;
    }

    /**
     * <h2> 查询cards的进账或者出账,获取它的去重对方卡号或者查询卡号集合 </h2>
     */
    private List<String> getDistinctOppositeCardOrQueryCard(List<String> cards, String caseId, int singleQuota, boolean isIn, boolean isLocal, int from, int size) {

        QuerySpecialParams query = fastInFastOutQueryParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isIn);
        AggregationParams agg;
        if (isLocal) {
            agg = aggParamFactory.groupByAndCountField(FundTacticsAnalysisField.QUERY_CARD, transitCardCount, new Pagination(from, size));
            agg.setMapping(entityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        } else {
            agg = aggParamFactory.groupByAndCountField(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, transitCardCount, new Pagination(from, size));
            agg.setMapping(entityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD));
        }
        agg.setResultName("distinctCards");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultMap)) {
            return null;
        }
        List<List<Object>> result = resultMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return result.stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
    }

    /**
     * <h2> 通过查询卡号获取进出记录(根据排序) </h2>
     */
    private List<BankTransactionRecord> getInOutRecordOrderViaQueryCards(List<String> cards, String caseId, int singleQuota,
                                                                         boolean isIn, String property, Sort.Direction direction, int orderChunkSize) {

        QuerySpecialParams query = fastInFastOutQueryParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isIn);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(0, orderChunkSize, direction, property), caseId, BankTransactionRecord.class, query);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 通过查询卡号、对方卡号 获取进出记录(不排序) </h2>
     */
    private List<BankTransactionRecord> getInOutRecordViaQueryCards(List<String> cards, List<String> oppositeCards, String caseId, int singleQuota,
                                                                    boolean isIn, int from, int orderChunkSize) {

        QuerySpecialParams query;
        if (CollectionUtils.isEmpty(oppositeCards)) {
            query = fastInFastOutQueryParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isIn);
        } else {
            query = fastInFastOutQueryParamFactory.getInoutRecordsViaQueryAndOpposite(cards, oppositeCards, caseId, singleQuota, isIn);
        }
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(from, orderChunkSize), caseId, BankTransactionRecord.class, query);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 通过查询卡号 与 对方卡号(可能为空) 获取进账/出账的总次数 </h2>
     */
    private Map<String, Integer> getInOutTotalTimesViaLocalAndOppositeCards(List<String> cards, @Nullable List<String> adjustCards, String caseId, int singleQuota, boolean isInFlow) {

        QuerySpecialParams query;
        if (CollectionUtils.isEmpty(adjustCards)) {
            query = fastInFastOutQueryParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isInFlow);
        } else {
            query = fastInFastOutQueryParamFactory.getInoutRecordsViaQueryAndOpposite(cards, adjustCards, caseId, singleQuota, isInFlow);
        }
        AggregationParams agg = aggParamFactory.groupByAndCountField(FundTacticsAnalysisField.QUERY_CARD, cards.size(), new Pagination(0, cards.size()));
        agg.setMapping(entityMappingFactory.buildGroupByAggDocCountMapping(FundTacticsAnalysisField.QUERY_CARD));
        agg.setResultName("queryCardInoutTimes");
        Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultMaps) || CollectionUtils.isEmpty(resultMaps.get(agg.getResultName()))) {
            return null;
        }
        List<List<Object>> results = resultMaps.get(agg.getResultName());
        return results.stream().collect(Collectors.toMap(e -> e.get(0).toString(), e -> Integer.parseInt(e.get(1).toString()), (v1, v2) -> v1));
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为来源情况
     * 需要排除 来源卡号 与 中转卡号相同 或者 中转卡号 与 沉淀卡号相同的情况
     * 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
     */
    private FastInFastOutResult convertFromDataSource(int characteristicRatio, long timeInterval,
                                                      BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord, boolean isInflow) {

        FastInFastOutResult source = new FastInFastOutResult();
        // 入账金额
        double inflowAmount;
        // 按照入账金额排序
        if (isInflow) {
            inflowAmount = orderRecord.getChangeAmount();
            setFastInoutSourceInflow(source, orderRecord, unsortedRecord);
            // 时间间隔
            // 流出日期 - 流入日期
            long computeTimeInterval = computeTimeInterval(unsortedRecord.getTradingTime(), orderRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        } else {
            // 按照流出金额、流出日期排序
            inflowAmount = unsortedRecord.getChangeAmount();
            setFastInoutSourceTransitOutflow(source, orderRecord, unsortedRecord);
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(orderRecord.getTradingTime(), unsortedRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        }
        if (filterFastInFastOutResult(source)) return null;
        // 特征比(入账金额-出账金额) / 入账金额
        double computeFeatureRatio = computeFastInFastOutCharacteristicRatio(orderRecord.getChangeAmount(), unsortedRecord.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // FNV1算法
        source.setHashId(HashUtil.fnvHash(FastInFastOutResult.hashString(source)));
        // 设置特征比
        source.setCharacteristicRatio(computeFeatureRatio);
        return source;
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为中转情况
     * 需要排除 来源卡号 与 中转卡号相同 或者 中转卡号 与 沉淀卡号相同的情况
     * 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
     */
    private FastInFastOutResult convertFromDataTransit(int characteristicRatio, long timeInterval,
                                                       BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord, boolean isInflow) {

        FastInFastOutResult transit = new FastInFastOutResult();
        // 入账金额
        double inflowAmount;
        if (isInflow) {
            inflowAmount = orderRecord.getChangeAmount();
            setFastInoutDepositTransitInflow(transit, orderRecord, unsortedRecord);
            // 时间间隔 判断
            // 流出日期 - 流入日期
            long computeTimeInterval = computeTimeInterval(unsortedRecord.getTradingTime(), orderRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        } else {
            inflowAmount = unsortedRecord.getChangeAmount();
            setFastInoutSourceTransitOutflow(transit, orderRecord, unsortedRecord);
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(orderRecord.getTradingTime(), unsortedRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        }
        if (filterFastInFastOutResult(transit)) return null;
        // 特征比(入账金额-出账金额) / 入账金额
        double computeFeatureRatio = computeFastInFastOutCharacteristicRatio(orderRecord.getChangeAmount(), unsortedRecord.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // FNV1算法
        transit.setHashId(HashUtil.fnvHash(FastInFastOutResult.hashString(transit)));
        // 设置特征比
        transit.setCharacteristicRatio(computeFeatureRatio);
        return transit;
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为沉淀情况
     * 需要排除 来源卡号 与 中转卡号相同 或者 中转卡号 与 沉淀卡号相同的情况
     * 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
     */
    private FastInFastOutResult convertFromDataDeposit(int characteristicRatio, long timeInterval,
                                                       BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord, boolean isInflow) {

        FastInFastOutResult deposit = new FastInFastOutResult();
        double inflowAmount;
        if (isInflow) {
            inflowAmount = orderRecord.getChangeAmount();
            setFastInoutDepositTransitInflow(deposit, orderRecord, unsortedRecord);
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(unsortedRecord.getTradingTime(), orderRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        } else {
            inflowAmount = unsortedRecord.getChangeAmount();
            setFastInoutDepositOutflow(deposit, orderRecord, unsortedRecord);
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(orderRecord.getTradingTime(), unsortedRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        }
        if (filterFastInFastOutResult(deposit)) return null;
        // 特征比(入账金额-出账金额) / 入账金额
        double computeFeatureRatio = computeFastInFastOutCharacteristicRatio(orderRecord.getChangeAmount(), unsortedRecord.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // FNV1算法
        deposit.setHashId(HashUtil.fnvHash(FastInFastOutResult.hashString(deposit)));
        // 设置特征比
        deposit.setCharacteristicRatio(computeFeatureRatio);
        return deposit;
    }


    private Integer computeResultHashFromSource(int characteristicRatio, long timeInterval,
                                                BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord, boolean isInflow) {

        // 入账金额
        double inflowAmount;
        String value;
        if (isInflow) {
            if (excludeHashSourceInflow(orderRecord, unsortedRecord)) {
                return null;
            }
            inflowAmount = orderRecord.getChangeAmount();
            // 时间间隔 判断
            long computeTimeInterval = computeTimeInterval(unsortedRecord.getTradingTime(), orderRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = computeHashStringSourceInflow(orderRecord, unsortedRecord);
        } else {
            if (excludeHashSourceTransitOutflow(orderRecord, unsortedRecord)) {
                return null;
            }
            inflowAmount = unsortedRecord.getChangeAmount();
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(orderRecord.getTradingTime(), unsortedRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = computeHashStringSourceTransitOutflow(orderRecord, unsortedRecord);
        }
        // 特征比(入账金额-出账金额) / 入账金额
        double computeFeatureRatio = computeFastInFastOutCharacteristicRatio(orderRecord.getChangeAmount(), unsortedRecord.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // FNV1算法
        return HashUtil.fnvHash(value);
    }

    private Integer computeResultHashFromTransit(int characteristicRatio, long timeInterval,
                                                 BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord, boolean isInflow) {
        // 入账金额
        double inflowAmount;
        String value;
        if (isInflow) {
            if (excludeHashDepositTransitInflow(orderRecord, unsortedRecord)) {
                return null;
            }
            inflowAmount = orderRecord.getChangeAmount();
            // 时间间隔 判断
            long computeTimeInterval = computeTimeInterval(unsortedRecord.getTradingTime(), orderRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = computeHashStringDepositTransitInflow(orderRecord, unsortedRecord);
        } else {
            if (excludeHashSourceTransitOutflow(orderRecord, unsortedRecord)) {
                return null;
            }
            inflowAmount = unsortedRecord.getChangeAmount();
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(orderRecord.getTradingTime(), unsortedRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = computeHashStringSourceTransitOutflow(orderRecord, unsortedRecord);
        }
        // 特征比(入账金额-出账金额) / 入账金额
        double computeFeatureRatio = computeFastInFastOutCharacteristicRatio(orderRecord.getChangeAmount(), unsortedRecord.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // FNV1算法
        return HashUtil.fnvHash(value);
    }

    public Integer computeResultHashFromDeposit(int characteristicRatio, long timeInterval,
                                                BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord, boolean isInflow) {

        // 入账金额
        double inflowAmount;
        String value;
        if (isInflow) {
            if (excludeHashDepositTransitInflow(orderRecord, unsortedRecord)) {
                return null;
            }
            inflowAmount = orderRecord.getChangeAmount();
            // 时间间隔 判断
            long computeTimeInterval = computeTimeInterval(unsortedRecord.getTradingTime(), orderRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = computeHashStringDepositTransitInflow(orderRecord, unsortedRecord);
        } else {
            if (excludeHashDepositOutflow(orderRecord, unsortedRecord)) {
                return null;
            }
            inflowAmount = unsortedRecord.getChangeAmount();
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(orderRecord.getTradingTime(), unsortedRecord.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = computeHashStringDepositOutflow(orderRecord, unsortedRecord);
        }
        // 特征比(入账金额-出账金额) / 入账金额
        double computeFeatureRatio = computeFastInFastOutCharacteristicRatio(orderRecord.getChangeAmount(), unsortedRecord.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // FNV1算法
        return HashUtil.fnvHash(value);
    }

    /**
     * <h2> 计算快进快出结果的特征比 </h2>
     */
    private double computeFastInFastOutCharacteristicRatio(double firstChangeAmount, double secondChangeAmount,
                                                           double inflowAmount) {
        BigDecimal sub;
        if (firstChangeAmount < secondChangeAmount) {
            sub = BigDecimalUtil.sub(secondChangeAmount, firstChangeAmount);
        } else {
            sub = BigDecimalUtil.sub(firstChangeAmount, secondChangeAmount);
        }
        BigDecimal div = BigDecimalUtil.divReserveFour(sub.doubleValue(), inflowAmount);
        return BigDecimalUtil.mul(div.doubleValue(), 100).doubleValue();
    }

    private long computeTimeInterval(Date inflowDate, Date outflowDate) {
        long minutes = ChronoUnit.MINUTES.between(Instant.ofEpochMilli(outflowDate.getTime()), Instant.ofEpochMilli(inflowDate.getTime()));
        if (minutes < 0) {
            return -1L;
        }
        return minutes;
    }

    /**
     * <h2> 将未排序的一边包装成map 结构 </h2>
     */
    private Map<String, List<BankTransactionRecord>> getBankTransactionRecordMap(List<BankTransactionRecord> unOrderedSide) {
        // 将second 转成map,避免 n * n, 改成 2 * n
        Map<String, List<BankTransactionRecord>> maps = new HashMap<>();
        unOrderedSide.forEach(e -> {

            if (maps.containsKey(e.getQueryCard())) {
                maps.get(e.getQueryCard()).add(e);
            } else {
                List<BankTransactionRecord> bankTransactionRecords = new ArrayList<>();
                bankTransactionRecords.add(e);
                maps.put(e.getQueryCard(), bankTransactionRecords);
            }
        });
        return maps;
    }

    private String requireSortProperty(SortRequest sortRequest) {
        String sortProperty = FundTacticsAnalysisField.CHANGE_MONEY;
        if (sortRequest.getProperty().equals(FundTacticsAnalysisField.FastInoutSort.OUTFLOW_TIME)) {
            sortProperty = FundTacticsAnalysisField.TRADING_TIME;
        }
        return sortProperty;
    }

    private boolean isInFlow(SortRequest sortRequest) {
        boolean isInflow = false;
        if (sortRequest.getProperty().equals(FundTacticsAnalysisField.FastInoutSort.INFLOW_AMOUNT)) {
            isInflow = true;
        }
        return isInflow;
    }

    private Sort.Direction requireSortDirection(SortRequest sortRequest) {
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortRequest.getOrder().isAscending()) {
            direction = Sort.Direction.ASC;
        }
        return direction;
    }

    // 沉淀流入、中转流入
    @SuppressWarnings("all")
    private void setFastInoutDepositTransitInflow(FastInFastOutResult result, BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {
        // 资金来源卡号
        result.setFundSourceCard(orderRecord.getTransactionOppositeCard());
        // 资金来源户名
        result.setFundSourceAccountName(orderRecord.getTransactionOppositeName());
        // 流入时间日期
        result.setInflowDate(DateFormatUtils.format(orderRecord.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        result.setInflowDateTime(orderRecord.getTradingTime().getTime());
        // 流入金额
        result.setInflowAmount(BigDecimalUtil.value(orderRecord.getChangeAmount()));
        // 资金中转卡号
        result.setFundTransitCard(orderRecord.getQueryCard());
        // 资金中转户名
        result.setFundTransitAccountName(orderRecord.getCustomerName());
        // 流出时间日期
        result.setOutflowDate(DateFormatUtils.format(unsortedRecord.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        result.setOutflowDateTime(unsortedRecord.getTradingTime().getTime());
        // 流出金额
        result.setOutflowAmount(BigDecimalUtil.value(unsortedRecord.getChangeAmount()));
        // 资金沉淀卡号
        result.setFundDepositCard(unsortedRecord.getTransactionOppositeCard());
        // 资金沉淀户名
        result.setFundDepositAccountName(unsortedRecord.getTransactionOppositeName());
    }

    // 沉淀流入、中转流入
    private String computeHashStringDepositTransitInflow(BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {
        return orderRecord.getTransactionOppositeCard() + orderRecord.getTradingTime().getTime() + BigDecimalUtil.value(orderRecord.getChangeAmount())
                + orderRecord.getQueryCard() + unsortedRecord.getTradingTime().getTime() + BigDecimalUtil.value(unsortedRecord.getChangeAmount())
                + unsortedRecord.getTransactionOppositeCard();
    }

    // 需要排除 来源卡号 与 中转卡号相同 或者 中转卡号 与 沉淀卡号相同的情况
    // 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
    private boolean excludeHashDepositTransitInflow(BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {

        if (StringUtils.equals(orderRecord.getTransactionOppositeCard(), orderRecord.getQueryCard()) ||
                StringUtils.equals(orderRecord.getQueryCard(), unsortedRecord.getTransactionOppositeCard())) {
            return true;
        }
        return StringUtils.equals(orderRecord.getTransactionOppositeCard(), unsortedRecord.getTransactionOppositeCard()) &&
                unsortedRecord.getChangeAmount().equals(orderRecord.getChangeAmount()) &&
                unsortedRecord.getTradingTime().getTime() == orderRecord.getTradingTime().getTime();
    }

    // 来源流入
    private void setFastInoutSourceInflow(FastInFastOutResult result, BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {
        // 资金来源卡号
        result.setFundSourceCard(orderRecord.getQueryCard());
        // 资金来源户名
        result.setFundSourceAccountName(orderRecord.getCustomerName());
        // 流入时间日期
        result.setInflowDate(DateFormatUtils.format(orderRecord.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        result.setInflowDateTime(orderRecord.getTradingTime().getTime());
        // 流入金额
        result.setInflowAmount(BigDecimalUtil.value(orderRecord.getChangeAmount()));
        // 资金中转卡号
        result.setFundTransitCard(orderRecord.getTransactionOppositeCard());
        // 资金中转户名
        result.setFundTransitAccountName(orderRecord.getTransactionOppositeName());
        // 流出时间日期
        result.setOutflowDate(DateFormatUtils.format(unsortedRecord.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        result.setOutflowDateTime(unsortedRecord.getTradingTime().getTime());
        // 流出金额
        result.setOutflowAmount(BigDecimalUtil.value(unsortedRecord.getChangeAmount()));
        // 资金沉淀卡号
        result.setFundDepositCard(unsortedRecord.getTransactionOppositeCard());
        // 资金沉淀户名
        result.setFundDepositAccountName(unsortedRecord.getTransactionOppositeName());
    }

    // 来源流入
    private String computeHashStringSourceInflow(BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {
        return orderRecord.getQueryCard() + orderRecord.getTradingTime().getTime() + BigDecimalUtil.value(orderRecord.getChangeAmount())
                + orderRecord.getTransactionOppositeCard() + unsortedRecord.getTradingTime().getTime() + BigDecimalUtil.value(unsortedRecord.getChangeAmount())
                + unsortedRecord.getTransactionOppositeCard();
    }

    // 需要排除 来源卡号 与 中转卡号相同 或者 中转卡号 与 沉淀卡号相同的情况
    // 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
    private boolean excludeHashSourceInflow(BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {

        if (StringUtils.equals(orderRecord.getQueryCard(), orderRecord.getTransactionOppositeCard()) ||
                StringUtils.equals(orderRecord.getTransactionOppositeCard(), unsortedRecord.getTransactionOppositeCard())) {
            return true;
        }
        return StringUtils.equals(orderRecord.getQueryCard(), unsortedRecord.getTransactionOppositeCard()) &&
                unsortedRecord.getChangeAmount().equals(orderRecord.getChangeAmount()) &&
                unsortedRecord.getTradingTime().getTime() == orderRecord.getTradingTime().getTime();
    }

    // 来源流出、中转流出
    @SuppressWarnings("all")
    private void setFastInoutSourceTransitOutflow(FastInFastOutResult result, BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {
        result.setFundSourceCard(unsortedRecord.getTransactionOppositeCard());
        result.setFundSourceAccountName(unsortedRecord.getTransactionOppositeName());
        result.setInflowDate(DateFormatUtils.format(unsortedRecord.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        result.setInflowDateTime(unsortedRecord.getTradingTime().getTime());
        result.setInflowAmount(BigDecimalUtil.value(unsortedRecord.getChangeAmount()));
        result.setFundTransitCard(unsortedRecord.getQueryCard());
        result.setFundTransitAccountName(unsortedRecord.getCustomerName());
        result.setOutflowDate(DateFormatUtils.format(orderRecord.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        result.setOutflowDateTime(orderRecord.getTradingTime().getTime());
        result.setOutflowAmount(BigDecimalUtil.value(orderRecord.getChangeAmount()));
        result.setFundDepositCard(orderRecord.getTransactionOppositeCard());
        result.setFundDepositAccountName(orderRecord.getTransactionOppositeName());
    }

    // 来源流出、中转流出
    private String computeHashStringSourceTransitOutflow(BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {
        return unsortedRecord.getTransactionOppositeCard() + unsortedRecord.getTradingTime().getTime() + BigDecimalUtil.value(unsortedRecord.getChangeAmount())
                + unsortedRecord.getQueryCard() + orderRecord.getTradingTime().getTime() + BigDecimalUtil.value(orderRecord.getChangeAmount())
                + orderRecord.getTransactionOppositeCard();
    }

    // 需要排除 来源卡号 与 中转卡号相同 或者 中转卡号 与 沉淀卡号相同的情况
    // 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
    private boolean excludeHashSourceTransitOutflow(BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {

        if (StringUtils.equals(unsortedRecord.getTransactionOppositeCard(), unsortedRecord.getQueryCard()) ||
                StringUtils.equals(unsortedRecord.getQueryCard(), orderRecord.getTransactionOppositeCard())) {
            return true;
        }
        return StringUtils.equals(unsortedRecord.getTransactionOppositeCard(), orderRecord.getTransactionOppositeCard()) &&
                unsortedRecord.getChangeAmount().equals(orderRecord.getChangeAmount()) &&
                unsortedRecord.getTradingTime().getTime() == orderRecord.getTradingTime().getTime();
    }

    // 沉淀流出
    @SuppressWarnings("all")
    private void setFastInoutDepositOutflow(FastInFastOutResult result, BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {
        result.setFundSourceCard(unsortedRecord.getTransactionOppositeCard());
        result.setFundSourceAccountName(unsortedRecord.getTransactionOppositeName());
        result.setInflowDate(DateFormatUtils.format(unsortedRecord.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        result.setInflowDateTime(unsortedRecord.getTradingTime().getTime());
        result.setInflowAmount(BigDecimalUtil.value(unsortedRecord.getChangeAmount()));
        result.setFundTransitCard(unsortedRecord.getQueryCard());
        result.setFundTransitAccountName(unsortedRecord.getCustomerName());
        result.setOutflowDate(DateFormatUtils.format(orderRecord.getTradingTime(), "yyyy-MM-dd HH:mm:ss"));
        result.setOutflowDateTime(orderRecord.getTradingTime().getTime());
        result.setOutflowAmount(BigDecimalUtil.value(orderRecord.getChangeAmount()));
        result.setFundDepositCard(orderRecord.getQueryCard());
        result.setFundDepositAccountName(orderRecord.getCustomerName());
    }

    // 沉淀流出
    private String computeHashStringDepositOutflow(BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {
        return unsortedRecord.getTransactionOppositeCard() + unsortedRecord.getTradingTime().getTime() + BigDecimalUtil.value(unsortedRecord.getChangeAmount())
                + unsortedRecord.getQueryCard() + orderRecord.getTradingTime().getTime() + BigDecimalUtil.value(orderRecord.getChangeAmount())
                + orderRecord.getQueryCard();
    }

    // 需要排除 来源卡号 与 中转卡号相同 或者 中转卡号 与 沉淀卡号相同的情况
    // 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
    private boolean excludeHashDepositOutflow(BankTransactionRecord orderRecord, BankTransactionRecord unsortedRecord) {

        if (StringUtils.equals(unsortedRecord.getTransactionOppositeCard(), unsortedRecord.getQueryCard()) ||
                StringUtils.equals(unsortedRecord.getQueryCard(), orderRecord.getQueryCard())) {
            return true;
        }
        return StringUtils.equals(unsortedRecord.getTransactionOppositeCard(), orderRecord.getQueryCard()) &&
                unsortedRecord.getChangeAmount().equals(orderRecord.getChangeAmount()) &&
                unsortedRecord.getTradingTime().getTime() == orderRecord.getTradingTime().getTime();
    }

    /**
     * <h2>
     * 需要排除 来源卡号 与 中转卡号相同 或者 中转卡号 与 沉淀卡号相同的情况
     * 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
     * </h2>
     */
    private boolean filterFastInFastOutResult(FastInFastOutResult result) {
        if (StringUtils.equals(result.getFundSourceCard(), result.getFundTransitCard()) || StringUtils.equals(result.getFundTransitCard(), result.getFundDepositCard())) {
            return true;
        }
        // 还需要排除 来源卡号 与 沉淀卡号相同 并且 流入金额与流出金额相同 并且 流入日期与流出日期相同
        return StringUtils.equals(result.getFundSourceCard(), result.getFundDepositCard()) &&
                result.getInflowAmount().equals(result.getOutflowAmount()) && result.getInflowDateTime() == result.getOutflowDateTime();
    }
}
