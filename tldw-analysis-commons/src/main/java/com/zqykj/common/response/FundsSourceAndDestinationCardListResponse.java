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
public class FundsSourceAndDestinationCardListResponse {

    /**
     * 对方开户名称
     */
    private String oppositeName;

    /**
     * 对方证件号码
     */
    private String oppositeIdentityCard;

    /**
     * 交易对方卡号
     */
    private String oppositeCard;

    /**
     * 账户状态（已经调单和未调单,其中已调单的意思是当前卡号在交易流水总表数据的本方卡号的数据中出现过，未调单则仅仅在存在于对方卡号中
     * 未调单也可以手动的转为调单卡号.）
     */
    private String accountStatus;

    /**
     * 对方银行
     */
    private String oppositeBank;

    /**
     * 交易金额
     */
    private BigDecimal transationMoney;

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
