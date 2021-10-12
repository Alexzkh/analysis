package com.zqykj.common.enums;

/**
 * @Description: 交易统计-柱状图统计结果依据的字段
 * @Author zhangkehou
 * @Date 2021/10/9
 */
public enum HistogramField {

    /**
     * 交易金额
     */
    TRANSACTION_AMOUNT,

    /**
     * 入账金额(借贷标志为:进)
     */
    RECORDED_AMOUNT,

    /**
     * 出账金额(借贷标志为:出)
     */
    OUTGOING_AMOUNT;
}
