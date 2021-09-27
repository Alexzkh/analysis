package com.zqykj.support;

/**
 * @Description: 聚合结果常量
 * @Author zhangkehou
 * @Date 2021/9/24
 */
public class AggregationResultConstants {

    public interface Multilayer {

        /**
         * 卡号：人员证件号码or查询卡号
         */
        String CARD = "card";

        /**
         * 调单个体数量
         */
        String ACCOUNT_CARD_NUMS = "accountCardNums";

        /**
         * 交易总次数
         */
        String TRANSACTION_TOTAL_NUMS = "transactionTotalNums";

        /**
         * 最早交易时间
         */
        String EARLIEST_TRADING_TIME = "earliestTradingTime";

        /**
         * 最晚交易时间
         */
        String LATEST_TRADING_TIME = "latestTradingTime";

        /**
         * 交易总金额
         */
        String TRANSACTION_TOTAL_AMOUNT = "transactionTotalAmount";

        /**
         * 交易净额
         */
        String TRANSACTION_NET_AMOUNT = "transactionNetAmount";

        /**
         * 进账金额
         */
        String ENTRIES_AMOUNT = "entriesAmount";

        /**
         * 进账笔数
         */
        String ENTRIES_NUMS = "entriesNums";

        /**
         * 出账金额
         */
        String OUTGOING_AMOUNT = "outGoingAmount";

        /**
         * 出账笔数
         */
        String OUTGOING_NUMS = "outGoingNums";

        /**
         * 借贷标志：进
         */
        String ENTRIES = "进";

        /**
         * 借贷标志：出
         */
        String OUTGOING = "出";

        /**
         * 对卡号的聚合
         */
        String TERMS_ACCOUNT_CARD = "terms_account_card";
    }

}
