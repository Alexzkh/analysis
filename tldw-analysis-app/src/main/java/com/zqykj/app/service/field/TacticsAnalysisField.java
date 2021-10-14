/**
 * @作者 Mcj
 */
package com.zqykj.app.service.field;


/**
 * 战法分析查询涉及字段
 */
public interface TacticsAnalysisField {

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

}
