/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.mapping.fund;

import com.zqykj.app.service.annotation.*;
import com.zqykj.app.service.field.SingleCardPortraitAnalysisField;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.util.ReflectionUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FundTacticsEntityAggMappingFactory implements AggregationEntityMappingFactory {

    @Override
    public void buildTradeAnalysisResultMappingLocal(Map<String, String> localAggMapping, Map<String, String> localEntityMapping, Class<?> entity) {

        List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段
        for (Field field : fields) {
            Local local = field.getAnnotation(Local.class);
            Key key = field.getAnnotation(Key.class);
            if (null != local && null != key) {
                localAggMapping.put(local.name(), key.name());
                localEntityMapping.put(field.getName(), local.name());
            }
        }
        // 取聚合名称的映射(es 特有), 其他数据源正常(field name = 聚合名称)
        Local agg = entity.getAnnotation(Local.class);
        Key key = entity.getAnnotation(Key.class);
        if (null == key) {
            return;
        }
        if (null != agg) {
            localAggMapping.put(agg.name(), key.name());
        }
    }

    public void buildTradeAnalysisResultMappingOpposite(Map<String, String> oppositeAggMapping, Map<String, String> oppositeEntityMapping, Class<?> entity) {

        List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段
        for (Field field : fields) {
            Opposite opposite = field.getAnnotation(Opposite.class);
            Key key = field.getAnnotation(Key.class);
            if (null != opposite && null != key) {
                oppositeAggMapping.put(opposite.name(), key.name());
                oppositeEntityMapping.put(field.getName(), opposite.name());
            }
        }
        // 取聚合名称的映射(es 特有), 其他数据源正常  eg. mysql 可以将 field 直接带出(field name = 聚合名称)
        Opposite agg = entity.getAnnotation(Opposite.class);
        Key key = entity.getAnnotation(Key.class);
        if (null == key) {
            return;
        }
        if (null != agg) {
            oppositeAggMapping.put(agg.name(), key.name());
        }
    }

    @Override
    public void buildTradeAnalysisResultAggMapping(Map<String, String> mapping, Class<?> entity) {

        List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段

        for (Field field : fields) {

            Key key = field.getAnnotation(Key.class);

            Agg agg = field.getAnnotation(Agg.class);

            if (null != key && null != agg) {

                mapping.put(agg.name(), key.name());
            }
        }
    }

    @Override
    public void buildTradeAnalysisResultAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> entity) {

        List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段

        for (Field field : fields) {

            Key key = field.getAnnotation(Key.class);

            Agg agg = field.getAnnotation(Agg.class);

            if (null == key) {
                continue;
            }
            if (null != agg) {
                aggKeyMapping.put(agg.name(), key.name());
                entityAggKeyMapping.put(field.getName(), agg.name());
            }
        }
        // 取聚合名称的映射(es 特有), 其他数据源正常(field name = 聚合名称)
        Agg entityAgg = entity.getAnnotation(Agg.class);
        Key entityKey = entity.getAnnotation(Key.class);
        if (null == entityKey) {
            return;
        }
        if (null != entityAgg) {
            aggKeyMapping.put(entityAgg.name(), entityKey.name());
        }
    }

    public Map<String, String> buildDistinctTotalAggMapping(String field) {

        Map<String, String> aggMapping = new HashMap<>();
        aggMapping.put("distinct_" + field, "value");
        return aggMapping;
    }

    public Map<String, String> buildGroupByAggMapping(String field) {
        Map<String, String> aggMapping = new HashMap<>();
        aggMapping.put("groupBy_" + field, "keyAsString");
        return aggMapping;
    }

    public Map<String, String> buildGroupByAggDocCountMapping(String field) {

        Map<String, String> aggMapping = new LinkedHashMap<>();
        aggMapping.put("groupBy_" + field, "keyAsString");
        aggMapping.put("count_" + field, "value");
        return aggMapping;
    }

    public Map<String, String> buildShowFieldsAggMapping() {
        Map<String, String> aggMapping = new HashMap<>();
        aggMapping.put("local_hits", "hits");
        return aggMapping;
    }

    public Map<String, String> buildSingleAggKeyMapping(String aggName, String key) {

        Map<String, String> mapping = new HashMap<>();
        mapping.put(aggName, key);
        return mapping;
    }

    @Override
    public void buildSingleCardPortraitResultAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity) {
        // 查询卡号分桶后的聚合结果map(聚合名称:聚合值属性名称)
        aggKeyMapping.put(SingleCardPortraitAnalysisField.AggResultName.LOCAL_IN_TRANSACTION_MONEY, SingleCardPortraitAnalysisField.AggResultField.VALUE);
        aggKeyMapping.put(SingleCardPortraitAnalysisField.AggResultName.LOCAL_OUT_TRANSACTION_MONEY, SingleCardPortraitAnalysisField.AggResultField.VALUE);
        aggKeyMapping.put(SingleCardPortraitAnalysisField.AggResultName.LOCAL_HITS, SingleCardPortraitAnalysisField.AggResultField.HITS);
    }

    @Override
    public void buildIndividualInfoAndStatisticsAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity) {
        buildIndividualPortraitAggMapping(aggKeyMapping, entityAggKeyMapping, mappingEntity);
    }

    @Override
    public void buildIndividualCardTransactionStatisticsAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity) {
        buildIndividualPortraitAggMapping(aggKeyMapping, entityAggKeyMapping, mappingEntity);
    }

    private void buildIndividualPortraitAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity) {
        List<Field> allFields = ReflectionUtils.getAllFields(mappingEntity);
        for (Field field : allFields) {
            Agg agg = field.getAnnotation(Agg.class);
            Key key = field.getAnnotation(Key.class);
            if (agg != null && key != null) {
                aggKeyMapping.put(agg.name(), key.name());
                entityAggKeyMapping.put(field.getName(), agg.name());
            }
        }
        Agg entityAgg = mappingEntity.getAnnotation(Agg.class);
        Key entityKey = mappingEntity.getAnnotation(Key.class);
        if (null != entityAgg) {
            aggKeyMapping.put(entityAgg.name(), entityKey.name());
        }
    }

    public void buildUnadjustedAccountAnalysisAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping) {

        aggKeyMapping.put("queryCardGroup", "keyAsString");
        aggKeyMapping.put("hits", "hits");
        aggKeyMapping.put("linked_account_times", "value");
        entityAggKeyMapping.put("oppositeCard", "hits");
        entityAggKeyMapping.put("accountName", "hits");
        entityAggKeyMapping.put("numberOfLinkedAccounts", "linked_account_times");
    }
}
