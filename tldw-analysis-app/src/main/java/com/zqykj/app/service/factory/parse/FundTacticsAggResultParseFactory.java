/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.parse;


import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.vo.fund.Hits;
import com.zqykj.app.service.vo.fund.Local;
import com.zqykj.app.service.vo.fund.Opposite;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisBankFlow;
import com.zqykj.util.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.util.*;

/**
 * <h1> 战法聚合结果解析工厂 </h1>
 */
public class FundTacticsAggResultParseFactory {

    /**
     * <h2> 获取交易统计分析结果 </h2>
     */
    public static List<TradeStatisticalAnalysisBankFlow> getTradeStatisticalAnalysisResult(List<Map<String, Object>> data) {

        return JacksonUtils.parse(JacksonUtils.toJson(data), new TypeReference<List<TradeStatisticalAnalysisBankFlow>>() {
        });
    }


    public static List<Map<String, Object>> getColValueMapList(List<List<Object>> values, List<String> titles) {

        List<Map<String, Object>> colValueMapList = new ArrayList<>();

        for (List<Object> perLine : values) {

            Map<String, Object> map = new HashMap<>();

            for (int i = 0; i < perLine.size(); i++) {

                map.put(titles.get(i), perLine.get(i));
            }
            colValueMapList.add(map);
        }
        return colValueMapList;
    }


    public static List<Map<String, Object>> convertEntityMapping(List<Map<String, Object>> dataMap, Map<String, String> entityAggColMapping) {

        List<Map<String, Object>> dataInfoList = new ArrayList<>();
        for (Map<String, Object> map : dataMap) {
            Map<String, Object> result = new HashMap<>();

            entityAggColMapping.forEach((agg, colName) -> {
                Object value = map.get(agg);
                // 如果colName 是 source, 代表是聚合需要展示的字段, 需要特殊处理
                if (StringUtils.equals(colName, TradeStatisticalAnalysisBankFlow.EntityMapping.local_source.name())) {
                    // 本方字段
                    applyLocalSource(result, value);
                } else if (StringUtils.equals(colName, TradeStatisticalAnalysisBankFlow.EntityMapping.opposite_source.name())) {
                    // 对方字段
                    applyOppositeSource(result, value);
                } else {
                    result.put(colName, value);
                }
            });
            dataInfoList.add(result);
        }
        return dataInfoList;
    }

    private static void applyLocalSource(Map<String, Object> map, Object value) {

        if (value instanceof ArrayList) {

            List<Map<String, Object>> source = (List<Map<String, Object>>) value;
            if (CollectionUtils.isEmpty(source)) {
                return;
            }
            // 取出聚合结果中需要展示的字段
            Map<String, Object> sourceMap = source.get(0);

            // 本方开户名称、本方开户证件号码、本方开户银行、本方账号、本方交易卡号
            ReflectionUtils.doWithFields(TradeStatisticalAnalysisBankFlow.class, field -> {

                Local local = field.getAnnotation(Local.class);
                Hits hits = field.getAnnotation(Hits.class);
                if (null != local && null != hits) {
                    map.put(field.getName(), sourceMap.get(local.name()));
                }
            });
        }
    }

    private static void applyOppositeSource(Map<String, Object> map, Object value) {

        if (value instanceof ArrayList) {

            List<Map<String, Object>> oppositeSource = (List<Map<String, Object>>) value;
            if (CollectionUtils.isEmpty(oppositeSource)) {
                return;
            }

            //
            Map<String, Object> sourceMap = oppositeSource.get(0);

            // 对方开户名称、对方开户证件号码、对方开户银行、对方账号、对方交易卡号
            ReflectionUtils.doWithFields(TradeStatisticalAnalysisBankFlow.class, field -> {

                Opposite opposite = field.getAnnotation(Opposite.class);
                Hits hits = field.getAnnotation(Hits.class);
                if (null != opposite && null != hits) {
                    map.put(field.getName(), sourceMap.get(opposite.name()));
                }
            });
        }
    }
}
