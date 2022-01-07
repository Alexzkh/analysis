/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * <h1> 交易区间筛选数据图表请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TradeRangeScreeningDataChartRequest extends FundTacticsPartGeneralRequest {

    /**
     * 卡号集合
     */
    private List<String> cardNums;

    /**
     * 选中查看的起始 交易笔数
     */
    private int startNumberOfTrade = 1;

    /**
     * 选中查看的结束 交易笔数
     */
    private int endNumberOfTrade = 1000;
}
