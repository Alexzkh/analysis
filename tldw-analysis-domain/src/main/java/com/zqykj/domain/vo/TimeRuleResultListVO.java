package com.zqykj.domain.vo;

import com.zqykj.infrastructure.compare.BaseCompareBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description: 时间规律分析结果列表vo
 * @Author zhangkehou
 * @Date 2021/12/30
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TimeRuleResultListVO implements BaseCompareBean {


    /**
     * 交易时间段
     */
    private String timePeriod;

    /**
     * 原始日期
     */
    private String originDate;

    /**
     * 交易金额
     */
    private BigDecimal transationMoney;

    /**
     * 交易金额占比
     */
    private String transactionMoneyRatio;

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
     * 出账金额占比
     */
    private String inTransactionMoneyRatio;

    /**
     * 出账金额
     */
    private BigDecimal outTransactionMoney;

    /**
     * 出账金额占比
     */
    private String outTransactionMoneyRatio;
}
