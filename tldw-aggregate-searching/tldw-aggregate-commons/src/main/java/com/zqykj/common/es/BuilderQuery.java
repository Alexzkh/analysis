package com.zqykj.common.es;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * @Description: 查询构建器.
 * @Author zhangkehou
 * @Date 2021/10/13
 */
public class BuilderQuery {


    /**
     * Elasticsearch中模糊匹配连接符.
     * */
    private static final String WILDCARD = ".*";

    /**
     * @param values: values[0] 卡号、values[1] 案件编号、values[2] keyword(模糊查询的值).
     * @return: org.elasticsearch.index.query.QueryBuilder
     **/
    public static QueryBuilder build(String... values) {

        /**
         * 按照查询卡号和案件编号来精准匹配.
         * */
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("query_card", values[0]))
                .must(QueryBuilders.termsQuery("case_id", values[1]))
                .should(QueryBuilders.regexpQuery("query_card", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("customer_name", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("customer_identity_card", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("bank", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("transaction_opposite_card", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("transaction_opposite_name", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("transaction_opposite_certificate_number", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("loan_flag", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("transaction_type", WILDCARD + values[2] + WILDCARD))
                .should(QueryBuilders.regexpQuery("transaction_opposite_account_open_bank", WILDCARD + values[2] + WILDCARD));
        boolQueryBuilder.minimumShouldMatch(1);
        return boolQueryBuilder;
    }

}
