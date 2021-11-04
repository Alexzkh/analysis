/**
 * @作者 Mcj
 */
package com.zqykj.domain.archive;

import com.zqykj.annotations.*;
import lombok.Data;

import java.util.Date;

/**
 * <h1> 人员档案库 </h1>
 */
@Data
@Document(indexName = "people_card_info", shards = 3)
public class PeopleCardInfo {

    /**
     * 全局唯一id
     */
    @Id
    private String id;

    /**
     * 身份证号
     */
    @Field(name = "person_ssn_id", type = FieldType.Keyword)
    private String person_ssn_id;

    /**
     * 姓名
     */
    @Field(name = "person_name", type = FieldType.Keyword)
    private String person_name;

    /**
     * 银行卡号
     */
    @Field(name = "bank_card_no", type = FieldType.Keyword)
    private String bank_card_no;

    /**
     * 账号开户行
     */
    @Field(name = "bank_card_bankname", type = FieldType.Keyword)
    private String bank_card_bankname;

    /**
     * 性别
     */
    @Field(name = "person_gender", type = FieldType.Keyword)
    private String person_gender;

    /**
     * 国籍
     */
    @Field(name = "person_nationality", type = FieldType.Keyword)
    private String person_nationality;

    /**
     * 民族
     */
    @Field(name = "person_ethnicity", type = FieldType.Keyword)
    private String person_ethnicity;

    /**
     * 身高
     */
    @Field(name = "person_height", type = FieldType.Keyword)
    private String person_height;

    /**
     * 血型
     */
    @Field(name = "person_bloodtype", type = FieldType.Keyword)
    private String person_bloodtype;

    /**
     * 开始时间
     */
    @Field(name = "begin_time", type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date begin_time;

    /**
     * 结束时间
     */
    @Field(name = "end_time", type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date end_time;

    /**
     * 来源Id
     */
    @Field(name = "dataSource_id", type = FieldType.Keyword)
    private String dataSource_id;

    /**
     * 描述
     */
    @Field(name = "elpType", type = FieldType.Keyword)
    private String elpType;
}
