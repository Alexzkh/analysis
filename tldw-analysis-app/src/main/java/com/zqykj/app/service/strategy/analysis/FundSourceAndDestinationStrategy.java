package com.zqykj.app.service.strategy.analysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.vo.fund.FundSourceAndDestinationBankRecord;
import com.zqykj.app.service.vo.fund.FundSourceAndDestinationFlowDiagramResponse;
import com.zqykj.app.service.vo.fund.FundSourceAndDestinationResultCardList;
import com.zqykj.app.service.vo.fund.FundSourceAndDestinationResultList;
import com.zqykj.common.request.FundSourceAndDestinationCardResultRequest;
import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.common.response.FundsSourceAndDestinationPieChartStatisticsResponse;
import com.zqykj.common.response.FundsSourceAndDestinationTrendResponse;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.util.JacksonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

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
    List<FundSourceAndDestinationResultCardList> accessFundSourceAndDestinationCardList(FundsSourceAndDestinationStatisticsRequest fundSourceAndDestinationCardResultRequest, String caseId);


    /**
     * 用来计算百分比，主要计算来源、去向账户个数以及金额百分比.
     *
     * @param source: 代表来源或去向的金额或者个数
     * @param dest:   总数或者总金额
     * @return: java.lang.String
     **/
    default String format(Object source, Object dest) {
        if (source instanceof BigDecimal) {
            BigDecimal s = (BigDecimal) source;
            BigDecimal d = (BigDecimal) dest;
            DecimalFormat decimalFormat = new DecimalFormat("##.##%");
            return decimalFormat.format(s.divide(d, 2, RoundingMode.HALF_UP));
        }
        return "";
    }

    /**
     * 用来解析来源去向的结果集
     *
     * @param sourceResult:es返回的来源结果
     * @param destResult:es返回的去向的结果
     * @return: com.zqykj.common.response.FundsSourceAndDestinationPieChartStatisticsResponse
     **/
    default FundsSourceAndDestinationPieChartStatisticsResponse transferPieChartResult(List<List<Object>> sourceResult, List<List<Object>> destResult) {
        BigDecimal sourceMoney = new BigDecimal((String) sourceResult.get(0).get(1)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sourceNumber = new BigDecimal((String) sourceResult.get(0).get(0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal destinationMoney = new BigDecimal((String) destResult.get(0).get(1)).abs().setScale(2, RoundingMode.HALF_UP);
        BigDecimal destinationNumber = new BigDecimal((String) destResult.get(0).get(0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sumMoney = sourceMoney.add(destinationMoney);
        BigDecimal sumNumber = sourceNumber.add(destinationNumber);

        return FundsSourceAndDestinationPieChartStatisticsResponse.builder()
                .destinationAccountMoney(destinationMoney)
                .destinationAccountNumber(destinationNumber)
                .destinationPercentage(format(destinationNumber, sumNumber))
                .destinationMoneyPercentage(format(destinationMoney, sumMoney))
                .sourceAccountMoney(sourceMoney)
                .sourceAccountNumber(sourceNumber)
                .sourcePercentage(format(sourceNumber, sumNumber))
                .sourceMoneyPercentage(format(sourceMoney, sumMoney))
                .transcationAccountTotalMoney(sumMoney)
                .transcationAccountTotalNumber(sumNumber)
                .build();
    }

    default  List<FundSourceAndDestinationBankRecord> accessTopN(AggregationParams aggregationParams ,Map<String, List<List<Object>>> result,List<Map<String, Object>> localEntityMapping){
        // 来源实体数据组装
        List<FundSourceAndDestinationBankRecord> topNResult = JacksonUtils.parse(JacksonUtils.toJson(localEntityMapping), new TypeReference<List<FundSourceAndDestinationBankRecord>>() {
        });
        return  topNResult;
    }



}
