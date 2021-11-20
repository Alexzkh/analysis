/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.mapping.fund.es;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Local;
import com.zqykj.app.service.annotation.Opposite;
import com.zqykj.app.service.field.SingleCardPortraitAnalysisField;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.util.ReflectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class FundTacticsEntityAggMappingFactory implements AggregationEntityMappingFactory {

    public static final String LOCAL_SOURCE = "local_source";
    public static final String OPPOSITE_SOURCE = "opposite_source";
    public static final String LOCAL_HITS = "local_hits";
    public static final String OPPOSITE_HITS = "opposite_hits";
    public static final String HITS = "hits";

    @Deprecated
    @Override
    public void buildTradeStatisticsAnalysisResultAggMapping(Map<String, Map<String, String>> localOppositeAggMapping,
                                                             Map<String, Map<String, String>> localOppositeEntityMapping,
                                                             Class<?> entity) {

        Map<String, String> localAggMapping = new LinkedHashMap<>();
        Map<String, String> localEntityMapping = new LinkedHashMap<>();
        Map<String, String> oppositeEntityMapping = new LinkedHashMap<>();
        Map<String, String> oppositeAggMapping = new LinkedHashMap<>();
        List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段

        for (Field field : fields) {
            Local local = field.getAnnotation(Local.class);
            Key key = field.getAnnotation(Key.class);
            if (null != local && null != key) {
                if (key.name().equals(HITS)) {
                    localAggMapping.put(LOCAL_HITS, key.name());
                    localEntityMapping.put(LOCAL_HITS, LOCAL_SOURCE);
                } else {
                    localAggMapping.put(local.name(), key.name());
                    localEntityMapping.put(local.name(), field.getName());
                }
            }
            Opposite opposite = field.getAnnotation(Opposite.class);
            if (null != opposite && null != key) {
                if (key.name().equals(HITS)) {
                    oppositeAggMapping.put(OPPOSITE_HITS, key.name());
                    oppositeEntityMapping.put(OPPOSITE_HITS, OPPOSITE_SOURCE);
                } else {
                    oppositeAggMapping.put(opposite.name(), key.name());
                    oppositeEntityMapping.put(opposite.name(), field.getName());
                }
            }
        }
        localOppositeAggMapping.put("localMapping", localAggMapping);
        localOppositeAggMapping.put("oppositeMapping", oppositeAggMapping);
        localOppositeEntityMapping.put("localEntityAggColMapping", localEntityMapping);
        localOppositeEntityMapping.put("oppositeEntityAggColMapping", oppositeEntityMapping);
    }

    @Override
    public void buildTradeAnalysisResultAggMapping(Map<String, String> localAggMapping, Map<String, String> localEntityMapping, Class<?> entity) {

        List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段
        for (Field field : fields) {
            Local local = field.getAnnotation(Local.class);
            Key key = field.getAnnotation(Key.class);
            if (null != local && null != key) {
                if (key.name().equals(HITS)) {
                    localAggMapping.put(LOCAL_HITS, key.name());
                    localEntityMapping.put(LOCAL_HITS, LOCAL_SOURCE);
                } else {
                    localAggMapping.put(local.name(), key.name());
                    localEntityMapping.put(field.getName(), local.name());
                }
            }
        }
    }

    @Override
    public void buildTradeStatisticsFundTimeAggMapping(Map<String, String> mapping, Class<?> entity) {

        List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段

        for (Field field : fields) {

            Key key = field.getAnnotation(Key.class);

            Agg agg = field.getAnnotation(Agg.class);

            if (null != key && null != agg) {

                mapping.put(agg.name(), key.name());
            }
        }
    }

    public Map<String, String> buildFundTacticsAnalysisResultTotalAggMapping() {

        Map<String, String> aggMapping = new HashMap<>();
        aggMapping.put("cardinality_total", "value");
        return aggMapping;
    }

    public Map<String, String> buildGetCardNumsInBatchesAggMapping() {

        Map<String, String> aggMapping = new HashMap<>();
        aggMapping.put("groupQueryCard", "KeyAsString");
        return aggMapping;
    }

    public Map<String, String> buildGetCardNumsTotalAggMapping() {

        Map<String, String> aggMapping = new HashMap<>();
        aggMapping.put("distinctQueryCard", "value");
        return aggMapping;
    }

    @Override
    public void buildSingleCardPortraitResultAggMapping(Map<String, String> aggKeyMapping, Map<String, String> entityAggKeyMapping, Class<?> mappingEntity) {
        // 查询卡号分桶后的聚合结果map(聚合名称:聚合值属性名称)
        aggKeyMapping.put(SingleCardPortraitAnalysisField.AggResultName.LOCAL_IN_TRANSACTION_MONEY, SingleCardPortraitAnalysisField.AggResultField.VALUE);
        aggKeyMapping.put(SingleCardPortraitAnalysisField.AggResultName.LOCAL_OUT_TRANSACTION_MONEY, SingleCardPortraitAnalysisField.AggResultField.VALUE);
        aggKeyMapping.put(SingleCardPortraitAnalysisField.AggResultName.LOCAL_HITS, SingleCardPortraitAnalysisField.AggResultField.HITS);
    }
}
