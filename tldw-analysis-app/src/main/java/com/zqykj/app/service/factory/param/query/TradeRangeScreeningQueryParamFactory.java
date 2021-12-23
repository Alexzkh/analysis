/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.query;

import com.zqykj.app.service.vo.fund.TradeRangeScreeningDataChartRequest;
import com.zqykj.parameters.query.QuerySpecialParams;

/**
 * <h1> 交易区间筛选查询请求参数工厂 </h1>
 */
public interface TradeRangeScreeningQueryParamFactory {

    /**
     * <h2> 查询交易金额 </h2>
     */
    QuerySpecialParams queryTradeAmount(TradeRangeScreeningDataChartRequest request);

    /**
     * <h2> 查询入账金额 或者 出账金额 </h2>
     */
    QuerySpecialParams queryCreditOrPayoutAmount(TradeRangeScreeningDataChartRequest request, boolean isCredit);

    /**
     * <h2> 根据案件Id查询 </h2>
     */
    QuerySpecialParams queryCase(String caseId);
}
