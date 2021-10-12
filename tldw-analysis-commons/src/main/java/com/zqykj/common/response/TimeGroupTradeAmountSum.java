/**
 * @作者 Mcj
 */
package com.zqykj.common.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * <h1> 时间类型分组 统计交易金额 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TimeGroupTradeAmountSum {

    /**
     * 时间
     */
    private Set<String> dates;

    /**
     * 交易金额汇总
     */
    private List<String> tradeAmounts;
}
