/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import com.zqykj.util.BigDecimalUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 交易区间筛选操作记录 个体银行卡部分统计结果 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@Agg(name = "hits")
@Key
public class TradeOperationIndividualBankCardsStatistical {

    // 交易卡号
    @Agg(name = "query_card", showField = true)
    @Key(name = "query_card")
    private String tradeCard;

    // 开户银行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    private String bank;

    // 最早交易时间
    @Agg(name = "min_date")
    @Key(name = "valueAsString")
    @Sort(name = "min_date")
    private String earliestTradingTime;

    // 最晚交易时间
    @Agg(name = "max_date")
    @Key(name = "valueAsString")
    @Sort(name = "max_date")
    private String latestTradingTime;

    // 交易笔数
    @Agg(name = "trade_times")
    @Key(name = "value")
    @Sort(name = "trade_times")
    private int tradeTotalTimes;

    // 交易金额
    @Agg(name = "trade_amount")
    @Key(name = "valueAsString")
    @Sort(name = "trade_amount")
    private BigDecimal tradeTotalAmount;

    public static void amountReservedTwo(TradeOperationIndividualBankCardsStatistical individualBankCardsStatistical) {
        individualBankCardsStatistical.setTradeTotalAmount(BigDecimalUtil.value(individualBankCardsStatistical.getTradeTotalAmount()));
    }
}
