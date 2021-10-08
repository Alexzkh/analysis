/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.parameters.Pagination;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // 组合查询
    private List<CombinationQueryParams> combiningQuery;

    // TODO 分页参数设置 可以放在最外面
    private Pagination pagination;

    /**
     * <h1> 查询类型的翻译 </h1>
     * TODO 后面做一个翻译器, 将公共的类型 翻译成不同数据源需要的查询类型
     */
    public String queryTypeConvert(String type) {

        return type;
    }
}
