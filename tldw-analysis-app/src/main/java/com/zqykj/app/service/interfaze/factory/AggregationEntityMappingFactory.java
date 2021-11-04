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
     * <h2>  </h2>
     */
    void entityAggColMapping(Map<String, Map<String, String>> aggKeyMapping, Map<String, Map<String, String>> entityAggKeyMapping, Class<?> mappingEntity);
}
