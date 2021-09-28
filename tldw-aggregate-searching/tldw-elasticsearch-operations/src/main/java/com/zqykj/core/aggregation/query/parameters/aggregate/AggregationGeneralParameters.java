/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.parameters.aggregate;


import lombok.*;

/**
 * <h1> 一些通用的聚合查询参数 <h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AggregationGeneralParameters {

    /**
     * 聚合需要的字段 (如果此聚合需要对某个字段进行操作的话)
     */
    private String field;

    /**
     * 聚合桶数量返回 限制 (若此聚合产生的结果是多个,可以使用此参数做相应限制)
     */
    private int size = 1;

    public AggregationGeneralParameters(String field) {
        this.field = field;
    }
}
