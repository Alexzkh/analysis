/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.util.BigDecimalUtil;
import lombok.*;


/**
 * <h1> 交易汇聚结果查询实体 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ExcelIgnoreUnannotated
public class TradeConvergenceAnalysisResult extends FundPartAnalysisResult {

    // 合并卡号
    @Agg(name = "local_card_terms")
    @Key(name = "keyAsString")
    private String mergeCardKey;

    // 开户名称
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    @ExcelProperty(value = "开户名称")
    private String customerName;

    // 开户证件号码
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
    @ExcelProperty(value = "开户证件号码")
    private String customerIdentityCard;

    // 开户银行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    @ExcelProperty(value = "开户银行")
    private String bank;

    // 账号
    @Agg(name = "query_account", showField = true)
    @Key(name = "query_account")
    @JsonIgnore
    private String queryAccount;

    // 交易卡号
    @Agg(name = "query_card", showField = true)
    @Key(name = "query_card")
    @ExcelProperty(value = "交易卡号")
    private String tradeCard;

    // 对方开户名称
    @Agg(name = "transaction_opposite_name", showField = true)
    @Key(name = "transaction_opposite_name")
    @ExcelProperty(value = "对方开户名称")
    private String oppositeCustomerName;

    // 对方开户证件号码
    @Agg(name = "transaction_opposite_certificate_number", showField = true)
    @Key(name = "transaction_opposite_certificate_number")
    @ExcelProperty(value = "对方开户证件号码")
    private String oppositeIdentityCard;

    // 对方开户银行
    @Agg(name = "transaction_opposite_account_open_bank", showField = true)
    @Key(name = "transaction_opposite_account_open_bank")
    @ExcelProperty(value = "对方开户银行")
    private String oppositeBank;

    // 对方卡号
    @Agg(name = "transaction_opposite_card", showField = true)
    @Key(name = "transaction_opposite_card")
    @ExcelProperty(value = "对方卡号")
    private String oppositeTradeCard;

    // 合并卡号
    @Agg(name = "merge_card", showField = true)
    @Key(name = "merge_card")
    private String mergeCard;

    public static void amountReservedTwo(TradeConvergenceAnalysisResult bankFlow) {
        bankFlow.setTradeTotalAmount(BigDecimalUtil.value(bankFlow.getTradeTotalAmount()));
        bankFlow.setCreditsAmount(BigDecimalUtil.value(bankFlow.getCreditsAmount()));
        bankFlow.setPayOutAmount(BigDecimalUtil.value(bankFlow.getPayOutAmount()));
        bankFlow.setTradeNet(BigDecimalUtil.value(bankFlow.getTradeNet()));
    }
}
