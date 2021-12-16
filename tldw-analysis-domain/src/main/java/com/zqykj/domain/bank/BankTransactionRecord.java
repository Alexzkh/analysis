package com.zqykj.domain.bank;


import com.zqykj.annotations.*;
import lombok.Data;

import java.util.Date;

@Data
@Document(indexName = "bank_transaction_record", shards = 3)
public class BankTransactionRecord {


    /**
     * 全局唯一id
     */
    @Id
    private String id;


    /**
     * 总表编号
     */
    @Field(type = FieldType.Long, name = "flow_id")
    private Long flowId;

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
     * 资源id
     */
    @Field(type = FieldType.Keyword, name = "resource_id")
    private String resourceId;

    /**
     * 资源id(长整型)
     */
    @Field(type = FieldType.Long, name = "resource_key_id")
    private String resourceKeyId;


    /**
     * 本方银行名称
     * fields 多字段类型在 mapping 创建之后, 是可以继续更新的(另外Object 对象也可以添加新的属性, 字段还可以添加 ignore_above属性)
     * 以上这种三种情况 , 不用着急reindex更新索引，直接更新Mapping也是可以的
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "bank", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "bank_wildcard", type = FieldType.Wildcard)}
    )
    private String bank;

    /**
     * 本方姓名
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "customer_name", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "customer_name_wildcard", type = FieldType.Wildcard)}
    )
    private String customerName;

    /**
     * 本方开户人证件号码
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "customer_identity_card"),
            otherFields = {@InnerField(suffix = "customer_identity_card_wildcard", type = FieldType.Wildcard)}
    )
    private String customerIdentityCard;

    /**
     * 查询账号
     */
    @Field(type = FieldType.Text, name = "query_account", analyzer = "ik_max_word")
    private String queryAccount;

    /**
     * 查询卡号
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "query_card"),
            otherFields = {@InnerField(suffix = "query_card_wildcard", type = FieldType.Wildcard)}
    )
    private String queryCard;

    /**
     * 交易对方姓名
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "transaction_opposite_name", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "opposite_name_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionOppositeName;

    /**
     * 交易对方证件号码
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_opposite_certificate_number"),
            otherFields = {@InnerField(suffix = "opposite_certificate_number_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionOppositeCertificateNumber;

    /**
     * 交易对方账号
     */
    @Field(type = FieldType.Text, name = "transaction_opposite_account", analyzer = "ik_max_word")
    private String transactionOppositeAccount;

    /**
     * 交易对方卡号
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_opposite_card"),
            otherFields = {@InnerField(suffix = "opposite_card_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionOppositeCard;

    /**
     * 交易类型
     */
    @Field(type = FieldType.Text, name = "transaction_type", analyzer = "ik_max_word")
    private String transactionType;

    /**
     * 借贷标志
     */
    @Field(type = FieldType.Keyword, name = "loan_flag")
    private String loanFlag;


    /**
     * 交易金额
     */
    @Field(type = FieldType.Double, name = "transaction_money")
    private Double transactionMoney;

    /**
     * 交易余额
     */
    @Field(type = FieldType.Double, name = "transaction_balance")
    private Double transactionBalance;

    /**
     * 交易对方余额
     */
    @Field(type = FieldType.Double, name = "transaction_opposite_balance")
    private Double transactionOppositeBalance;

    /**
     * 交易时间
     */
    @Field(type = FieldType.Date, name = "trading_time", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tradingTime;


    /**
     * 交易对方开户行
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "transaction_opposite_account_open_bank", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "opposite_bank_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionOppositeAccountOpenBank;

    /**
     * 交易是否成功标志
     */
    @Field(type = FieldType.Keyword, name = "transaction_success_flag")
    private String transactionSuccessFlag;

    /**
     * 变动金额(代表交易记录中交易金额, 与字段 transactionMoney 不同的是,它不会区分正负号)
     */
    @Field(type = FieldType.Double, name = "change_amount")
    private Double changeAmount;

    /**
     * 组合卡号 (查询卡号-对方卡号)
     */
    @Field(type = FieldType.Keyword, name = "merge_card")
    private String mergeCard;

    /**
     * 组合证件号码 (本方证件号码-对方证件号码)
     */
    @Field(type = FieldType.Keyword, name = "merge_identity_card")
    private String mergeIdentityCard;

    /**
     * 翻转标记  1: 代表原始记录  2:  代表的是把对方的 放到本方, 本方放到对方
     */
    @Field(type = FieldType.Integer, name = "reverse_mark")
    private Integer reverseMark;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, name = "create_time", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 最后一次更新时间
     */
    @Field(type = FieldType.Date, name = "last_update_time", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateDate;
}
