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
@Document(indexName = "transaction_record_info",shards = 2)
public class TransactionRecord {

    @Field(type = FieldType.Keyword,name = "account_card")
    private String accountCard;

    @Field(type = FieldType.Keyword)
    private String bank;

    @Field(type = FieldType.Keyword,name = "case_id")
    private String caseId;

    @Field(type = FieldType.Double,name = "trade_amount")
    private Double tradeAmount;

    @Field(type = FieldType.Double,name = "trade_balance")
    private Double tradeBalance;

    @Field(type = FieldType.Double,name = "trade_opposite_balance")
    private Double tradeOppositeBalance;

    @Field(type = FieldType.Date ,name ="trade_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tradeTime;



}
