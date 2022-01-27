/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zqykj.app.service.annotation.*;
import com.zqykj.util.BigDecimalUtil;
import lombok.*;

/**
 * <h1> 交易统计分析结果查询实体 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
// 对于es 来说聚合需要带出字段名称,需要再类上加上 @Local 或者 @Opposite 或者 @Agg 注解指定聚合名称, @Key(name="hits")是固定的
// 其他数据源正常 eg. mysql 可以直接带出field (field 的名称  就等于 我们定义的聚合名称)
// eg. customerName 定义了  @Local(name = "customer_name", showField = true)
@Agg(name = "local_hits")
@Key
@JsonIgnoreProperties(ignoreUnknown = true)
@ExcelIgnoreUnannotated
public class TradeStatisticalAnalysisResult extends FundPartAnalysisResult {

    // 合并卡号
    @Agg(name = "local_card_terms")
    @Key(name = "keyAsString")
    private String queryCardKey;

    // 开户名称
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    @ExcelProperty(value = "开户名称", order = 0)
    private String customerName;

    // 开户证件号码
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
    @ExcelProperty(value = "开户证件号码", order = 1)
    private String customerIdentityCard;

    // 开户银行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    @ExcelProperty(value = "开户银行", order = 2)
    private String bank;

    // 账号
    @Agg(name = "query_account", showField = true)
    @Key(name = "query_account")
    @ExcelProperty(value = "账号", order = 3)
    private String queryAccount;

    // 交易卡号
    @Agg(name = "query_card", showField = true)
    @Key(name = "query_card")
    @ExcelProperty(value = "交易卡号", order = 4)
    private String tradeCard;

    public static void amountReservedTwo(TradeStatisticalAnalysisResult bankFlow) {
        bankFlow.setTradeTotalAmount(BigDecimalUtil.value(bankFlow.getTradeTotalAmount()));
        bankFlow.setCreditsAmount(BigDecimalUtil.value(bankFlow.getCreditsAmount()));
        bankFlow.setPayOutAmount(BigDecimalUtil.value(bankFlow.getPayOutAmount()));
        bankFlow.setTradeNet(BigDecimalUtil.value(bankFlow.getTradeNet()));
    }
}
