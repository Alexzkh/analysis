/**
 * @作者 Mcj
 */
package com.zqykj.app.service.field;

/**
 * <h1> 资金战法模糊匹配字段 </h1>
 */
public interface FundTacticsFuzzyQueryField {

    // 开户名称 customer_name eg  customer_name.customer_name_wildcard 中 customer_name_wildcard 属于多字段类型(专门用来模糊匹配)

    // 开户证件号码 customer_identity_card

    // 开户银行 bank

    // 备注 note

    // 查询卡号 query_card

    String[] fuzzyFields = new String[]{"customer_name.customer_name_wildcard", "customer_identity_card.customer_identity_card_wildcard",
            "bank.bank_wildcard", "note.note_wildcard", "query_card.query_card_wildcard"};
}
