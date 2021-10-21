/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.parse;


import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisBankFlow;
import com.zqykj.util.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

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
//            if (perLine.size() == 1) {
//
//                // 数据总量的值
//                map.put("total", perLine.get(0));
//                colValueMapList.add(map);
//                continue;
//            }
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
//            if (map.size() == 1) {
//                // 总量字段
//                dataInfoList.add(map);
//                continue;
//            }
            Map<String, Object> result = new HashMap<>();
            entityAggColMapping.forEach((agg, colName) -> {
                Object value = map.get(agg);
                // 如果colName 是 source, 代表是聚合需要展示的字段, 需要特殊处理
                if (StringUtils.equals(colName, TradeStatisticalAnalysisBankFlow.EntityMapping.source.name())) {
                    applySource(result, value);
                }else {
                    result.put(colName, value);
                }
            });
            dataInfoList.add(result);
        }
        return dataInfoList;
    }

    private static void applySource(Map<String, Object> map, Object value) {

        if (value instanceof ArrayList) {

            List<Map<String, Object>> source = (List<Map<String, Object>>) value;
            if (CollectionUtils.isEmpty(source)) {
                return;
            }

            //
            Map<String, Object> sourceMap = source.get(0);
            // 开户名称
            map.put(TradeStatisticalAnalysisBankFlow.EntityMapping.customerName.name(),
                    sourceMap.get(FundTacticsAnalysisField.CUSTOMER_NAME));
            // 开户证件号码
            map.put(TradeStatisticalAnalysisBankFlow.EntityMapping.customerIdentityCard.name(),
                    sourceMap.get(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD));
            // 开户银行
            map.put(TradeStatisticalAnalysisBankFlow.EntityMapping.bank.name(),
                    sourceMap.get(FundTacticsAnalysisField.BANK));
            // 账号
            map.put(TradeStatisticalAnalysisBankFlow.EntityMapping.queryAccount.name(),
                    sourceMap.get(FundTacticsAnalysisField.QUERY_ACCOUNT));
            // 交易卡号
            map.put(TradeStatisticalAnalysisBankFlow.EntityMapping.queryCard.name(),
                    sourceMap.get(FundTacticsAnalysisField.QUERY_CARD));
        }
    }
}
