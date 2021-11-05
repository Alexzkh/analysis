/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.factory;

import java.util.Map;

/**
 * <h1> 聚合key映射,通过key 获取对应聚合值, key 与 实体映射 </h1>
 */
public interface AggregationEntityMappingFactory {

    /**
     * <h2> 实体属性 与 聚合属性映射(适用于需要区分本方和对方) </h2>
     */
    void entityAggMetricsMappingOfLocalOpposite(Map<String, Map<String, String>> aggKeyMapping, Map<String, Map<String, String>> entityAggKeyMapping, Class<?> mappingEntity);

    void aggNameForMetricsMapping(Map<String, String> mapping, Class<?> mappingEntity);
}
