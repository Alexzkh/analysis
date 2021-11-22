package com.zqykj.app.service.field;

/**
 * 单卡画像查询涉及字段名称
 *
 * @author: SunChenYu
 * @date: 2021年11月16日 11:32:01
 */
public interface SingleCardPortraitAnalysisField {
    /**
     * 案件编号
     */
    String CASE_ID = "case_id";

    /**
     * 查询账号
     */
    String QUERY_ACCOUNT = "query_account";
    /**
     * 查询卡号
     */
    String QUERY_CARD = "query_card";

    /**
     * 客户名称
     */
    String CUSTOMER_NAME = "customer_name";

    /**
     * 客户身份证号码
     */
    String CUSTOMER_IDENTITY_ID = "customer_identity_id";

    /**
     * 开户银行
     */
    String BANK = "bank";

    /**
     * 交易对方姓名
     */
    String TRANSACTION_OPPOSITE_NAME = "transaction_opposite_name";

    /**
     * 交易对方证件号码
     */
    String TRANSACTION_OPPOSITE_CERTIFICATE_NUMBER = "transaction_opposite_certificate_number";

    /**
     * 交易对方账号
     */
    String TRANSACTION_OPPOSITE_ACCOUNT = "transaction_opposite_account";

    /**
     * 交易对方卡号
     */
    String TRANSACTION_OPPOSITE_CARD = "transaction_opposite_card";

    /**
     * 交易类型
     */
    String TRANSACTION_TYPE = "transaction_type";

    /**
     * 借贷标志
     */
    String LOAN_FLAG = "loan_flag";

    /**
     * 交易金额
     */
    String TRANSACTION_MONEY = "transaction_money";

    /**
     * 交易余额
     */
    String TRANSACTION_BALANCE = "transaction_balance";

    /**
     * 交易时间
     */
    String TRADING_TIME = "trading_time";

    /**
     * 交易对方余额
     */
    String TRANSACTION_OPPOSITE_BALANCE = "transaction_opposite_balance";

    /**
     * 交易对方开户行
     */
    String TRANSACTION_OPPOSITE_ACCOUNT_OPEN_BANK = "transaction_opposite_account_open_bank";

    /**
     * 交易是否成功标志
     */
    String TRANSACTION_SUCCESS_FLAG = "transaction_success_flag";

    interface AggResultName {
        // 查询卡号聚合分桶名称（第一层）
        String LOCAL_CARD_TERMS = "localCardTerms";
        // 最早交易时间聚合名称（第一层）
        String EARLIEST_TRADING_TIME = "earliestTradingTime";
        // 最晚交易时间聚合名称（第一层）
        String LATEST_TRADING_TIME = "latestTradingTime";

        String LOCAL_IN_TRANSACTION_MONEY_SUM = "localInTransactionMoneySum";
        String LOCAL_IN_TRANSACTION_MONEY = "localInTransactionMoney";
        String LOCAL_OUT_TRANSACTION_MONEY_SUM = "localOutTransactionMoneySum";
        String LOCAL_OUT_TRANSACTION_MONEY = "localOutTransactionMoney";
        String LOCAL_TOTAL_TRANSACTION_MONEY_SUM = "localTotalTransactionMoneySum";
        String LOCAL_HITS = "localHits";
    }

    interface AggResultField {
        String VALUE = "value";
        String VALUE_AS_STRING = "valueAsString";
        String HITS = "hits";
    }

    interface Value {
        String LOAN_FLAG_IN = "进";
        String LOAN_FLAG_OUT = "出";
        String[] LOCAL_INCLUDES_TOP_HITS = {
                "customer_name",
                "customer_identity_card",
                "query_account",
                "query_card",
                "bank",
                "transaction_balance",
                "trading_time"
        };
    }

    interface ResultName {
        String QUERY_CARD_TERMS = "query_card_terms";
        String EARLIEST_TRADING_TIME = "earliest_trading_time";
        String LATEST_TRADING_TIME = "latest_trading_time";
    }

}
