/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HashUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.config.ThreadPoolConfig;
import com.zqykj.app.service.factory.param.agg.UnadjustedAccountAggParamFactory;
import com.zqykj.app.service.factory.param.query.UnadjustedAccountQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IUnadjustedAccountsAnalysis;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.bank.SuggestAdjusted;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * <h1> 未调单账户分析 </h1>
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UnadjustedAccountsAnalysisImpl extends FundTacticsCommonImpl implements IUnadjustedAccountsAnalysis {

    private final UnadjustedAccountQueryParamFactory unadjustedAccountQueryParamFactory;
    private final UnadjustedAccountAggParamFactory unadjustedAccountAggParamFactory;
    private final InitFeatureRatioHandleChain handleChain;

    private final static String SOURCE_LOGO = "来源";
    private final static String TRANSIT_LOGO = "中转";
    private final static String DEPOSIT_LOGO = "沉淀";
    private final static String OTHER_LOGO = "其他";

    @Override
    public ServerResponse<FundAnalysisResultResponse<UnadjustedAccountAnalysisResult>> unAdjustedAnalysis(UnadjustedAccountAnalysisRequest request) throws Exception {

        request.setGroupInitSize(initGroupSize);
        // 查询调单卡号(最大8000个)
        if (checkAdjustCardCountByDate(request.getCaseId(), FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()))) {
            //
            List<String> adjustCards = queryMaxAdjustCardsByDate(request.getCaseId(), FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()));
            // 查询排除这些调单的未调单卡号数据分析结果
            if (!CollectionUtils.isEmpty(adjustCards)) {
                // 结果 与 总量同时查询
                com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
                int from = com.zqykj.common.vo.PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize());
                CompletableFuture<List<UnadjustedAccountAnalysisResult>> resultFuture = CompletableFuture.supplyAsync(() -> getAnalysisResult(request, from, pageRequest.getPageSize(), adjustCards),
                        ThreadPoolConfig.getExecutor());
                CompletableFuture<Long> totalFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return asyncComputeTotal(request, adjustCards);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        log.error("get unadjusted analysis result total error!");
                        return -1L;
                    }
                }, ThreadPoolConfig.getExecutor());
                // 获取
                List<UnadjustedAccountAnalysisResult> analysisResult = resultFuture.get();
                Long total = totalFuture.get();
                if (total == -1L) {
                    return ServerResponse.createByErrorMessage("get unadjusted analysis result total error!");
                }
                if (!CollectionUtils.isEmpty(analysisResult)) {
                    FundAnalysisResultResponse<UnadjustedAccountAnalysisResult> resultResponse = new FundAnalysisResultResponse<>();
                    resultResponse.setTotal(total);
                    resultResponse.setSize(request.getPageRequest().getPageSize());
                    resultResponse.setTotalPages(PageRequest.getTotalPages(total, request.getPageRequest().getPageSize()));
                    resultResponse.setContent(analysisResult);
                    return ServerResponse.createBySuccess(resultResponse);
                }
            }
        }
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
    }

    @Override
    public ServerResponse<FundAnalysisResultResponse<SuggestAdjustedAccountResult>> suggestAdjustedAccounts(FundTacticsPartGeneralRequest request) {

        QuerySpecialParams query = unadjustedAccountQueryParamFactory.querySuggestAdjustAccount(request);
        com.zqykj.common.vo.PageRequest pageRequest = request.getPageRequest();
        SortRequest sortRequest = request.getSortRequest();
        Page<SuggestAdjusted> page = entranceRepository.findAll(PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize(),
                Sort.Direction.valueOf(sortRequest.getOrder().name()), sortRequest.getProperty()), request.getCaseId(), SuggestAdjusted.class, query);
        if (null == page) {
            return ServerResponse.createBySuccess(FundAnalysisResultResponse.empty());
        }
        List<SuggestAdjusted> content = page.getContent();
        List<SuggestAdjustedAccountResult> results = convertFromSuggestAdjusted(content);
        return ServerResponse.createBySuccess(FundAnalysisResultResponse.build(results, page.getTotalElements(), page.getSize()));
    }

    @Override
    public ServerResponse<String> deleteSuggestAdjusted(FundTacticsPartGeneralRequest request) {

        try {
            entranceRepository.deleteAll(request.getIds(), request.getCaseId(), SuggestAdjusted.class);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("刪除建议调单账号失败!");
            return ServerResponse.createByErrorMessage("删除失败!");
        }
        return ServerResponse.createBySuccess("刪除成功!");
    }

    public ServerResponse<String> suggestAdjustedAccountManualSave(SuggestAdjustedAccountAddRequest request) {
        try {
            handleSuggestAdjustedAccountRequestContent(request);
            entranceRepository.saveAll(request.getSuggestAdjusted(), request.getCaseId(), SuggestAdjusted.class);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("添加建议调单账号失败!");
            return ServerResponse.createByErrorMessage("添加建议调单账号失败!");
        }
        return ServerResponse.createBySuccess("添加建议调单账号成功!");
    }

    /**
     * <h2> 添加建议调单账号(自动保存) </h2>
     */
    @SuppressWarnings("all")
    public ServerResponse<String> suggestAdjustedAccountAutoSave(UnadjustedAccountAnalysisRequest request) throws Exception {

        // 首先获取的是数据总量
        // 查询结果(分批量保存,每次保存数量根据阈值处理)
        request.setGroupInitSize(initGroupSize);
        // 查询调单卡号(最大8000个)
        if (checkAdjustCardCountByDate(request.getCaseId(), FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()))) {
            //
            List<String> adjustCards = queryMaxAdjustCardsByDate(request.getCaseId(), FundTacticsPartGeneralRequest.getDateRange(request.getDateRange()));
            if (CollectionUtils.isEmpty(adjustCards)) {
                return ServerResponse.createByErrorMessage("案件数据不存在!");
            }
            long total = asyncComputeTotal(request, adjustCards);
            int size;
            if (request.getTopRange() != null) {
                size = request.getTopRange();
            } else if (request.getPercentageOfAccountNumber() != null) {
                size = BigDecimalUtil.mul(String.valueOf(total), BigDecimalUtil.div(request.getPercentageOfAccountNumber(), 100).toString()).intValue();
            } else {
                return ServerResponse.createByErrorMessage("请设置选择范围!");
            }
            // 查询排除这些调单的未调单卡号数据分析结果
            List<Future<Boolean>> futures = new ArrayList<>();
            int position = 0;
            while (position < size) {
                int next = Math.min(position + queryCardSize, size);
                int finalPosition = position;
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    List<UnadjustedAccountAnalysisResult> analysisResults = getAnalysisResult(request, finalPosition, next - finalPosition, adjustCards);
                    if (!CollectionUtils.isEmpty(analysisResults)) {
                        return saveSuggestAdjustedAccountOfAuto(request.getCaseId(), analysisResults);
                    }
                    return false;
                }, ThreadPoolConfig.getExecutor());
                position = next;
                futures.add(future);
            }
            for (Future<Boolean> future : futures) {
                future.get();
            }
            return ServerResponse.createBySuccess("自动保存成功!");
        } else {
            // TODO 调单数量大于阈值的时候,数据量会很大,处理时间会很长(暂时不处理)
            return ServerResponse.createByErrorMessage("数据太大,暂不支持处理");
        }
    }

    /**
     * <h2> 保存自动保存的建议调单账号 </h2>
     */
    private boolean saveSuggestAdjustedAccountOfAuto(String caseId, List<UnadjustedAccountAnalysisResult> results) {

        List<Map<String, ?>> maps = new ArrayList<>();
        results.forEach(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", HashUtil.fnvHash(caseId + "_" + result.getOppositeCard()));
            map.put("case_id", caseId);
            map.put("opposite_card", result.getOppositeCard());
            map.put("account_name", result.getAccountName());
            map.put("bank", result.getBank());
            map.put("linked_accounts_number", result.getNumberOfLinkedAccounts());
            map.put("trade_total_times", result.getTradeTotalTimes());
            map.put("trade_total_amount", result.getTradeTotalAmount());
            map.put("credits_total_amount", result.getCreditsTotalAmount());
            map.put("payout_total_amount", result.getPayoutTotalAmount());
            map.put("trade_net", result.getTradeNet());
            map.put("account_feature", result.getAccountFeature());
            map.put("add_type", 2);
            // TODO 这里需要动态获取
            map.put("add_account", "MCJ");
            map.put("add_datetime", new Date());
            maps.add(map);
        });
        try {
            entranceRepository.saveAll(maps, caseId, SuggestAdjusted.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("suggest adjusted account auto save error!");
            throw e;
        }
    }


    private void handleSuggestAdjustedAccountRequestContent(SuggestAdjustedAccountAddRequest request) {

        List<SuggestAdjusted> suggestAdjusted = request.getSuggestAdjusted();
        for (SuggestAdjusted adjusted : suggestAdjusted) {
            // TODO 获取当前session持有的用户(账号)
            adjusted.setAddAccount("");
            adjusted.setAddDateTime(new Date());
            adjusted.setCaseId(request.getCaseId());
            adjusted.setId(HashUtil.fnvHash(request.getCaseId() + "_" + adjusted.getOppositeCard()));
        }
    }

    private List<SuggestAdjustedAccountResult> convertFromSuggestAdjusted(List<SuggestAdjusted> suggestAdjusted) {

        List<SuggestAdjustedAccountResult> results = new ArrayList<>();
        suggestAdjusted.forEach(e -> {
            SuggestAdjustedAccountResult result = new SuggestAdjustedAccountResult();
            BeanUtil.copyProperties(e, result);
            result.setCreditsTotalAmount(BigDecimalUtil.value(e.getCreditsTotalAmount()));
            result.setPayoutTotalAmount(BigDecimalUtil.value(e.getPayoutTotalAmount()));
            result.setTradeTotalAmount(BigDecimalUtil.value(e.getTradeTotalAmount()));
            result.setTradeNet(BigDecimalUtil.value(e.getTradeNet()));
            results.add(result);
        });
        return results;
    }

    /**
     * <h2> 查询排除这些调单的未调单卡号数据分析结果 </h2>
     */
    private List<UnadjustedAccountAnalysisResult> getAnalysisResult(UnadjustedAccountAnalysisRequest request,
                                                                    int from, int size, List<String> adjustCards) {

        DateRange dateRange = FundTacticsPartGeneralRequest.getDateRange(request.getDateRange());
        QuerySpecialParams queryUnadjusted = unadjustedAccountQueryParamFactory.queryUnadjusted(request.getCaseId(), adjustCards, request.getKeyword(), dateRange);
        AggregationParams aggUnadjusted = unadjustedAccountAggParamFactory.unadjustedAccountAnalysis(request, from, size, request.getGroupInitSize());
        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityKeyMapping = new LinkedHashMap<>();
        entityMappingFactory.buildTradeAnalysisResultAggMapping(aggKeyMapping, entityKeyMapping, UnadjustedAccountAnalysisResult.class);
        aggUnadjusted.setResultName("unadjustedAccountAnalysis");
        aggUnadjusted.setMapping(aggKeyMapping);
        // 获取总计分析结果与总量
        List<UnadjustedAccountAnalysisResult> unadjustedAccountAnalysisResults = getUnadjustedAccountAnalysisResults(request, queryUnadjusted, aggUnadjusted, entityKeyMapping);
        if (unadjustedAccountAnalysisResults == null) return null;
        List<String> queryCards = unadjustedAccountAnalysisResults.stream().map(UnadjustedAccountAnalysisResult::getOppositeCard).collect(Collectors.toList());
        // 查询这些卡号的账户名称、开户行
        Map<String, UnadjustedAccountAnalysisResult> aggShowField = getAggShowFieldAndLinkedAccount(request, queryCards);
        if (CollectionUtils.isEmpty(aggShowField)) {
            return null;
        }
        for (UnadjustedAccountAnalysisResult result : unadjustedAccountAnalysisResults) {
            // 将金额保留2位小数,转化科学计算方式的金额
            UnadjustedAccountAnalysisResult.amountReservedTwo(result);
            // 处理特征比
            handleChain.doHandle(result, request.getRatioValue(), 1);
            // 填充账户开户名称、对方开户行、账户关联数
            UnadjustedAccountAnalysisResult partAnalysisResult = aggShowField.get(result.getOppositeCard());
            result.setAccountName(partAnalysisResult.getAccountName());
            result.setBank(partAnalysisResult.getBank());
            result.setNumberOfLinkedAccounts(partAnalysisResult.getNumberOfLinkedAccounts());
        }
        return unadjustedAccountAnalysisResults;
    }

    /**
     * <h2> 获取这些查询卡号的账户名称、开户行,关联账户数 </h2>
     */
    private Map<String, UnadjustedAccountAnalysisResult> getAggShowFieldAndLinkedAccount(UnadjustedAccountAnalysisRequest request, List<String> queryCards) {

        QuerySpecialParams query = unadjustedAccountQueryParamFactory.queryUnadjustedExtraInfo(request.getCaseId(), queryCards);
        String[] showFields = new String[]{FundTacticsAnalysisField.CUSTOMER_NAME, FundTacticsAnalysisField.BANK};
        AggregationParams agg = unadjustedAccountAggParamFactory.unadjustedAccountAnalysisSecondQuery(request, queryCards.size(), showFields);
        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityKeyMapping = new LinkedHashMap<>();
        entityMappingFactory.buildUnadjustedAccountAnalysisAggMapping(aggKeyMapping, entityKeyMapping);
        agg.setMapping(aggKeyMapping);
        agg.setResultName("showFieldsAndLinedAccount");
        // 获取总计分析结果
        List<UnadjustedAccountAnalysisResult> unadjustedAccountAnalysisResults = getUnadjustedAccountAnalysisResults(request, query, agg, entityKeyMapping);
        if (unadjustedAccountAnalysisResults == null) return null;
        return unadjustedAccountAnalysisResults.stream().collect(Collectors.toMap(UnadjustedAccountAnalysisResult::getOppositeCard, e -> e, (v1, v2) -> v1));
    }

    /**
     * <h2> 构建交易汇聚分析结果查询计算总数据量 聚合请求 </h2>
     */
    private long total(UnadjustedAccountAnalysisRequest request, List<String> queryCards) {

        QuerySpecialParams query = queryRequestParamFactory.filterMainCards(request.getCaseId(), queryCards);
        AggregationParams totalAgg = unadjustedAccountAggParamFactory.computeTotal(request, queryCards.size());
        totalAgg.setMapping(entityMappingFactory.buildSingleAggKeyMapping("total", "valueAsString"));
        totalAgg.setResultName("total");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, totalAgg, BankTransactionRecord.class, request.getCaseId());
        if (CollectionUtils.isEmpty(resultMap) || CollectionUtils.isEmpty(resultMap.get(totalAgg.getResultName()))) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(Double.valueOf(resultMap.get(totalAgg.getResultName()).get(0).get(0).toString()).intValue()));
    }

    /**
     * <h2> 获取未调单卡号总数量 </h2>
     */
    private int getUnadjustedCardCount(UnadjustedAccountAnalysisRequest request, List<String> adjustCards) {

        DateRange dateRange = FundTacticsPartGeneralRequest.getDateRange(request.getDateRange());
        QuerySpecialParams queryUnadjusted = unadjustedAccountQueryParamFactory.queryUnadjusted(request.getCaseId(), adjustCards, request.getKeyword(), dateRange);
        AggregationParams unadjustedCountAgg = aggParamFactory.buildDistinctViaField(FundTacticsAnalysisField.QUERY_CARD);
        unadjustedCountAgg.setMapping(entityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        unadjustedCountAgg.setResultName("unadjustedCount");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(queryUnadjusted, unadjustedCountAgg, BankTransactionRecord.class, request.getCaseId());
        if (CollectionUtils.isEmpty(resultMap) || CollectionUtils.isEmpty(resultMap.get(unadjustedCountAgg.getResultName()))) {
            return 0;
        }
        return Integer.parseInt(resultMap.get(unadjustedCountAgg.getResultName()).get(0).get(0).toString());
    }

    /**
     * <h2> 异步计算总数据量 </h2>
     * <p>
     * 查询固定数据量的符合条件的未调单卡号,然后计算这些调单卡号是否满足用户设置的特征比(目的降低groupBy的数量,从而加快查询速度)
     */
    private long asyncComputeTotal(UnadjustedAccountAnalysisRequest request, List<String> adjustCards) throws
            ExecutionException, InterruptedException {

        long total = 0L;
        int unadjustedCardCount = getUnadjustedCardCount(request, adjustCards);
        int size = maxAdjustCardQueryCount;
        if (unadjustedCardCount < maxUnadjustedCardQueryCount) {
            size = unadjustedCardCount;
        }
        DateRange dateRange = FundTacticsPartGeneralRequest.getDateRange(request.getDateRange());
        int position = 0;
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        while (position < size) {
            int next = Math.min(position + queryCardSize, size);
            int finalPosition = position;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                List<String> queryCards = batchGetQueryCards(request.getCaseId(), adjustCards, request.getKeyword(), dateRange, finalPosition, next - finalPosition);
                if (CollectionUtils.isEmpty(queryCards)) {
                    return 0L;
                }
                return total(request, queryCards);
            }, ThreadPoolConfig.getExecutor());
            futures.add(future);
            position = next;
        }
        for (CompletableFuture<Long> future : futures) {
            Long curTotal = future.get();
            total += curTotal;
        }
        return total;
    }

    /**
     * <h2> 批量获取查询卡号 </h2>
     */
    private List<String> batchGetQueryCards(String caseId, List<String> adjustCards, String keyword, DateRange dateRange, int from, int size) {

        QuerySpecialParams query = unadjustedAccountQueryParamFactory.queryUnadjusted(caseId, adjustCards, keyword, dateRange);
        AggregationParams agg = aggParamFactory.groupByField(FundTacticsAnalysisField.QUERY_CARD, initGroupSize, new Pagination(from, size));
        agg.setMapping(entityMappingFactory.buildGroupByAggMapping(FundTacticsAnalysisField.QUERY_CARD));
        agg.setResultName("getQueryCard");
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultMap) || CollectionUtils.isEmpty(resultMap.get(agg.getResultName()))) {
            return null;
        }
        return resultMap.get(agg.getResultName()).stream().map(e -> e.get(0).toString()).collect(Collectors.toList());
    }

    /**
     * <h2> 获取映射UnadjustedAccountAnalysisResult 结果 </h2>
     */
    @Nullable
    private List<UnadjustedAccountAnalysisResult> getUnadjustedAccountAnalysisResults(UnadjustedAccountAnalysisRequest request, QuerySpecialParams query,
                                                                                      AggregationParams agg, Map<String, String> entityKeyMapping) {
        Map<String, List<List<Object>>> resultMaps = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, request.getCaseId());
        if (CollectionUtils.isEmpty(resultMaps) || CollectionUtils.isEmpty(resultMaps.get(agg.getResultName()))) {
            return null;
        }
        List<List<Object>> results = resultMaps.get(agg.getResultName());
        List<String> titles = new ArrayList<>(entityKeyMapping.keySet());
        List<Map<String, Object>> keyValueMapping = parseFactory.convertEntity(results, titles, UnadjustedAccountAnalysisResult.class);
        List<UnadjustedAccountAnalysisResult> unadjustedAccountAnalysisResults = JacksonUtils.parse(JacksonUtils.toJson(keyValueMapping), new TypeReference<List<UnadjustedAccountAnalysisResult>>() {
        });
        if (CollectionUtils.isEmpty(unadjustedAccountAnalysisResults)) {
            return null;
        }
        return unadjustedAccountAnalysisResults;
    }

    @Configuration
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class InitFeatureRatioHandleChain {

        private final List<FeatureRatioHandleAbstract> featureRatioHandleAbstracts;

        @PostConstruct
        private void initFeatureRatioHandleChain() {
            // 排序(其实也可以不排序,没有先后顺序强制,只要每个特征比处理器处理了即可)
            featureRatioHandleAbstracts.sort(AnnotationAwareOrderComparator.INSTANCE);
            int size = featureRatioHandleAbstracts.size();
            for (int i = 0; i < size; i++) {
                if (i == size - 1) {
                    featureRatioHandleAbstracts.get(i).setNext(null);
                } else {
                    featureRatioHandleAbstracts.get(i).setNext(featureRatioHandleAbstracts.get(i + 1));
                }
            }
        }

        /**
         * <h2>  根据指定的位置,执行责任链,依次处理 </h2>
         */
        public void doHandle(UnadjustedAccountAnalysisResult result, UnadjustedAccountAnalysisRequest.FeatureRatioValue ratioValue, int index) {
            featureRatioHandleAbstracts.get(index - 1).handle(result, ratioValue);
        }
    }

    /**
     * <h1> 特征比设置处理抽象类 </h1>
     */
    static abstract class FeatureRatioHandleAbstract {

        private FeatureRatioHandleAbstract next;

        public void setNext(FeatureRatioHandleAbstract next) {
            this.next = next;
        }

        public FeatureRatioHandleAbstract getNext() {
            return next;
        }

        public void handle(UnadjustedAccountAnalysisResult result, UnadjustedAccountAnalysisRequest.FeatureRatioValue ratioValue) {
            handlePattern(result, ratioValue);
            FeatureRatioHandleAbstract next = getNext();
            while (null != next) {
                next.handlePattern(result, ratioValue);
                next = next.getNext();
            }
        }

        /**
         * <h2> 特征比处理规则 </h2>
         */
        public abstract void handlePattern(UnadjustedAccountAnalysisResult result, UnadjustedAccountAnalysisRequest.FeatureRatioValue ratioValue);
    }

    /**
     * <h2> 来源特征比设置处理类 </h2>
     */
    @Order(1)
    @Component
    static class SourceFeatureRatioHandle extends FeatureRatioHandleAbstract {

        @Override
        public void handlePattern(UnadjustedAccountAnalysisResult result, UnadjustedAccountAnalysisRequest.FeatureRatioValue ratioValue) {

            if (result.getSourceRatio() >= ratioValue.getSourceRatio()) {
                setAccountFeature(result, SOURCE_LOGO);
            }
        }
    }

    /**
     * <h2> 中转特征比设置处理类 </h2>
     */
    @Order(2)
    @Component
    static class TransitFeatureRatioHandle extends FeatureRatioHandleAbstract {

        @Override
        public void handlePattern(UnadjustedAccountAnalysisResult result, UnadjustedAccountAnalysisRequest.FeatureRatioValue ratioValue) {

            if (result.getTransitRatio() <= ratioValue.getTransitRatio()) {
                setAccountFeature(result, TRANSIT_LOGO);
            }
        }
    }

    /**
     * <h2> 沉淀特征比设置处理类 </h2>
     */
    @Order(3)
    @Component
    static class DepositFeatureRatioHandle extends FeatureRatioHandleAbstract {

        @Override
        public void handlePattern(UnadjustedAccountAnalysisResult result, UnadjustedAccountAnalysisRequest.FeatureRatioValue ratioValue) {

            if (result.getDepositRatio() >= ratioValue.getDepositRatio()) {
                setAccountFeature(result, DEPOSIT_LOGO);
            }
        }
    }

    /**
     * <h2> 其他特征比设置处理类 </h2>
     */
    @Order(4)
    @Component
    static class OtherFeatureRatioHandle extends FeatureRatioHandleAbstract {

        @Override
        public void handlePattern(UnadjustedAccountAnalysisResult result, UnadjustedAccountAnalysisRequest.FeatureRatioValue ratioValue) {

            if (result.getSourceRatio() < ratioValue.getSourceRatio() && result.getTransitRatio() > ratioValue.getTransitRatio()
                    && result.getDepositRatio() < ratioValue.getDepositRatio()) {
                setAccountFeature(result, OTHER_LOGO);
            }
        }

    }

    public static void setAccountFeature(UnadjustedAccountAnalysisResult result, String logo) {
        if (StringUtils.isBlank(result.getAccountFeature())) {
            result.setAccountFeature(logo);
        } else {
            result.setAccountFeature(result.getAccountFeature() + " " + logo);
        }
    }
}
