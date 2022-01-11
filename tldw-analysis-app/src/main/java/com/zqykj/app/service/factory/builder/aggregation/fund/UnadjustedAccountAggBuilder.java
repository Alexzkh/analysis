/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.param.agg.UnadjustedAccountAggParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.UnadjustedAccountAnalysisRequest;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1> 未调单账户特征分析聚合构建 </h1>
 */
@Service
public class UnadjustedAccountAggBuilder extends FundTacticsCommonAggBuilder implements UnadjustedAccountAggParamFactory {

    @Override
    public AggregationParams unadjustedAccountAnalysis(UnadjustedAccountAnalysisRequest request, int from, int size, int groupSize) {

        // 根据查询卡号分组聚合
        AggregationParams queryCardGroup = AggregationParamsBuilders.terms("queryCardGroup", FundTacticsAnalysisField.QUERY_CARD);
        queryCardGroup.setSize(groupSize);
        queryCardGroup.setCollectMode("BREADTH_FIRST");
        // 关联账户数(和对方交易(对方卡去重)) -- 当不按照关联账户数排序的时候(需要单独计算,否则查询速度很慢)
        SortRequest sortRequest = request.getSortRequest();
        if (sortRequest.getProperty().equals("linked_account_times")) {
            AggregationParams linkedAccountTimes = AggregationParamsBuilders.cardinality("linked_account_times", FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);
            queryCardGroup.setPerSubAggregation(linkedAccountTimes);
        }
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("trade_total_times", FundTacticsAnalysisField.QUERY_CARD, null);
        queryCardGroup.setPerSubAggregation(tradeTotalTimes);
        // 交易净和
        AggregationParams tradeNetSum = AggregationParamsBuilders.sum("trade_net", FundTacticsAnalysisField.TRANSACTION_MONEY);
        queryCardGroup.setPerSubAggregation(tradeNetSum);
        // 计算特征比
        computeFeatureRatio(request, queryCardGroup);
        // bucketSort(排序)
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(request.getSortRequest(), from, size);
        queryCardGroup.setPerSubAggregation(sort);
        return queryCardGroup;
    }

    @Override
    public AggregationParams unadjustedAccountAnalysisSecondQuery(UnadjustedAccountAnalysisRequest request, int groupSize, String... showFields) {

        // 根据查询卡号分组聚合
        AggregationParams queryCardGroup = AggregationParamsBuilders.terms("queryCardGroup", FundTacticsAnalysisField.QUERY_CARD);
        queryCardGroup.setSize(groupSize);
        queryCardGroup.setCollectMode("BREADTH_FIRST");
        // 关联账户数
        SortRequest sortRequest = request.getSortRequest();
        if (!sortRequest.getProperty().equals("linked_account_times")) {
            AggregationParams linkedAccountTimes = AggregationParamsBuilders.cardinality("linked_account_times", FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);
            queryCardGroup.setPerSubAggregation(linkedAccountTimes);
        }
        // 聚合展示字段
        if (showFields != null && showFields.length > 0) {
            AggregationParams aggShowFields = fundTacticsPartUniversalAggShowFields(showFields, "hits");
            queryCardGroup.setPerSubAggregation(aggShowFields);
        }
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(0, groupSize);
        queryCardGroup.setPerSubAggregation(sort);
        return queryCardGroup;
    }

    @Override
    public AggregationParams computeTotal(UnadjustedAccountAnalysisRequest request, int groupSize) {

        // 根据查询卡号分组聚合
        AggregationParams queryCardGroup = AggregationParamsBuilders.terms("queryCardGroup", FundTacticsAnalysisField.QUERY_CARD);
        queryCardGroup.setSize(groupSize);
        queryCardGroup.setCollectMode("BREADTH_FIRST");
        // 计算特征比
        computeFeatureRatio(request, queryCardGroup);
        // 辅助计算总量的桶个数
        Map<String, String> path = new HashMap<>();
        path.put("valueCount", "_count");
        PipelineAggregationParams bucketCount = AggregationParamsBuilders.pipelineBucketScript("bucketCount", path, "1");
        queryCardGroup.setPerSubAggregation(bucketCount);
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(0, groupSize);
        queryCardGroup.setPerSubAggregation(sort);
        // 设置同级聚合算总量
        PipelineAggregationParams computeTotal = AggregationParamsBuilders.pipelineBucketSum("total", "queryCardGroup>bucketCount");
        queryCardGroup.addSiblingAggregation(computeTotal);
        return queryCardGroup;
    }

