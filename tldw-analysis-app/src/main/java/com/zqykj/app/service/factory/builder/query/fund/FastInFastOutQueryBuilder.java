/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.param.query.FastInFastOutQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.middle.FastInFastOutDetailRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 快进快出查询请求参数构建 </h1>
 */
@Service
public class FastInFastOutQueryBuilder extends FundTacticsCommonQueryBuilder implements FastInFastOutQueryParamFactory {

    @Override
    public QuerySpecialParams getInoutRecordsViaAdjustCards(List<String> cards, FastInFastOutRequest request, boolean isIn) {
        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id 过滤
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        // 金额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, request.getSingleQuota(), QueryOperator.gte));
        if (request.getAnalysisType() == SELECT_INDIVIDUAL) {
            String identityCard = StringUtils.isBlank(request.getIdentityCard()) ? "" : request.getIdentityCard();
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD, identityCard));
        }
        // 查询卡号集合过滤
        if (!CollectionUtils.isEmpty(cards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, cards));
        }
        if (isIn) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        querySpecialParams.addCombiningQueryParams(filter);
        // 设置source
        querySpecialParams.setIncludeFields(FundTacticsAnalysisField.fastInFastOutQueryFields());
        return querySpecialParams;
    }

    public QuerySpecialParams getInoutRecordsViaQueryAndOpposite(List<String> cards, @Nullable List<String> oppositeCards, String caseId, int singleQuota, boolean isIn) {

        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id 过滤
        filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.CASE_ID, caseId));
        // 金额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        // 查询卡号集合过滤
        if (!CollectionUtils.isEmpty(cards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, cards));
        }
        // 对方卡号集合过滤
        if (!CollectionUtils.isEmpty(oppositeCards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, oppositeCards));
        }
        if (isIn) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        querySpecialParams.addCombiningQueryParams(filter);
        // 设置source
        querySpecialParams.setIncludeFields(FundTacticsAnalysisField.fastInFastOutQueryFields());
        return querySpecialParams;
    }

    public QuerySpecialParams getResultDetail(FastInFastOutDetailRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        // filter
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        CombinationQueryParams should = new CombinationQueryParams(ConditionType.should);
        // 第一个should 过滤条件
        CombinationQueryParams shouldFilter1 = new CombinationQueryParams(ConditionType.filter);
        shouldFilter1.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getFundSourceCard(), request.getFundTransitCard()));
        shouldFilter1.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.TRADING_TIME, request.getFlowInDateTime()));
        shouldFilter1.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.TRANSACTION_MONEY, request.getFlowInAmount()));
        shouldFilter1.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, request.getFundSourceCard(), request.getFundTransitCard()));
        should.addCombinationQueryParams(shouldFilter1);
        CombinationQueryParams shouldFilter2 = new CombinationQueryParams(ConditionType.filter);
        shouldFilter2.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getFundTransitCard(), request.getDepositCard()));
        shouldFilter2.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.TRADING_TIME, request.getFlowOutDateTime()));
        shouldFilter2.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.TRANSACTION_MONEY, request.getFlowOutAmount()));
        shouldFilter2.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, request.getFundTransitCard(), request.getDepositCard()));
        should.addCombinationQueryParams(shouldFilter2);
        filter.addCombinationQueryParams(should);
        query.addCombiningQueryParams(filter);
        return query;
    }

}
