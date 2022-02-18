/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.param.agg.TransactionFieldAggParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.stereotype.Service;

/**
 * <h1> 交易字段分析聚合构建 </h1>
 */
@Service
public class TransactionFieldAggBuilder extends FundTacticsCommonAggBuilder implements TransactionFieldAggParamFactory {

    public AggregationParams transactionFieldTypeProportion(TransactionFieldAnalysisRequest request, int from, int size, int groupSize) {

        AggregationParams fieldGroup = AggregationParamsBuilders.terms("field_group", request.getStatisticsField());
        fieldGroup.setSize(groupSize);
        fieldGroup.setCollectMode("BREADTH_FIRST");
        // 交易金额
        AggregationParams tradeTotalAmount = AggregationParamsBuilders.sum("trade_amount", FundTacticsAnalysisField.TRANSACTION_MONEY);
        fieldGroup.setPerSubAggregation(tradeTotalAmount);
        // 交易次数
        AggregationParams tradeTimes = AggregationParamsBuilders.count("trade_times", request.getStatisticsField(), null);
        fieldGroup.setPerSubAggregation(tradeTimes);
        // 交易字段类型统计部分的聚合查询
        if (request.getAggQueryType() == 2) {
            transactionFieldTypeStatisticsPartAgg(fieldGroup);
            sumData(fieldGroup);
        }
        // bucketSort(排序)
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(request.getSortRequest(), from, size);
        fieldGroup.setPerSubAggregation(sort);
        return fieldGroup;
    }

    public AggregationParams fieldTypeProportionCustomCollationQuery(TransactionFieldAnalysisRequest request, int groupSize) {

        AggregationParams fieldGroup = AggregationParamsBuilders.terms("field_group", request.getStatisticsField());
        fieldGroup.setSize(groupSize);
        fieldGroup.setCollectMode("BREADTH_FIRST");
        // 交易金额
        AggregationParams tradeTotalAmount = AggregationParamsBuilders.sum("trade_amount", FundTacticsAnalysisField.TRANSACTION_MONEY);
        fieldGroup.setPerSubAggregation(tradeTotalAmount);
        // 交易次数
        AggregationParams tradeTimes = AggregationParamsBuilders.count("trade_times", request.getStatisticsField(), null);
        fieldGroup.setPerSubAggregation(tradeTimes);
        // 交易字段类型统计部分的聚合查询
        if (request.getAggQueryType() == 2) {
            transactionFieldTypeStatisticsPartAgg(fieldGroup);
        }
        // 需要合并它们的交易金额、交易次数(增加2个同级聚合查询)
        sumData(fieldGroup);
        return fieldGroup;
    }

    /**
     * <h2> 交易字段类型统计聚合查询部分 </h2>
     */
    private void transactionFieldTypeStatisticsPartAgg(AggregationParams fieldGroup) {
        // 入账次数
        QuerySpecialParams creditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams creditsTimes = AggregationParamsBuilders.filter("credits_times", creditsFilter, null);
        // 入账金额
        AggregationParams creditsAmount = AggregationParamsBuilders.sum("credits_amount", FundTacticsAnalysisField.CHANGE_MONEY, null);
        creditsTimes.setPerSubAggregation(creditsAmount);
        fieldGroup.setPerSubAggregation(creditsTimes);
        // 出账次数
        QuerySpecialParams outFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams outTimes = AggregationParamsBuilders.filter("payout_times", outFilter, null);
        // 出账金额
        AggregationParams outAmount = AggregationParamsBuilders.sum("payout_amount", FundTacticsAnalysisField.CHANGE_MONEY, null);
        outTimes.setPerSubAggregation(outAmount);
        fieldGroup.setPerSubAggregation(outTimes);
    }

    /**
     * <h2> 汇总数据 </h2>
     */
    private void sumData(AggregationParams fieldGroup) {
        // 汇总后的交易金额
        PipelineAggregationParams sumTradeAmount = AggregationParamsBuilders.pipelineBucketSum("sum_trade_amount", "field_group>trade_amount");
        fieldGroup.addSiblingAggregation(sumTradeAmount);
        // 汇总后的交易次数
        PipelineAggregationParams sumTradeTimes = AggregationParamsBuilders.pipelineBucketSum("sum_trade_times", "field_group>trade_times");
        fieldGroup.addSiblingAggregation(sumTradeTimes);
        // 汇总后的入账次数
        PipelineAggregationParams sumCreditsTimes = AggregationParamsBuilders.pipelineBucketSum("sum_credits_times", "field_group>credits_times");
        fieldGroup.addSiblingAggregation(sumCreditsTimes);
        // 汇总后的入账金额
        PipelineAggregationParams sumCreditsAmount = AggregationParamsBuilders.pipelineBucketSum("sum_credits_amount", "field_group>credits_amount");
        fieldGroup.addSiblingAggregation(sumCreditsAmount);
        // 汇总后的出账次数
        PipelineAggregationParams sumPayoutTimes = AggregationParamsBuilders.pipelineBucketSum("sum_payout_times", "field_group>payout_times");
        fieldGroup.addSiblingAggregation(sumPayoutTimes);
        // 汇总后的出账金额
        PipelineAggregationParams sumPayoutAmount = AggregationParamsBuilders.pipelineBucketSum("sum_payout_amount", "field_group>payout_amount");
        fieldGroup.addSiblingAggregation(sumPayoutAmount);
    }
}
