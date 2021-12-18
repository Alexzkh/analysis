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
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
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
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
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
    @Value("${per_query_count}")
    private int perQueryCount;
    @Value("${transit_card_count}")
    private int transitCardCount;

    public ServerResponse fastInFastOutAnalysis(FastInFastOutRequest request) throws ExecutionException, InterruptedException {

        if (request.getType() == 1) {
            // 代表全部查询
            return fastInFastOutViaAllQuery(request);
        } else {
            return fastInFastOutViaChosenIndividual(request);
        }
    }

    private ServerResponse fastInFastOutViaAllQuery(FastInFastOutRequest request) {

        // 直接查询调单卡号的数量

        return ServerResponse.createBySuccess();
    }

    /**
     * 选择个体查询
     */
    private ServerResponse fastInFastOutViaChosenIndividual(FastInFastOutRequest request) throws ExecutionException, InterruptedException {
        // 需要返回的数量
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        String property = request.getSortRequest().getProperty();
        Direction order = request.getSortRequest().getOrder();
        int limit = pageRequest.getPage() == 0 ? pageRequest.getPageSize() : pageRequest.getPage() * pageRequest.getPageSize();
        int skip = pageRequest.getPage() * pageRequest.getPageSize();
        // 来源的
        CompletableFuture<? extends Map<String, ?>> sourceOrderFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getSourceOrder(request, limit);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                log.error("get source order result error !");
            }
            return null;
        }, ThreadPoolConfig.getExecutor());

        // 中转的
        CompletableFuture<? extends Map<String, ?>> transitOrderFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getTransitOrder(request, limit);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                log.error("get transit order result error !");
            }
            return null;
        }, ThreadPoolConfig.getExecutor());

        // 沉淀的
        CompletableFuture<? extends Map<String, ?>> depositOrderFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getDepositOrder(request, limit);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                log.error("get deposit order result error !");
            }
            return null;
        }, ThreadPoolConfig.getExecutor());

        List<FastInFastOutResult> results = new ArrayList<>();
        int total = 0;
        // 包含limit数量 的快进快出结果 与 满足条件的总数据量
        Map<String, ?> sourceOrderMap = sourceOrderFuture.get();
        Map<String, ?> transitOrderMap = transitOrderFuture.get();
        Map<String, ?> depositOrderMap = depositOrderFuture.get();
        if (!CollectionUtils.isEmpty(sourceOrderMap)) {
            List<FastInFastOutResult> sourceOrderRecords = (List<FastInFastOutResult>) sourceOrderMap.get("records");
            Set<String> sourceOrderHashes = (Set<String>) sourceOrderMap.get("hashes");
            results.addAll(sourceOrderRecords);
            total += sourceOrderHashes.size();
        }
        if (!CollectionUtils.isEmpty(transitOrderMap)) {
            List<FastInFastOutResult> transitOrderRecords = (List<FastInFastOutResult>) transitOrderMap.get("records");
            Set<String> transitOrderHashes = (Set<String>) transitOrderMap.get("hashes");
            results.addAll(transitOrderRecords);
            total += transitOrderHashes.size();
        }
        if (!CollectionUtils.isEmpty(depositOrderMap)) {
            List<FastInFastOutResult> depositOrderRecords = (List<FastInFastOutResult>) depositOrderMap.get("records");
            results.addAll(depositOrderRecords);
            Set<String> depositOrderHashes = (Set<String>) depositOrderMap.get("hashes");
            total += depositOrderHashes.size();
        }
        // 结果合并去重
        Comparator<BigDecimal> amountComparator = Comparator.reverseOrder();
        Comparator<Long> dateComparator = Comparator.reverseOrder();
        if (order.isAscending()) {
            amountComparator = Comparator.naturalOrder();
            dateComparator = Comparator.naturalOrder();
        }
        if (property.equals("inflowAmount")) {

            results = results.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                    Comparator.comparing(FastInFastOutResult::getHashId)
            )), ArrayList::new)).stream().sorted(Comparator.comparing(FastInFastOutResult::getInflowAmount, amountComparator))
                    .skip(skip).limit(pageRequest.getPageSize()).collect(Collectors.toList());
        } else if (property.equals("outflowAmount")) {

            results = results.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                    Comparator.comparing(FastInFastOutResult::getHashId)
            )), ArrayList::new)).stream().sorted(Comparator.comparing(FastInFastOutResult::getOutflowAmount, amountComparator))
                    .skip(skip).limit(pageRequest.getPageSize()).collect(Collectors.toList());
        } else {
            results = results.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                    Comparator.comparing(FastInFastOutResult::getHashId)
            )), ArrayList::new)).stream().sorted(Comparator.comparing(e -> {
                try {
                    return format.parse(e.getOutflowDate()).getTime();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                return null;
            }, dateComparator)).skip(skip).limit(pageRequest.getPageSize()).collect(Collectors.toList());
        }
        FundAnalysisResultResponse<FastInFastOutResult> resultResponse = new FundAnalysisResultResponse<>();
        resultResponse.setTotal(total);
        resultResponse.setTotalPages(PageRequest.getTotalPages(total, pageRequest.getPageSize()));
        resultResponse.setSize(pageRequest.getPageSize());
        resultResponse.setContent(results);
        return ServerResponse.createBySuccess(resultResponse);
    }

    /**
     * <h2> 处理并保存调单卡号为中转卡号的快进快出记录 </h2>
     */
    private Map<String, ?> getTransitOrder(FastInFastOutRequest request, int limit) throws ExecutionException, InterruptedException {
        SortRequest sortRequest = request.getSortRequest();
        // 需要排序的字段
        String sortProperty = FundTacticsAnalysisField.CHANGE_MONEY;
        if (sortRequest.getProperty().equals("outflowDate")) {
            sortProperty = FundTacticsAnalysisField.TRADING_TIME;
        }
        // 是根据流入 还是 流出的排序
        boolean isInflow = true;
        if (sortRequest.getProperty().equals("outflowAmount") || sortRequest.getProperty().equals("outflowDate")) {
            isInflow = false;
        }
        // 排序方向
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortRequest.getOrder().isAscending()) {
            direction = Sort.Direction.ASC;
        }
        // 根据配置值生成固定数量的快进快出记录
        return generateResultViaTransit(request, isInflow, sortProperty, direction, limit);
    }


    /**
     * <h2> 处理并保存调单卡号为来源卡号的快进快出记录 </h2>
     */
    private Map<String, ?> getSourceOrder(FastInFastOutRequest request, int limit) throws ExecutionException, InterruptedException {

        SortRequest sortRequest = request.getSortRequest();
        // 排序方向
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortRequest.getOrder().isAscending()) {
            direction = Sort.Direction.ASC;
        }
        String sortProperty = FundTacticsAnalysisField.CHANGE_MONEY;
        if (request.getSortRequest().getProperty().equals("outflowDate")) {
            sortProperty = FundTacticsAnalysisField.TRADING_TIME;
        }
        if (sortRequest.getProperty().equals("inflowAmount")) {
            return generateResultViaSourceInflowOrDepositOutFlow(request, sortProperty, direction, limit, true);
        } else {
            return generateResultViaSourceOutflowOrDepositInFlow(request, sortProperty, direction, limit, false);
        }
    }

    /**
     * <h2> 处理并保存调单卡号为沉淀卡号的快进快出记录 </h2>
     */
    private Map<String, ?> getDepositOrder(FastInFastOutRequest request, int limit) throws ExecutionException, InterruptedException {

        SortRequest sortRequest = request.getSortRequest();
        // 排序方向
        Sort.Direction direction = Sort.Direction.DESC;
        if (request.getSortRequest().getOrder().isAscending()) {
            direction = Sort.Direction.ASC;
        }
        String sortProperty = FundTacticsAnalysisField.CHANGE_MONEY;
        if (request.getSortRequest().getProperty().equals("outflowDate")) {
            sortProperty = FundTacticsAnalysisField.TRADING_TIME;
        }
        if (sortRequest.getProperty().equals("inflowAmount")) {
            return generateResultViaSourceOutflowOrDepositInFlow(request, sortProperty, direction, limit, true);
        } else {
            return generateResultViaSourceInflowOrDepositOutFlow(request, sortProperty, direction, limit, false);
        }
    }

    /**
     * <h2> 生成按流入金额(调单卡号作为来源卡号) 或者 生成按流出金额、流出日期(调单卡号作为沉淀卡号) 快进快出记录 </h2>
     * <p>
     * 调单卡号作为来源卡号 或者 沉淀卡号
     */
    private Map<String, ?> generateResultViaSourceInflowOrDepositOutFlow(FastInFastOutRequest request, String sortProperty, Sort.Direction direction, int limit, boolean isInFlow) throws ExecutionException, InterruptedException {

        String caseId = request.getCaseId();
        List<String> adjustCards = request.getCardNum();
        int singleQuota = request.getSingleQuota();
        long timeInterval = request.getTimeInterval();
        int characteristicRatio = request.getCharacteristicRatio();
        // 获取调单卡号的出账
        List<BankTransactionRecord> inOutRecord = getInOutRecordOrderViaQueryCards(adjustCards, caseId, singleQuota, !isInFlow, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(inOutRecord)) {
            return null;
        }
        // 去重 outRecord 中的对方卡号,查询这些卡号的出账记录
        List<String> distinctOppositeCard = inOutRecord.stream().map(BankTransactionRecord::getTransactionOppositeCard).distinct().collect(Collectors.toList());
        // 获取这些卡号的出账次数
        Map<String, Integer> oppositeCardInOutTimes = getInOutTotalTimesViaQueryCards(distinctOppositeCard, caseId, singleQuota, !isInFlow);
        // 取出满足 resultChunkSize 数量的对方卡号( 即 中转 - 沉淀 / 来源 - 中转的数据)
        if (CollectionUtils.isEmpty(oppositeCardInOutTimes)) {
            return null;
        }
        int computeTotal = 0;
        Map<String, Integer> requireQueryInOutCards = new HashMap<>();
        List<BankTransactionRecord> sourceToTransit = new ArrayList<>();
        for (BankTransactionRecord record : inOutRecord) {
            if (computeTotal >= resultChunkSize) {
                break;
            }
            Integer outTimes = oppositeCardInOutTimes.get(record.getTransactionOppositeCard());
            if (outTimes > 0) {
                sourceToTransit.add(record);
                computeTotal += outTimes;
                if (!requireQueryInOutCards.containsKey(record.getTransactionOppositeCard())) {
                    // 检查需要查询的卡号
                    requireQueryInOutCards.put(record.getTransactionOppositeCard(), 0);
                }
            }
        }
        // 需要查询的卡号
        List<String> queryCards = new ArrayList<>(requireQueryInOutCards.keySet());
        List<BankTransactionRecord> SourceToTransitOrTransitToDeposit = asyncQueryInOutRecord(computeTotal, queryCards, null, caseId, singleQuota, isInFlow);
        if (CollectionUtils.isEmpty(SourceToTransitOrTransitToDeposit)) {
            return null;
        }
        // 生成快进快出记录(调单作为中转卡号-流入金额/流出金额/流出日期 降序/升序情况)
        if (isInFlow) {
            return generateFastInOutFromSource(characteristicRatio, timeInterval, limit, sourceToTransit, SourceToTransitOrTransitToDeposit, true);
        }
        return generateFastInOutFromSource(characteristicRatio, timeInterval, limit, sourceToTransit, SourceToTransitOrTransitToDeposit, false);
    }

    /**
     * <h2> 生成按流出金额(调单卡号作为来源卡号)、流出日期 / 流入金额(调单卡号作为沉淀卡号) 快进快出记录 </h2>
     * <p>
     * 来源 - 中转 - 沉淀 (按照流出金额/流出日期排序,这种属于 2 跳排序, 实际上是拿来源卡号筛选出来的 中转卡号 去 查出账然后排序,
     * 关键问题在于 中转卡号很大的时候无法一次性取出,这就导致没办法进行流出金额与流出日期排序,因此只能随机取出一定量的卡号数量 暂定 transitCardCount
     */
    private Map<String, ?> generateResultViaSourceOutflowOrDepositInFlow(FastInFastOutRequest request, String sortProperty, Sort.Direction direction, int limit, boolean isInFlow) {

        List<String> adjustCards = request.getCardNum();
        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota();
        // TODO 这里可以看一下中转卡号的数量,如果数量大于  transitCardCount(查询一定量的数据放在内存,然后获取前1000条的数据的卡号),速度会很慢
        // 获取上面去重的对方卡号
        List<String> oppositeCards = getQueryCardInOutOppositeCard(adjustCards, caseId, singleQuota, isInFlow);
        if (CollectionUtils.isEmpty(oppositeCards)) {
            return null;
        }
        return generateResultSourceOutOne(oppositeCards, request, sortProperty, direction, limit, isInFlow);
    }

    private Map<String, ?> generateResultSourceOutOne(List<String> oppositeCards, FastInFastOutRequest request, String sortProperty,
                                                      Sort.Direction direction, int limit, boolean isInFlow) {

        String caseId = request.getCaseId();
        List<String> adjustCards = request.getCardNum();
        int singleQuota = request.getSingleQuota(); // 单笔限额
        long timeInterval = request.getTimeInterval(); // 时间间隔
        int characteristicRatio = request.getCharacteristicRatio(); // 特征比

        List<BankTransactionRecord> inOutflowOrder = getInOutRecordOrderViaQueryCards(oppositeCards, caseId, singleQuota, !isInFlow, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(inOutflowOrder)) {
            return null;
        }
        // 去重 inOutflowOrder 中的查询卡号,查询这些卡号的进账/出账记录数
        List<String> distinctQueryCard = inOutflowOrder.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
        Map<String, Integer> queryCardInOutTimes = getInOutTotalTimesViaQueryAndOppositeCards(distinctQueryCard, adjustCards, caseId, singleQuota, isInFlow);
        if (CollectionUtils.isEmpty(queryCardInOutTimes)) {
            return null;
        }
        int computeTotal = 0;
        Map<String, Integer> requireQueryInOutCards = new HashMap<>();
        List<BankTransactionRecord> sortedSide = new ArrayList<>();
        for (BankTransactionRecord record : inOutflowOrder) {
            if (computeTotal >= resultChunkSize) {
                break;
            }
            Integer inOutTimes = queryCardInOutTimes.get(record.getQueryCard());
            if (inOutTimes > 0) {
                sortedSide.add(record);
                computeTotal += inOutTimes;
                if (!requireQueryInOutCards.containsKey(record.getQueryCard())) {
                    // 检查需要查询的卡号
                    requireQueryInOutCards.put(record.getQueryCard(), 0);
                }
            }
        }
        // 需要查询的卡号
        List<String> queryCards = new ArrayList<>(requireQueryInOutCards.keySet());
        List<BankTransactionRecord> unSortedSide = asyncQueryInOutRecord(computeTotal, queryCards, adjustCards, caseId, singleQuota, isInFlow);
        if (CollectionUtils.isEmpty(unSortedSide)) {
            return null;
        }
        if (isInFlow) {
            // 生成的是沉淀的
            return generateFastInOutFromDeposit(characteristicRatio, timeInterval, limit, sortedSide, unSortedSide, true);
        }
        // 生成的是来源的
        return generateFastInOutFromSource(characteristicRatio, timeInterval, limit, sortedSide, unSortedSide, false);
    }


    /**
     * <h2> 生成按流入金额/流出金额/流出日期 排序的快进快出记录 </h2>
     */
    private Map<String, ?> generateResultViaTransit(FastInFastOutRequest request, boolean isInflow, String sortProperty, Sort.Direction direction, int limit) throws ExecutionException, InterruptedException {

        // 流入金额/流出金额/流出日期 降序排序生成快进快出记录
        List<String> adjustCards = request.getCardNum();
        String caseId = request.getCaseId();
        int singleQuota = request.getSingleQuota(); // 单笔限额
        long timeInterval = request.getTimeInterval(); // 时间间隔
        int characteristicRatio = request.getCharacteristicRatio();
        List<BankTransactionRecord> inOutflowOrder = getInOutRecordOrderViaQueryCards(adjustCards, caseId, singleQuota, isInflow, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(inOutflowOrder)) {
            return null;
        }
        // 去重 inOutflowOrder 中的查询卡号,查询这些卡号的进账/出账记录数
        List<String> distinctQueryCard = inOutflowOrder.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
        Map<String, Integer> queryCardInOutTimes = getInOutTotalTimesViaQueryCards(distinctQueryCard, caseId, singleQuota, !isInflow);
        if (CollectionUtils.isEmpty(queryCardInOutTimes)) {
            return null;
        }
        // 计算 inOutflowOrder 记录 的查询卡号的进账/出账总次数是否 == resultChunkSize
        // 当大于 resultChunkSize 的时候截止,或者 queryCardOutTimes 的 size 扫描结束为止
        // 为了取出满足 resultChunkSize 数量的 中转 到 沉淀数据
        int computeTotal = 0;
        // 需要查询出账的卡集合
        Map<String, Integer> requireQueryInOutCards = new HashMap<>();
        List<BankTransactionRecord> sourceToTransit = new ArrayList<>();
        for (BankTransactionRecord record : inOutflowOrder) {
            if (computeTotal >= resultChunkSize) {
                break;
            }
            Integer inOutTimes = queryCardInOutTimes.get(record.getQueryCard());
            if (inOutTimes > 0) {
                sourceToTransit.add(record);
                computeTotal += inOutTimes;
                if (!requireQueryInOutCards.containsKey(record.getQueryCard())) {
                    // 检查需要查询的卡号
                    requireQueryInOutCards.put(record.getQueryCard(), 0);
                }
            }
        }
        // 需要查询的卡号
        List<String> queryCards = new ArrayList<>(requireQueryInOutCards.keySet());
        List<BankTransactionRecord> transitToDeposit = asyncQueryInOutRecord(computeTotal, queryCards, null, caseId, singleQuota, isInflow);
        if (CollectionUtils.isEmpty(transitToDeposit)) {
            return null;
        }
        // 生成快进快出记录(调单作为中转卡号-流入金额/流出金额/流出日期 降序/升序情况)
        return generateFastInOutFromTransit(characteristicRatio, timeInterval, limit, sourceToTransit, transitToDeposit, isInflow);
    }

    /**
     * <h2> 批量查询卡号的进/出记录 </h2>
     */
    @SuppressWarnings("all")
    private List<BankTransactionRecord> asyncQueryInOutRecord(int computeTotal, List<String> requireQueryInOutCards, @Nullable List<String> oppositeCards,
                                                              String caseId, int singleQuota, boolean isInflow) {

        int position = 0;
        List<CompletableFuture<List<BankTransactionRecord>>> futures = new ArrayList<>();
        int page = 0;
        while (position < computeTotal) {
            // 查询这些卡号的入账(批量查询,每次查询 perQueryCount)
            int next = Math.min(position + perQueryCount, computeTotal);
            // 批量查询这些卡号的出账记录
            int finalPosition = position;
            int finalPage = page;
            CompletableFuture<List<BankTransactionRecord>> future = CompletableFuture.supplyAsync(() ->
                    getInOutRecordViaQueryCards(requireQueryInOutCards, caseId, singleQuota, !isInflow, finalPage, next - finalPosition));
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
                log.warn("async query card out records exception from adjust card!");
            }
        });
        return inOutflowOrderInOutRecords;
    }

    /**
     * <h2> 生成快进快出结果 {@link FastInFastOutRecord} </h2>
     * <p>
     * 调单卡号作为中转卡号的情况
     *
     * @return 返回符合limit 数量的结果,返回数据总量
     */
    private Map<String, ?> generateFastInOutFromTransit(int characteristicRatio, long timeInterval, int limit,
                                                        List<BankTransactionRecord> first, List<BankTransactionRecord> second, boolean isInflow) {

        // 将second 转成map,避免 n * n, 改成 2 * n
        Map<String, List<BankTransactionRecord>> maps = getBankTransactionRecordMap(second);
        // 快进快出记录
        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();
        // 记录总量(一个不重复得hash 值代表一个)
        Set<Integer> hashes = new HashSet<>();
        for (BankTransactionRecord orderRecord : first) {

            List<BankTransactionRecord> otherRecords = maps.get(orderRecord.getQueryCard());
            if (CollectionUtils.isEmpty(otherRecords)) {
                continue;
            }
            for (BankTransactionRecord otherRecord : otherRecords) {
                // 获取limit数量 所需要的结果
                if (fastInFastOutResults.size() < limit) {
                    FastInFastOutResult fastInFastOutResult = convertFromDataTransit(characteristicRatio, timeInterval, orderRecord, otherRecord, isInflow);
                    if (null != fastInFastOutResult) {
                        fastInFastOutResults.add(fastInFastOutResult);
                    }
                }
                // 记录总量
                Integer hash = computeResultHashFromTransit(characteristicRatio, timeInterval, orderRecord, otherRecord, isInflow);
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
     * 调单卡号作为来源卡号的情况
     *
     * @return 返回符合limit 数量的结果,返回数据总量
     */
    private Map<String, ?> generateFastInOutFromSource(int characteristicRatio, long timeInterval, int limit,
                                                       List<BankTransactionRecord> first, List<BankTransactionRecord> second, boolean isInflow) {
        Map<String, List<BankTransactionRecord>> maps = getBankTransactionRecordMap(second);
        // 快进快出记录
        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();
        // 记录总量(一个不重复得hash 值代表一个)
        Set<Integer> hashes = new HashSet<>();
        for (BankTransactionRecord orderRecord : first) {

            List<BankTransactionRecord> otherRecords = maps.get(orderRecord.getTransactionOppositeCard());
            if (CollectionUtils.isEmpty(otherRecords)) {
                continue;
            }
            for (BankTransactionRecord otherRecord : otherRecords) {
                if (fastInFastOutResults.size() < limit) {
                    FastInFastOutResult fastInFastOutRecord = convertFromDataSource(characteristicRatio, timeInterval, orderRecord, otherRecord, isInflow);
                    if (null != fastInFastOutRecord) {
                        fastInFastOutResults.add(fastInFastOutRecord);
                    }
                }
                // 记录总量
                Integer hash = computeResultHashFromSource(characteristicRatio, timeInterval, orderRecord, otherRecord, isInflow);
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

    private Map<String, ?> generateFastInOutFromDeposit(int characteristicRatio, long timeInterval, int limit,
                                                        List<BankTransactionRecord> first, List<BankTransactionRecord> second, boolean isInflow) {

        Map<String, List<BankTransactionRecord>> maps = getBankTransactionRecordMap(second);
        // 快进快出记录
        List<FastInFastOutResult> fastInFastOutResults = new ArrayList<>();
        // 记录总量(一个不重复得hash 值代表一个)
        Set<Integer> hashes = new HashSet<>();
        for (BankTransactionRecord orderRecord : first) {

            List<BankTransactionRecord> otherRecords = maps.get(orderRecord.getTransactionOppositeCard());
            if (CollectionUtils.isEmpty(otherRecords)) {
                continue;
            }
            for (BankTransactionRecord otherRecord : otherRecords) {
                if (fastInFastOutResults.size() < limit) {
                    FastInFastOutResult fastInFastOutRecord = convertFromDataDeposit(characteristicRatio, timeInterval, orderRecord, otherRecord, isInflow);
                    if (null != fastInFastOutRecord) {
                        fastInFastOutResults.add(fastInFastOutRecord);
                    }
                }
                // 记录总量
                Integer hash = computeResultHashFromDeposit(characteristicRatio, timeInterval, orderRecord, otherRecord, isInflow);
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
     * <h2> 获取查询卡号 进账/出账 对方卡号去重的数量 </h2>
     */
    private long getQueryCardInOutOppositeCardCount(List<String> cards, String caseId, int singleQuota, boolean isIn) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isIn);
        AggregationParams agg = aggregationRequestParamFactory.buildDistinctViaField(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);
        agg.setMapping(aggregationEntityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD));
        agg.setResultName("distinctTotal");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultMap)) {
            return 0L;
        }
        List<List<Object>> result = resultMap.get(agg.getResultName());
        if (CollectionUtils.isEmpty(result)) {
            return 0L;
        }
        return Long.parseLong(result.get(0).get(0).toString());
    }

    private List<String> getQueryCardInOutOppositeCard(List<String> cards, String caseId, int singleQuota, boolean isIn) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isIn);
        AggregationParams agg = aggregationRequestParamFactory.groupByAndCountField(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, transitCardCount, new Pagination(0, transitCardCount));
        agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD));
        agg.setResultName("oppositeCard");
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

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isIn);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(0, orderChunkSize, direction, property), caseId, BankTransactionRecord.class, query);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 通过查询卡号获取进出记录(不排序) </h2>
     */
    private List<BankTransactionRecord> getInOutRecordViaQueryCards(List<String> cards, String caseId, int singleQuota,
                                                                    boolean isIn, int from, int orderChunkSize) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isIn);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(from, orderChunkSize), caseId, BankTransactionRecord.class, query);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
    }

    /**
     * <h2> 通过查询卡号与对方卡号获取进出记录(排序) </h2>
     * <p>
     * 本方查询卡号与对方卡号都是调单卡号
     */
    private List<BankTransactionRecord> getInOutRecordViaQueryAndOppositeCards(List<String> cards, List<String> oppositeCards, String caseId, int singleQuota,
                                                                               boolean isIn, String property, Sort.Direction direction) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaQueryAndOpposite(cards, oppositeCards, caseId, singleQuota, isIn);
        Page<BankTransactionRecord> recordPage = entranceRepository.findAll(PageRequest.of(0, transitCardCount, direction, property), caseId, BankTransactionRecord.class, query);
        if (null == recordPage || CollectionUtils.isEmpty(recordPage.getContent())) {
            return null;
        }
        return recordPage.getContent();
    }


    /**
     * <h2> 通过查询卡号获取进账/出账的总次数 </h2>
     */
    private Map<String, Integer> getInOutTotalTimesViaQueryCards(List<String> cards, String caseId, int singleQuota, boolean isInFlow) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, singleQuota, isInFlow);
        AggregationParams agg = aggregationRequestParamFactory.groupByAndCountField(FundTacticsAnalysisField.QUERY_CARD, cards.size(), new Pagination(0, cards.size()));
        agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggDocCountMapping(FundTacticsAnalysisField.QUERY_CARD));
        agg.setResultName("getQueryCardInOutTimes");
        Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultMaps) || CollectionUtils.isEmpty(resultMaps.get(agg.getResultName()))) {
            return null;
        }
        List<List<Object>> results = resultMaps.get(agg.getResultName());
        return results.stream().collect(Collectors.toMap(e -> e.get(0).toString(), e -> Integer.parseInt(e.get(1).toString()), (v1, v2) -> v1));
    }

    /**
     * <h2> 通过查询卡号 与 对方卡号 获取进账/出账的总次数 </h2>
     */
    private Map<String, Integer> getInOutTotalTimesViaQueryAndOppositeCards(List<String> cards, List<String> adjustCards, String caseId, int singleQuota, boolean isInFlow) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaQueryAndOpposite(cards, adjustCards, caseId, singleQuota, isInFlow);
        AggregationParams agg = aggregationRequestParamFactory.groupByAndCountField(FundTacticsAnalysisField.QUERY_CARD, cards.size(), new Pagination(0, cards.size()));
        agg.setMapping(aggregationEntityMappingFactory.buildGroupByAggDocCountMapping(FundTacticsAnalysisField.QUERY_CARD));
        agg.setResultName("getQueryCardInOutTimes");
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
     */
    @SuppressWarnings("all")
    private FastInFastOutResult convertFromDataSource(int characteristicRatio, long timeInterval,
                                                      BankTransactionRecord first, BankTransactionRecord second, boolean isInflow) {

        FastInFastOutResult source = new FastInFastOutResult();
        // 入账金额
        double inflowAmount;
        // 按照入账金额排序
        if (isInflow) {
            // 资金来源卡号
            source.setFundSourceCard(first.getTransactionOppositeCard());
            // 资金来源户名
            source.setFundSourceAccountName(first.getTransactionOppositeName());
            // 流入时间日期
            source.setInflowDate(format.format(first.getTradingTime()));
            // 流入金额
            source.setInflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            inflowAmount = first.getChangeAmount();
            // 资金中转卡号
            source.setFundTransitCard(first.getQueryCard());
            // 资金中转户名
            source.setFundTransitAccountName(first.getCustomerName());
            // 流出时间日期
            source.setOutflowDate(format.format(second.getTradingTime()));
            // 流出金额
            source.setOutflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            // 资金沉淀卡号
            source.setFundDepositCard(second.getTransactionOppositeCard());
            // 资金沉淀户名
            source.setFundDepositAccountName(second.getTransactionOppositeName());

            // 时间间隔
            long computeTimeInterval = computeTimeInterval(first.getTradingTime(), second.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        } else {
            // 按照流出金额、流出日期排序
            source.setFundSourceCard(second.getTransactionOppositeCard());
            source.setFundSourceAccountName(second.getTransactionOppositeName());
            source.setInflowDate(format.format(second.getTradingTime()));
            source.setInflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            inflowAmount = second.getChangeAmount();
            source.setFundTransitCard(first.getQueryCard());
            source.setFundTransitAccountName(first.getCustomerName());
            source.setOutflowDate(format.format(first.getTradingTime()));
            source.setOutflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            source.setFundDepositCard(first.getTransactionOppositeCard());
            source.setFundDepositAccountName(first.getTransactionOppositeName());
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(second.getTradingTime(), first.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        }
        // 特征比(入账金额-出账金额) / 入账金额
        int computeFeatureRatio = computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        source.setHashId(FastInFastOutResult.hashString(source).hashCode());
        return source;
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为中转情况
     */
    @SuppressWarnings("all")
    private FastInFastOutResult convertFromDataTransit(int characteristicRatio, long timeInterval,
                                                       BankTransactionRecord first, BankTransactionRecord second, boolean isInflow) {

        FastInFastOutResult transit = new FastInFastOutResult();
        // 入账金额
        double inflowAmount;
        if (isInflow) {
            // 资金来源卡号
            transit.setFundSourceCard(first.getTransactionOppositeCard());
            // 资金来源户名
            transit.setFundSourceAccountName(first.getTransactionOppositeName());
            // 流入时间日期
            transit.setInflowDate(format.format(first.getTradingTime()));
            // 流入金额
            transit.setInflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            inflowAmount = first.getChangeAmount();
            // 资金中转卡号
            transit.setFundTransitCard(first.getQueryCard());
            // 资金中转户名
            transit.setFundTransitAccountName(first.getCustomerName());
            // 流出时间日期
            transit.setOutflowDate(format.format(second.getTradingTime()));
            // 流出金额
            transit.setOutflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            // 资金沉淀卡号
            transit.setFundDepositCard(second.getTransactionOppositeCard());
            // 资金沉淀户名
            transit.setFundDepositAccountName(second.getTransactionOppositeName());
            // 时间间隔 判断
            long computeTimeInterval = computeTimeInterval(first.getTradingTime(), second.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        } else {
            transit.setFundSourceCard(second.getTransactionOppositeCard());
            transit.setFundSourceAccountName(second.getTransactionOppositeName());
            transit.setInflowDate(format.format(second.getTradingTime()));
            transit.setInflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            inflowAmount = second.getChangeAmount();
            transit.setFundTransitCard(second.getQueryCard());
            transit.setFundTransitAccountName(second.getCustomerName());
            transit.setOutflowDate(format.format(first.getTradingTime()));
            transit.setOutflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            transit.setFundDepositCard(first.getTransactionOppositeCard());
            transit.setFundDepositAccountName(first.getTransactionOppositeName());
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(second.getTradingTime(), first.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        }
        // 特征比(入账金额-出账金额) / 入账金额
        int computeFeatureRatio = computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        transit.setHashId(FastInFastOutResult.hashString(transit).hashCode());
        return transit;
    }

    private Integer computeResultHashFromTransit(int characteristicRatio, long timeInterval,
                                                 BankTransactionRecord first, BankTransactionRecord second, boolean isInflow) {
        // 入账金额
        double inflowAmount;
        String value;
        if (isInflow) {
            inflowAmount = first.getChangeAmount();
            // 时间间隔 判断
            long computeTimeInterval = computeTimeInterval(first.getTradingTime(), second.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = first.getTransactionOppositeCard() + first.getTradingTime().getTime() + first.getChangeAmount().toString()
                    + first.getQueryCard() + second.getTradingTime().getTime() + second.getChangeAmount().toString()
                    + second.getTransactionOppositeCard();
        } else {
            inflowAmount = second.getChangeAmount();
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(second.getTradingTime(), first.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = second.getTransactionOppositeCard() + second.getTradingTime().getTime() + second.getChangeAmount().toString()
                    + second.getQueryCard() + first.getTradingTime().getTime() + first.getChangeAmount().toString()
                    + first.getTransactionOppositeCard();
        }
        // 特征比(入账金额-出账金额) / 入账金额
        int computeFeatureRatio = computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // 后续使用雪花算法
        return value.hashCode();
    }

    public Integer computeResultHashFromSource(int characteristicRatio, long timeInterval,
                                               BankTransactionRecord first, BankTransactionRecord second, boolean isInflow) {

        // 入账金额
        double inflowAmount;
        String value;
        if (isInflow) {
            inflowAmount = first.getChangeAmount();
            // 时间间隔 判断
            long computeTimeInterval = computeTimeInterval(first.getTradingTime(), second.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = first.getTransactionOppositeCard() + first.getTradingTime().getTime() + first.getChangeAmount().toString()
                    + first.getQueryCard() + second.getTradingTime().getTime() + second.getChangeAmount().toString()
                    + second.getTransactionOppositeCard();
        } else {
            inflowAmount = second.getChangeAmount();
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(second.getTradingTime(), first.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = second.getTransactionOppositeCard() + second.getTradingTime().getTime() + second.getChangeAmount().toString()
                    + first.getQueryCard() + first.getTradingTime().getTime() + first.getChangeAmount().toString()
                    + first.getTransactionOppositeCard();
        }
        // 特征比(入账金额-出账金额) / 入账金额
        int computeFeatureRatio = computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // 后续使用雪花算法
        return value.hashCode();
    }

    /**
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为沉淀情况
     */
    @SuppressWarnings("all")
    private FastInFastOutResult convertFromDataDeposit(int characteristicRatio, long timeInterval,
                                                       BankTransactionRecord first, BankTransactionRecord second, boolean isInflow) {

        FastInFastOutResult deposit = new FastInFastOutResult();
        double inflowAmount;
        if (isInflow) {
            // 资金来源卡号
            deposit.setFundSourceCard(first.getTransactionOppositeCard());
            // 资金来源户名
            deposit.setFundSourceAccountName(first.getTransactionOppositeName());
            // 流入时间日期
            deposit.setInflowDate(format.format(first.getTradingTime()));
            // 流入金额
            deposit.setInflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            inflowAmount = first.getChangeAmount();
            // 资金中转卡号
            deposit.setFundTransitCard(first.getQueryCard());
            // 资金中转户名
            deposit.setFundTransitAccountName(first.getCustomerName());
            // 流出时间日期
            deposit.setOutflowDate(format.format(second.getTradingTime()));
            // 流出金额
            deposit.setOutflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            // 资金沉淀卡号
            deposit.setFundDepositCard(second.getQueryCard());
            // 资金沉淀户名
            deposit.setFundDepositAccountName(second.getCustomerName());
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(first.getTradingTime(), second.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        } else {
            deposit.setFundSourceCard(second.getTransactionOppositeCard());
            deposit.setFundSourceAccountName(second.getTransactionOppositeName());
            deposit.setInflowDate(format.format(second.getTradingTime()));
            deposit.setInflowAmount(BigDecimalUtil.value(second.getChangeAmount().toString()));
            inflowAmount = second.getChangeAmount();
            deposit.setFundTransitCard(second.getQueryCard());
            deposit.setFundTransitAccountName(second.getCustomerName());
            deposit.setOutflowDate(format.format(first.getTradingTime()));
            deposit.setOutflowAmount(BigDecimalUtil.value(first.getChangeAmount().toString()));
            deposit.setFundDepositCard(first.getQueryCard());
            deposit.setFundDepositAccountName(first.getCustomerName());
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(second.getTradingTime(), first.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
        }
        // 特征比(入账金额-出账金额) / 入账金额
        int computeFeatureRatio = computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        deposit.setHashId(FastInFastOutResult.hashString(deposit).hashCode());
        return deposit;
    }

    public Integer computeResultHashFromDeposit(int characteristicRatio, long timeInterval,
                                                BankTransactionRecord first, BankTransactionRecord second, boolean isInflow) {

        // 入账金额
        double inflowAmount;
        String value;
        if (isInflow) {
            inflowAmount = first.getChangeAmount();
            // 时间间隔 判断
            long computeTimeInterval = computeTimeInterval(first.getTradingTime(), second.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = first.getTransactionOppositeCard() + first.getTradingTime().getTime() + first.getChangeAmount().toString()
                    + first.getQueryCard() + second.getTradingTime().getTime() + second.getChangeAmount().toString()
                    + second.getTransactionOppositeCard();
        } else {
            inflowAmount = second.getChangeAmount();
            // 时间间隔
            long computeTimeInterval = computeTimeInterval(second.getTradingTime(), first.getTradingTime());
            if (computeTimeInterval == -1L || computeTimeInterval > timeInterval) {
                return null;
            }
            value = second.getTransactionOppositeCard() + second.getTradingTime().getTime() + second.getChangeAmount().toString()
                    + second.getQueryCard() + first.getTradingTime().getTime() + first.getChangeAmount().toString()
                    + first.getTransactionOppositeCard();
        }
        // 特征比(入账金额-出账金额) / 入账金额
        int computeFeatureRatio = computeFastInFastOutCharacteristicRatio(first.getChangeAmount(), second.getChangeAmount(), inflowAmount);
        if (computeFeatureRatio > characteristicRatio) {
            return null;
        }
        // 后续使用雪花算法
        return value.hashCode();
    }

    /**
     * <h2> 计算快进快出结果的特征比 </h2>
     */
    private int computeFastInFastOutCharacteristicRatio(double firstChangeAmount, double secondChangeAmount,
                                                        double inflowAmount) {
        BigDecimal sub;
        if (firstChangeAmount < secondChangeAmount) {
            sub = BigDecimalUtil.sub(secondChangeAmount, firstChangeAmount);
        } else {
            sub = BigDecimalUtil.sub(firstChangeAmount, secondChangeAmount);
        }
        BigDecimal div = BigDecimalUtil.divReserveFour(sub.doubleValue(), inflowAmount);
        return BigDecimalUtil.mul(div.doubleValue(), 100).intValue();
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
}
