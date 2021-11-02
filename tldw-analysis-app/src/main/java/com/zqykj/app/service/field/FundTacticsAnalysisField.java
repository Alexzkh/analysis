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
     * 交易对方卡号
     */
    String TRANSACTION_OPPOSITE_CARD = "transaction_opposite_card";

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
     * 对方开户名称
     */
    String TRANSACTION_OPPOSITE_NAME = "transaction_opposite_name";

    /**
     * 对方开户银行
     */
    String OPPOSITE_BANK = "transaction_opposite_account_open_bank";

    /**
     * 开户银行
     */
    String BANK = "bank";

    /**
     * 查询账号
     */
    String QUERY_ACCOUNT = "query_account";

    /**
     * 对方开户账号
     */
    String OPPOSITE_ACCOUNT = "transaction_opposite_account";

    /**
     * 交易对方开户证件号码
     */
    String OPPOSITE_IDENTITY_CARD = "transaction_opposite_certificate_number";

    String LOAN_FLAG_OUT = "出";
    String LOAN_FLAG_OUT_EN = "pay_out";

    String LOAN_FLAG_IN = "进";
    String LOAN_FLAG_IN_EN = "credits";

    /**
     * 省份对应字段
     */
    String PROVINCE_FIELD = "province.province_wildcard";

    /**
     * 城市对应字段
     */
    String CITY_FIELD = "city.city_wildcard";

    /**
     * 区县对应字段
     */
    String AREA_FIELD = "area.area_wildcard";

    // 交易统计分析结果本方需要展示的字段
    static String[] tradeStatisticalAnalysisLocalShowField() {

        return new String[]{CUSTOMER_NAME, CUSTOMER_IDENTITY_CARD, BANK, QUERY_ACCOUNT, QUERY_CARD};
    }

    // 交易统计分析结果本方需要展示的字段
    static String[] tradeStatisticalAnalysisOppositeShowField() {

        return new String[]{TRANSACTION_OPPOSITE_NAME, OPPOSITE_IDENTITY_CARD, OPPOSITE_BANK, OPPOSITE_ACCOUNT, TRANSACTION_OPPOSITE_CARD};
    }
}
