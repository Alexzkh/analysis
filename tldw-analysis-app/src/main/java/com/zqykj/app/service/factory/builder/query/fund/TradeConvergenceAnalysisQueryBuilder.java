/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.factory.requestparam.query.TradeConvergenceAnalysisQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisQueryRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 交易汇聚分析查询请求参数构建 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeConvergenceAnalysisQueryBuilder implements TradeConvergenceAnalysisQueryParamFactory {

    private final QueryRequestParamFactory queryRequestParamFactory;

    @Override
    public <T, V> QuerySpecialParams buildTradeConvergenceAnalysisResultMainCardsRequest(T t, V v) {

        QuerySpecialParams convergenceQuery = new QuerySpecialParams();
        TradeConvergenceAnalysisQueryRequest request = (TradeConvergenceAnalysisQueryRequest) t;
        // 获取前置请求
        FundTacticsPartGeneralPreRequest preRequest = request.convertFrom(request);
        CombinationQueryParams combinationQueryParams = queryRequestParamFactory.buildCommonQueryParamsViaBankTransactionRecord(preRequest, v);
        // 合并卡号集合过滤
        if (!CollectionUtils.isEmpty(request.getMergeCards())) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.MERGE_CARD, request.getMergeCards()));
        }
        // 增加模糊查询条件
        if (StringUtils.isNotBlank(request.getKeyword())) {

            CombinationQueryParams localFuzzy = queryRequestParamFactory.assembleLocalFuzzy(request.getKeyword());

            CombinationQueryParams oppositeFuzzy = queryRequestParamFactory.assembleOppositeFuzzy(request.getKeyword());

            localFuzzy.getCommonQueryParams().addAll(oppositeFuzzy.getCommonQueryParams());

            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }
        // 过滤 查询卡号 和 对方卡号为空的交易记录
        CombinationQueryParams mustNot = new CombinationQueryParams();
        mustNot.setType(ConditionType.must_not);
        mustNot.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.QUERY_CARD, ""));
        mustNot.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, ""));
        convergenceQuery.addCombiningQueryParams(mustNot);
        convergenceQuery.addCombiningQueryParams(combinationQueryParams);
        return convergenceQuery;
    }

    @Override
    public QuerySpecialParams buildTradeConvergenceAnalysisHitsQuery(List<String> mergeCards, String caseId) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.filter);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定合并卡号集合过滤
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.MERGE_CARD, mergeCards));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        // 设置需要返回的字段
        querySpecialParams.setIncludeFields(FundTacticsAnalysisField.tradeConvergenceAnalysisShowField());

        return querySpecialParams;
    }
}
