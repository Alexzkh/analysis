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
     * 合并卡号(查询卡号 - 对方卡号)
     */
    String MERGE_CARD = "merge_card";

    /**
     * 合并账号(本方账号 - 对方账号)
     */
    String MERGE_IDENTITY_CARD = "merge_identity_card";

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
     * 交易金额（交易记录表中,不区分正负的金额）
     */
    String CHANGE_AMOUNT = "change_amount";


    /**
     * 案件id hash
     */
    String CASE_KEY_ID_HASH = "case_key_id";

    /**
     * 翻转标记  1: 代表原始记录  2:  代表的是把对方的 放到本方, 本方放到对方
     */
    String REVERSE_MARK = "reverse_mark";

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

    String OPPOSITE_IDENTITY_CARD_WILDCARD = "transaction_opposite_card.opposite_card_wildcard";

    String LOAN_FLAG_OUT = "出";
    String LOAN_FLAG_OUT_EN = "pay_out";

    String LOAN_FLAG_IN = "进";
    String LOAN_FLAG_IN_EN = "credits";


    String FLOW_ID = "flow_id";

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

    /**
     * 交易金额 {@link com.zqykj.domain.bank.BankTransactionRecord}
     */
    String CHANGE_MONEY = "change_amount";

    /**
     * 去向的 桶过滤脚本
     */
    String SELECTOR_SCRIPT_DESTINATION = "params.final_sum < 0";

    /**
     * 来源的 桶过滤脚本
     */
    String SELECTOR_SCRIPT_SOURCE = "params.final_sum >=0";

    /**
     * 来源的 桶过滤脚本
     */
    String SELECTOR_SCRIPT_LINE_CHART = "params.final_sum >= 0";

    /**
     * 管道聚合,selector名称
     */
    String PIPLINE_SELECTOR_BUCKET_NAME = "sum_bucket_selector";

    /**
     * 管道聚合,selector名称
     */
    String PIPLINE_SUM_BUCKET = "card_number_sum_bucket";

    /**
     * 管道聚合,selector名称
     */
    String PIPLINE_TRANSACTION_NET_NUMBER = "transaction_net_amount";


    /**
     * 管道聚合结果的名称
     */
    String SUB_AGG_SUM_NAME = "final_sum";

    /**
     * 统计交易净额
     */
    String TRANSACTION_MONEY_SUM = "transaction_money_sum";

    /**
     * 折现图过滤条件
     */
    String LINE_CHART_SELECTOR = "opposite_out_times>opposite_out_amount";

    /**
     * 资金来源去向桶名称
     */
    String MULTI_IDENTITY_TERMS = "multi_identity_terms";


    // 交易统计分析结果本方需要展示的字段
    static String[] tradeStatisticalAnalysisLocalShowField() {

        return new String[]{CUSTOMER_NAME, CUSTOMER_IDENTITY_CARD, BANK, QUERY_ACCOUNT, QUERY_CARD};
    }

    // 交易统计分析结果本方需要展示的字段
    static String[] tradeStatisticalAnalysisOppositeShowField() {

        return new String[]{TRANSACTION_OPPOSITE_NAME, OPPOSITE_IDENTITY_CARD, OPPOSITE_BANK, OPPOSITE_ACCOUNT, TRANSACTION_OPPOSITE_CARD};
    }

    // 交易统计分析结果本方需要展示的字段
    static String[] tradeStatisticalAnalysisOppositeResultShowField() {

        return new String[]{TRANSACTION_OPPOSITE_NAME, OPPOSITE_IDENTITY_CARD};
    }

    // 交易汇聚分析需要展示的字段
    static String[] tradeConvergenceAnalysisShowField() {

        return new String[]{CUSTOMER_NAME, CUSTOMER_IDENTITY_CARD, BANK, QUERY_CARD, TRANSACTION_OPPOSITE_NAME, OPPOSITE_IDENTITY_CARD,
                OPPOSITE_BANK, TRANSACTION_OPPOSITE_CARD, MERGE_CARD};
    }

    // 交易统计分析结果本方需要展示的字段
    static String[] fundSourceAndDestinationAnalysisOppositeShowField() {

        return new String[]{CUSTOMER_NAME, CUSTOMER_IDENTITY_CARD, BANK, QUERY_ACCOUNT, QUERY_CARD};
    }

    // 快进快出分析结果展示字段(本方的)
    static String[] fastInFastOutFields() {

        return new String[]{FundTacticsAnalysisField.QUERY_CARD, FundTacticsAnalysisField.CUSTOMER_NAME,
                FundTacticsAnalysisField.TRADING_TIME, FundTacticsAnalysisField.CHANGE_AMOUNT,
                FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD};
    }
}
