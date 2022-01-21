package com.zqykj.domain.vo;

import com.zqykj.infrastructure.compare.BaseCompareBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Description: 资金回路响应体
 * @Author zhangkehou
 * @Date 2022/1/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundsLoopResultListVO implements BaseCompareBean {

    /**
     * 最早交易账号
     */
    private String earliestTransactionAccount;

    /**
     * 最早交易开户名称
     */
    private String earliestTransactionName;

    /**
     * 最早交易证件号码
     */
    private String earliestTransactionIdentityCard;

    /**
     * 最早交易时间
     */
    private Date earliestTransactionTradingTime;
    /**
     * 最早交易金额
     */
    private BigDecimal earliestTransactionMoney;

    /**
     * 最晚交易账号
     */
    private String latestTransactionAccount;

    /**
     * 最晚交易开户名称
     */
    private String latestTransactionName;

    /**
     * 最晚交易证件号码
     */
    private String latestTransactionIdentityCard;

    /**
     * 最晚交易时间
     */
    private Date latestTransactionTradingTime;

    /**
     * 最晚交易金额
     */
    private BigDecimal latestTransactionMoney;

    /**
     * 交易流水id集合(用于做详细信息的查询)
     */
    private List<String> ids;


}
