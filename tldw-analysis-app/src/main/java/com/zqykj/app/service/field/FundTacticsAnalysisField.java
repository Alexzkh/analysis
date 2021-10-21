/**
 * @作者 Mcj
 */
package com.zqykj.app.service.field;


/**
 * <h1> 资金战法分析查询涉及字段 </h1>
 */
public interface FundTacticsAnalysisField {

    /**
     * 查询卡号
     */
    String QUERY_CARD = "query_card";

    /**
     * 客户身份证
     */
    String CUSTOMER_IDENTITY_CARD = "customer_identity_card";

    /**
     * 交易时间
     */
    String TRADING_TIME = "trading_time";

    /**
     * 交易金额
     */
    String TRANSACTION_MONEY = "transaction_money";

    /**
     * 案件id hash
     */
    String CASE_KEY_ID_HASH = "case_key_id";


    /**
     * 案件id
     */
    String CASE_ID = "case_id";

    /**
     * 借贷标识
     */
    String LOAN_FLAG = "loan_flag";

    /**
     * 开户名称
     */
    String CUSTOMER_NAME = "customer_name";

    /**
     * 开户银行
     */
    String BANK = "bank";

    /**
     * 查询账号
     */
    String QUERY_ACCOUNT = "query_account";

    String LOAN_FLAG_OUT = "出";
    String LOAN_FLAG_OUT_EN = "pay_out";

    String LOAN_FLAG_IN = "进";
    String LOAN_FLAG_IN_EN = "credits";

    // 交易统计分析结果 聚合中需要展示的字段
    static String[] tradeStatisticalAggShowField() {

        return new String[]{CUSTOMER_NAME, CUSTOMER_IDENTITY_CARD, BANK, QUERY_ACCOUNT, QUERY_CARD};
    }
}
