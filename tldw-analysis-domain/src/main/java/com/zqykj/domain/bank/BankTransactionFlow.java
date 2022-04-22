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
 * @Author zhangkehou、machengjun
 * @Date 2021/8/20 13:50
 */
@Data
@Document(indexName = "tldw_banktransactionflow", shards = 3)
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
            mainField = @Field(type = FieldType.Keyword, name = "bank"),
            otherFields = {@InnerField(suffix = "bank_wildcard", type = FieldType.Wildcard)}
    )
    private String bank;

    /**
     * 本方姓名(客户名称)
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "customer_name"),
            otherFields = {@InnerField(suffix = "customer_name_wildcard", type = FieldType.Wildcard)}
    )
    private String customerName;

    /**
     * 本方开户人证件号码(客户证件号码)
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "customer_identity_card"),
            otherFields = {@InnerField(suffix = "customer_identity_card_wildcard", type = FieldType.Wildcard)}
    )
    private String customerIdentityCard;

    /**
     * 查询账号
     */
    @Field(type = FieldType.Keyword, name = "query_account")
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
     * 交易对方姓名(交易对方名称)
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_opposite_name"),
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
    @Field(type = FieldType.Keyword, name = "transaction_opposite_account")
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
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_type"),
            otherFields = {@InnerField(suffix = "transaction_type_wildcard", type = FieldType.Wildcard)}
    )
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
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_opposite_account_open_bank"),
            otherFields = {@InnerField(suffix = "opposite_bank_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionOppositeAccountOpenBank;

    /**
     * 交易摘要
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_summary"),
            otherFields = {@InnerField(suffix = "transaction_summary_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionSummary;

    /**
     * 交易网点名称
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_network_name"),
            otherFields = {@InnerField(suffix = "transaction_network_name_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionNetworkName;

    /**
     * 交易网点代码
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
    @Field(type = FieldType.Keyword, name = "certificate_type")
    private String certificateType;

    /**
     * 凭证号
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
     * 交易发生地
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_place"),
            otherFields = {@InnerField(suffix = "transaction_place_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionPlace;

    /**
     * 商户号
     */
    @Field(type = FieldType.Keyword, name = "merchant_number")
    private String merchantNumber;

    /**
     * 商户名称
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "merchant_name"),
            otherFields = {@InnerField(suffix = "merchant_name_wildcard", type = FieldType.Wildcard)}
    )
    private String merchantName;

    /**
     * 本方IP地址
     */
    @Field(type = FieldType.Keyword, name = "ip_address")
    private String ipAddress;

    /**
     * 本方MAC地址
     */
    @Field(type = FieldType.Keyword, name = "mac_address")
    private String macAddress;

    /**
     * 对方IP地址
     */
    @Field(type = FieldType.Keyword, name = "opposite_ip_address")
    private String oppositeIpAddress;

    /**
     * 对方MAC地址
     */
    @Field(type = FieldType.Keyword, name = "opposite_mac_address")
    private String oppositeMacAddress;

    /**
     * 交易柜员号
     */
    @Field(type = FieldType.Keyword, name = "transaction_teller_number")
    private String transactionTellerNumber;

    /**
     * 交易渠道
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "transaction_channel"),
            // 模糊搜索专用(若设置它为text且分词,是无法进行模糊搜索的)
            otherFields = {@InnerField(suffix = "transaction_channel_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionChannel;

    /**
     * 备注
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "note"),
            // 模糊搜索专用(若设置它为text且分词,是无法进行模糊搜索的)
            otherFields = {@InnerField(suffix = "note_wildcard", type = FieldType.Wildcard)}
    )
    private String note;

    /**
     * 摘要备注
     */
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, name = "summary_notes"),
            // 模糊搜索专用(若设置它为text且分词,是无法进行模糊搜索的)
            otherFields = {@InnerField(suffix = "summary_notes_wildcard", type = FieldType.Wildcard)}
    )
    private String summaryNotes;

    /**
     * 交易场所
     */
    @MultiField(
            mainField = @Field(name = "transaction_places", type = FieldType.Keyword),
            // 模糊搜索专用(若设置它为text且分词,是无法进行模糊搜索的)
            otherFields = {@InnerField(suffix = "transaction_places_wildcard", type = FieldType.Wildcard)}
    )
    private String transactionPlaces;

    /**
     * 渠道
     */
    @MultiField(
            mainField = @Field(name = "channel", type = FieldType.Keyword),
            // 模糊搜索专用(若设置它为text且分词,是无法进行模糊搜索的)
            otherFields = {@InnerField(suffix = "channel_wildcard", type = FieldType.Wildcard)}
    )
    private String channel;

    /**
     * 数据类型
     */
    @Field(type = FieldType.Keyword, name = "data_type")
    private String dataType;

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
}
