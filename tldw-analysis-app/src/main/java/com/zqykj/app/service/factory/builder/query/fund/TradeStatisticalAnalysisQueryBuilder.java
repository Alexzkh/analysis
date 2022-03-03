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
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        CombinationQueryParams filter = queryRequestParamFactory.buildCommonQueryParamsViaBankTransactionRecord(request, other);
        querySpecialParams.addCombiningQueryParams(filter);
        return querySpecialParams;
    }

    @Override
    public <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequestByMainCards(T requestParam, V other) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) requestParam;
        // 获取前置请求
        FundTacticsPartGeneralPreRequest preRequest = request.convertFrom(request);
        CombinationQueryParams filter = queryRequestParamFactory.buildCommonQueryParamsViaBankTransactionRecord(preRequest, other);
        // 本方需要的模糊匹配
        if (StringUtils.isNotBlank(request.getKeyword())) {
            CombinationQueryParams fuzzyQuery = queryRequestParamFactory.assembleLocalFuzzy(request.getKeyword());
            filter.addCombinationQueryParams(fuzzyQuery);
        }
        querySpecialParams.addCombiningQueryParams(filter);

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
