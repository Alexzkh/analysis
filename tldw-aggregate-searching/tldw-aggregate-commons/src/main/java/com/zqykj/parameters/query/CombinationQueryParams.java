/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.common.enums.QueryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合查询参数
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CombinationQueryParams {

    /**
     * 查询类型 eg. must -> and , should -> or, must_not -> != , filter -> where
     */
    private QueryType type;

    /**
     * 组合查询参数
     */
    private List<CommonQueryParams> combinationQuery;

    public void addCombinationQueryParams(CommonQueryParams commonQueryParams) {

        if (CollectionUtils.isEmpty(this.combinationQuery)) {
            this.combinationQuery = new ArrayList<>();
        }
        this.combinationQuery.add(commonQueryParams);
    }


    public String convert(QueryType type) {

        if (QueryType.must_not == type) {
            return "mustNot";
        }
        return type.toString();
    }
}
