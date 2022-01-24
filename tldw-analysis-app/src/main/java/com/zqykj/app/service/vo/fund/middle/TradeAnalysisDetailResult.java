/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund.middle;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 交易分析详情结果 </h1>
 */
@Setter
@Getter
@ExcelIgnoreUnannotated
public class TradeAnalysisDetailResult {

    /** 交易卡号 */
    @ExcelProperty(value = "交易卡号")
    private String queryCard;

    /** 开户名称 */
    @ExcelProperty(value = "开户名称",index = 1)
    private String customerName;

    /** 开户证件号码 */
    @ExcelProperty(value = "开户证件号码",index = 2)
    private String customerIdentityCard;

    /** 开户银行 */
    @ExcelProperty(value = "开户银行",index = 3)
    private String bank;

    /** 对方卡号 */
    @ExcelProperty(value = "对方卡号",index = 4)
    private String transactionOppositeCard;

    /** 对方开户名称 */
    @ExcelProperty(value = "对方开户名称",index = 5)
    private String transactionOppositeName;

    /** 对方开户证件号码 */
    @ExcelProperty(value = "对方证件号",index = 6)
    private String transactionOppositeCertificateNumber;

    /** 对方开户银行 */
    @ExcelProperty(value = "对方银行名称",index = 7)
    private String transactionOppositeAccountOpenBank;

    /** 交易时间 */
    @ExcelProperty(value = "交易时间",index = 8)
    private String tradeTime;

    /** 交易金额 */
    @ExcelProperty(value = "交易金额",index = 9)
    private BigDecimal changeAmount;

    /** 借贷标识 */
    @ExcelProperty(value = "借贷标志",index = 10)
    private String loanFlag;

    /** 交易类型 */
    @ExcelProperty(value = "交易类型",index = 11)
    private String transactionType;

    /** 交易摘要 */
    @ExcelProperty(value = "交易摘要",index = 12)
    private String transactionSummary;
}
