package com.zqykj.domain.transaction;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.zqykj.annotations.Document;
import com.zqykj.annotations.Field;
import com.zqykj.annotations.FieldType;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName Transactions
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/6 19:45
 */
@Data
@Document(indexName = "transaction_record", shards = 3)
public class TransactionRecord {

    @Field(type = FieldType.Keyword)
    private String account_card;

    @Field(type = FieldType.Keyword)
    private String bank;

    @Field(type = FieldType.Keyword)
    private String case_id;

    @Field(type = FieldType.Double)
    private Double trade_amount;

    @Field(type = FieldType.Double)
    private Double trade_balance;

    @Field(type = FieldType.Double)
    private Double trade_opposite_balance;

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date trade_time;



}
