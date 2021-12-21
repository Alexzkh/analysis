/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.requestparam.query.FastInFastOutQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 快进快出查询请求参数构建 </h1>
 */
@Service
public class FastInFastOutQueryBuilder implements FastInFastOutQueryParamFactory {

    @Override
    public QuerySpecialParams getInoutRecordsViaAdjustCards(List<String> cards, String caseId, int singleQuota, boolean isIn) {
        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id 过滤
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 金额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
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

    @Override
    public QuerySpecialParams getAdjustCards(String caseId, int singleQuota) {
        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRANSACTION_MONEY, singleQuota, QueryOperator.gte));

        querySpecialParams.addCombiningQueryParams(filter);
        // 只查询卡号
        querySpecialParams.setIncludeFields(new String[]{FundTacticsAnalysisField.QUERY_CARD});
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

}
