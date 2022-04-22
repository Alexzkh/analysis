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
 * @Description: 工作日时间过滤器.过滤出时间日期是工作日的过滤器
 * @Author zhangkehou
 * @Date 2022/1/8
 */
@Component
@Order(2)
@Slf4j
public class WeekdaysFilter implements TimeFilter {
    @Override
    public Set<TimeRuleAnalysisResult> filter(TimeFilteringParam param, Set<TimeRuleAnalysisResult> resultSet, List<TimeRuleAnalysisResult> results, List<Festival> festivals) {
        if (param.getWeekdays()) {
          Set<TimeRuleAnalysisResult>  resu = results.stream().filter(x -> {
                try {

                    return DateTimeCalculatorUtil.isWorkDay(DateTimeConverterUtil.toLocalDateTime(new SimpleDateFormat("yyyy-MM-dd").parse(x.getTradingTime())) )&& !festivals.stream().anyMatch(m -> m.getDateTime().equals(x.getTradingTime()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toSet());
            resultSet.addAll(resu);

        }
        return resultSet;
    }
}
