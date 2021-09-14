/**
 * @作者 Mcj
 */
package com.zqykj.domain.test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zqykj.annotations.*;
import com.zqykj.domain.routing.Routing;
import lombok.*;

import java.util.Date;

/**
 * <h1> 银行交易流水测试 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "bank_transaction_flow_test", shards = 3)
@Builder
public class BankTransactionFlowTest {

    /**
     * 客户名称
     */
    @Field(type = FieldType.Keyword)
    private String accountName;

    /**
     * 客户证件号码
     */
    @Field(type = FieldType.Keyword)
    private String accountIdNumber;

    /**
     * 查询账号
     */
    @Field(type = FieldType.Keyword)
    private String queryAccount;

    /**
     * 查询卡号
     */
    @Field(type = FieldType.Keyword)
    private String queryCardNumber;

    /**
     * 银行
     */
    @Field(type = FieldType.Keyword)
    private String bank;

    /**
     * 交易对方名称
     */
    @Field(type = FieldType.Keyword)
    private String tradeOppositeName;

    /**
     * 交易对方证件号码
     */
    @Field(type = FieldType.Keyword)
    private String tradeOppositeIdNumber;

    /**
     * 交易对方账号
     */
    @Field(type = FieldType.Keyword)
    private String tradeOppositeAccount;

    /**
     * 交易对方卡号
     */
    @Field(type = FieldType.Keyword)
    private String tradeOppositeCardNumber;

    /**
     * 交易对方开户行
     */
    @Field(type = FieldType.Keyword)
    private String tradeOppositeBank;

    /**
     * 交易类型
     */
    @Field(type = FieldType.Keyword)
    private String tradeType;

    /**
     * 借贷标志
     */
    @Field(type = FieldType.Keyword)
    private String loanSign;

    /**
     * 币种
     */
    @Field(type = FieldType.Keyword)
    private String currency;

    /**
     * 交易金额
     */
    @Field(type = FieldType.Double)
    private Double tradeAmount;

    /**
     * 交易余额
     */
    @Field(type = FieldType.Double)
    private Double tradeBalance;

    /**
     * 交易时间
     */
    @MultiField(
            mainField = @Field(type = FieldType.Date, pattern = "yyyy-mm-dd HH:mm:ss"),
            otherFields = {@InnerField(suffix = "longDate", type = FieldType.Long)}
    )
    @JsonFormat(pattern = "yyyy-mm-dd HH:mm:ss")
    private Date tradeDate;

    /**
     * 交易流水
     */
    @Field(type = FieldType.Keyword)
    private String tradeFlow;

    /**
     * 交易对方余额
     */
    @Field(type = FieldType.Double)
    private Double tradeOppositeBalance;

    private Routing routing;
}
