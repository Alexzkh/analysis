/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.factory;

import java.util.List;
import java.util.Map;

/**
 * <h1> 将聚合结果返回的结果,根据 映射定义, 反序列化成实体 </h1>
 */
public interface AggregationResultEntityParseFactory {

    List<Map<String, Object>> convertEntity(List<List<Object>> values, List<String> titles, Class<?> entity);
}
