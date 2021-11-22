package com.zqykj.app.service.strategy.analysis;

import com.zqykj.app.service.vo.fund.FundSourceAndDestinationFlowDiagramResponse;
import com.zqykj.app.service.vo.fund.FundSourceAndDestinationResultCardList;
import com.zqykj.app.service.vo.fund.FundSourceAndDestinationResultList;
import com.zqykj.common.request.FundSourceAndDestinationCardResultRequest;
import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.common.response.FundsSourceAndDestinationPieChartStatisticsResponse;
import com.zqykj.common.response.FundsSourceAndDestinationTrendResponse;

import java.util.List;

/**
 * @Description: 资金来源去向业务实现策略接口
 * @Author zhangkehou
 * @Date 2021/11/18
 */
public interface FundSourceAndDestinationStrategy {

    /**
     * 资金来源去向--获取topN结果数据.
     *
     * @param fundsSourceAndDestinationStatisticsRequest:资金来源去向聚合和查询请求入参
     * @param caseId:案件编号用作路由id
     * @return: com.zqykj.app.service.vo.fund.FundSourceAndDestinationFlowDiagramResponse
     **/
    FundSourceAndDestinationFlowDiagramResponse accessFundSourceAndDestinationTopN(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId);

    /**
     * 资金来源去向获取--趋势折线图数据
     *
     * @param fundsSourceAndDestinationStatisticsRequest:资金来源去向聚合和查询请求入参
     * @param caseId:案件编号用作路由id
     * @return: com.zqykj.common.response.FundsSourceAndDestinationTrendResponse
     **/
    FundsSourceAndDestinationTrendResponse accessFundSourceAndDestinationTrend(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId);


    /**
     * 资金来源去向--获取列表数据
     *
     * @param fundsSourceAndDestinationStatisticsRequest: 资金来源去向聚合和查询请求入参
     * @param caseId:                                     案件编号用作路由id
     * @return: java.util.List<com.zqykj.common.response.FundsSourceAndDestinationListResponse>
     **/
    List<FundSourceAndDestinationResultList> accessFundSourceAndDestinationList(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId);


    /**
     * 资金来源去向--获取饼图数据
     *
     * @param fundsSourceAndDestinationStatisticsRequest: 资金来源去向聚合和查询请求入参
     * @param caseId:                                     案件编号用作路由id
     * @return: com.zqykj.common.response.FundsSourceAndDestinationPieChartStatisticsResponse
     **/
    FundsSourceAndDestinationPieChartStatisticsResponse accessFundSourceAndDestinationPieChart(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId);


    /**
     * @param fundSourceAndDestinationCardResultRequest: 资金来源去向聚合和查询请求入参
     * @param caseId:                                    案件编号用作路由id
     * @return: java.util.List<com.zqykj.app.service.vo.fund.FundSourceAndDestinationResultCardList>
     **/
    List<FundSourceAndDestinationResultCardList> accessFundSourceAndDestinationCardList(FundSourceAndDestinationCardResultRequest fundSourceAndDestinationCardResultRequest, String caseId);
}
