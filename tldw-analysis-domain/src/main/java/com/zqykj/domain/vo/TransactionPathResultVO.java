package com.zqykj.domain.vo;


import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.infrastructure.compare.BaseCompareBean;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Description: 交易路径分析结果返回vo
 * @Author zhangkehou
 * @Date 2021/12/16
 */
@Data
@Builder
public class TransactionPathResultVO implements BaseCompareBean {

    /**
     * 起始账号
     */
    private String sourceAccount;

    /**
     * 起始账户开户名称
     */
    private String sourceName;

    /**
     * 起始账户证件号码
     */
    private String sourceIdentityCard;

    /**
     * 起始交易金额
     */
    private BigDecimal sourceTransactionMoney;

    /**
     * 起始交易时间
     */
    private Date sourceTransactionTime;

    /**
     * 终点账号
     */
    private String destAccount;

    /**
     * 终点账户开户名称
     */
    private String destName;

    /**
     * 终点账户证件号码
     */
    private String destIdentityCard;

    /**
     * 终点交易金额
     */
    private BigDecimal destTransactionMoney;

    /**
     * 终点交易时间
     */
    private Date destTransactionTime;

    /**
     * 时间跨度
     */
    private long timeSpan;

    /**
     * 交易流水id集合
     */
    private List<String> ids;



}
