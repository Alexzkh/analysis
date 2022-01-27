package com.zqykj.app.service.strategy.analysis.impl;

import com.zqykj.app.service.strategy.analysis.TimeRuleAnalysisStrategy;
import com.zqykj.app.service.tools.AccumulationTools;
import com.zqykj.app.service.tools.SetValuesTools;
import com.zqykj.app.service.tools.TimeRuleResultHandlerTools;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisRequest;
import com.zqykj.domain.response.TimeRuleLineChartResponse;
import com.zqykj.domain.response.TimeRuleResultListResponse;
import com.zqykj.domain.vo.TimeRuleLineChartVO;
import com.zqykj.domain.vo.TimeRuleResultListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 汇总
 * @Author zhangkehou
 * @Date 2021/12/30
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SummaryStrategyImpl implements TimeRuleAnalysisStrategy {

    @Override
    public TimeRuleLineChartResponse accessTimeRuleAnalysisLineChartResult(TimeRuleAnalysisRequest request, List<TimeRuleAnalysisResult> results) {
        TimeRuleLineChartResponse response = new TimeRuleLineChartResponse();
        Map<String, TimeRuleLineChartVO> map = new HashMap<>();
        try {
            results.stream().forEach(result -> {
                String date = TimeRuleResultHandlerTools.transfer(result.getTradingTime(), request.getDateType(), request.getStatisticType()).toString();
                TimeRuleLineChartVO timeRuleLineChartVO = (TimeRuleLineChartVO) SetValuesTools.transfer(result, request.getLineChartDisplayType());
                timeRuleLineChartVO.setTimePeriod(date);
                timeRuleLineChartVO.setOriginDate(result.getTradingTime());
                if (map.containsKey(date)) {
                    TimeRuleLineChartVO sum = (TimeRuleLineChartVO) AccumulationTools.transfer(timeRuleLineChartVO, map.get(date), request.getLineChartDisplayType());
                    map.put(date, sum);
                } else {
                    map.put(date, timeRuleLineChartVO);
                }

            });
            // 收集所有的map 的value 值作为volist 的结果集 此时的结果暂不做排序 将结果向上游传递后再排序
            List<TimeRuleLineChartVO> voList = map.values().stream().sorted(Comparator.comparing(vo -> vo.getTimePeriod())).collect(Collectors.toList());
            response.setResult(voList);
            // 此处的魔法值是用来取集合中的总交易金额、交易次数以及交易平均金额。由于集合中每个元素中都有这三个值。所以取集合中第一元素即可获得
            response.setTransactionTotalMoney(results.get(0).getTradeTotalAmount());
            response.setAvgTransactionMoney(results.get(0).getTradeAvgAmount());
            response.setTransactionTotalFrequency(Long.valueOf(results.get(0).getTradeTotalTimes()));
        } catch (Exception e) {
            log.error("统计类型为汇总时,处理elasticsearch结果出错：{}", e);
        }
        return response;
    }

    @Override
    public TimeRuleResultListResponse accessTimeRuleAnalysisResultList(TimeRuleAnalysisRequest request, List<TimeRuleAnalysisResult> list) {

        TimeRuleResultListResponse response = new TimeRuleResultListResponse();
        Map<String, TimeRuleResultListVO> map = new HashMap<>();
        try {

            list.stream().forEach(result -> {
                String date = TimeRuleResultHandlerTools.transfer(result.getTradingTime(), request.getDateType(), request.getStatisticType()).toString();
                if (map.containsKey(date)) {
                    TimeRuleResultListVO originalVo = map.get(date);
                    TimeRuleResultListVO timeRuleResultListVO = accumulationResult(originalVo, result, date);
                    timeRuleResultListVO.setOriginDate(result.getTradingTime());
                    map.put(date, timeRuleResultListVO);
                } else {
                    TimeRuleResultListVO timeRuleResultListVO = accessResultListVO(result, date);
                    timeRuleResultListVO.setOriginDate(result.getTradingTime());
                    map.put(date, timeRuleResultListVO);
                }

            });
            // 收集所有的map 的value 值作为volist 的结果集 此时的结果暂不做排序 将结果向上游传递后再排序
            List<TimeRuleResultListVO> voList = map.values().stream().collect(Collectors.toList());
            response.setResults(voList);
            response.doOrderOrPaging(request.getPaging().getPage()
                    , request.getPaging().getPageSize()
                    , request.getSorting().getProperty()
                    , request.getSorting().getOrder().toString());
        } catch (Exception e) {
            log.error("获取时间规律列表数据出错{},", e);
        }

        return response;
    }


}
