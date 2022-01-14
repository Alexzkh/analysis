package com.zqykj.app.service.strategy.analysis.impl;

import com.zqykj.app.service.strategy.analysis.TimeRuleAnalysisStrategy;
import com.zqykj.common.enums.StatisticType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 时间规律分析统计工厂类
 * @Author zhangkehou
 * @Date 2022/01/06
 */
@Component
public class TimeRuleStatisticsAnalysisFactory {

    /**
     * 自定义工厂map
     */
    private static final Map<StatisticType, TimeRuleAnalysisStrategy> fundFactoryMap = new ConcurrentHashMap<>(4);

    @Autowired
    private SingleStrategyImpl singleStrategy;

    @Autowired
    private SummaryStrategyImpl summaryStrategy;

    /**
     * @param type: 统计枚举类,单一和汇总
     * @return: com.zqykj.app.service.strategy.analysis.FundSourceAndDestinationStrategy
     **/
    public TimeRuleAnalysisStrategy access(StatisticType type) throws Exception {
        fundFactoryMap.put(StatisticType.SINGLE, singleStrategy);
        fundFactoryMap.put(StatisticType.SUMMARY, summaryStrategy);
        if (null == fundFactoryMap.get(type)) {
            throw new Exception("No Strategy exist with type{}" + type.toString());
        }
        return fundFactoryMap.get(type);
    }
}
