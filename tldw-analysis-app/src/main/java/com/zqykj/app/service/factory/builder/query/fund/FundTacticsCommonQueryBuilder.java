/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.parameters.query.CombinationQueryParams;

import java.util.List;

/**
 * <h1> 资金战法部分通用查询构建 </h1>
 */
public abstract class FundTacticsCommonQueryBuilder {

    protected CombinationQueryParams queryCardsAndCase(String caseId, List<String> cards) {

        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, cards));
        return filter;
    }
}
