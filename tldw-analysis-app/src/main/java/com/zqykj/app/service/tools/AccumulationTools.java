package com.zqykj.app.service.tools;

import com.zqykj.common.enums.LineChartDisplayType;
import com.zqykj.domain.vo.TimeRuleLineChartVO;
import com.zqykj.util.BigDecimalUtil;

import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @Description: 处理折线图上交易金额和交易次数工具类
 * @Author zhangkehou
 * @Date 2022/1/6
 */
public class AccumulationTools {
    /**
     * 设置交易金额到TimeRuleLineChartVO中去
     */
    private static final BiFunction<TimeRuleLineChartVO, TimeRuleLineChartVO, TimeRuleLineChartVO> TRANSACTION_MONEY =
            (timeRuleLineChartVO, result) -> {
                timeRuleLineChartVO.setTransaction(BigDecimalUtil.add(timeRuleLineChartVO.getTransaction()
                        , result.getTransaction()).setScale(2, RoundingMode.HALF_UP));
                timeRuleLineChartVO.setInTransaction(BigDecimalUtil.add(timeRuleLineChartVO.getInTransaction()
                        , result.getInTransaction()).setScale(2, RoundingMode.HALF_UP));
                timeRuleLineChartVO.setOutTransaction(BigDecimalUtil.add(timeRuleLineChartVO.getOutTransaction()
                        , result.getOutTransaction()).setScale(2, RoundingMode.HALF_UP));
                return timeRuleLineChartVO;
            };

    /**
     * 设置交易次数到TimeRuleLineChartVO中去
     */
    private static final BiFunction<TimeRuleLineChartVO, TimeRuleLineChartVO, TimeRuleLineChartVO> NUMBER_OF_TRANSACTIONS =
            (timeRuleLineChartVO, result) -> {
                timeRuleLineChartVO.setTransaction(BigDecimalUtil.add(timeRuleLineChartVO.getTransaction()
                        , result.getTransaction()).setScale(0, RoundingMode.HALF_UP));
                timeRuleLineChartVO.setInTransaction(BigDecimalUtil.add(timeRuleLineChartVO.getInTransaction()
                        , result.getInTransaction()).setScale(0, RoundingMode.HALF_UP));
                timeRuleLineChartVO.setOutTransaction(BigDecimalUtil.add(timeRuleLineChartVO.getOutTransaction()
                        , result.getOutTransaction()).setScale(0, RoundingMode.HALF_UP));
                return timeRuleLineChartVO;
            };


    /**
     * 静态映射表.
     */
    private static final Map<LineChartDisplayType, BiFunction> functionMap = new ConcurrentHashMap<>();

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
    public static Object transfer(TimeRuleLineChartVO result, TimeRuleLineChartVO vo, LineChartDisplayType type) {
        if (functionMap.containsKey(type)) {
            return functionMap.get(type).apply(vo, result);
        }
        return null;

    }

}
