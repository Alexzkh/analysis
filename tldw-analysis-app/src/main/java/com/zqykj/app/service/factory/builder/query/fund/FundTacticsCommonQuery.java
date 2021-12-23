/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.parameters.query.QuerySpecialParams;

/**
 * <h1> 资金战法部分通用查询构建 </h1>
 */
public class FundTacticsCommonQuery {

    protected QuerySpecialParams queryByCaseId(String caseId) {
        QuerySpecialParams query = new QuerySpecialParams();
        query.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        return query;
    }
}
