package com.zqykj.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description: 时间规律折线图返回vo数据
 * @Author zhangkehou
 * @Date 2021/12/30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeRuleLineChartVO {


    /**
     * 交易时间段
     */
    private String timePeriod;

    /**
     * 原始时间
     */
    private String originDate;

    /**
     * 交易金额、交易次数
     */
    private BigDecimal transaction;

    /**
     * 入账金额、入账次数
     */
    private BigDecimal inTransaction;

    /**
     * 出账金额、出账次数
     */
    private BigDecimal outTransaction;

}
