/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.agg;

import com.zqykj.app.service.vo.fund.UnadjustedAccountAnalysisRequest;
import com.zqykj.parameters.aggregate.AggregationParams;

/**
 * <h1> 未调单账户分析聚合参数工厂 </h1>
 */
public interface UnadjustedAccountAggParamFactory {

    /**
     * <h2> 获取未调单账户分析结果 </h2>
     */
    AggregationParams unadjustedAccountAnalysis(UnadjustedAccountAnalysisRequest request, int from, int size, int groupSize);

    /**
     * <h2> 单独获取(分页参数大小的卡号数量) 的聚合展示字段(开户账户名称、对方开户行) 、关联账户数</h2>
     * <p>
     * 这些度量值全部放在第一次查询,由于groupBy 数量很大,查询会很慢(这里groupBy的数量只有 分页参数大小的卡号数量)
     */
    AggregationParams unadjustedAccountAnalysisSecondQuery(UnadjustedAccountAnalysisRequest request, int groupSize, String... showFields);

    /**
     * <h2> 计算结果总量 </h2>
     */
    AggregationParams computeTotal(UnadjustedAccountAnalysisRequest request, int groupSize);
}
