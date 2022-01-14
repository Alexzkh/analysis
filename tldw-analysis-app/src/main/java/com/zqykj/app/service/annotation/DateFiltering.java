package com.zqykj.app.service.annotation;

import com.zqykj.app.service.vo.fund.TimeRuleAnalysisDetailRequest;

import java.lang.annotation.*;

/**
 * @Description: 用于标识节假日、周末、工作日的注解
 * @Author zhangkehou
 * @Date 2022/1/14
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface DateFiltering {

    TimeRuleAnalysisDetailRequest.DateFilteringEnum value() default TimeRuleAnalysisDetailRequest.DateFilteringEnum.weekdays;
}
