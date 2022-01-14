package com.zqykj.app.service.chain;

import com.zqykj.app.service.vo.fund.TimeFilteringParam;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.domain.base.Festival;

import java.util.List;
import java.util.Set;

/**
 * @Description: 自定义拦截器接口
 * @Author zhangkehou
 * @Date 2022/1/8
 */
public interface TimeFilter {

    /**
     * @param param:   时间过滤器判断参数
     * @param results: 需要过滤的数据，此数据是elasticsearch返回查询聚合结果
     * @return: java.util.List<com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult>
     **/
    Set<TimeRuleAnalysisResult> filter(TimeFilteringParam param, Set<TimeRuleAnalysisResult> resultSet, List<TimeRuleAnalysisResult> results, List<Festival> festivals);
}
