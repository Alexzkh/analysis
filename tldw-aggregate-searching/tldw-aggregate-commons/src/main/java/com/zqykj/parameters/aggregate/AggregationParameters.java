/**
 * @作者 Mcj
 */
package com.zqykj.parameters.aggregate;

import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParameters;
import com.zqykj.parameters.annotation.OptionalParameter;
import com.zqykj.parameters.query.QueryParameters;
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
    @OptionalParameter
    private String script;

    /**
     * 聚合类型
     */
    private String type;

    /**
     * 聚合需要的字段 (如果此聚合需要对某个字段进行操作的话)
     */
    @OptionalParameter
    private String field;

    /**
     * 聚合桶数量返回 限制 (若此聚合产生的结果是多个,可以使用此参数做相应限制)
     */
    @OptionalParameter
    private int size = 1;

    /**
     * 通用参数
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

    public AggregationParameters(String name, String type, String field) {
        this.name = name;
        this.type = type;
        this.field = field;
    }

    public AggregationParameters(String name, String type, String field, int size) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.size = size;
    }

    public AggregationParameters(String name, String type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }

    public AggregationParameters(String name, String type, AggregationGeneralParameters generalParameters) {
        this.name = name;
        this.type = type;
        this.aggregationGeneralParameters = generalParameters;
    }

    public AggregationParameters(String name, String type, String field,
                                 AggregationGeneralParameters generalParameters) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.aggregationGeneralParameters = generalParameters;
    }

    public AggregationParameters(String name, String type, DateParameters dateParameters) {
        this.name = name;
        this.type = type;
        this.dateParameters = dateParameters;
    }

    public AggregationParameters(String name, String type, String field, DateParameters dateParameters) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.dateParameters = dateParameters;
    }

    public AggregationParameters(String name, String type, AggregationGeneralParameters generalParameters, DateParameters dateParameters) {
        this.name = name;
        this.type = type;
        this.aggregationGeneralParameters = generalParameters;
        this.dateParameters = dateParameters;
    }

    public AggregationParameters(String name, String type, String field, AggregationGeneralParameters generalParameters, DateParameters dateParameters) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.aggregationGeneralParameters = generalParameters;
        this.dateParameters = dateParameters;
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


    public void setPerSubAggregation(AggregationParameters aggregation, PipelineAggregationParameters pipelineAggregationParameters) {
        if (CollectionUtils.isEmpty(this.subAggregation)) {
            this.subAggregation = new ArrayList<>();
        }
        this.subAggregation.add(aggregation);

        // 需要塞入这层子聚合的管道聚合
        int index = this.subAggregation.indexOf(aggregation);
        AggregationParameters aggregationParameters = this.subAggregation.get(index);
        aggregationParameters.setPerPipelineAggregation(pipelineAggregationParameters);
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
