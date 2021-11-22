/**
 * @作者 Mcj
 */
package com.zqykj.domain.bank;

import com.zqykj.annotations.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * <h1> 去重信息 </h1>
 */
@Data
@Document(indexName = "repetit_exclusion_info", shards = 3)
public class RepetitExclusionInfo {
    /**
     * 全局唯一id
     */
    @Id
    private String id;

    /**
     * 案件编号
     */
    @Field(type = FieldType.Keyword, name = "case_id")
    private String case_id;

    /**
     * 去重key
     */
    @Field(name = "repetit_exclusion_key", type = FieldType.Keyword)
    private String repetit_exclusion_key;

    /**
     * 交易时间
     */
    @Field(type = FieldType.Date, name = "trading_time", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date trading_time;
}
