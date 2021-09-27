/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.parameters.aggregate;

import com.zqykj.core.aggregation.query.parameters.DateParameters;
import com.zqykj.core.aggregation.query.parameters.GeneralParameters;
import lombok.*;

import java.util.List;

/**
 * <h1> 聚合参数封装 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AggregationParameters {

    /**
     * 聚合名称
     */
    private String name;

    /**
     * 脚本表达式
     */
    private String script;

    /**
     * 聚合类型
     */
    private String type;

    /**
     * 通过参数
     */
    private GeneralParameters generalParameters;

    /**
     * 日期参数
     */
    private DateParameters dateParameters;

    /**
     * 子聚合
     */
    private List<AggregationParameters> subAggregation;
}
