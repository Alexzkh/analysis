package com.zqykj.domain.bank;


import com.zqykj.annotations.*;
import com.zqykj.domain.graph.EntityGraph;
import com.zqykj.domain.graph.LinkGraph;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName Transactions
 * @Description 银行交易流水
 * @Author zhangkehou
 * @Date 2021/8/20 13:50
 */
@Data
@Document(indexName = "bank_transaction_flow", shards = 3)
public class BankTransactionFlow {


    /**
     * 全局唯一id
     */
    @Id
    private Long id;

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
    @Field(type = FieldType.Text, name = "transaction_opposite_name", analyzer = "ik_max_word")
    private String transactionOppositeName;

    /**
     * 交易对方证件号码
     */
    @Field(type = FieldType.Keyword, name = "transaction_opposite_certificate_number")
    private String transactionOppositeCertificateNumber;

    /**
     * 交易对方账号
     */
    @Field(type = FieldType.Text, name = "transaction_opposite_account", analyzer = "ik_max_word")
    private String transactionOppositeAccount;

    /**
     * 交易对方卡号
     */
    @Field(type = FieldType.Text, name = "transaction_opposite_card", analyzer = "ik_max_word")
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
     * 币种
     */
    @Field(type = FieldType.Keyword, name = "currency")
    private String currency;

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
     * 交易时间
     */
    @Field(type = FieldType.Date, name = "trading_time", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tradingTime;

    /**
     * 交易序列号
     */
    @Field(type = FieldType.Keyword, name = "transaction_serial_number")
    private String transactionSerialNumber;

    /**
     * 交易对方余额
     */
    @Field(type = FieldType.Double, name = "transaction_opposite_balance")
    private Double transactionOppositeBalance;

    /**
     * 交易对方开户行
     */
    @Field(type = FieldType.Text, name = "transaction_opposite_account_open_bank", analyzer = "ik_max_word")
    private String transactionOppositeAccountOpenBank;

    /**
     * 交易摘要
     */
    @Field(type = FieldType.Text, name = "transaction_summary", analyzer = "ik_max_word")
    private String transactionSummary;

    /**
     * 交易渠道
     */
    @Field(type = FieldType.Text, name = "transaction_channel", analyzer = "ik_max_word")
    private String transactionChannel;

    /**
     * 交易网点名称
     */
    @Field(type = FieldType.Text, name = "transaction_network_name", analyzer = "ik_max_word")
    private String transactionNetworkName;

    /**
     * 交易网点码
     */
    @Field(type = FieldType.Keyword, name = "transaction_network_code")
    private String transactionNetworkCode;

    /**
     * 日志号
     */
    @Field(type = FieldType.Keyword, name = "log_number")
    private String logNumber;

    /**
     * 传票号
     */
    @Field(type = FieldType.Keyword, name = "summons_number")
    private String summonsNumber;

    /**
     * 凭证种类
     */
    @Field(type = FieldType.Text, name = "certificate_type", analyzer = "ik_max_word")
    private String certificateType;

    /**
     * 凭证类型
     */
    @Field(type = FieldType.Keyword, name = "certificate_number")
    private String certificateNumber;

    /**
     * 现金标志
     */
    @Field(type = FieldType.Keyword, name = "cash_flag")
    private String cashFlag;

    /**
     * 终端号
     */
    @Field(type = FieldType.Keyword, name = "terminal_number")
    private String terminalNumber;

    /**
     * 交易是否成功标志
     */
    @Field(type = FieldType.Keyword, name = "transaction_success_flag")
    private String transactionSuccessFlag;

    /**
     * 交易地点
     */
    @Field(type = FieldType.Text, name = "transaction_place")
    private String transactionPlace;

    /**
     * 商户号
     */
    @Field(type = FieldType.Text, name = "merchant_number")
    private String merchantNumber;

    /**
     * ip地址
     */
    @Field(type = FieldType.Keyword, name = "ip_address")
    private String ipAddress;

    /**
     * mac地址
     */
    @Field(type = FieldType.Keyword, name = "mac_address")
    private String macAddress;

    /**
     * 交易柜员号
     */
    @Field(type = FieldType.Keyword, name = "transaction_teller_number")
    private String transactionTellerNumber;

    /**
     * 备注
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "note", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "note_wildcard", type = FieldType.Wildcard)}
    )
    private String note;

    /**
     * data schema id
     */
    @Field(type = FieldType.Keyword, name = "data_schema_id")
    private String dataSchemaId;

    /**
     * 实体嵌套数据
     */
    @Field(name = "entity", type = FieldType.Nested)
    private List<EntityGraph> entityGraphs;

    /**
     * 链接嵌套数据(图数据id - 实体Id 和 链接Id )
     */
    @Field(name = "link", type = FieldType.Nested)
    private List<LinkGraph> linkGraphs;

    /**
     * 数据补全标志位
     */
    @Field(name = "completion_flag", type = FieldType.Keyword)
    private String completionFlag;
}
