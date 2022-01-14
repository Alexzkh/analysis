package com.zqykj.app.service.chain.filter;

import com.zqykj.app.service.chain.TimeFilter;
import com.zqykj.app.service.vo.fund.TimeFilteringParam;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.domain.base.Festival;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 时间过滤器工厂类. 用于执行实现了{@link TimeFilter}接口的实现类
 * @Author zhangkehou
 * @Date 2022/1/8
 */
@Component
public class TimeFilterFactory {

    @Autowired
    List<TimeFilter> filters;

    public List<TimeRuleAnalysisResult> check(TimeFilteringParam param, List<TimeRuleAnalysisResult> results, List<Festival> festivals) {

        List<TimeRuleAnalysisResult> timeRuleAnalysisResults = new ArrayList<>();
        Set<TimeRuleAnalysisResult> resultSet = new HashSet<>();
        for (TimeFilter filter : filters) {
            resultSet = filter.filter(param, resultSet, results, festivals);
        }
        timeRuleAnalysisResults = resultSet.stream().collect(Collectors.toList());
        return timeRuleAnalysisResults;
    }

}
