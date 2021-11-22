/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class TradeStatisticalAnalysisFundSumByDate {

    /**
     * 日期参数
     */
    @Key(name = "keyAsString")
    @Agg(name = "date_group")
    private Set<String> dates;

    /**
     * 交易金额汇总
     */
    @Key(name = "valueAsString")
    @Agg(name = "trade_amount_sum")
    private List<BigDecimal> tradeAmounts;
}
