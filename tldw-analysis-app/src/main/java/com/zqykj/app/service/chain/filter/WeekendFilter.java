package com.zqykj.app.service.chain.filter;

import com.xkzhangsan.time.calculator.DateTimeCalculatorUtil;
import com.xkzhangsan.time.converter.DateTimeConverterUtil;
import com.zqykj.app.service.chain.TimeFilter;
import com.zqykj.app.service.vo.fund.TimeFilteringParam;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.domain.base.Festival;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 周末的过滤器 过滤出结果数据全是时间为周末发生的数据
 * @Author zhangkehou
 * @Date 2022/1/8
 */
@Component
@Order(3)
@Slf4j
public class WeekendFilter implements TimeFilter {
    @Override
    public Set<TimeRuleAnalysisResult> filter(TimeFilteringParam param, Set<TimeRuleAnalysisResult> resultSet, List<TimeRuleAnalysisResult> results, List<Festival> festivals) {
        if (param.getWeekend()) {
            Set<TimeRuleAnalysisResult> res = results.stream().filter(x -> {

                try {
                    return DateTimeCalculatorUtil.isWeekend(DateTimeConverterUtil.toLocalDateTime(new SimpleDateFormat("yyyy-MM-dd").parse(x.getTradingTime())));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toSet());
            resultSet.addAll(res);
        }

        return resultSet;
    }
}
