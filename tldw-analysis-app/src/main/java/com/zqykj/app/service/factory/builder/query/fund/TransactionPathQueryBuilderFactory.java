package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.ITransactionPathQueryRequestFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.TransactionPathDetailRequest;
import com.zqykj.common.request.TransactionPathRequest;
import com.zqykj.parameters.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description: 交易路径操作elasticsearch查询请求构建工厂类
 * @Author zhangkehou
 * @Date 2021/12/17
 */
@Service
public class TransactionPathQueryBuilderFactory implements ITransactionPathQueryRequestFactory {


    @Override
    public <T, V> QuerySpecialParams accessTransactionPathDataByCondition(T request, V param, List<String> ids) {
        TransactionPathRequest transactionPathRequest = (TransactionPathRequest) request;
        String caseId = param.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));

        // 指定日期范围
        if (null != transactionPathRequest.getDateRange() && StringUtils.isNotBlank(transactionPathRequest.getDateRange().getStart())
                & StringUtils.isNotBlank(transactionPathRequest.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRADING_TIME
                    , new DateRange(transactionPathRequest.getDateRange().getStart()
                    , transactionPathRequest.getDateRange().getEnd())));
        }
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRANSACTION_MONEY, transactionPathRequest.getFund(),
                QueryOperator.of(transactionPathRequest.getOperator().name())
        ));
        // 指定ids集合的in 查询
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.ID, ids));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    @Override
    public <T, V> QuerySpecialParams accessTransactionPathDetailRequest(T t, V param) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        TransactionPathDetailRequest request = (TransactionPathDetailRequest) t;
        String caseId = param.toString();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));
        if (!CollectionUtils.isEmpty(request.getIds())) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.ID, request.getIds()));
        }
        // 增加模糊查询条件
        if (StringUtils.isNotBlank(request.getQueryRequest().getKeyword())) {
            CombinationQueryParams localFuzzy = assembleLocalFuzzy(request.getQueryRequest().getKeyword());
            CombinationQueryParams oppositeFuzzy = assembleOppositeFuzzy(request.getQueryRequest().getKeyword());
            localFuzzy.getCommonQueryParams().addAll(oppositeFuzzy.getCommonQueryParams());
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }


}
