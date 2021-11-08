package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 资金来源去向数据返回体
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundsSourceAndDestinationStatisticsResponse {

    /**
     * 来源topN列表
     */
    private List<FundsSourceAndDestinationTopNResponse> sourceTopNResponses;

    /**
     * 去向topN列表
     */
    private List<FundsSourceAndDestinationTopNResponse> destinationTopNResponses;

    /**
     * 来源趋势列表
     */
    private List<FundsSourceAndDestinationTrendResponse> sourceTrendResponses;

    /**
     * 去向趋势列表
     */
    private List<FundsSourceAndDestinationTrendResponse> destinationTrendResponses;

    /**
     * 调单个体列表
     */
    private List<FundsSourceAndDestinationListResponse> identityCardResponses;

    /**
     * 资金来源去向饼图返回体
     */
    private FundsSourceAndDestinationPieChartStatisticsResponse fundsSourceAndDestinationPieChartStatisticsResponse;


}
