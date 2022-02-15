/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.param.agg.TransactionFieldAggParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
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
        String tradeAmountField = FundTacticsAnalysisField.TRANSACTION_MONEY;
        AggregationParams tradeTotalAmount = AggregationParamsBuilders.sum("trade_total_amount", tradeAmountField);
        fieldGroup.setPerSubAggregation(tradeTotalAmount);
        // 交易次数
        AggregationParams tradeTimes = AggregationParamsBuilders.count("trade_times", request.getStatisticsField(), null);
        fieldGroup.setPerSubAggregation(tradeTimes);
        // bucketSort(排序)
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(request.getSortRequest(), from, size);
        fieldGroup.setPerSubAggregation(sort);
        return fieldGroup;
    }
}
