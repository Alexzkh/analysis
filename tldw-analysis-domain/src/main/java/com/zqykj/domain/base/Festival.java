package com.zqykj.domain.base;

import com.zqykj.annotations.Document;
import com.zqykj.annotations.Field;
import com.zqykj.annotations.FieldType;
import lombok.Data;

/**
 * @Description: 节假日基础类
 * @Author zhangkehou
 * @Date 2022/1/11
 */
@Data
@Document(indexName = "festival", shards = 3)
public class Festival {

    @Field(type = FieldType.Keyword, name = "date_time")
    private String dateTime;

    @Field(type = FieldType.Keyword, name = "festival")
    private String festival;
}
