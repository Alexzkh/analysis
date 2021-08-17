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
import java.util.Date;

@Document(indexName = "tang_poems", refreshInterval = "30s")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ThreeHundredTangPoems implements Serializable {
    private static final long serialVersionUID = -5124555083009873163L;

    @Id
    private String id;

    private String contents;

    private String type;

    private String author;

    private String title;

    private Date timestamp;

    @Field(type = FieldType.Long)
    private long cont_length;
}
