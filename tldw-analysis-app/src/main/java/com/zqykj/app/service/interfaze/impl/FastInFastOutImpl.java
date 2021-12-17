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
import org.hibernate.validator.constraints.CreditCardNumber;
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

    public ServerResponse fastInFastOutAnalysis(FastInFastOutRequest request) throws ExecutionException, InterruptedException {

        // 来源的

        // 中转的
        if (request.getType() == 0) {
            saveTransit(request);
        }
        // 沉淀的

        // 当快进快出数据统计表完成后, 开始统计
        return ServerResponse.createBySuccess(queryFastInFastOutResult(request));
    }

    /**
     * <h2> 处理并保存调单卡号为中转卡号的快进快出记录 </h2>
     */
    private boolean saveTransit(FastInFastOutRequest request) throws ExecutionException, InterruptedException {
        List<String> adjustCards = request.getCardNum();
        String caseId = request.getCaseId();
        // 根据配置值生成固定数量的快进快出记录
        String tradeAmount = FundTacticsAnalysisField.CHANGE_MONEY;
        String tradingTime = FundTacticsAnalysisField.TRADING_TIME;
        // 流入金额排序记录
        CompletableFuture<List<Map<String, ?>>> inflowAmountRecord = CompletableFuture.supplyAsync(() -> {
            try {
                return generateResultViaTransit(adjustCards, caseId, true, tradeAmount);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                log.error("async get transit fastInout record error!");
            }
            return new ArrayList<>();
        }, ThreadPoolConfig.getExecutor());
        // 流出金额排序记录
        CompletableFuture<List<Map<String, ?>>> outflowAmountRecord = CompletableFuture.supplyAsync(() -> {
            try {
                return generateResultViaTransit(adjustCards, caseId, false, tradeAmount);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                log.error("async get transit fastInout record error!");
            }
            return new ArrayList<>();
        }, ThreadPoolConfig.getExecutor());
        // 流出日期排序记录
        CompletableFuture<List<Map<String, ?>>> outflowDateRecord = CompletableFuture.supplyAsync(() -> {
            try {
                return generateResultViaTransit(adjustCards, caseId, false, tradingTime);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                log.error("async get transit fastInout record error!");
            }
            return new ArrayList<>();
        }, ThreadPoolConfig.getExecutor());
        // 合并数据
        List<Map<String, ?>> maps = inflowAmountRecord.get();
        List<Map<String, ?>> maps1 = outflowAmountRecord.get();
        List<Map<String, ?>> maps2 = outflowDateRecord.get();
        maps.addAll(maps1);
        maps.addAll(maps2);
        // 去重
        maps = maps.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                Comparator.comparing(e -> Integer.parseInt(e.get("id").toString()))
        )), ArrayList::new));
        // 保存数据记录
        return saveFastInOutRecords(maps, caseId);
    }

    @SuppressWarnings("all")
    private boolean saveFastInOutRecords(List<Map<String, ?>> recordMaps, String caseId) {

        // 每次保存2W
        int chunkSize = 20_000;
        int position = 0;
        int size = recordMaps.size();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        while (position < size) {
            int next = Math.min(position + chunkSize, size);
            int finalPosition = position;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> saveFastInout(recordMaps.subList(finalPosition, next), caseId));
            position = next;
            futures.add(future);
        }
        for (CompletableFuture<Boolean> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("batch save fastInout records error!");
                return false;
            }
        }
        return true;
    }

    /**
     * <h2> 处理并保存调单卡号为来源卡号的快进快出记录 </h2>
     */
    private boolean saveSource(FastInFastOutRequest request) {

        List<String> adjustCards = request.getCardNum();
        String caseId = request.getCaseId();
        // 根据配置值生成固定数量的快进快出记录
        String tradeAmount = FundTacticsAnalysisField.CHANGE_MONEY;
        String tradingTime = FundTacticsAnalysisField.TRADING_TIME;
        // 流入金额排序记录

        return true;
    }

    /**
     * <h2> 生成按流入金额快进快出记录 </h2>
     */
    private List<Map<String, ?>> generateResultViaSourceInflow(List<String> adjustCards, String caseId, String property) throws ExecutionException, InterruptedException {

        // 流入 与 流出区别很大,需要分开处理(跟调单卡号作为中转卡号的情况不一样)
        // 降序
        CompletableFuture<List<Map<String, ?>>> inflowDesc = CompletableFuture.supplyAsync(() ->
                saveFastInoutFromSourceInflow(adjustCards, caseId, property, Sort.Direction.DESC), ThreadPoolConfig.getExecutor());
        // 查看这些卡号的进账的数量
        // 如果 count <= orderChunkSize, 那么降序 或者 升序 的数据只需要入一次即可
        long count = getInOutCountViaQueryCards(adjustCards, caseId, false);
        // 升序
        if (count <= orderChunkSize) {
            return inflowDesc.get();
        }
        CompletableFuture<List<Map<String, ?>>> inflowAsc = CompletableFuture.supplyAsync(() ->
                saveFastInoutFromSourceInflow(adjustCards, caseId, property, Sort.Direction.ASC), ThreadPoolConfig.getExecutor());
        List<Map<String, ?>> inflowDescResult = inflowDesc.get();
        List<Map<String, ?>> inflowAscResult = inflowAsc.get();
        // 数据量大的时候addAll 效率好一点
        if (!CollectionUtils.isEmpty(inflowDescResult)) {
            inflowDescResult.addAll(inflowAscResult);
            return inflowDescResult;
        } else if (!CollectionUtils.isEmpty(inflowAscResult)) {
            inflowAscResult.addAll(inflowDescResult);
            return inflowAscResult;
        }
        return null;
    }

    /**
     * <h2> 生成按流出金额 / 流出日期 快进快出记录 </h2>
     * <p>
     * 来源 - 中转 - 沉淀 (按照流出金额/流出日期排序,这种属于 2 跳排序, 实际上是拿来源卡号筛选出来的 中转卡号 去 查出账然后排序,
     * 关键问题在于 中转卡号很大的时候无法一次性取出,这就导致没办法进行流出金额与流出日期排序)
     */
    private List<Map<String, ?>> generateResultViaSourceOutflow(List<String> adjustCards, String caseId, String property) {


        // 查询调单卡号出账的(对方卡号去重后的数量)


        // 方式一
        // 当然你这里可以设置一个阈值, 若中转卡号的数量 < 阈值,可以一次性查询出来,拿着它作为查询卡号继续筛选出账记录就可以直接排序


        // 方式二
        // 获取调单卡号的出账记录(条件是对方卡号也是调单的),按排序字段 取 orderChunkSize 数量的记录(只需要带出对方卡号、交易金额、交易日期即可)
        // 获取调单卡号的出账记录(条件是对方卡号不是调单的),按排序字段 取 orderChunkSize 数量的记录(只需要带出对方卡号、交易金额、交易日期即可)
        // 2种卡号合并,根据排序字段,再取 前 orderChunkSize 数量的记录即可(这时候就获取到了来源情况下, 流出金额/流出日期排序的内容)

        // 降序


        // 升序
        return null;
    }


    /**
     * <h2> 生成按流入金额/流出金额/流出日期 排序的快进快出记录 </h2>
     */
    private List<Map<String, ?>> generateResultViaTransit(List<String> adjustCards, String caseId, boolean isInflow, String property) throws ExecutionException, InterruptedException {

        // 降序
        CompletableFuture<List<Map<String, ?>>> transitDescFuture = CompletableFuture.supplyAsync(() ->
                saveFastInOutFromTransit(adjustCards, caseId, isInflow, property, Sort.Direction.DESC), ThreadPoolConfig.getExecutor());
        // 查看这些卡号的进账的数量
        // 如果 count <= orderChunkSize, 那么降序 或者 升序 的数据只需要入一次即可
        long count = getInOutCountViaQueryCards(adjustCards, caseId, isInflow);
        if (count <= orderChunkSize) {
            return transitDescFuture.get();
        }
        // 升序
        CompletableFuture<List<Map<String, ?>>> transitAscFuture = CompletableFuture.supplyAsync(() ->
                saveFastInOutFromTransit(adjustCards, caseId, isInflow, property, Sort.Direction.ASC), ThreadPoolConfig.getExecutor());
        List<Map<String, ?>> transitDescMapResult = transitDescFuture.get();
        List<Map<String, ?>> transitAscMapResult = transitAscFuture.get();
        // 数据量大的时候addAll 效率好一点
        if (!CollectionUtils.isEmpty(transitDescMapResult)) {
            transitDescMapResult.addAll(transitAscMapResult);
            return transitDescMapResult;
        } else if (!CollectionUtils.isEmpty(transitAscMapResult)) {
            transitAscMapResult.addAll(transitDescMapResult);
            return transitAscMapResult;
        }
        return null;
    }

    /**
     * <h2> 调单卡号作为来源卡号情况(流出金额 / 流出日期) 快进快出记录 </h2>
     */
    private List<Map<String, ?>> saveFastInoutFromSourceOutflow(List<String> adjustCards, String caseId, String sortProperty, Sort.Direction direction) {

        return null;
    }

    /**
     * <h2> 调单卡号作为来源卡号情况(流入金额) 快进快出记录 </h2>
     */
    private List<Map<String, ?>> saveFastInoutFromSourceInflow(List<String> adjustCards, String caseId, String sortProperty, Sort.Direction direction) {

        // 获取调单卡号的出账
        List<BankTransactionRecord> outRecord = getInOutRecordOrderViaQueryCards(adjustCards, caseId, false, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(outRecord)) {
            return null;
        }
        // 去重 outRecord 中的对方卡号,查询这些卡号的出账记录
        List<String> distinctOppositeCard = outRecord.stream().map(BankTransactionRecord::getTransactionOppositeCard).distinct().collect(Collectors.toList());
        // 获取这些卡号的出账次数
        Map<String, Integer> oppositeCardOutTimes = getInOutTotalTimesViaQueryCards(distinctOppositeCard, caseId, false);
        // 取出满足 resultChunkSize 数量的对方卡号( 即 中转 - 沉淀的数据)
        if (CollectionUtils.isEmpty(oppositeCardOutTimes)) {
            return null;
        }
        int computeTotal = 0;
        Map<String, Integer> requireQueryOutCards = new HashMap<>();
        List<BankTransactionRecord> sourceToTransit = new ArrayList<>();
        for (BankTransactionRecord record : outRecord) {
            if (computeTotal >= resultChunkSize) {
                break;
            }
            Integer outTimes = oppositeCardOutTimes.get(record.getTransactionOppositeCard());
            if (outTimes > 0) {
                sourceToTransit.add(record);
                computeTotal += outTimes;
                if (!requireQueryOutCards.containsKey(record.getTransactionOppositeCard())) {
                    // 检查需要查询的卡号
                    requireQueryOutCards.put(record.getTransactionOppositeCard(), 0);
                }
            }
        }
        // 需要查询的卡号
        List<String> queryCards = new ArrayList<>(requireQueryOutCards.keySet());
        List<BankTransactionRecord> transitToDeposit = asyncQueryInOutRecord(computeTotal, queryCards, caseId, true);
        if (CollectionUtils.isEmpty(transitToDeposit)) {
            return null;
        }
        // 生成快进快出记录(调单作为中转卡号-流入金额/流出金额/流出日期 降序/升序情况)
        return generateFastInOutFromSource(sourceToTransit, transitToDeposit, caseId, true);
    }

    /**
     * <h2> 调单卡号作为中转卡号情况 快进快出记录 </h2>
     */
    private List<Map<String, ?>> saveFastInOutFromTransit(List<String> adjustCards, String caseId, boolean isInflow, String sortProperty, Sort.Direction direction) {

        // 流入金额/流出金额/流出日期 降序排序生成快进快出记录
        List<BankTransactionRecord> inOutflowOrder = getInOutRecordOrderViaQueryCards(adjustCards, caseId, isInflow, sortProperty, direction, orderChunkSize);
        if (CollectionUtils.isEmpty(inOutflowOrder)) {
            return null;
        }
        // 去重 inOutflowOrder 中的查询卡号,查询这些卡号的进账/出账记录数
        List<String> distinctQueryCard = inOutflowOrder.stream().map(BankTransactionRecord::getQueryCard).distinct().collect(Collectors.toList());
        Map<String, Integer> queryCardInOutTimes = getInOutTotalTimesViaQueryCards(distinctQueryCard, caseId, !isInflow);
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
            Integer outTimes = queryCardInOutTimes.get(record.getQueryCard());
            if (outTimes > 0) {
                sourceToTransit.add(record);
                computeTotal += outTimes;
                if (!requireQueryInOutCards.containsKey(record.getQueryCard())) {
                    // 检查需要查询的卡号
                    requireQueryInOutCards.put(record.getQueryCard(), 0);
                }
            }
        }
        // 需要查询的卡号
        List<String> queryCards = new ArrayList<>(requireQueryInOutCards.keySet());
        List<BankTransactionRecord> transitToDeposit = asyncQueryInOutRecord(computeTotal, queryCards, caseId, isInflow);
        if (CollectionUtils.isEmpty(transitToDeposit)) {
            return null;
        }
        // 生成快进快出记录(调单作为中转卡号-流入金额/流出金额/流出日期 降序/升序情况)
        return generateFastInOutFromTransit(sourceToTransit, transitToDeposit, caseId, isInflow);
    }

    /**
     * <h2> 批量查询卡号的进/出记录 </h2>
     */
    @SuppressWarnings("all")
    private List<BankTransactionRecord> asyncQueryInOutRecord(int computeTotal, List<String> requireQueryInOutCards, String caseId, boolean isInflow) {

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
                    getInOutRecordViaQueryCards(requireQueryInOutCards, caseId, !isInflow, finalPage, next - finalPosition));
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
     * <h2> 生成快进快出记录 入统计表 {@link FastInFastOutRecord} </h2>
     * <p>
     * 调单卡号作为中转卡号的情况
     *
     * @return 返回一条快进快出记录的hash值
     */
    private List<Map<String, ?>> generateFastInOutFromTransit(List<BankTransactionRecord> first, List<BankTransactionRecord> second,
                                                              String caseId, boolean isInflow) {

        // 将second 转成map,避免 n * n, 改成 2 * n
        Map<String, List<BankTransactionRecord>> maps = getBankTransactionRecordMap(second);
        // 快进快出记录
        List<Map<String, ?>> fastInFastOutRecords = new ArrayList<>();
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
                }
            }
        }
        return fastInFastOutRecords;
    }

    private List<Map<String, ?>> generateFastInOutFromSource(List<BankTransactionRecord> first, List<BankTransactionRecord> second,
                                                             String caseId, boolean isInflow) {
        Map<String, List<BankTransactionRecord>> maps = getBankTransactionRecordMap(second);

        // 快进快出记录
        List<Map<String, ?>> fastInFastOutRecords = new ArrayList<>();
        // 快进快出每一条记录的hash值
        for (BankTransactionRecord orderRecord : first) {

            List<BankTransactionRecord> otherRecords = maps.get(orderRecord.getTransactionOppositeCard());
            if (CollectionUtils.isEmpty(otherRecords)) {
                continue;
            }
            for (BankTransactionRecord otherRecord : otherRecords) {
                Map<String, Object> fastInFastOutRecord = convertFromDataSource(orderRecord, otherRecord, caseId, isInflow);
                if (!CollectionUtils.isEmpty(fastInFastOutRecord)) {
                    fastInFastOutRecords.add(fastInFastOutRecord);
                }
            }
        }
        return fastInFastOutRecords;
    }


    /**
     * <h2> 通过查询卡号获取进出的总次数 </h2>
     */
    private long getInOutCountViaQueryCards(List<String> cards, String caseId, boolean isIn) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, isIn);
        return entranceRepository.count(caseId, BankTransactionRecord.class, query);
    }

    /**
     * <h2> 获取查询卡号进出的对方卡号去重数量 与 卡号记录 </h2>
     */
    private Map<String, Object> getInOutDistinctCardCount(List<String> cards, String caseId, boolean isIn) {

        QuerySpecialParams query = queryRequestParamFactory.getInoutRecordsViaAdjustCards(cards, caseId, isIn);
        AggregationParams agg = aggregationRequestParamFactory.getCardGroupByAndDistinct(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);

        return null;
    }

    /**
     * <h2> 通过查询卡号获取进出记录(根据排序) </h2>
     */
    private List<BankTransactionRecord> getInOutRecordOrderViaQueryCards(List<String> cards, String caseId,
                                                                         boolean isIn, String property, Sort.Direction direction, int orderChunkSize) {

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
    private List<BankTransactionRecord> getInOutRecordViaQueryCards(List<String> cards, String caseId,
                                                                    boolean isIn, int from, int orderChunkSize) {

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
    private Map<String, Integer> getInOutTotalTimesViaQueryCards(List<String> cards, String caseId,
                                                                 boolean isInFlow) {

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
     * <h2> 生成一条快进快出记录 </h2>
     * <p>
     * 适用于调单卡号作为来源情况
     */
    @SuppressWarnings("all")
    private Map<String, Object> convertFromDataSource(BankTransactionRecord first, BankTransactionRecord
            second, String caseId, boolean isInflow) {

        Map<String, Object> map = new HashMap<>();
        // 入账金额
        double inflowAmount;
        // 按照入账金额排序
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
            // 按照流出金额、流出日期排序
            map.put("source_card", second.getTransactionOppositeCard());
            map.put("source_account_name", second.getTransactionOppositeName());
            map.put("inflow_date", second.getTradingTime());
            map.put("inflow_amount", BigDecimalUtil.value(second.getChangeAmount().toString()));
            inflowAmount = second.getChangeAmount();
            map.put("transit_card", first.getQueryCard());
            map.put("transit_account_name", first.getCustomerName());
            map.put("outflow_date", first.getTradingTime());
            map.put("outflow_amount", first.getChangeAmount());
            map.put("deposit_card", first.getTransactionOppositeCard());
            map.put("deposit_account_name", first.getTransactionOppositeName());
            // 时间间隔
            long timeInterval = computeTimeInterval(first.getTradingTime(), second.getTradingTime());
            if (timeInterval == -1L) {
                return null;
            }
            map.put("time_interval", timeInterval);
            // 调单卡号
            map.put("adjust_card", second.getTransactionOppositeCard());
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
     * 适用于调单卡号作为中转情况
     */
    @SuppressWarnings("all")
    private Map<String, Object> convertFromDataTransit(BankTransactionRecord first, BankTransactionRecord
            second, String caseId, boolean isInflow) {

        Map<String, Object> map = new HashMap<>();
        // 入账金额
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
    private FastInFastOutRecord convertFromDataDeposit(BankTransactionRecord first, BankTransactionRecord
            second, String sortType) {

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

    /**
     * <h2> 检查满足特征比与时间间隔的快进快出结果 </h2>
     */
    private boolean checkTimeIntervalAndFeatureRatio(String sortType, int characteristicRatio,
                                                     long timeInterval, BankTransactionRecord first, BankTransactionRecord second) {
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
    private boolean checkTimeInterval(boolean checkFeatureRatio, long timeInterval, Date inflowDate, Date
            outflowDate) {
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

    private Map<String, List<BankTransactionRecord>> getBankTransactionRecordMap(List<BankTransactionRecord> second) {
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
        return maps;
    }

    /**
     * <h2> 保存快进快出记录到 FastInFastOutRecord </h2>
     */
    private boolean saveFastInout(List<Map<String, ?>> fastInoutRecords, String caseId) {
        entranceRepository.saveAll(fastInoutRecords, caseId, FastInFastOutRecord.class);
        return true;
    }
}
