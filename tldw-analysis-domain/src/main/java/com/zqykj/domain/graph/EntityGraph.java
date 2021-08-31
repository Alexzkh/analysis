/**
 * @作者 Mcj
 */
package com.zqykj.domain.graph;

import com.zqykj.annotations.Field;
import com.zqykj.annotations.FieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EntityGraph {

    @Field(name = "id", type = FieldType.Long)
    private long id;

    @Field(name = "type", type = FieldType.Keyword)
    private String type;
}
