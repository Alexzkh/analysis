package com.zqykj.app.service.vo.fund.middle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 个体画像-基本信息与统计
 *
 * @author: SunChenYu
 * @date: 2021年11月30日 11:02:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Agg(name = "queryCardTopHits")
@Key
public class IndividualInfoAndStatistics {
    /**
     * 姓名
     */
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String customerName;

    /**
     * 手机
     */
    private String phoneNumber;

    /**
     * 身份证号
     */
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
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
    @Agg(name = "cumulativeIncome")
    @Key(name = "valueAsString")
    private BigDecimal cumulativeIncome;

    /**
     * 成功累计支出金额（RMB）
     */
    @Agg(name = "cumulativeExpenditure")
    @Key(name = "valueAsString")
    private BigDecimal cumulativeExpenditure;

    /**
     * 交易净额
     */
    @Agg(name = "cumulativeNet")
    @Key(name = "valueAsString")
    private BigDecimal cumulativeNet;

    /**
     * 账号
     */
    @Agg(name = "query_account", showField = true)
    @Key(name = "query_account")
    private String queryAccount;

    /**
     * 卡号
     */
    @Agg(name = "query_card", showField = true)
    @Key(name = "query_card")
    private String queryCard;

    /**
     * 银行
     */
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    private String bank;

    /**
     * 余额
     */
    @Agg(name = "transaction_balance", showField = true)
    @Key(name = "transaction_balance")
    private Double transactionBalance;

}
