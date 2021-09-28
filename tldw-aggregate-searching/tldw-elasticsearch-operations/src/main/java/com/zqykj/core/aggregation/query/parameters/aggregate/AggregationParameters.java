/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.parameters.aggregate;

import com.zqykj.core.aggregation.query.parameters.aggregate.pipeline.PipelineAggregationParameters;
import com.zqykj.core.aggregation.query.parameters.query.QueryParameters;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
    private AggregationGeneralParameters aggregationGeneralParameters;

    /**
     * 日期参数
     */
    private DateParameters dateParameters;

    /**
     * 子聚合
     */
    private List<AggregationParameters> subAggregation;

    /**
     * 管道聚合
     */
    private List<PipelineAggregationParameters> pipelineAggregation;

    /**
     * 查询参数
     */
    private QueryParameters queryParameters;

    public AggregationParameters(String name, String type, String script) {
        this.name = name;
        this.type = type;
        this.script = script;
    }

    public AggregationParameters(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void setPerSubAggregation(AggregationParameters aggregation) {
        if (CollectionUtils.isEmpty(this.subAggregation)) {
            this.subAggregation = new ArrayList<>();
        }
        this.subAggregation.add(aggregation);
    }


    public void setPerPipelineAggregation(PipelineAggregationParameters aggregation) {
        if (CollectionUtils.isEmpty(this.pipelineAggregation)) {
            this.pipelineAggregation = new ArrayList<>();
        }
        this.pipelineAggregation.add(aggregation);
    }

    /**
     * <h1> 聚合类型的翻译 </h1>
     * TODO 后面做一个翻译器, 将公共的类型 翻译成不同数据源需要的聚合类型
     */
    public String aggregationTypeConvert(String type) {

        switch (type) {
            case "count":
                return "value_count";
            default:
                return type;
        }
    }
}
