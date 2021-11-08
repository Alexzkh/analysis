package com.zqykj.app.service.interfaze;

import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.common.response.FundsSourceAndDestinationStatisticsResponse;


/**
 * @Description: 资金来源去向战法业务接口
 * @Author zhangkehou
 * @Date 2021/11/6
 */
public interface IFundsSourceAndDestinationStatistics {


    /**
     * 资金来源去向战法统计业务接口.
     * @param fundsSourceAndDestinationStatisticsRequest: 资金来源去向请求体.
     * @param caseId: 案件编号
     * @return: com.zqykj.common.response.FundsSourceAndDestinationStatisticsResponse
     **/
    FundsSourceAndDestinationStatisticsResponse accessFundsSourceAndDestinationStatisticsResult(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId);




}
