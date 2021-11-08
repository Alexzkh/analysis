package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description: 资金来源去向列表返回体
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundsSourceAndDestinationListResponse {

    /**
     * 对方开户名称
     */
    private String oppositeName;

    /**
     * 对方证件号码
     */
    private String oppositeIdentityCard;

    /**
     * 交易金额
     */
    private BigDecimal transationMoney;

    /**
     * 调单卡号个数
     */
    private Integer adjustmentCardNumber;

    /**
     * 交易次数
     */
    private Integer transcationFrequency;

    /**
     * 入账交易次数
     */
    private Integer inTranscationFrequency;

    /**
     * 出账交易次数
     */
    private Integer outTranscationFrequency;

    /**
     * 入账金额
     */
    private BigDecimal inTransactionMoney;

    /**
     * 出账金额
     */
    private BigDecimal outTransactionMoney;

    /**
     * 交易净额
     */
    private BigDecimal netTransactionMoney;

    /**
     * 账户类型
     */
    private String accountType;

    /**
     * 最早交易时间
     */
    private String minTransationTime;

    /**
     * 最晚交易时间
     */
    private String maxTransationTime;


}
