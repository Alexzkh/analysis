/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1> 查询参数 </h1>
 * <p>
 * 具体使用方式: 请看该类的 main 方法
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuerySpecialParams {

    // 单个查询(不能和组合查询同时使用)
    private CommonQueryParams commonQuery;

    // 组合查询 (eg. 可以满足  a = 1 and b = 2 and (c = 4 or d = 5) 等复合查询)
    private List<CombinationQueryParams> combiningQuery;

    // 全局查询默认参数
    private DefaultQueryParam defaultParam;

    // TODO 分页参数设置 可以放在最外面(如果有参数 com.zqykj.domain.Pageable 和它同时存在,优先取Pageable)
    private Pagination pagination;

    // 排序
    private FieldSort sort;

    // 对query 查询 的数据 再次过滤
    private QuerySpecialParams postFilter;

    // 查询返回的数据, 需要带出哪些字段
    private String[] includeFields;

    // 查询返回的数据,不需要带出哪些字段
    private String[] excludeFields;

    public QuerySpecialParams(CommonQueryParams common) {
        this.commonQuery = common;
    }

    public QuerySpecialParams(List<CombinationQueryParams> combiningQuery) {
        this.combiningQuery = combiningQuery;
    }

    public void addCommonQueryParams(CommonQueryParams commonQuery) {
        this.commonQuery = commonQuery;
    }

    public void addCombiningQueryParams(CombinationQueryParams combinationQueryParams) {

        if (CollectionUtils.isEmpty(this.combiningQuery)) {
            this.combiningQuery = new ArrayList<>();
        }
        this.combiningQuery.add(combinationQueryParams);
    }

    public void addCombiningQueryParams(List<CombinationQueryParams> combinationQueryParams) {

        if (CollectionUtils.isEmpty(this.combiningQuery)) {
            this.combiningQuery = new ArrayList<>();
        }
        this.combiningQuery.addAll(combinationQueryParams);
    }

    /**
     * <h1> 查询类型的翻译 </h1>
     * TODO 后面做一个翻译器, 将公共的类型 翻译成不同数据源需要的查询类型
     */
    public String queryTypeConvert(String type) {

        return type;
    }

    /**
     * <h2> 使用方式 </h2>
     */
    public static void main(String[] args) {

        // 1. 定义一个普通查询
        QuerySpecialParams commonQuery = new QuerySpecialParams();
        // 具体含义为 select .... from ... where field = value
        commonQuery.addCommonQueryParams(QueryParamsBuilders.term("field", "value"));
        // 或者定义成   QueryParamsBuilders.term("field", "value") 等价于 new CommonQueryParams(QueryType.term, "field", "value")
        // QueryParamsBuilders 做了一些简单的封装
        commonQuery.addCommonQueryParams(new CommonQueryParams(QueryType.term, "field", "value"));

        //2. 定义组合查询
        QuerySpecialParams combinationQuery = new QuerySpecialParams();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();

        // 定义组合查询类型 and or filter !=
        // and 示例  field1 = value1 and field2 = value2
        combinationQueryParams.setType(ConditionType.must);
        // 组合多个单个查询
        // 查询1
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term("field1", "value1"));
        // 查询2
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term("field2", "value2"));
        // 包装组合查询
        combinationQuery.addCombiningQueryParams(combinationQueryParams);

        // 单个查询嵌套组合查询
        // 示例 or   field3 = value3 or (field1 = value1 and field2 = value2)
        QuerySpecialParams combinationQuery2 = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams2 = new CombinationQueryParams();
        combinationQueryParams2.setType(ConditionType.should);

        // 条件 field3 = value3
        combinationQueryParams2.addCommonQueryParams(QueryParamsBuilders.term("field3", "value3"));

        // 单个条件继续组合查询 条件 field1 = value1 and field2 = value2
        combinationQueryParams2.addCommonQueryParams(new CommonQueryParams(combinationQueryParams));

        // 包装组合查询
        combinationQuery2.addCombiningQueryParams(combinationQueryParams2);
    }
}
