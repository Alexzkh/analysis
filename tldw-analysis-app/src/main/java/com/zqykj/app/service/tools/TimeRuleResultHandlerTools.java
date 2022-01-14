package com.zqykj.app.service.tools;

import com.zqykj.common.enums.StatisticType;
import com.zqykj.infrastructure.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @Description: 时间规律结果处理工具
 * @Author zhangkehou
 * @Date 2022/1/6
 */
@Slf4j
public class TimeRuleResultHandlerTools {


    /**
     * 月份的处理
     * 原始日期为：2022-08
     * 如果统计类型是单一(StatisticType.SINGLE) 则 拼接后结果为：2022年08月
     * 否则拼接结果为08月
     */
    private static final BiFunction<String, StatisticType, String> MONTH =
            (source, type) -> {
                String[] spiltDate = source.split("-");
                String key;
                if (type.equals(StatisticType.SINGLE)) {
                    key = spiltDate[0] + "年" + spiltDate[1] + "月";
                } else {
                    key = spiltDate[1] + "月";
                }
                return key;
            };

    /**
     * 计算以周为单位的时间规律统计结果
     * 原始日期为 2022-01-06
     * 如果统计类型是单一(StatisticType.SINGLE) 则 拼接后结果为：2022年第2周
     * 否则拼接结果为：第2周
     */
    private static final BiFunction<String, StatisticType, String> WEEK =
            (source, type) -> {
                // 初始化日期月份所在周
                Integer week = 1;
                try {
                    week = DateUtils.whatWeek(source);
                } catch (ParseException e) {
                    log.error("根据日期时间计算年度所在周出错{}", e);
                }
                String weeks = "第" + week + "周";
                // spilt[0]: 2022 spilt[1]: 01 spilt[2]:06
                String[] spiltDate = source.split("-");
                String key;
                if (type.equals(StatisticType.SINGLE)) {
                    key = spiltDate[0] + "年" + weeks;

                } else {
                    return weeks;
                }
                return key;
            };

    /**
     * 计算以日为单位的时间规律统计结果
     * 原始日期为 2022-01-06
     * 如果统计类型是单一(StatisticType.SINGLE) 则 拼接后结果为：2022-01-06
     * 否则拼接结果为：
     */
    private static final BiFunction<String, StatisticType, String> DAY =
            (source, type) -> {
                // spilt[0]: 2022 spilt[1]: 01 spilt[2]:06
                String[] spiltDate = source.split("-");
                String key;
                if (type.equals(StatisticType.SUMMARY)) {
                    key = "第" + spiltDate[2] + "天";
                } else {
                    return source;
                }
                return key;
            };

    /**
     * 计算以日为单位的时间规律统计结果
     * 原始日期为 2022-01-06 14
     * 如果统计类型是单一(StatisticType.SINGLE) 则 拼接后结果为：2022-01-06
     * 否则拼接结果为：
     */
    private static final BiFunction<String, StatisticType, String> HOUR =
            (source, type) -> {
                // spilt[0]: 2022-01-06 spilt[1]: 14
                String[] dates = source.split("\\s");
                // 统计类型为单一是 没有针对以小时为单位的时间规律统计
                if (type.equals(StatisticType.SUMMARY)) {
                    return dates[1] + ":00";
                }
                return "";
            };

    /**
     * 静态映射表.
     */
    private static final Map<String, BiFunction> functionMap = new ConcurrentHashMap<>();

    static {
        /**
         * 按照月统计.
         * */
        functionMap.put("M", MONTH);
        /**
         * 按照周统计.
         * */
        functionMap.put("w", WEEK);
        /**
         * 按照日统计.
         * */
        functionMap.put("d", DAY);

        /**
         * 按照时统计.
         * */
        functionMap.put("h", HOUR);
    }


    /**
     * @param souce:待处理字符串
     * @param date:          时间类型
     * @param statisticType: 统计类型
     * @return: java.lang.Object
     **/
    public static Object transfer(String souce, String date, StatisticType statisticType) {
        if (functionMap.containsKey(date)) {
            return functionMap.get(date).apply(souce, statisticType);
        }
        return null;

    }
}
