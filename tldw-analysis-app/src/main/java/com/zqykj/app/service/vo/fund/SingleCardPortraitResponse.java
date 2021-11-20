package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Hits;
import com.zqykj.app.service.annotation.Key;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * 单卡画像返回体
 *
 * @author: SunChenYu
 * @date: 2021年11月15日 11:55:50
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleCardPortraitResponse implements Serializable {
    /**
     * 调单账号（查询账号）
     */
    @Hits
    @Key(name = "hits")
    private String queryAccount;

    /**
     * 调单卡号（查询卡号）
     */
    @Hits
    @Key(name = "hits")
    private String queryCard;

    /**
     * 客户名称
     */
    @Hits
    @Key(name = "hits")
    private String customerName;

    /**
     * 客户身份证号码
     */
    @Hits
    @Key(name = "hits")
    private String customerIdentityCard;

    /**
     * 开户银行
     */
    @Hits
    @Key(name = "hits")
    private String bank;

    /**
     * 开户日期
     */
    private String accountOpeningDate;

    /**
     * 入账金额
     */
    @Agg(name = "localInTransactionMoney")
    @Key(name = "valueAsString")
    private Double entriesAmount;

    /**
     * 出账金额
     */
    @Agg(name = "localOutTransactionMoney")
    @Key(name = "valueAsString")
    private Double outGoingAmount;

    /**
     * 交易总金额
     */
    private Double transactionTotalAmount;

    /**
     * 最早交易时间
     */
    @Key(name = "valueAsString")
    private String earliestTradingTime;

    /**
     * 最晚交易时间
     */
    @Key(name = "valueAsString")
    private String latestTradingTime;

    /**
     * 账户余额
     */
    @Hits
    @Key(name = "hits")
    private Double transactionBalance;

}