    /**
     * <h2> 计算特征比 </h2>
     */
    private void computeFeatureRatio(UnadjustedAccountAnalysisRequest request, AggregationParams queryCardGroup) {
        // 交易总金额
        AggregationParams tradeTotalAmount = AggregationParamsBuilders.sum("trade_total_amount", FundTacticsAnalysisField.CHANGE_MONEY);
        queryCardGroup.setPerSubAggregation(tradeTotalAmount);
        // 入账总金额
        QuerySpecialParams queryCredit = new QuerySpecialParams();
        queryCredit.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams filterCreditAgg = AggregationParamsBuilders.filter("filterCredit", queryCredit, null);
        AggregationParams creditSum = AggregationParamsBuilders.sum("credits_total_amount", FundTacticsAnalysisField.CHANGE_MONEY);
        filterCreditAgg.setPerSubAggregation(creditSum);
        queryCardGroup.setPerSubAggregation(filterCreditAgg);
        // 出账总金额
        QuerySpecialParams queryPayout = new QuerySpecialParams();
        queryPayout.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams filterPayoutAgg = AggregationParamsBuilders.filter("filterPayout", queryPayout, null);
        AggregationParams payoutSum = AggregationParamsBuilders.sum("payout_total_amount", FundTacticsAnalysisField.CHANGE_MONEY);
        filterPayoutAgg.setPerSubAggregation(payoutSum);
        queryCardGroup.setPerSubAggregation(filterPayoutAgg);
        // 计算特征比
        // 来源特征比
        Map<String, String> bucketPath = new HashMap<>();
        bucketPath.put("creditAmountTotal", "filterCredit>credits_total_amount");
        bucketPath.put("payoutAmountTotal", "filterPayout>payout_total_amount");
        bucketPath.put("tradeAmountTotal", "trade_total_amount");
        String sourceScript = "params.payoutAmountTotal / params.tradeAmountTotal";
        PipelineAggregationParams sourceFeatureRatio = AggregationParamsBuilders.pipelineBucketScript("sourceFeatureRatio", bucketPath, sourceScript);
        queryCardGroup.setPerSubAggregation(sourceFeatureRatio);
        // 中转特征比
        String transitScript = "Math.abs(params.creditAmountTotal - params.payoutAmountTotal) / params.tradeAmountTotal";
        PipelineAggregationParams transitFeatureRatio = AggregationParamsBuilders.pipelineBucketScript("transitFeatureRatio", bucketPath, transitScript);
        queryCardGroup.setPerSubAggregation(transitFeatureRatio);
        // 沉淀特征比
        String depositScript = "params.creditAmountTotal / params.tradeAmountTotal";
        PipelineAggregationParams depositFeatureRatio = AggregationParamsBuilders.pipelineBucketScript("depositFeatureRatio", bucketPath, depositScript);
        queryCardGroup.setPerSubAggregation(depositFeatureRatio);
        // 特征比筛选
        Map<String, String> selectorPath = new HashMap<>();
        selectorPath.put("sourceRatio", "sourceFeatureRatio");
        selectorPath.put("transitRatio", "transitFeatureRatio");
        selectorPath.put("depositRatio", "depositFeatureRatio");
        // 获取特征比筛选脚本
        String pipelineSelectorScript = request.getPipelineSelectorScript();
        PipelineAggregationParams featureRatioSelector = AggregationParamsBuilders.pipelineSelector("featureRatioSelector", selectorPath, pipelineSelectorScript);
        queryCardGroup.setPerSubAggregation(featureRatioSelector);
    }
}
