/**
 * @作者 Mcj
 */
package com.zqykj.domain.aggregate;

import com.zqykj.annotations.Document;
import com.zqykj.annotations.Field;
import com.zqykj.annotations.FieldType;
import com.zqykj.annotations.Id;
import com.zqykj.domain.routing.Routing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * <h1> 别名实体测试 </h1>
 */
@Document(indexName = "alias_pojo", shards = 3)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AliasPojo {

    @Id
    private String id;

    @Field(name = "alias_name", type = FieldType.Text)
    private String name;

    @Field(name = "alias_describe", type = FieldType.Nested)
    private TeacherInfo describe;

    @Field(name = "date_date", type = FieldType.Date)
    private Date date;

    private Long longId;

    private Routing routing;
}
