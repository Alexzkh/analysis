/**
 * @作者 Mcj
 */
package com.zqykj.domain.aggregate;

import com.zqykj.annotations.Document;
import com.zqykj.annotations.Field;
import com.zqykj.annotations.FieldType;
import com.zqykj.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <h1> 聚合查询测试类 </h1>
 */
@Document(indexName = "teacher_info")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeacherInfo implements Serializable {

    private static final long serialVersionUID = 4994769441339785679L;
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Integer)
    private int age;

    @Field(type = FieldType.Keyword)
    private String job;

    @Field(type = FieldType.Integer)
    private int sex;

    @Field(type = FieldType.Float)
    private BigDecimal salary;

}
