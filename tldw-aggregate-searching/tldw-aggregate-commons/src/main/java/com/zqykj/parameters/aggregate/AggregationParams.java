/**
 * @作者 Mcj
 */
package com.zqykj.parameters.aggregate;

import com.zqykj.parameters.aggregate.date.DateParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.annotation.NotResolve;
import com.zqykj.parameters.annotation.OptionalParam;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <h1> 聚合参数封装 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AggregationParams {

    /**
     * 聚合名称
     */
    private String name;

    /**
     * 脚本表达式
     */
    @OptionalParam
    private String script;

    /**
     * 聚合类型
     */
    private String type;

    /**
     * 聚合需要的字段 (如果此聚合需要对某个字段进行操作的话)
     */
    @OptionalParam
    private String field;

    /**
     * 聚合桶数量返回 限制 (若此聚合产生的结果是多个,可以使用此参数做相应限制)
     */
    @OptionalParam
    private int size = 1000;

    /**
     * 通用参数
     */
    private CommonAggregationParams commonAggregationParams;

    /**
     * 日期参数
     */
    private DateParams dateParams;

    /**
     * 子聚合
     */
    private List<AggregationParams> subAggregation;

    /**
     * 管道聚合
     */
    private List<PipelineAggregationParams> pipelineAggregation;

    /**
     * 查询参数
     */
    private QuerySpecialParams querySpecialParams;

    // 聚合里面也可以带出相关的字段
    private FetchSource fetchSource;

    // key: 聚合名称,  value: 聚合字段属性( 可以根据此属性取出对应聚合值)
    @NotResolve
    private Map<String, String> mapping;

    // key: 聚合名称, value: 实体映射属性
    @NotResolve
    private Map<String, String> entityAggColMapping;

    // 同级聚合参数
    @NotResolve
    private List<AggregationParams> siblingAggregation;

    public AggregationParams(String name, String type, String field) {
        this.name = name;
        this.type = type;
        this.field = field;
    }

    public AggregationParams(String name, String type, FetchSource fetchSource) {
        this.name = name;
        this.type = type;
        this.fetchSource = fetchSource;
    }

    public AggregationParams(String name, String type, String field, int size) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.size = size;
    }

    public AggregationParams(String name, String type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }

    public AggregationParams(String name, String type, CommonAggregationParams generalParameters) {
        this.name = name;
        this.type = type;
        this.commonAggregationParams = generalParameters;
    }

    public AggregationParams(String name, String type, String field,
                             CommonAggregationParams generalParameters) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.commonAggregationParams = generalParameters;
    }

    public AggregationParams(String name, String type, DateParams dateParams) {
        this.name = name;
        this.type = type;
        this.dateParams = dateParams;
    }

    public AggregationParams(String name, String type, String field, DateParams dateParams) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.dateParams = dateParams;
    }

    public AggregationParams(String name, String type, CommonAggregationParams generalParameters, DateParams dateParams) {
        this.name = name;
        this.type = type;
        this.commonAggregationParams = generalParameters;
        this.dateParams = dateParams;
    }

    public AggregationParams(String name, String type, String field, CommonAggregationParams generalParameters, DateParams dateParams) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.commonAggregationParams = generalParameters;
        this.dateParams = dateParams;
    }

    public AggregationParams(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public AggregationParams(String name, String type, QuerySpecialParams query) {
        this.name = name;
        this.type = type;
        this.querySpecialParams = query;
    }

    public void setPerSubAggregation(AggregationParams aggregation) {
        if (CollectionUtils.isEmpty(this.subAggregation)) {
            this.subAggregation = new ArrayList<>();
        }
        this.subAggregation.add(aggregation);
    }

    public void setPerSubAggregation(AggregationParams aggregation, PipelineAggregationParams pipelineAggregationParams) {
        if (CollectionUtils.isEmpty(this.subAggregation)) {
            this.subAggregation = new ArrayList<>();
        }
        this.subAggregation.add(aggregation);

        // 需要塞入这层子聚合的管道聚合
        int index = this.subAggregation.indexOf(aggregation);
        AggregationParams aggregationParams = this.subAggregation.get(index);
        aggregationParams.setPerPipelineAggregation(pipelineAggregationParams);
    }


    public void setPerPipelineAggregation(PipelineAggregationParams aggregation) {
        if (CollectionUtils.isEmpty(this.pipelineAggregation)) {
            this.pipelineAggregation = new ArrayList<>();
        }
        this.pipelineAggregation.add(aggregation);
    }

    public void setSiblingAggregation(AggregationParams sibling) {

        if (CollectionUtils.isEmpty(this.siblingAggregation)) {
            this.siblingAggregation = new ArrayList<>();
        }
        this.siblingAggregation.add(sibling);
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
