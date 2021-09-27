package com.zqykj.common;


/**
 * @Description: 常量类
 * @Author zhangkehou
 * @Date 2021/9/23
 */
public class Constants {

    public interface Individual {
        /**
         * 查询证件号码
         */
        String FIRST_AGGREGATE_NAME = "customerIdentityCard";

        /**
         * 外层聚合结果个数
         */
        int FIRST_AGGS_SIZE = 25;

        /**
         * 查询卡号
         */
        String SECOND_AGGREGATE_NAME = "queryCard";

        /**
         * 内层聚合结果个数
         */
        int SECOND_AGGS_SIZE = 100;

        /**
         * 借贷标志
         */
        String THIRD_AGGREGATE_NAME = "loanFlag";

        /**
         * 交易金额
         */
        String FOURTH_AGGREGATE_NAME = "transactionMoney";

        /**
         * 交易时间
         */
        String FIFTH_AGGREGATE_NAME = "tradingTime";

        /**
         * 聚合结果名称
         */
        String TERMS_CUSTOM_IDENTITY_CARD = "terms_customer_identity_card";

    }


}
