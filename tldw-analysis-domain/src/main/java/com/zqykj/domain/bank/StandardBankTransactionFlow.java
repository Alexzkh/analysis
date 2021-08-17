package com.zqykj.domain.bank;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.zqykj.annotations.Document;
import com.zqykj.annotations.Field;
import com.zqykj.annotations.FieldType;
import com.zqykj.annotations.Id;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName Transactions
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/6 19:45
 */
@Data
@Document(indexName = "standard_bank_transaction_flow", shards = 2)
public class StandardBankTransactionFlow {

    @Field(type = FieldType.Keyword)
    private String account_card;

    @Field(type = FieldType.Keyword)
    private String account_query;

    @Field(type = FieldType.Keyword)
    private String bank;

    @Field(type = FieldType.Keyword)
    private String case_id;

    @Field(type = FieldType.Keyword)
    private String cash_mark;

    @Field(type = FieldType.Keyword)
    private String certificate_no;

    @Field(type = FieldType.Keyword)
    private String certificate_type;

    @Field(type = FieldType.Keyword)
    private String currency_type;

    @Field(type = FieldType.Keyword)
    private String customer_identity_card;

    @Field(type = FieldType.Keyword)
    private String customer_name;

    @Field(type = FieldType.Keyword)
    private String dataSchemaId;

    @Field(type = FieldType.Keyword)
    private String ip_address;

    @Field(type = FieldType.Keyword)
    private String jyqd;

    @Field(type = FieldType.Keyword)
    private String lend_mark;

    @Field(type = FieldType.Keyword)
    private String log_number;

    @Field(type = FieldType.Keyword)
    private String mac_address;

    @Field(type = FieldType.Keyword)
    private String memo;

    @Field(type = FieldType.Keyword)
    private String merchant_name;

    @Field(type = FieldType.Keyword)
    private String merchant_number;

    @Field(type = FieldType.Keyword)
    private String resid;

    @Field(type = FieldType.Keyword)
    private String summons_no;

    @Field(type = FieldType.Keyword)
    private String terminal_no;

    @Field(type = FieldType.Double)
    private Double trade_amount;

    @Field(type = FieldType.Double)
    private Double trade_balance;

    @Field(type = FieldType.Keyword)
    private String trade_network_code;

    @Field(type = FieldType.Keyword)
    private String trade_network_name;

    @Field(type = FieldType.Keyword)
    private String trade_opposite_account;

    @Field(type = FieldType.Keyword)
    private String trade_opposite_account_open_bank;

    @Field(type = FieldType.Double)
    private Double trade_opposite_balance;

    @Field(type = FieldType.Keyword)
    private String trade_opposite_card;

    @Field(type = FieldType.Keyword)
    private String trade_opposite_identity_card;

    @Field(type = FieldType.Keyword)
    private String trade_opposite_name;

    @Field(type = FieldType.Keyword)
    private String trade_place;

    @Field(type = FieldType.Keyword)
    private String trade_serial_number;

    @Field(type = FieldType.Keyword)
    private String trade_success_flag;

    @Field(type = FieldType.Keyword)
    private String trade_teller_no;

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date trade_time;

    @Field(type = FieldType.Keyword)
    private String trade_type;

    @Field(type = FieldType.Keyword)
    private String transaction_summary;

}
