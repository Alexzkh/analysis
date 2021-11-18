/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import java.util.List;
import java.util.Map;

/**
 * <h1> 将聚合返回的结果 与 实体属性 做映射 并反序列化成实体 </h1>
 */
public interface AggregationResultEntityParseFactory {

    List<Map<String, Object>> convertEntity(List<List<Object>> values, List<String> titles, Class<?> entity);
}
