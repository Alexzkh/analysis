/**
 * @作者 Mcj
 */
package com.zqykj.app.service.field;

/**
 * <h1> 资金战法模糊匹配字段 </h1>
 */
public interface FundTacticsFuzzyQueryField {

    // 开户名称   customer_name(本方) 、 transaction_opposite_name(对方)

    // 身份证号码 customer_identity_card(本方)  、 transaction_opposite_certificate_number(对方)

    // 开户银行   bank(本方) 、transaction_opposite_account_open_bank(对方)

    // 交易卡号 query_card(本方) 、 transaction_opposite_card(对方)

    // 以上字段都是多字段类型 eg. customer_name 实际上 模糊匹配的时候,
    // 用的是 customer_name.customer_name_wildcard(customer_name_wildcard 是 wildcard类型,专门用来模糊匹配)

    String[] localFuzzyFields = new String[]{
            "customer_name.customer_name_wildcard", "customer_identity_card.customer_identity_card_wildcard",
            "bank.bank_wildcard", "query_card.query_card_wildcard"};
    String[] oppositeFuzzyFields = new String[]{
            "transaction_opposite_name.opposite_name_wildcard", "transaction_opposite_certificate_number.opposite_certificate_number_wildcard",
            "transaction_opposite_card.opposite_card_wildcard", "transaction_opposite_account_open_bank.opposite_bank_wildcard"
    };
}
