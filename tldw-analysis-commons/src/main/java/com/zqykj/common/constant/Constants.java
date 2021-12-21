package com.zqykj.common.constant;


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

        /**
         * 案件编号
         */
        String CASE_ID = "caseId";

    }


    /**
     * 聚合统计返回结果桶名称
     */
    public interface BucketName {

        /**
         * 统计计算
         */
        String STATS = "stats";

        /**
         * 聚合卡号
         */
        String TERMS_ACCOUNT_CARD = "terms_account_card";
    }

    /**
     * 图相关常量定义
     */
    public interface AthenaGdbConstants {

        /**
         * 老图库id链接符
         */
        String OLD_GRAPH_ID_CONNECTOR = "~`#";

        /**
         * 银行卡实体
         */
        String BANK_CARD = "bank_card";

        /**
         * 银行卡交易实体
         */
        String BANK_CARD_TRADE = "bank_card_trade";

        /**
         * 新图schema
         */
        String SCHEMA = "tldw";

        /**
         * 获取图idAPI --{/graph/{gsKey}/{gKey}/graphByKey},gsKey:schema, gKey:案件编号
         */
        String GRAPH_ID_API = "/graph/%s/%s/graphByKey";

        /**
         * 根据图id获取交易路径 -->/graph/{gid}/allpaths gid:图id
         */
        String Path_API = "/graph/%s/allpaths";

        /**
         * 图接口返回的路径数据中对应的总表的数据的主键id的key名称
         */
        String DATA_ROWS_ID = "__DATAROWID";

        /**
         * 图接口返回边对应总表数据的key名称
         */
        String DATA_ROWS = "DataRows";

        /**
         * 图接口返回的数据data集合的key名称
         */
        String DATA = "data";
    }
}
