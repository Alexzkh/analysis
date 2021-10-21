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


    @Id
    private Long id;

    @Field(type = FieldType.Keyword, name = "case_id")
    private String caseId;

    @Field(type = FieldType.Long, name = "case_key_id")
    private Long caseKeyId;

    @Field(type = FieldType.Keyword, name = "resource_id")
    private String resourceId;

    @Field(type = FieldType.Long, name = "resource_key_id")
    private String resourceKeyId;

    // fields 多字段类型在 mapping 创建之后, 是可以继续更新的(另外Object 对象也可以添加新的属性, 字段还可以添加 ignore_above属性)
    // 以上这种三种情况 , 不用着急reindex更新索引，直接更新Mapping也是可以的
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "bank", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "bank_wildcard", type = FieldType.Wildcard)}
    )
    private String bank;

    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "customer_name", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "customer_name_wildcard", type = FieldType.Wildcard)}
    )
    private String customerName;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "customer_identity_card"),
            otherFields = {@InnerField(suffix = "customer_identity_card_wildcard", type = FieldType.Wildcard)}
    )
    private String customerIdentityCard;

    @Field(type = FieldType.Text, name = "query_account", analyzer = "ik_max_word")
    private String queryAccount;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "query_card"),
            otherFields = {@InnerField(suffix = "query_card_wildcard", type = FieldType.Wildcard)}
    )
    private String queryCard;

    @Field(type = FieldType.Text, name = "transaction_opposite_name", analyzer = "ik_max_word")
    private String transactionOppositeName;

    @Field(type = FieldType.Keyword, name = "transaction_opposite_certificate_number")
    private String transactionOppositeCertificateNumber;

    @Field(type = FieldType.Text, name = "transaction_opposite_account", analyzer = "ik_max_word")
    private String transactionOppositeAccount;

    @Field(type = FieldType.Text, name = "transaction_opposite_card", analyzer = "ik_max_word")
    private String transactionOppositeCard;

    @Field(type = FieldType.Text, name = "transaction_type", analyzer = "ik_max_word")
    private String transactionType;

    @Field(type = FieldType.Keyword, name = "loan_flag")
    private String loanFlag;

    @Field(type = FieldType.Keyword, name = "currency")
    private String currency;

    @Field(type = FieldType.Double, name = "transaction_money")
    private Double transactionMoney;

    @Field(type = FieldType.Double, name = "transaction_balance")
    private Double transactionBalance;

    @Field(type = FieldType.Date, name = "trading_time", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tradingTime;

    @Field(type = FieldType.Keyword, name = "transaction_serial_number")
    private String transactionSerialNumber;

    @Field(type = FieldType.Double, name = "transaction_opposite_balance")
    private Double transactionOppositeBalance;

    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "transaction_opposite_account_open_bank", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "transaction_opposite_account_open_bank.opposite_bank_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionOppositeAccountOpenBank;

    @Field(type = FieldType.Text, name = "transaction_summary", analyzer = "ik_max_word")
    private String transactionSummary;

    @Field(type = FieldType.Text, name = "transaction_channel", analyzer = "ik_max_word")
    private String transactionChannel;

    @Field(type = FieldType.Text, name = "transaction_network_name", analyzer = "ik_max_word")
    private String transactionNetworkName;

    @Field(type = FieldType.Keyword, name = "transaction_network_code")
    private String transactionNetworkCode;

    @Field(type = FieldType.Keyword, name = "log_number")
    private String logNumber;

    @Field(type = FieldType.Keyword, name = "summons_number")
    private String summonsNumber;

    @Field(type = FieldType.Text, name = "certificate_type", analyzer = "ik_max_word")
    private String certificateType;

    @Field(type = FieldType.Keyword, name = "certificate_number")
    private String certificateNumber;

    @Field(type = FieldType.Keyword, name = "cash_flag")
    private String cashFlag;

    @Field(type = FieldType.Keyword, name = "terminal_number")
    private String terminalNumber;

    @Field(type = FieldType.Keyword, name = "transaction_success_flag")
    private String transactionSuccessFlag;

    @Field(type = FieldType.Text, name = "transaction_place")
    private String transactionPlace;

    @Field(type = FieldType.Text, name = "merchant_number")
    private String merchantNumber;

    @Field(type = FieldType.Keyword, name = "ip_address")
    private String ipAddress;

    @Field(type = FieldType.Keyword, name = "mac_address")
    private String macAddress;

    @Field(type = FieldType.Keyword, name = "transaction_teller_number")
    private String transactionTellerNumber;

    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "note", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "note_wildcard", type = FieldType.Wildcard)}
    )
    private String note;

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
