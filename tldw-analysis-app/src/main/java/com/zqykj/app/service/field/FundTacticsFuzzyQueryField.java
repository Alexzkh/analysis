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

    // 以上字段都是 wildcard类型,专门用来模糊匹配

    String[] localFuzzyFields = new String[]{"customer_name", "customer_identity_card", "bank", "query_card"};
    String[] oppositeFuzzyFields = new String[]{"transaction_opposite_name", "transaction_opposite_certificate_number", "transaction_opposite_card", "transaction_opposite_account_open_bank"};

    /**
     * <h2> 未调单分析模糊字段 </h2>
     */
    String[] unadjustedAnalysisFuzzyFields = new String[]{"query_card", "customer_name", "bank"};

    /**
     * <h2> 建议调单模糊字段 </h2>
     */
    String[] suggestAdjustedFuzzyFields = new String[]{"opposite_card", "account_name", "bank"};

    /**
     * <h2> 详情模糊字段 </h2>
     */
    String[] detailFuzzyFields = new String[]{"customer_name", "customer_identity_card", "bank", "query_card", "transaction_opposite_name", "transaction_opposite_certificate_number", "transaction_opposite_card", "transaction_opposite_account_open_bank"};
}
