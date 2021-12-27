/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 交易区间筛选操作记录点击查看结果 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TradeRangeOperationDetailSeeResult {

    /**
     * 交易卡号
     */
    private String queryCard;

    /**
     * 开户名称
     */
    private String customerName;

    /**
     * 开户证件号码
     */
    private String customerIdentityCard;

    /**
     * 开户银行
     */
    private String bank;

    /**
     * 对方卡号
     */
    private String transactionOppositeCard;

    /**
     * 对方开户名称
     */
    private String transactionOppositeName;

    /**
     * 对方开户证件号码
     */
    private String transactionOppositeCertificateNumber;

    /**
     * 对方开户银行
     */
    private String transactionOppositeAccountOpenBank;

    /**
     * 交易时间
     */
    private String tradingTime;

    /**
     * 交易金额
     */
    private BigDecimal tradingAmount;

    /**
     * 借贷标志
     */
    private String loanFlag;

    /**
     * 交易类型
     */
    private String transactionType;

    /**
     * 交易摘要
     */
    private String transactionSummary;
}
