package com.zqykj.app.service.tools;

import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.common.enums.LineChartDisplayType;
import com.zqykj.domain.vo.TimeRuleLineChartVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @Description: 处理折线图上交易金额和交易次数工具类
 * @Author zhangkehou
 * @Date 2022/1/6
 */
public class SetValuesTools {
    /**
     * 设置交易金额到TimeRuleLineChartVO中去
     */
    private static final Function<TimeRuleAnalysisResult, TimeRuleLineChartVO> TRANSACTION_MONEY =
            (source) -> {
                TimeRuleLineChartVO timeRuleLineChartVO = new TimeRuleLineChartVO();
                timeRuleLineChartVO.setInTransaction(source.getCreditsAmount().setScale(2, RoundingMode.HALF_UP));
                timeRuleLineChartVO.setOutTransaction(source.getPayOutAmount().setScale(2, RoundingMode.HALF_UP));
                timeRuleLineChartVO.setTransaction(source.getDatetradeAmountSum().setScale(2, RoundingMode.HALF_UP));
                return timeRuleLineChartVO;
            };

    /**
     * 设置交易次数到TimeRuleLineChartVO中去
     */
    private static final Function<TimeRuleAnalysisResult, TimeRuleLineChartVO> NUMBER_OF_TRANSACTIONS =
            (timeRuleAnalysisResult) -> {
                TimeRuleLineChartVO timeRuleLineChartVO = new TimeRuleLineChartVO();
                timeRuleLineChartVO.setInTransaction(new BigDecimal(timeRuleAnalysisResult.getCreditsTimes()));
                timeRuleLineChartVO.setOutTransaction(new BigDecimal(timeRuleAnalysisResult.getPayOutTimes()));
                timeRuleLineChartVO.setTransaction(new BigDecimal(timeRuleAnalysisResult.getDateTradeTotalTimes()));
                return timeRuleLineChartVO;
            };

    /**
     * 静态映射表.
     */
    private static final Map<LineChartDisplayType, Function> functionMap = new ConcurrentHashMap<>();

    static {
        /**
         * 计算交易金额
         * */
        functionMap.put(LineChartDisplayType.TRANSACTION_MONEY, TRANSACTION_MONEY);

        /**
         * 计算交易次数.
         * */
        functionMap.put(LineChartDisplayType.NUMBER_OF_TRANSACTION, NUMBER_OF_TRANSACTIONS);


    }


    /**
     * @param result: 原始待处理数据
     * @param type:   折线图展示类型
     * @return: java.lang.Object
     **/
    public static Object transfer(TimeRuleAnalysisResult result, LineChartDisplayType type) {
        if (functionMap.containsKey(type)) {
            return functionMap.get(type).apply(result);
        }
        return null;

    }

}
