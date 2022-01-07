/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.param.query.UnadjustedAccountQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1> 未调单账户分析查询参数构建 </h1>
 */
@Service
public class UnadjustedAccountQueryBuilder implements UnadjustedAccountQueryParamFactory {


    public QuerySpecialParams queryUnadjusted(String caseId, List<String> adjustCards, String keyword, DateRange dateRange) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        if (null != dateRange) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
        CombinationQueryParams mustNot = new CombinationQueryParams(ConditionType.must_not);
        mustNot.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, adjustCards));
        // 去除查询卡号为 "" 的情况
        mustNot.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.QUERY_CARD, ""));
        filter.addCombinationQueryParams(mustNot);
        // 增加模糊查询
        if (StringUtils.isNotBlank(keyword)) {
            CombinationQueryParams fuzzyQuery = fuzzyQuery(keyword);
            filter.addCombinationQueryParams(fuzzyQuery);
        }
        query.addCombiningQueryParams(filter);
        return query;
    }

    /**
     * <h2> 模糊查询 </h2>
     */
    public CombinationQueryParams fuzzyQuery(String keyword) {

        CombinationQueryParams localFuzzy = new CombinationQueryParams();
        localFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.unadjustAnalysisFuzzyFields) {

            localFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
        }
        return localFuzzy;
    }

    public QuerySpecialParams queryUnadjustedExtraInfo(String caseId, List<String> unAdjustedCards) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, unAdjustedCards));
        query.addCombiningQueryParams(filter);
        return query;
    }
}
