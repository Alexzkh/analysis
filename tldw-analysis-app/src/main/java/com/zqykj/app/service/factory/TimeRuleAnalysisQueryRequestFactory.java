package com.zqykj.app.service.factory;

import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * @Description: 时间规律查询elasticsearch查询构建
 * @Author zhangkehou
 * @Date 2021/12/30
 */
public interface TimeRuleAnalysisQueryRequestFactory {


    /**
     * 构建时间规律分析请求查询elasticsearch的参数
     *
     * @param request: 时间规律请求body
     * @param param:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildTimeRuleAnalysisQueryRequest(T request, V param);

    /**
     * 构建时间规律分析详情请求查询elasticsearch的参数
     *
     * @param request: 时间规律详情请求body
     * @param param:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildTimeRuleAnalysisDetailQueryRequest(T request, V param, List<String> festival);


    /**
     * 构建本方模糊参数参数
     *
     * @param keyword: 模糊查询关键字
     * @return: com.zqykj.parameters.query.CombinationQueryParams
     **/
    default CombinationQueryParams assembleLocalFuzzy(String keyword) {

        CombinationQueryParams localFuzzy = new CombinationQueryParams();
        localFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.localFuzzyFields) {

            localFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
        }
        return localFuzzy;
    }

    /**
     * 构建对方模糊查询参数
     *
     * @param keyword: 模糊查询关键字
     * @return: com.zqykj.parameters.query.CombinationQueryParams
     **/
    default CombinationQueryParams assembleOppositeFuzzy(String keyword) {

        CombinationQueryParams oppositeFuzzy = new CombinationQueryParams();
        oppositeFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.oppositeFuzzyFields) {

            oppositeFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
        }
        return oppositeFuzzy;
    }
}
