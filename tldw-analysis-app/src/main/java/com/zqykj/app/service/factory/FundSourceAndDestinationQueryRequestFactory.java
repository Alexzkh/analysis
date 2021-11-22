package com.zqykj.app.service.factory;

import com.zqykj.common.enums.FundsResultType;
import com.zqykj.parameters.query.QuerySpecialParams;

/**
 * @Description: 资金战法查询elasticsearch查询请求构建工厂
 * @Author zhangkehou
 * @Date 2021/11/22
 */
public interface FundSourceAndDestinationQueryRequestFactory {


    <T, V> QuerySpecialParams buildFundsSourceAndDestinationAnalysisResquest(T requestParam, V parameter, FundsResultType type);

    /**
     * 构建资金来源去向es前置查询参数.
     *
     * @param requestParam: 资金来源去向body. T -> FundsSourceAndDestinationStatistisRequest.
     * @param parameter:    案件编号.
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildFundsSourceAndDestinationLineChartResquest(T requestParam, V parameter);


    /**
     * 构建资金来源去向中人名下卡的明细查询请求参数.
     *
     * @param requestParam: 资金来源去向body. T -> FundsSourceAndDestinationStatistisRequest.
     * @param parameter:    案件编号.
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildFundsSourceAndDestinationCardResultResquest(T requestParam, V parameter);
}
