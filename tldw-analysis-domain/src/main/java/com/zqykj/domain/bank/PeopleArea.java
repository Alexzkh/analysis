package com.zqykj.domain.bank;

import com.zqykj.annotations.*;
import lombok.Data;

/**
 * @Description: 人员地域表用作战法地域分析使用.
 * @Author zhangkehou
 * @Date 2021/10/21
 */
@Data
@Document(indexName = "people_area", shards = 3)
public class PeopleArea {

    /**
     * 案件编号.
     */
    @Field(type = FieldType.Keyword, name = "case_id")
    private String caseId;

    /**
     * 身份证号码.
     */
    @Field(type = FieldType.Keyword, name = "identity_card")
    private String identityCard;

    /**
     * 资源id.
     */
    @Field(type = FieldType.Keyword, name = "resource_id")
    private String resourceId;

    /**
     * 省份.
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "province", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "province_wildcard", type = FieldType.Wildcard)}
    )
    private String province;

    /**
     * 市.
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "city", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "city_wildcard", type = FieldType.Wildcard)}
    )
    private String city;

    /**
     * 区、县.
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "area", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "area_wildcard", type = FieldType.Wildcard)}
    )
    private String area;

    /**
     * 开户人姓名.
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "account_holder_name", analyzer = "ik_max_word"),
            otherFields = {@InnerField(suffix = "account_holder_name_wildcard", type = FieldType.Wildcard)}
    )
    private String accountHolderName;

}
