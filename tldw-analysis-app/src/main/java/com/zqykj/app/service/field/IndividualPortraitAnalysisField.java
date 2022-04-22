package com.zqykj.app.service.field;

/**
 * 个体画像查询涉及字段名称
 *
 * @author: SunChenYu
 * @date: 2021年11月16日 11:32:01
 */
public interface IndividualPortraitAnalysisField {
    /**
     * 案件编号
     */
    String CASE_ID = "case_id";

    /**
     * 查询卡号
     */
    String QUERY_CARD = "query_card";


    /**
     * 客户身份证号码
     */
    String CUSTOMER_IDENTITY_CARD = "customer_identity_card";

    /**
     * 借贷标志
     */
    String LOAN_FLAG = "loan_flag";

    /**
     * 交易金额
     */
    String TRANSACTION_MONEY = "transaction_money";

    /**
     * 交易时间
     */
    String TRADING_TIME = "trading_time";

    interface AggResultName {
        // 身份证号分桶聚合名称（1）
        String CUSTOMER_IDENTITY_CARD_TERMS = "customerIdentityCardTerms";

        // 查询卡号分桶聚合名称（1.1）
        String QUERY_CARD_TERMS = "queryCardTerms";
        // 单卡入账金额过滤（1.1.1）
        String ENTRY_FILTER = "entryFilter";
        // 单卡入账金额统计（1.1.1.1）
        String ENTRY_TRANSACTION_MONEY_SUM = "entryTransactionMoneySum";
        // 入账次数
        String ENTRY_TRANSACTION_TIMES = "entryTransactionTimes";
        // 单卡出账金额过滤（1.1.2）
        String OUT_FILTER = "outFilter";
        // 单卡出账金额统计（1.1.2.1）
        String OUT_TRANSACTION_MONEY_SUM = "outTransactionMoneySum";
        // 出账次数
        String OUT_TRANSACTION_TIMES = "outTransactionTimes";
        // 单卡交易净额(1.1.3)
        String NET_TRANSACTION_MONEY = "netTransactionMoney";
        // 单卡交易总金额(1.1.4)
        String TOTAL_TRANSACTION_MONEY = "totalTransactionMoney";
        // 单卡交易进账总金额（1.1.5）
        String TOTAL_ENTRY_TRANSACTION_MONEY = "totalEntryTransactionMoney";
        // 单卡交易出账总金额（1.1.6）
        String TOTAL_OUT_TRANSACTION_MONEY = "totalOutTransactionMoney";
        // 单卡基本信息top_hits(1.1.7)
        String QUERY_CARD_TOP_HITS = "queryCardTopHits";
        // 单卡最早交易时间聚合名称（1.1.8）
        String EARLIEST_TRADING_TIME = "earliestTradingTime";
        // 单卡最晚交易时间聚合名称（1.1.9）
        String LATEST_TRADING_TIME = "latestTradingTime";
        // 单卡交易总次数
        String TOTAL_TRANSACTION_TIMES = "totalTransactionTimes";

        // 成功累计收入金额(1.2)
        String CUMULATIVE_INCOME = "cumulativeIncome";
        // 成功累计支出金额(1.3)
        String CUMULATIVE_EXPENDITURE = "cumulativeExpenditure";
        // 成功累计交易净额
        String CUMULATIVE_NET = "cumulativeNet";

        // 桶排序
        String BUCKET_SORT_NAME = "bucketSort";
    }

    interface AggResultField {
        String VALUE = "value";
        String VALUE_AS_STRING = "valueAsString";
        String HITS = "hits";
    }

    interface Value {
        String LOAN_FLAG_IN = "进";
        String LOAN_FLAG_OUT = "出";
        String[] INCLUDES_TOP_HITS = {
                "query_account",
                "query_card",
                "bank",
                "transaction_balance",
                "customer_name",
                "customer_identity_card"
        };
    }

    interface ResultName {
        String CUSTOMER_IDENTITY_CARD = "customerIdentityCardTerms";
    }

}
