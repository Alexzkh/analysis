/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.TradeRangeScreeningDataChartRequest;
import com.zqykj.app.service.vo.fund.TradeRangeScreeningDataChartResult;
import com.zqykj.app.service.vo.fund.TradeRangeScreeningSaveRequest;
import com.zqykj.common.core.ServerResponse;

/**
 * <h1> 交易区间筛选 </h1>
 */
public interface ITradeRangeScreening {

    /**
     * <h2> 获取交易区间筛选数据图表结果 </h2>
     */
    ServerResponse<TradeRangeScreeningDataChartResult> getDataChartResult(TradeRangeScreeningDataChartRequest request);

    /**
     * <h2> 保存交易区间筛选操作记录 </h2>
     */
    ServerResponse<String> saveOperationRecord(TradeRangeScreeningSaveRequest request);
}
