package com.zqykj.app.service.strategy.analysis.impl;

import com.zqykj.app.service.strategy.analysis.TimeRuleAnalysisStrategy;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 单一
 * @Author zhangkehou
 * @Date 2021/12/30
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SingleStrategyImpl implements TimeRuleAnalysisStrategy {


    @Override
    public TimeRuleLineChartResponse accessTimeRuleAnalysisLineChartResult(TimeRuleAnalysisRequest request, List<TimeRuleAnalysisResult> results) {
        TimeRuleLineChartResponse response = new TimeRuleLineChartResponse();
        List<TimeRuleLineChartVO> voList = new ArrayList<>();

        try {
            if (request.getDateType().equals("h")) {
                throw new RuntimeException("统计方式为单一时-【" + request.getStatisticType() + "】-不支持以小时为周期的统计分析！");
            }
            results.stream().forEach(timeRuleAnalysisResult -> {
                // 根据传入的折线图展示类型获取到vo结果
                TimeRuleLineChartVO timeRuleLineChartVO = (TimeRuleLineChartVO) SetValuesTools.transfer(timeRuleAnalysisResult, request.getLineChartDisplayType());
                timeRuleLineChartVO.setOriginDate(timeRuleAnalysisResult.getTradingTime());
                timeRuleLineChartVO.setTimePeriod(TimeRuleResultHandlerTools.transfer(timeRuleAnalysisResult.getTradingTime(), request.getDateType(), request.getStatisticType()).toString());
                voList.add(timeRuleLineChartVO);
            });
            List<TimeRuleLineChartVO> result = voList.stream().sorted(Comparator.comparing(vo -> vo.getTimePeriod())).collect(Collectors.toList());
            response.setResult(result);
            response.setTransactionTotalMoney(results.get(0).getTradeTotalAmount());
            response.setAvgTransactionMoney(results.get(0).getTradeAvgAmount());
            response.setTransactionTotalFrequency(Long.valueOf(results.get(0).getTradeTotalTimes()));
        } catch (Exception e) {
            log.error("根据elasticsearch返回结果构建时间规律折线图结果出错：{}", e);
        }

        return response;
    }

    @Override
    public TimeRuleResultListResponse accessTimeRuleAnalysisResultList(TimeRuleAnalysisRequest request, List<TimeRuleAnalysisResult> list) {
        // 需要计算单一的
        TimeRuleResultListResponse response = new TimeRuleResultListResponse();
        List<TimeRuleResultListVO> timeRuleResultListVOs = new ArrayList<>();
        try {
            list.stream().forEach(result -> {
                String timePeriod = TimeRuleResultHandlerTools.transfer(result.getTradingTime(), request.getDateType(), request.getStatisticType()).toString();
                TimeRuleResultListVO timeRuleResultListVO = accessResultListVO(result, timePeriod);
                timeRuleResultListVO.setOriginDate(result.getTradingTime());
                timeRuleResultListVOs.add(timeRuleResultListVO);
            });

            response.setResults(timeRuleResultListVOs);
            response.doOrderOrPaging(request.getPaging().getPage()
                    , request.getPaging().getPageSize()
                    , request.getSorting().getProperty()
                    , request.getSorting().getOrder().toString());

        } catch (Exception e) {
            log.error("根据elasticsearch返回结果构建时间规律列表数据出错：{} ", e);
        }

        return response;
    }

}
