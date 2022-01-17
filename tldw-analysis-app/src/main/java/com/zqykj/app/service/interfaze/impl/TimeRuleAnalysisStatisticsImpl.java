package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.chain.filter.TimeFilterFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.TimeRuleAnalysisAggRequestParamFactory;
import com.zqykj.app.service.factory.TimeRuleAnalysisQueryRequestFactory;
import com.zqykj.app.service.interfaze.ITimeRuleAnalysisStatistics;
import com.zqykj.app.service.strategy.analysis.impl.TimeRuleStatisticsAnalysisFactory;
import com.zqykj.app.service.vo.fund.TimeFilteringParam;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.common.enums.StatisticType;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisDetailRequest;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisRequest;
import com.zqykj.core.aggregation.impl.AggregatedPageImpl;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.base.Festival;
import com.zqykj.domain.response.TimeRuleLineChartResponse;
import com.zqykj.domain.response.TimeRuleResultListResponse;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 时间规律业务接口实现
 * @Author zhangkehou
 * @Date 2022/1/6
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TimeRuleAnalysisStatisticsImpl implements ITimeRuleAnalysisStatistics {


    private final TimeRuleAnalysisQueryRequestFactory timeRuleAnalysisQueryRequestFactory;

    private final TimeRuleAnalysisAggRequestParamFactory timeRuleAnalysisAggRequestParamFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final EntranceRepository entranceRepository;

    private final TimeRuleStatisticsAnalysisFactory factory;

    private final TimeFilterFactory timeFilterFactory;

    @Override
    public TimeRuleLineChartResponse accessLineChartResult(TimeRuleAnalysisRequest request, String caseId) {

        TimeRuleLineChartResponse response;
        try {
            // 根据条件获取elasticsearch查询聚合计算的结果。
            List<TimeRuleAnalysisResult> results = getStatisticResult(request, caseId);
            response = factory.access(request.getStatisticType()).accessTimeRuleAnalysisLineChartResult(request, results);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @Override
    public TimeRuleResultListResponse accessTimeRuleResultList(TimeRuleAnalysisRequest request, String caseId) {
        TimeRuleResultListResponse response;
        try {
            if (request.getDateType().equals("h") && request.getStatisticType().equals(StatisticType.SINGLE)) {
                throw new RuntimeException("统计方式为单一时-【" + request.getStatisticType() + "】-不支持以小时为周期的统计分析！");
            }
            List<TimeRuleAnalysisResult> results = getStatisticResult(request, caseId);
            response = factory.access(request.getStatisticType()).accessTimeRuleAnalysisResultList(request, results);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    public Page<BankTransactionFlow> accessTimeRuleAnalysisResultDetail(TimeRuleAnalysisDetailRequest request, String caseId) {
        try {
            List<String> festival = new ArrayList<>();
            if (request.getDateFiltering().stream().anyMatch(a -> a.equals(TimeRuleAnalysisDetailRequest.DateFilteringEnum.holidays))) {
                AggregatedPageImpl<Festival> festivals = (AggregatedPageImpl<Festival>) entranceRepository.findAll("festival", Festival.class);
                List<Festival> festivalList = festivals.getContent();
                festival = festivalList.stream().map(Festival::getDateTime).collect(Collectors.toList());
            }
            QuerySpecialParams querySpecialParams = timeRuleAnalysisQueryRequestFactory.buildTimeRuleAnalysisDetailQueryRequest(request, caseId, festival);
            PageRequest destPageRequest = new PageRequest(request.getQueryRequest().getPaging().getPage()
                    , request.getQueryRequest().getPaging().getPageSize()
                    , Sort.by(request.getQueryRequest().getSorting().getOrder().isDescending()
                            ? Sort.Direction.DESC : Sort.Direction.ASC
                    , request.getQueryRequest().getSorting().getProperty()));
            Page<BankTransactionFlow> result = entranceRepository.findAll(destPageRequest, caseId, BankTransactionFlow.class, querySpecialParams);

            return result;
        } catch (Exception e) {
            log.error("查询详情时失败：{}",e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 根据查询和聚合条件获取统计结果
     *
     * @param request: 时间规律分析请求体
     * @param caseId:  案件编号
     * @return: java.util.List<com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult>
     **/
    private List<TimeRuleAnalysisResult> getStatisticResult(TimeRuleAnalysisRequest request, String caseId) {

        List<TimeRuleAnalysisResult> results = new ArrayList<>();
        List<TimeRuleAnalysisResult> re = new ArrayList<>();
        try {
            QuerySpecialParams querySpecialParams = timeRuleAnalysisQueryRequestFactory.buildTimeRuleAnalysisQueryRequest(request, caseId);
            AggregationParams aggregationParams = timeRuleAnalysisAggRequestParamFactory.bulidTimeRuleAnalysisAggParams(request, caseId);
            Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);
            List<String> sourceOppositeTitles = new ArrayList<>(aggregationParams.getEntityAggColMapping().keySet());
            List<Map<String, Object>> localEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                    result.get(aggregationParams.getResultName()), sourceOppositeTitles, TimeRuleAnalysisResult.class);
            // 来源实体数据组装
            results = com.zqykj.util.JacksonUtils.parse(com.zqykj.util.JacksonUtils.toJson(localEntityMapping), new com.fasterxml.jackson.core.type.TypeReference<List<TimeRuleAnalysisResult>>() {
            });

            AggregatedPageImpl<Festival> festivals = (AggregatedPageImpl<Festival>) entranceRepository.findAll("festival", Festival.class);
            List<Festival> festivalList = festivals.getContent();
            // 根据入参判断是否是工作日、节假日、工作日

            re = timeFilterFactory.check(new TimeFilteringParam(request.getWeekdays(), request.getHolidays(), request.getWeekend()), results, festivalList);
        } catch (Exception e) {
            log.error("时间规律战法分析获取折线图结果出错：{}", e);
        }


        return re;
    }

}
