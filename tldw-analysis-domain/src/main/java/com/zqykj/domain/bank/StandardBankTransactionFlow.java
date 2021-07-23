package com.zqykj.domain.bank;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMapping;
import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData;
import com.zqykj.tldw.aggregate.searching.esclientrhl.enums.DataType;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName Transactions
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/6 19:45
 */
@Data
@ESMetaData(indexName = "standard_bank_transaction_flow", number_of_shards = 2,number_of_replicas = 0,printLog = false)
public class StandardBankTransactionFlow {

    @ESMapping(datatype = DataType.keyword_type)
    private String account_card;

    @ESMapping(datatype = DataType.keyword_type)
    private String account_query;

    @ESMapping(datatype = DataType.keyword_type)
    private String bank;

    @ESMapping(datatype = DataType.keyword_type)
    private String case_id;

    @ESMapping(datatype = DataType.keyword_type)
    private String cash_mark;

    @ESMapping(datatype = DataType.keyword_type)
    private String certificate_no;

    @ESMapping(datatype = DataType.keyword_type)
    private String certificate_type;

    @ESMapping(datatype = DataType.keyword_type)
    private String currency_type;

    @ESMapping(datatype = DataType.keyword_type)
    private String customer_identity_card;

    @ESMapping(datatype = DataType.keyword_type)
    private String customer_name;

    @ESMapping(datatype = DataType.keyword_type)
    private String dataSchemaId;

    @ESMapping(datatype = DataType.keyword_type)
    private String ip_address;

    @ESMapping(datatype = DataType.keyword_type)
    private String jyqd;

    @ESMapping(datatype = DataType.keyword_type)
    private String lend_mark;

    @ESMapping(datatype = DataType.keyword_type)
    private String log_number;

    @ESMapping(datatype = DataType.keyword_type)
    private String mac_address;

    @ESMapping(datatype = DataType.keyword_type)
    private String memo;

    @ESMapping(datatype = DataType.keyword_type)
    private String merchant_name;

    @ESMapping(datatype = DataType.keyword_type)
    private String merchant_number;

    @ESMapping(datatype = DataType.keyword_type)
    private String resid;

    @ESMapping(datatype = DataType.keyword_type)
    private String summons_no;

    @ESMapping(datatype = DataType.keyword_type)
    private String terminal_no;

    @ESMapping(datatype = DataType.double_type)
    private Double trade_amount;

    @ESMapping(datatype = DataType.double_type)
    private Double trade_balance;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_network_code;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_network_name;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_opposite_account;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_opposite_account_open_bank;

    @ESMapping(datatype = DataType.double_type)
    private Double trade_opposite_balance;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_opposite_card;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_opposite_identity_card;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_opposite_name;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_place;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_serial_number;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_success_flag;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_teller_no;

    @ESMapping(datatype = DataType.date_type)
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    private Date trade_time;

    @ESMapping(datatype = DataType.keyword_type)
    private String trade_type;

    @ESMapping(datatype = DataType.keyword_type)
    private String transaction_summary;

}
