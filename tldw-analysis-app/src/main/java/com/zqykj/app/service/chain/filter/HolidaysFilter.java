package com.zqykj.app.service.chain.filter;

import com.zqykj.app.service.chain.TimeFilter;
import com.zqykj.app.service.vo.fund.TimeFilteringParam;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.domain.base.Festival;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 节假日时间过滤器，过滤出日期时间是在节假日范围类的数据
 * @Author zhangkehou
 * @Date 2022/1/8
 */
@Component
@Order(1)
@Slf4j
public class HolidaysFilter implements TimeFilter {
    @Override
    public Set<TimeRuleAnalysisResult> filter(TimeFilteringParam param, Set<TimeRuleAnalysisResult> resultSet,List<TimeRuleAnalysisResult> results, List<Festival> festivals) {
        if (param.getHolidays()){
          Set<TimeRuleAnalysisResult>  set= results.stream().filter(x->festivals.stream().anyMatch(m -> m.getDateTime().equals(x))).collect(Collectors.toSet());
          resultSet.addAll(set);
        }
        return resultSet;
    }
}
