/**
 * @作者 Mcj
 */
package com.zqykj.domain.bank;

import com.zqykj.annotations.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * <h1> 建议调单 </h1>
 */
@Setter
@Getter
@Document(indexName = "suggest_adjusted", shards = 3)
public class SuggestAdjusted {

    /**
     * 全局唯一id, 使用hashId(将 caseId 与 建议调单账号合并hash,如果下次有相同的案件下的建议调单数据直接更新覆盖原先数据)
     */
    @Id
    private int id;


    /**
     * 案件编号
     */
    @Field(type = FieldType.Keyword, name = "case_id")
    private String caseId;

    /**
     * 案件编号（长整型）
     */
    @Field(type = FieldType.Long, name = "case_key_id")
    private Long caseKeyId;

    /**
     * 对方卡号(建议调单账号)
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "opposite_card"),
            otherFields = {@InnerField(type = FieldType.Wildcard, suffix = "wildcard")}
    )
    private String oppositeCard;

    /**
     * 账户开户名称
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "account_name"),
            otherFields = {@InnerField(type = FieldType.Wildcard, suffix = "wildcard")}
    )
    private String accountName;

    /**
     * 对方开户行
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "bank"),
            otherFields = {@InnerField(type = FieldType.Wildcard, suffix = "wildcard")}
    )
    private String bank;

    /**
     * 关联账户数
     */
    @Field(type = FieldType.Integer, name = "linked_accounts_number")
    private Integer numberOfLinkedAccounts;

    /**
     * 交易总次数
     */
    @Field(type = FieldType.Integer, name = "trade_total_times")
    private Integer tradeTotalTimes;

    /**
     * 交易总金额
     */
    @Field(type = FieldType.Double, name = "trade_total_amount")
    private Double tradeTotalAmount;

    /**
     * 入账总金额
     */
    @Field(type = FieldType.Double, name = "credits_total_amount")
    private Double creditsTotalAmount;

    /**
     * 出账总金额
     */
    @Field(type = FieldType.Double, name = "payout_total_amount")
    private Double payoutTotalAmount;

    /**
     * 交易总净和(入账总金额 - 出账总金额)
     */
    @Field(type = FieldType.Double, name = "trade_net")
    private Double tradeNet;

    /**
     * 账户特征(可能有多个) eg. 来源 中转(同时存在)
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "account_feature"),
            otherFields = {@InnerField(type = FieldType.Wildcard, suffix = "wildcard")}
    )
    private String accountFeature;

    /**
     * 添加类型 1: 手动  2: 自动
     */
    @Field(type = FieldType.Integer, name = "add_type")
    private Integer addType;

    /**
     * 添加人
     */
    @Field(type = FieldType.Keyword, name = "add_account")
    private String addAccount;

    /**
     * 添加时间
     */
    @Field(type = FieldType.Date, name = "add_datetime", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date addDateTime;

}
