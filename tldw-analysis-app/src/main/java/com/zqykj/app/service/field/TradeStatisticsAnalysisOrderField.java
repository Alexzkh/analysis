/**
 * @作者 Mcj
 */
package com.zqykj.app.service.field;

/**
 * <h1> 交易统计分析排序字段 </h1>
 */
public enum TradeStatisticsAnalysisOrderField {


    //开户名称
    customer_name,

    // 开户证件号码
    customer_identity_card,

    // 开户银行
    bank,

    // 查询账号
    query_account,

    // 交易卡号
    query_card,

    // 交易总次数
    trade_total_times,

    // 交易总金额
    trade_total_amount,

    // 入账次数
    credits_times,

    // 入账金额
    credits_amount,

    // 出账次数
    pay_out_times,

    // 出账金额
    pay_out_amount,

    // 交易净和
    trade_net,

    // 最早交易时间
    earliest_trading_time,

    // 最晚交易时间
    latest_trading_time
}
