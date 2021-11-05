/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.mapping.fund.es;

import com.zqykj.app.service.vo.fund.*;
import com.zqykj.app.service.interfaze.factory.AggregationEntityMappingFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <h1> 交易统计分析聚合实体映射关系构建 </h1>
 */
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class TradeStatisticalAnalysisEntityAggMappingFactory implements AggregationEntityMappingFactory {

    @Override
    public void entityAggMetricsMappingOfLocalOpposite(Map<String, Map<String, String>> aggKeyMapping,
                                                       Map<String, Map<String, String>> entityAggKeyMapping,
                                                       Class<?> mappingEntity) {

        Map<String, String> localMapping = new LinkedHashMap<>();
        Map<String, String> localEntityAggColMapping = new LinkedHashMap<>();
        Map<String, String> oppositeEntityAggColMapping = new LinkedHashMap<>();
        Map<String, String> oppositeMapping = new LinkedHashMap<>();
        ReflectionUtils.doWithFields(mappingEntity, field -> {

            Local local = field.getAnnotation(Local.class);
            Key key = field.getAnnotation(Key.class);
            if (null != local && null != key) {
                if (key.name().equals("hits")) {
                    localMapping.put("local_hits", key.name());
                    localEntityAggColMapping.put("local_hits", TradeStatisticalAnalysisBankFlow.EntityMapping.local_source.name());
                } else {
                    localMapping.put(local.name(), key.name());
                    localEntityAggColMapping.put(local.name(), field.getName());
                }
            }
            Opposite opposite = field.getAnnotation(Opposite.class);
            if (null != opposite && null != key) {
                if (key.name().equals("hits")) {
                    oppositeMapping.put("opposite_hits", key.name());
                    oppositeEntityAggColMapping.put("opposite_hits", TradeStatisticalAnalysisBankFlow.EntityMapping.opposite_source.name());
                } else {
                    oppositeMapping.put(opposite.name(), key.name());
                    oppositeEntityAggColMapping.put(opposite.name(), field.getName());
                }
            }
        });
        aggKeyMapping.put("localMapping", localMapping);
        aggKeyMapping.put("oppositeMapping", oppositeMapping);
        entityAggKeyMapping.put("localEntityAggColMapping", localEntityAggColMapping);
        entityAggKeyMapping.put("oppositeEntityAggColMapping", oppositeEntityAggColMapping);
    }

    @Override
    public void aggNameForMetricsMapping(Map<String, String> mapping, Class<?> mappingEntity) {

        ReflectionUtils.doWithFields(mappingEntity, field -> {

            Key key = field.getAnnotation(Key.class);

            Agg agg = field.getAnnotation(Agg.class);

            if (null != key && null != agg) {

                mapping.put(agg.name(), key.name());
            }
        });
    }
}
