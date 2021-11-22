/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.parse.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Local;
import com.zqykj.app.service.annotation.Opposite;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.util.ReflectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * <h1> 资金战法聚合结果解析工厂 </h1>
 */
@Service
public class FundTacticsAggResultParseFactory implements AggregationResultEntityParseFactory {

    public List<Map<String, Object>> convertEntity(List<List<Object>> values, List<String> titles, Class<?> entity) {

        return convertEntityMapping(values, titles, entity);
    }

    /**
     * <h1> 直接将聚合返回的结果 与 实体属性做映射 </h1>
     * <p>
     * 其中聚合需要展示的字段需要特殊处理
     */
    protected static List<Map<String, Object>> convertEntityMapping(List<List<Object>> values, List<String> titles, Class<?> entity) {

        List<Map<String, Object>> colValueMapList = new ArrayList<>();

        values.forEach(perLine -> {

            Map<String, Object> map = new HashMap<>();

            for (int i = 0; i < perLine.size(); i++) {

                Object value = perLine.get(i);

                if (null == value) {
                    continue;
                }
                if (value instanceof List) {

                    // 需要处理hits(展示的字段)
                    applySource(map, perLine.get(i), entity);
                } else {
                    map.put(titles.get(i), perLine.get(i));
                }
            }
            colValueMapList.add(map);
        });

        return colValueMapList;
    }

    private static void applyLocalSource(Map<String, Object> map, Object value, Class<?> entity) {

        if (value instanceof ArrayList) {

            List<Map<String, Object>> source = (List<Map<String, Object>>) value;
            if (CollectionUtils.isEmpty(source)) {
                return;
            }
            // 取出聚合结果中需要展示的字段
            Map<String, Object> sourceMap = source.get(0);

            // 本方开户名称、本方开户证件号码、本方开户银行、本方账号、本方交易卡号
            List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段
            for (Field field : fields) {
                Local local = field.getAnnotation(Local.class);
                if (null != local && local.showField()) {
                    Object fieldValue = sourceMap.get(local.name());
                    if (null != fieldValue) {
                        map.put(field.getName(), fieldValue);
                    }
                }
            }
        }
    }

    private static void applyOppositeSource(Map<String, Object> map, Object value, Class<?> entity) {

        if (value instanceof ArrayList) {

            List<Map<String, Object>> oppositeSource = (List<Map<String, Object>>) value;
            if (CollectionUtils.isEmpty(oppositeSource)) {
                return;
            }

            //
            Map<String, Object> sourceMap = oppositeSource.get(0);

            // 对方开户名称、对方开户证件号码、对方开户银行、对方账号、对方交易卡号
            List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段

            for (Field field : fields) {
                Opposite opposite = field.getAnnotation(Opposite.class);
                if (null != opposite && opposite.showField()) {
                    Object fieldValue = sourceMap.get(opposite.name());
                    if (null != fieldValue) {
                        map.put(field.getName(), fieldValue);
                    }
                }
            }
        }
    }

    /**
     * <h2> 聚合需要展示 本方开户名称、本方证件号码、开户银行、对方开户名称、对方证件号码、对方开户行、对方卡号 </h2>
     */
    private static void applySource(Map<String, Object> map, Object value, Class<?> entity) {

        if (value instanceof ArrayList) {

            List<Map<String, Object>> source = (List<Map<String, Object>>) value;
            if (CollectionUtils.isEmpty(source)) {
                return;
            }
            Map<String, Object> sourceMap = source.get(0);

            List<Field> fields = ReflectionUtils.getAllFields(entity); // 这种方式可以拿到继承父类的字段

            for (Field field : fields) {
                Local local = field.getAnnotation(Local.class);
                if (null != local && local.showField()) {
                    Object fieldValue = sourceMap.get(local.name());
                    if (null != fieldValue) {
                        map.put(field.getName(), fieldValue);
                    }
                }
                Opposite opposite = field.getAnnotation(Opposite.class);
                if (null != opposite && opposite.showField()) {
                    Object fieldValue = sourceMap.get(opposite.name());
                    if (null != fieldValue) {
                        map.put(field.getName(), fieldValue);
                    }
                }
                Agg agg = field.getAnnotation(Agg.class);
                if (null != agg && agg.showField()) {
                    Object fieldValue = sourceMap.get(agg.name());
                    if (null != fieldValue) {
                        map.put(field.getName(), fieldValue);
                    }
                }
            }
        }
    }
}
