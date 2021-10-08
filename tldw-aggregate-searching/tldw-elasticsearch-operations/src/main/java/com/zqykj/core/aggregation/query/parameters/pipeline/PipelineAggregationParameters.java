/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.parameters.pipeline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * <h1> 管道聚合参数 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineAggregationParameters {

    /**
     * 聚合名称
     */
    private String name;

    /**
     * 聚合类型
     */
    private String type;

    /**
     * 大多数管道聚合需要另一个聚合作为它们的输入. 输入聚合是通过 bucketsPathMap 参数定义的
     * <p>
     * eg. sales_per_month>sales  (sales_per_month 、sales 都是其他聚合名称, 这里可以由你自己定义)
     */
    private Map<String, String> bucketsPathMap;

    /**
     * 需要另外一个聚合作为输入  eg. main_card_terms>trade_total_net_amount_per_card (需要的是main_card_terms 聚合下的trade_total_net_amount_per_card聚合作为输入)
     */
    private String bucketsPath;

    /**
     * 脚本表达式
     */
    private String script;

}
