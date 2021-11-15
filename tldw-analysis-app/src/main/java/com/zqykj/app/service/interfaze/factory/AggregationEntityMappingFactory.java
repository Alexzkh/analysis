/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.factory;

import java.util.Map;

/**
 * <h1>
 * <p>
 * 聚合名称 与 聚合属性映射(获取聚合值)
 * <p>
 * 聚合key 与 实体属性映射
 * </h1>
 */
public interface AggregationEntityMappingFactory {

    /**
     * <h2> 构建交易统计分析结果 聚合与实体属性映射 </h2>
     */
    @Deprecated
    void buildTradeStatisticsAnalysisResultAggMapping(Map<String, Map<String, String>> aggKeyMapping, Map<String, Map<String, String>> entityAggKeyMapping, Class<?> mappingEntity);

    /**
     * <h2> 构建交易统计分析按时间汇总交易金额聚合与实体属性映射 </h2>
     */
    void buildTradeStatisticsFundTimeAggMapping(Map<String, String> mapping, Class<?> mappingEntity);

    /**
     * <h2> 构建交易汇聚分析 / 交易统计分析 结果 聚合与实体属性映射 </h2>
     */
    void buildTradeAnalysisResultAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity);

    /**
     * <h2> 构建资金战法计算去重总数据量 聚合名称 - 聚合属性映射 </h2>
     */
    Map<String, String> buildFundTacticsAnalysisResultTotalAggMapping();

    /**
     * <h2> 构建批量获取调单卡号集合 聚合名称 - 聚合属性映射 </h2>
     */
    Map<String, String> buildGetCardNumsInBatchesAggMapping();
}
