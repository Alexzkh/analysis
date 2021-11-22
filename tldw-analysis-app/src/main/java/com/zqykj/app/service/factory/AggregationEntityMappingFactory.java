/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

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
     * <h2> 构建资金交易分析 结果 聚合映射 </h2>
     */
    void buildTradeAnalysisResultAggMapping(Map<String, String> aggKeyMapping, Class<?> mappingEntity);

    /**
     * <h2> 构建资金交易分析 结果 聚合与实体属性映射 </h2>
     */
    void buildTradeAnalysisResultAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity);

    /**
     * <h2> 构建资金交易分析 结果(本方) 聚合与实体属性映射 </h2>
     */
    void buildTradeAnalysisResultMappingLocal(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity);

    /**
     * <h2> 构建资金交易分析 结果(对方) 聚合与实体属性映射 </h2>
     */
    void buildTradeAnalysisResultMappingOpposite(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity);

    /**
     * <h2> 构建资金战法计算去重总数据量 聚合名称 - 聚合属性映射 </h2>
     */
    Map<String, String> buildFundTacticsAnalysisResultTotalAggMapping();

    /**
     * <h2> 构建批量获取调单卡号集合 聚合名称 - 聚合属性映射 </h2>
     */
    Map<String, String> buildGetCardNumsInBatchesAggMapping();

    /**
     * <h2> 构建获取调单卡号总量 聚合名称 - 聚合属性映射 </h2>
     */
    Map<String, String> buildGetCardNumsTotalAggMapping();

    /**
     * <h2> 构建groupBy 聚合名称 - 聚合属性映射  </h2>
     */
    Map<String, String> buildGetGroupByAggMapping();

    /**
     * 构建单卡画像分析结果,聚合和实体属性映射
     *
     * @param aggKeyMapping       聚合名称-聚合属性Map
     * @param entityAggKeyMapping 实体属性-聚合名称Map
     * @param mappingEntity       实体类
     */
    void buildSingleCardPortraitResultAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity);

}
