package com.zqykj.app.service.vo.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 个体画像-基本信息与统计返回体
 *
 * @author: SunChenYu
 * @date: 2021年11月30日 11:02:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndividualInfoAndStatisticsResponse {
    /**
     * 姓名
     */
    private String customerName;

    /**
     * 手机
     */
    private String phoneNumber;

    /**
     * 身份证号
     */
    private String customerIdentityCard;

    /**
     * 年龄
     */
    private int age;

    /**
     * 户籍
     */
    private String householdRegistration;

    /**
     * 当前总余额
     */
    private BigDecimal currentTotalBalance;

    /**
     * 成功累计收入金额（RMB）
     */
    private BigDecimal cumulativeIncome;

    /**
     * 成功累计支出金额（RMB）
     */
    private BigDecimal cumulativeExpenditure;

    /**
     * 交易净额
     */
    private BigDecimal cumulativeNet;

}
