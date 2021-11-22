package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.interfaze.IFundsSourceAndDestinationStatistics;
import com.zqykj.app.service.strategy.analysis.impl.FundSourceAndDestinationFactory;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.request.FundSourceAndDestinationCardResultRequest;
import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.common.response.FundsSourceAndDestinationPieChartStatisticsResponse;
import com.zqykj.common.response.FundsSourceAndDestinationTrendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Description: 资金来源去向战法业务接口
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundsSourceAndDestinationStatisticsImpl implements IFundsSourceAndDestinationStatistics {




    private final FundSourceAndDestinationFactory fundSourceAndDestinationFactory;

    @Override
    public FundSourceAndDestinationFlowDiagramResponse accessFundSourceAndDestinationTopN(FundsSourceAndDestinationStatisticsRequest request, String caseId) throws Exception {
       return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationTopN(request,caseId);
    }

    @Override
    public FundsSourceAndDestinationTrendResponse accessFundSourceAndDestinationTrend(FundsSourceAndDestinationStatisticsRequest request, String caseId) throws Exception {
        return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationTrend(request,caseId);
    }

    @Override
    public List<FundSourceAndDestinationResultList> accessFundSourceAndDestinationList(FundsSourceAndDestinationStatisticsRequest request, String caseId) throws Exception {
        return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationList(request,caseId);
    }

    @Override
    public FundsSourceAndDestinationPieChartStatisticsResponse accessFundSourceAndDestinationPieChart(FundsSourceAndDestinationStatisticsRequest request, String caseId) throws Exception {
        return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationPieChart(request,caseId);
    }

    @Override
    public List<FundSourceAndDestinationResultCardList> accessFundSourceAndDestinationCardList(FundSourceAndDestinationCardResultRequest request, String caseId) throws Exception {
        return fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationCardList(request,caseId);
    }




}
