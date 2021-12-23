/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * <h1> 交易区间筛选数据图表结果 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradeRangeScreeningDataChartResult {

    /**
     * 出账金额
     */
    List<BigDecimal> payoutAmount;

    /**
     * 入账金额
     */
    List<BigDecimal> creditAmount;

    /**
     * 交易金额
     */
    List<BigDecimal> tradeAmount;
}
