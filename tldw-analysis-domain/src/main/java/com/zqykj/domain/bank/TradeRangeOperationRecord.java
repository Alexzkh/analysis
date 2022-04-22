/**
 * @作者 Mcj
 */
package com.zqykj.domain.bank;

import com.zqykj.annotations.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * <h1> 交易区间筛选操作记录 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@Document(indexName = "trade_range_operation", shards = 3)
public class TradeRangeOperationRecord {

    /**
     * 全局唯一id
     */
    @Id
    private String id;

    /**
     * 案件Id
     */
    @Field(name = "case_id", type = FieldType.Keyword)
    private String caseId;

    /**
     * 调单卡号
     */
    @Field(name = "adjust_card", type = FieldType.Keyword)
    private List<String> adjustCards;

    /**
     * 操作日期
     */
    @Field(type = FieldType.Date, name = "operation_date", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationDate;

    /**
     * 操作日期(格式化后的日期字符串)
     */
    private String operationDateFormat;

    /**
     * 操作人
     */
    @Field(type = FieldType.Keyword, name = "operation_people")
    private String operationPeople;

    /**
     * 最小金额
     */
    @Field(type = FieldType.Double, name = "min_amount")
    private Double minAmount;

    /**
     * 最大金额(最小金额 与 最大金额 合并 属于金额范围 eg.  1 - 2000)
     */
    @Field(type = FieldType.Double, name = "max_amount")
    private Double maxAmount;

    /**
     * 账户开户名称
     */
    @Field(type = FieldType.Keyword, name = "account_name")
    private String accountOpeningName;

    /**
     * 账户开户证件号码
     */
    @Field(type = FieldType.Keyword, name = "account_id_number")
    private String accountOpeningIDNumber;

    /**
     * 个体银行卡数量 (当查询全部调单卡号的时候, 默认设置为-1)
     */
    @Field(type = FieldType.Integer, name = "individual_bankCards_number")
    private Integer individualBankCardsNumber;

    /**
     * 数据类别
     */
    @Field(type = FieldType.Integer, name = "data_cateGory")
    private Integer dataCategory;

    /**
     * 备注
     */
    @Field(type = FieldType.Keyword, name = "remark")
    private String remark;
}
