package com.zqykj.app.service.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @Description: 字符串拼接工具类.
 * @Author zhangkehou
 * @Date 2021/10/21
 */
public class SplicingStringTools {


    /**
     * eg:将`2020`拼接成 `2021年`.
     */
    private static final BiFunction<String, String, String> STRING_BI_FUNCTION_SPLICING_YEAR =
            (source, type) -> {
                source += "年";
                return source;
            };

    /**
     * eg:将`2020-10`拼接成 `2021-10~12月`.
     */
    private static final BiFunction<String, String, String> STRING_BI_FUNCTION_SPLICING_QUATER =
            (source, type) -> {
                String front = source.substring(0, source.indexOf("-"));
                String back = source.substring(front.length() + 1, source.length());
                Integer tmp = Integer.valueOf(back) + 2;
                source += "~" + tmp.toString() + "月";
                return source;
            };

    /**
     * eg:将`2020-11`拼接成 `2021-11月`.
     */
    private static final BiFunction<String, String, String> STRING_BI_FUNCTION_SPLICING_MONTH =
            (source, type) -> {
                source += "月";
                return source;
            };

    /**
     * 静态映射表.
     */
    private static final Map<String, BiFunction> functionMap = new ConcurrentHashMap<>();

    static {
        /**
         * 年份.
         * */
        functionMap.put("y", STRING_BI_FUNCTION_SPLICING_YEAR);
        /**
         * 季度.
         * */
        functionMap.put("q", STRING_BI_FUNCTION_SPLICING_QUATER);
        /**
         * 月份.
         * */
        functionMap.put("m", STRING_BI_FUNCTION_SPLICING_MONTH);
    }


    /**
     * @param souce:    源字符串.
     * @param dateType: 日期类型,此处对应处理的是年 、季度、月份
     * @return: java.lang.Object
     **/
    public static Object transfer(String souce, String dateType) {
        if (functionMap.containsKey(dateType)) {
            return functionMap.get(dateType).apply(souce, dateType);
        }
        return null;

    }

}
