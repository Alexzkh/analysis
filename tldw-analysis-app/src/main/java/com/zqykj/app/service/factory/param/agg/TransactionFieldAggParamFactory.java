/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.agg;

import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 交易字段分析聚合参数工厂 </h1>
 */
public interface TransactionFieldAggParamFactory {

    /**
     * <h2> 交易字段类型占比 </h2>
     */
    AggregationParams transactionFieldTypeProportion(TransactionFieldAnalysisRequest request, int from, int size, int groupSize);

    /**
     * <h2> 交易字段类型占比(自定义归类查询) </h2>
     */
    AggregationParams fieldTypeProportionCustomCollationQuery(TransactionFieldAnalysisRequest request, int groupSize);
}
