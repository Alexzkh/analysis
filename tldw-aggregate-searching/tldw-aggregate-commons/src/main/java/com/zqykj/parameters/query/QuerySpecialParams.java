/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1> 查询参数 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuerySpecialParams {

    // 单个查询(不能和组合查询同时使用)
    private CommonQueryParams commonQuery;

    // 组合查询 (eg. 可以满足  a = 1 and b = 2 and (c = 4 or d = 5) 等复合查询)
    private List<CombinationQueryParams> combiningQuery;

    // 全局查询默认参数
    private DefaultQueryParam defaultParam;

    // TODO 分页参数设置 可以放在最外面
    private Pagination pagination;

    // 排序
    private FieldSort sort;

    // 对query 查询 的数据 再次过滤
    private QuerySpecialParams postFilter;

    public QuerySpecialParams(CommonQueryParams common) {
        this.commonQuery = common;
    }

    public QuerySpecialParams(List<CombinationQueryParams> combiningQuery) {
        this.combiningQuery = combiningQuery;
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
}
