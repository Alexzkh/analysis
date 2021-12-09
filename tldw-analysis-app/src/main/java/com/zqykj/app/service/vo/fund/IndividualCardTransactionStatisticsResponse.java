package com.zqykj.app.service.vo.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 个体画像-名下卡交易情况统计返回体
 *
 * @author: SunChenYu
 * @date: 2021年11月30日 11:01:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Agg(name = "queryCardTopHits")
@Key
public class IndividualCardTransactionStatisticsResponse {
    /**
     * 案件id
     */
    private String caseId;

    /**
     * 姓名
     */
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String customerName;

    /**
     * 身份证号
     */
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
    private String customerIdentityCard;

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
    private BigDecimal transactionBalance;

    /**
     * 交易次数
     */
    @Agg(name = "totalTransactionTimes", showField = true)
    @Key(name = "value")
    private int totalTransactionTimes;

    /**
     * 入账笔数
     */
    @Agg(name = "entryTransactionTimes", showField = true)
    @Key(name = "value")
    private int entryTransactionTimes;

    /**
     * 出账笔数
     */
    @Agg(name = "outTransactionTimes", showField = true)
    @Key(name = "value")
    private int outTransactionTimes;

    /**
     * 交易金额
     */
    @Agg(name = "totalTransactionMoney")
    @Key(name = "valueAsString")
    private BigDecimal totalTransactionMoney;

    /**
     * 入账金额
     */
    @Agg(name = "totalEntryTransactionMoney")
    @Key(name = "valueAsString")
    private BigDecimal totalEntryTransactionMoney;

    /**
     * 出账金额
     */
    @Agg(name = "totalOutTransactionMoney")
    @Key(name = "valueAsString")
    private BigDecimal totalOutTransactionMoney;

    /**
     * 最早交易日期
     */
    @Agg(name = "earliestTradingTime")
    @Key(name = "valueAsString")
    private String earliestTradingTime;

    /**
     * 最晚交易日期
     */
    @Agg(name = "latestTradingTime")
    @Key(name = "valueAsString")
    private String latestTradingTime;

    /**
     * 交易净额
     */
    @Agg(name = "netTransactionMoney")
    @Key(name = "value")
    private BigDecimal netTransactionMoney;

    /**
     * 资金占比(余额/总资产)
     */
    private BigDecimal fundsProportion;
}
