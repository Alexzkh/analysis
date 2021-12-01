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

    public void addCommonQueryParams(CommonQueryParams commonQueryParams) {

        if (CollectionUtils.isEmpty(this.commonQueryParams)) {
            this.commonQueryParams = new ArrayList<>();
        }
        this.commonQueryParams.add(commonQueryParams);
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
}
