package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.DateFiltering;
import com.zqykj.common.enums.StatisticType;
import com.zqykj.common.request.QueryRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 时间规律战法分析详情数据请求体
 * @Author zhangkehou
 * @Date 2022/1/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TimeRuleAnalysisDetailRequest {

    /**
     * 时间规律分析选择的本端对象
     */
    private List<String> source;

    /**
     * 时间规律分析选择的对端对象
     */
    private List<String> dest;

    /**
     * 时间范围
     */
    private String tradingTime;

    /**
     * 时间类型
     */
    private String dateType;

    /**
     * 查询参数（主要是模糊搜索，分页，排序参数）
     */
    private QueryRequest queryRequest;

    /**
     * 统计类型
     */
    private StatisticType statisticType;


    /**
     * 周末、节假日、工作日
     */
    @NotNull(message = "请至少选择一个过滤条件")
    private List<DateFilteringEnum> dateFiltering;

    /**
     *  特征比枚举 
     */
    @Getter
    @AllArgsConstructor
    public enum DateFilteringEnum {

        weekdays("工作日"), holidays("节假日"), weekend("周末");

        private String value;
    }

    // 工作日
    private final String WEEKDAYS = "!(dayofWeek==6 && dayofWeek==7) ";

    // 节假日
    private final String HOLIDAYS = "params.festival.contains(dateTime)";

    // 周末
    private final String WEEKEND = "(dayofWeek==6 || dayofWeek==7) ";


    /**
     *  根据用户选择的时间规律统计, 生成对应过滤脚本参数 
     */
    public String getScriptCondition() {
        StringBuilder script = new StringBuilder(
                "DateTimeFormatter format = DateTimeFormatter.ofPattern('yyyy-MM-dd');" +
                        "String dateTime = format.format(doc['trading_time'].value);" +
                        "int dayofWeek = doc['trading_time'].value.get(ChronoField.DAY_OF_WEEK) ;" +
                        "if(");
        Method[] declaredMethods = ReflectionUtils.getDeclaredMethods(TimeRuleAnalysisDetailRequest.class);
        StringBuilder sb = new StringBuilder();
        for (DateFilteringEnum e : this.dateFiltering) {
            Method findMethod = Arrays.stream(declaredMethods).filter(method -> {
                if (Modifier.isPrivate(method.getModifiers())) {
                    DateFiltering filtering = method.getAnnotation(DateFiltering.class);
                    if (null != filtering) {
                        return e == filtering.value();
                    }
                }
                return false;
            }).findFirst().orElse(null);
            if (null == findMethod) {
                continue;
            }
            ReflectionUtils.makeAccessible(findMethod);
            String scriptParam = (String) ReflectionUtils.invokeMethod(findMethod, this);
            if (sb.length() > 0) {
                sb.append(" || ");
            }
            sb.append(scriptParam);
        }
        script.append(sb.toString());
        if (!CollectionUtils.isEmpty(this.dateFiltering)) {
            script.append("&&");
        }
        script.append(dateFilter.get(this.dateType));

        script.append(tradingTime).append("){return true}");
        return script.toString();
    }

    private static Map<String, String> dateFilter = new HashMap<>();

    static {
        /**
         * 按照月统计.
         * */
        dateFilter.put("M", "doc['trading_time'].value.get(ChronoField.MONTH_OF_YEAR) ==");
        /**
         * 按照周统计.
         * */
        dateFilter.put("w", "doc['trading_time'].value.get(ChronoField.ALIGNED_WEEK_OF_YEAR) ==");
        /**
         * 按照日统计.
         * */
        dateFilter.put("d", "doc['trading_time'].value.get(ChronoField.DAY_OF_MONTH) ==");

        /**
         * 按照时统计.
         * */
        dateFilter.put("h", "doc['trading_time'].value.get(ChronoField.HOUR_OF_DAY) ==");
    }

    /**
     *  工作日 
     */
    @DateFiltering(value = DateFilteringEnum.weekdays)
    private String weekdaysDateFiltering() {

        return WEEKDAYS;
    }

    /**
     *  节假日 
     */
    @DateFiltering(value = DateFilteringEnum.holidays)
    private String holidaysDateFiltering() {

        return HOLIDAYS;
    }

    /**
     *  周末 
     */
    @DateFiltering(value = DateFilteringEnum.weekend)
    private String weekendDateFiltering() {
        return WEEKEND;
    }


}
