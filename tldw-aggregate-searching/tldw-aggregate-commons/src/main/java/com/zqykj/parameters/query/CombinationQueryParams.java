/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import com.zqykj.common.enums.ConditionType;
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
    private ConditionType type;

    /**
     * 组合查询参数
     */
    private List<CommonQueryParams> commonQueryParams;

    // should 里 or 条件需要至少匹配几个
    private DefaultQueryParam defaultQueryParam;

    /**
     * <h2> 添加一个普通查询条件 </h2>
     */
    public void addCommonQueryParams(CommonQueryParams commonQueryParams) {

        if (CollectionUtils.isEmpty(this.commonQueryParams)) {
            this.commonQueryParams = new ArrayList<>();
        }
        this.commonQueryParams.add(commonQueryParams);
    }

    /**
     * <h2> 添加一个组合查询条件 </h2>
     */
    public void addCombinationQueryParams(CombinationQueryParams combinationQueryParams) {
        if (null != combinationQueryParams) {
            CommonQueryParams commonQueryParams = new CommonQueryParams(combinationQueryParams);
            addCommonQueryParams(commonQueryParams);
        }
    }

    public String convert(ConditionType type) {

        if (ConditionType.must_not == type) {
            return "mustNot";
        }
        return type.toString();
    }

    public CombinationQueryParams(ConditionType type) {
        this.type = type;
    }

    public void setType(ConditionType type) {
        if (type == ConditionType.should) {
            this.setDefaultQueryParam(new DefaultQueryParam());
        }
        this.type = type;
    }
}
