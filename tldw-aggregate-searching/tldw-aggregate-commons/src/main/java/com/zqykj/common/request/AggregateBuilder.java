package com.zqykj.common.request;

import com.zqykj.common.enums.AggregateType;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @Description: 构建聚合条件请求
 * @Author zhangkehou
 * @Date 2021/9/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class AggregateBuilder {


    /**
     * 聚合名称
     */
    private String aggregateName;

    /**
     * 聚合类型
     */
    private AggregateType aggregateType;

    /**
     * 父聚合名称
     * */
    private String parentAggregateName;

    /**
     * 聚合字段
     */
    private String field;

    /**
     * 当前聚合结果大小
     */
    private int size;

    /**
     * bucket聚合是的参数值
     */
    private Map<String, String> bucket;

    /**
     * bucket_script聚合时的script值
     */
    private String script;

    /**
     * 子聚合集合
     */
    private List<AggregateBuilder> subAggregations;
}
