/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.query;

import com.zqykj.app.service.vo.fund.TradeRangeScreeningDataChartRequest;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

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
     * <h2> 根据条件查询调单卡号的交易记录 </h2>
     */
    QuerySpecialParams queryAdjustCardsTradeRecord(String caseId, List<String> adjustCards, double minAmount, double maxAmount);
}
