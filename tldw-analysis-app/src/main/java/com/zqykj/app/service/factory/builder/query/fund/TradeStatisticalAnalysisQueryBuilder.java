/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.factory.param.query.TradeStatisticalAnalysisQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundDateRequest;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DefaultQueryParam;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 交易统计分析查询请求参数构建 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeStatisticalAnalysisQueryBuilder extends FundTacticsCommonQueryBuilder implements TradeStatisticalAnalysisQueryParamFactory {

    private final QueryRequestParamFactory queryRequestParamFactory;

    @Override
    public <T, V> QuerySpecialParams createTradeAmountByTimeQuery(T requestParam, V other) {

        // 构建前置查询条件
        FundDateRequest request = (FundDateRequest) requestParam;
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = queryRequestParamFactory.buildCommonQueryParamsViaBankTransactionFlow(requestParam, other);
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(cardsFilter(request.getCardNum())));
        }
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    private CombinationQueryParams cardsFilter(List<String> cardNums) {

        CombinationQueryParams combination = new CombinationQueryParams();
        combination.setType(ConditionType.should);

        if (!CollectionUtils.isEmpty(cardNums)) {
            combination.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, cardNums));
            combination.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, cardNums));
        }
        return combination;
    }

    @Override
    public <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequestByMainCards(T requestParam, V other, Class<?> queryTable) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) requestParam;
        // 获取前置请求
        FundTacticsPartGeneralPreRequest preRequest = request.convertFrom(request);
        CombinationQueryParams combinationQueryParams;
        if (BankTransactionFlow.class.isAssignableFrom(queryTable)) {
            combinationQueryParams = queryRequestParamFactory.buildCommonQueryParamsViaBankTransactionFlow(preRequest, other);
        } else {
            combinationQueryParams = queryRequestParamFactory.buildCommonQueryParamsViaBankTransactionRecord(preRequest, other);
        }

        CombinationQueryParams cardNumsAndFuzzyQuery = new CombinationQueryParams();

        cardNumsAndFuzzyQuery.setType(ConditionType.must);
        // 本方查询卡号(有可能是查询全部,那么卡号不为空的时候才能选用此条件)
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            cardNumsAndFuzzyQuery.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, request.getCardNum()));
        }
        // 本方需要的模糊匹配
        if (StringUtils.isNotBlank(request.getKeyword())) {
            CombinationQueryParams localFuzzy = queryRequestParamFactory.assembleLocalFuzzy(request.getKeyword());
            localFuzzy.setDefaultQueryParam(new DefaultQueryParam());
            cardNumsAndFuzzyQuery.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }

        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(cardNumsAndFuzzyQuery));

        querySpecialParams.addCombiningQueryParams(combinationQueryParams);

        return querySpecialParams;
    }

    @Override
    public QuerySpecialParams buildTradeStatisticalAnalysisHitsQuery(List<String> queryCards, String caseId) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.filter);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定合并卡号集合过滤
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, queryCards));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        // 设置需要返回的字段
        querySpecialParams.setIncludeFields(FundTacticsAnalysisField.tradeStatisticalAnalysisLocalShowField());

        return querySpecialParams;
    }
}
