/**
 * @作者 Mcj
 */
package com.zqykj.domain.aggregate;

import com.zqykj.annotations.*;
import com.zqykj.domain.Routing;
import com.zqykj.domain.graph.EntityGraph;
import com.zqykj.domain.graph.LinkGraph;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

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

    @Field(name = "alias_name", type = FieldType.Text, analyzer = "ik_smart")
    private String name;

    @Field(name = "alias_describe", type = FieldType.Nested)
    private TeacherInfo describe;

    @Field(name = "date_date", type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;

    private Long longId;

    private Routing routing;

    /**
     * 实体嵌套数据
     */
    @Field(name = "entity", type = FieldType.Nested)
    private List<EntityGraph> entityGraphs;

    /**
     * 链接嵌套数据
     */
    @Field(name = "link", type = FieldType.Nested)
    private List<LinkGraph> linkGraphs;
}
