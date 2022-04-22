/**
 * @作者 Mcj
 */
package com.zqykj.domain.bank;

import com.zqykj.annotations.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <h1> 快进快出统计记录表(包含调单卡号作为来源的、中转的、沉淀的) </h1>
 */
@Document(indexName = "fast_inout_record", shards = 3)
@Setter
@Getter
@NoArgsConstructor
public class FastInFastOutRecord {

    /**
     * 全局唯一id (使用hash值确定)
     */
    @Id
    private int id;

    /**
     * caseId hash 之后的值
     */
    @Field(type = FieldType.Integer, name = "case_id_hash")
    private int caseIdHash;

    /**
     * caseId 原始值
     */
    @Field(type = FieldType.Keyword, name = "case_id")
    private String caseId;

    /**
     * 资金来源卡号
     */
    @Field(type = FieldType.Keyword, name = "source_card")
    private String fundSourceCard;

    /**
     * 资金来源户名
     */
    @Field(type = FieldType.Keyword, name = "source_account_name")
    private String fundSourceAccountName;

    /**
     * 流入时间日期
     */
    @Field(type = FieldType.Date, name = "inflow_date", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date inflowDate;

    /**
     * 流入金额
     */
    @Field(type = FieldType.Double, name = "inflow_amount")
    private BigDecimal inflowAmount;

    /**
     * 资金中转卡号
     */
    @Field(type = FieldType.Keyword, name = "transit_card")
    private String fundTransitCard;

    /**
     * 资金中转户名
     */
    @Field(type = FieldType.Keyword, name = "transit_account_name")
    private String fundTransitAccountName;

    /**
     * 流出日期
     */
    @Field(type = FieldType.Date, name = "outflow_date", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date outflowDate;

    /**
     * 流出金额
     */
    @Field(type = FieldType.Double, name = "outflow_amount")
    private BigDecimal outflowAmount;

    /**
     * 资金沉淀卡号
     */
    @Field(type = FieldType.Keyword, name = "deposit_card")
    private String fundDepositCard;

    /**
     * 资金沉淀户名
     */
    @Field(type = FieldType.Keyword, name = "deposit_account_name")
    private String fundDepositAccountName;

    /**
     * 特征比: (流入金额 - 流出金额) / 流入金额
     */
    @Field(type = FieldType.Integer, name = "feature_ratio")
    private int characteristicRatio;

    /**
     * 时间间隔
     */
    @Field(type = FieldType.Long, name = "time_interval")
    private Long timeInterval;

    /**
     * 调单卡号
     */
    @Field(type = FieldType.Keyword, name = "adjust_card")
    private String adjustCard;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.fundSourceCard);
        sb.append(this.inflowDate);
        sb.append(this.inflowAmount);
        sb.append(this.fundTransitCard);
        sb.append(this.outflowDate);
        sb.append(this.outflowAmount);
        sb.append(this.fundDepositCard);
        return sb.toString();
    }

    public static String md5(String str) {

        return DigestUtils.md5Hex(str);
    }

    public static int hash(String str) {

        return str.hashCode();
    }
}
