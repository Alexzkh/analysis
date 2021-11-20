/**
 * @作者 Mcj
 */
package com.zqykj.parameters.aggregate;

import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.enums.AggsType;
import com.zqykj.parameters.aggregate.date.DateParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.annotation.NotResolve;
import com.zqykj.parameters.annotation.OptionalParam;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * <h1> 聚合参数封装 </h1>
 * <p>
 * 具体使用方式: 请看该类的 main 方法
 */
@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AggregationParams {

    /**
     * 聚合名称(当定义管道聚合的时候,无须定义此名称,因为 PipelineAggregationParams 参数需要定义在 AggregationParams之内)
     * eg. 需要AggregationParams params = new AggregationParams() , 然后 调用此方法 params.toPipeline(PipelineAggregationParams params) 即可;
     */
    private String name;

    /**
     * 脚本表达式(聚合可以通过脚本语言实现)
     * <p>
     * 注:
     * es    支持
     * mysql 未知(待查询官方文档)
     * mongo 未知(待查询官方文档)
     */
    @OptionalParam
    private String script;

    /**
     * 聚合类型 {@link com.zqykj.enums.AggsType} 最终需要翻译成不同数据源能识别的聚合类型名称
     * <p>
     * 注:
     * es
     * mysql
     * mongo
     */
    private String type;

    /**
     * 聚合操作字段
     */
    @OptionalParam
    private String field;

    /**
     * 聚合多个字段
     * <p>
     * eg. group by 多个字段
     * es 目前es 15 , kibana 可以使用 multi_terms 完成此功能, 对应java client 没有对应类实现此功能
     * mysql: 支持
     * mongo: 支持
     */
    @OptionalParam
    private String[] fields;

    /**
     * group by后 内存限制返回的数据集个数(默认10000)
     * <p>
     * 注:
     * es 由于是在内存中计算,默认最大支持返回65534(es 7.9以上) 该参数可以通过集群设置调整
     * mysql 实际返回多少处理多少(可以从磁盘中继续拿数据处理,不是纯内存处理)
     * mongo 未知(待查询官方文档)
     */
    @OptionalParam
    private int size = 10_000;

    /**
     * 子聚合推迟计算处理方式 (DEPTH_FIRST(深度优先) / BREADTH_FIRST(广度优先))
     * <p>
     * 示例问题场景: 是在电影数据库中查询 10 位最受欢迎的演员及其 5 位最常见的联合主演, 需要先group by 演员, 然后继续下一步子聚合再group by 演员
     * 即使演员的数量可能相对较少,并且我们只需要 10 个结果,但在计算过程中桶的组合爆炸 - 单个演员可以产生 n² 结果,其中 n 是演员的数量。
     * 明智的选择是首先确定 10 位最受欢迎的演员, 然后再检查这 10 位演员的顶级联合主演。这种替代策略就是我们所说的广度优先收集模式, 而不是深度优先模式
     * <p>
     * 注:
     * es 支持
     * mongo 未知(待查询官方文档)
     * mysql 未知(待查询官方文档)
     */
    @OptionalParam
    private String collectMode;

    /**
     * group by 的时候先过滤一组数据
     * <p>
     * key: includeValues value: 数据集(跟group by 的field 有关)
     * key: excludeValues value: 数据集(跟group by 的field 有关)
     * <p>
     * value 值类型目前只支持 String (该功能完全可以先对数据集进行filter 代替)
     */
    @OptionalParam
    private Map<String, String[]> includeExclude;

    /**
     * 日期
     */
    private DateParams dateParams;

    /**
     * 子聚合 (只有桶聚合才能定义子聚合, 例如group by 等,它能产生多组数据集) sum、avg等指标聚合不存在子聚合
     * <p>
     * 注:
     * es  它的子聚合 无非是在已经有的聚合下继续写聚合(对父层聚合结果的再次统计分析)
     * mysql 类似的可以用having(他可以跟聚合函数) 、或者嵌套子查询等去实现此功能
     * mongo 提供了此功能(Aggregation Pipeline)
     * <p>
     * eg:
     * db.orders.aggregate( [
     * { $match: { status: "urgent" } },
     * { $group: { _id: "$productName", sumQuantity: { $sum: "$quantity" } } }
     * ] )  此mongo 聚合包含两个阶段, 并返回每个产品的订单总数(先过滤 status = urgent,然后按照 productName 分组, 再分组计算产品总量)
     */
    private List<AggregationParams> subAggregation;

    /**
     * 管道聚合 (是对已经有的不同聚合结果再次做统计分析)
     * <p>
     * 注:
     * es  支持
     * mysql 可以使用 嵌套子查询 或者 having(后面跟聚合函数) 等等去实现此功能
     * mongo 未知(等待查询官方文档)
     */
    private List<PipelineAggregationParams> pipelineAggregation;

    /**
     * 过滤查询参数(可以再次对结果数据进行过滤后再进行聚合)
     * <p>
     * 注: mysql 这一项可能使用的是子查询方式
     */
    private QuerySpecialParams querySpecialParams;

    /**
     * 聚合需要显示的字段
     * <p>
     * 注: 其中es 需要特定的聚合, 该参数才会生效(eg. 定义了topHits聚合)
     */
    private FetchSource fetchSource;

    /**
     * key: 聚合名称,  value: 聚合字段属性( 可以根据此属性取出对应聚合值)
     * <p>
     * 使用的是LinkedHashMap 保证顺序
     */
    @NotResolve
    private Map<String, String> mapping;

    /**
     * key: 聚合名称, value: 实体映射属性 (聚合名称 与 实体属性映射) -- 使用的是LinkedHashMap 保证顺序
     * <p>
     * 注: 上方mapping key 的顺序与这里的key 顺序一致,这样就能保证 聚合取出的值 与 实体属性一一对应, 这样方便我们反序列化实体
     */
    @NotResolve
    private Map<String, String> entityAggColMapping;

    /**
     * 同级聚合
     */
    @NotResolve
    private List<AggregationParams> siblingAggregation;

    /**
     * 给聚合 定义一个功能描述名称 (方便根据此名称将此聚合所有的统计值带出)
     * <p>
     * 注:
     * mysql  select count(1) from test;  可以给这个聚合功能定义一个 total 描述功能名称
     * es 一个 aggregations
     */
    @NotResolve
    private String resultName;

    public AggregationParams(String name, String type, String[] fields) {
        this.name = name;
        this.type = type;
        this.fields = fields;
    }

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

    public AggregationParams(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public AggregationParams(String name, String type, QuerySpecialParams query) {
        this.name = name;
        this.type = type;
        this.querySpecialParams = query;
    }

    public AggregationParams(PipelineAggregationParams pipelineAggregationParams) {
        this.pipelineAggregation = Collections.singletonList(pipelineAggregationParams);
    }

    // 设置一个普通聚合
    public void setPerSubAggregation(AggregationParams aggregation) {
        if (CollectionUtils.isEmpty(this.subAggregation)) {
            this.subAggregation = new ArrayList<>();
        }
        this.subAggregation.add(aggregation);
    }

    // 设置一个管道聚合
    public void setPerSubAggregation(PipelineAggregationParams aggregation) {

        if (CollectionUtils.isEmpty(this.subAggregation)) {
            this.subAggregation = new ArrayList<>();
        }
        // new AggregationParams(aggregation) 意为 将一个普通聚合包装成管道聚合
        this.subAggregation.add(new AggregationParams(aggregation));
    }

    // 添加同级聚合(一个普通聚合)
    public void addSiblingAggregation(AggregationParams sibling) {

        if (CollectionUtils.isEmpty(this.siblingAggregation)) {

            siblingAggregation = new ArrayList<>();
        }
        this.siblingAggregation.add(sibling);
    }

    // 添加同级聚合(一个管道聚合) 需要将 PipelineAggregationParams 包装成 AggregationParams, 使用方法toPipeline 即可)
    public void addSiblingAggregation(PipelineAggregationParams sibling) {

        AggregationParams pipelineParams = new AggregationParams();
        pipelineParams.toPipeline(sibling);
        addSiblingAggregation(pipelineParams);
    }

    // 将 AggregationParams 包装成一个管道聚合
    public void toPipeline(PipelineAggregationParams params) {

        if (CollectionUtils.isEmpty(this.pipelineAggregation)) {

            pipelineAggregation = new ArrayList<>();
        }
        this.pipelineAggregation.add(params);
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

    public static void main(String[] args) {
        // 聚合参数封装示例
        // 1. 定义一个聚合 eg. group by
        AggregationParams groupByOne = AggregationParamsBuilders.terms("name", "field");
        // AggregationParamsBuilders.terms("name", "field") 等价于  new AggregationParams("name", AggsType.terms.name(), "field")
        // AggregationParamsBuilders 做了一些简单的封装
        AggregationParams groupByTwo = new AggregationParams("name", AggsType.terms.name(), "field");

        // 2. 定义一个管道聚合
        AggregationParams pipelineParams = new AggregationParams();
        PipelineAggregationParams pipelineAggregationParams = new PipelineAggregationParams("name", AggsType.bucket_selector.name(),
                "bucketsPath");
        pipelineParams.toPipeline(pipelineAggregationParams);

        // 3. 定义子聚合
        AggregationParams father = AggregationParamsBuilders.terms("name", "field");
        AggregationParams sub = AggregationParamsBuilders.sum("name", "field");
        // 设置一个普通子聚合
        father.setPerSubAggregation(sub);
        // 设置一个管道子聚合
        PipelineAggregationParams subPipeline = new PipelineAggregationParams("name", AggsType.bucket_selector.name(),
                "bucketsPath");
        father.setPerSubAggregation(subPipeline);

        // 4. 定义同级兄弟聚合
        AggregationParams first = AggregationParamsBuilders.terms("name", "field");
        // 第二个普通聚合
        AggregationParams second = AggregationParamsBuilders.sum("name", "field");
        // 第三个管道聚合
        PipelineAggregationParams third = new PipelineAggregationParams("name", AggsType.bucket_selector.name(),
                "bucketsPath");
        // 上述 first、second、third 三个聚合属于同级聚合
        // 设置同级兄弟聚合
        first.addSiblingAggregation(second);
        first.addSiblingAggregation(third);
        // ....
    }
}
