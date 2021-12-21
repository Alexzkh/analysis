package com.zqykj.app.service.tools;

import com.xkzhangsan.time.calculator.DateTimeCalculatorUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @Description: 资金追踪时间间隔转换工具类
 * @Author zhangkehou
 * @Date 2021/12/3
 */
public class FundTrackingDateConditionTools {

    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    /**
     * eg:2021-12-3 10:56:20 加1天 ->2021-12-4 10:56:20.
     */
    private static final BiFunction<String, Integer, String> PLUE_DAY =
            (source, time) -> {

                try {
                    return formatter.get().format(DateTimeCalculatorUtil.plusDays(formatter.get().parse(source), time));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            };


    /**
     * eg:2021-12-3 10:56:20 加1分钟 ->2021-12-3 10:57:20.
     */
    private static final BiFunction<String, Integer, String> PLUS_MINUTES =
            (source, time) -> {

                try {
                    return formatter.get().format(DateTimeCalculatorUtil.plusMinutes(formatter.get().parse(source), time));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            };

    /**
     * eg:2021-12-3 10:56:20 加1小时 ->2021-12-3 11:56:20
     */
    private static final BiFunction<String, Integer, String> PLUS_HOURS =
            (source, time) -> {

                try {
                    return formatter.get().format(DateTimeCalculatorUtil.plusHours(formatter.get().parse(source), time));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            };


    /**
     * 静态映射表.
     */
    private static final Map<String, BiFunction> functionMap = new ConcurrentHashMap<>();

    static {
        /**
         * 天.
         * */
        functionMap.put("d", PLUE_DAY);
        /**
         * 分钟.
         * */
        functionMap.put("m", PLUS_MINUTES);
        /**
         * 小时.
         * */
        functionMap.put("h", PLUS_HOURS);
    }


    /**
     * @param souce:    开始时间
     * @param time:     时间间隔
     * @param dateType: 时间类型
     * @return: java.lang.Object
     **/
    public static Object transfer(String souce, Integer time, String dateType) {
        if (functionMap.containsKey(dateType)) {
            return functionMap.get(dateType).apply(souce, time);
        }
        return null;

    }
}
