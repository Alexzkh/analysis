/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.param.query.UnadjustedAccountQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralRequest;
import com.zqykj.app.service.vo.fund.UnadjustedAccountAnalysisRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 未调单账户分析查询参数构建 </h1>
 */
@Service
public class UnadjustedAccountQueryBuilder implements UnadjustedAccountQueryParamFactory {


    public QuerySpecialParams queryUnadjusted(UnadjustedAccountAnalysisRequest request, List<String> adjustCards) {

        DateRange dateRange = FundTacticsPartGeneralRequest.getDateRange(request.getDateRange());
        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        if (null != dateRange) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
        CombinationQueryParams mustNot = new CombinationQueryParams(ConditionType.must_not);
        if (!CollectionUtils.isEmpty(adjustCards)) {
            mustNot.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, adjustCards));
        }
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNum()));
        }
        // 去除查询卡号为 "" 的情况
        mustNot.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.QUERY_CARD, ""));
        filter.addCombinationQueryParams(mustNot);
        // 增加模糊查询
        if (StringUtils.isNotBlank(request.getKeyword())) {
            CombinationQueryParams fuzzyQuery = unadjustedFuzzyQuery(request.getKeyword());
            filter.addCombinationQueryParams(fuzzyQuery);
        }
        query.addCombiningQueryParams(filter);
        return query;
    }

    /**
     * <h2> 未调单分析模糊查询 </h2>
     */
    private CombinationQueryParams unadjustedFuzzyQuery(String keyword) {

        CombinationQueryParams localFuzzy = new CombinationQueryParams();
        localFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.unadjustedAnalysisFuzzyFields) {

            localFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
        }
        return localFuzzy;
    }

    /**
     * <h2> 建议调单模糊查询 </h2>
     */
    private CombinationQueryParams suggestAdjustedFuzzyQuery(String keyword) {
        CombinationQueryParams localFuzzy = new CombinationQueryParams();
        localFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.suggestAdjustedFuzzyFields) {

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

    public QuerySpecialParams querySuggestAdjustAccount(FundTacticsPartGeneralRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        // 增加模糊查询
        if (StringUtils.isNotBlank(request.getKeyword())) {
            filter.addCombinationQueryParams(suggestAdjustedFuzzyQuery(request.getKeyword()));
        }
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams deleteSuggestAdjustAccount(FundTacticsPartGeneralRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField._ID, request.getIds()));
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        query.addCombiningQueryParams(filter);
        return query;
    }
}
