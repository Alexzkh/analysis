package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.FundSourceAndDestinationQueryRequestFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.FundsResultType;
import com.zqykj.common.enums.FundsSourceAndDestinationStatisticsType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.FundSourceAndDestinationCardResultRequest;
import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.parameters.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description: 资金来源去向查询构建工厂
 * @Author zhangkehou
 * @Date 2021/11/22
 */
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class FundSourceAndDestinationQueryBuilderFactory implements FundSourceAndDestinationQueryRequestFactory {


    @Override
    public <T, V> QuerySpecialParams buildFundsSourceAndDestinationAnalysisResquest(T requestParam, V parameter, FundsResultType type) {

        FundsSourceAndDestinationStatisticsRequest request = (FundsSourceAndDestinationStatisticsRequest) requestParam;
        String caseId = parameter.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));

        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRADING_TIME
                    , new DateRange(request.getDateRange().getStart()
                    , request.getDateRange().getEnd())));
        }
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.CHANGE_AMOUNT, request.getFund(),
                QueryOperator.of(request.getOperator().name())
        ));

        // 如果根据交易金额来计算资金来源去向,则查询条件需要加上对借贷标志的过滤
        if (request.getFundsSourceAndDestinationStatisticsType().equals(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT)) {
            String loanFlag = type.equals(FundsResultType.DESTINATION) ? FundTacticsAnalysisField.LOAN_FLAG_OUT : FundTacticsAnalysisField.LOAN_FLAG_IN;
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, loanFlag));
        }

        // 构建前置查询条件--不是选择人,而是选择具体的卡的时候
        if (!CollectionUtils.isEmpty(request.getCardNums())) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(cardsFilter(request.getCardNums())));
        }

        // 指定证件号码,添加证件号码过滤条件
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                request.getIdentityCard()));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    @Override
    public <T, V> QuerySpecialParams buildFundsSourceAndDestinationLineChartResquest(T requestParam, V parameter) {
        FundsSourceAndDestinationStatisticsRequest request = (FundsSourceAndDestinationStatisticsRequest) requestParam;
        String caseId = parameter.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));

        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRADING_TIME
                    , new DateRange(request.getDateRange().getStart()
                    , request.getDateRange().getEnd())));
        }
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.CHANGE_AMOUNT, request.getFund(),
                QueryOperator.of(request.getOperator().name())
        ));


        // 构建前置查询条件--不是选择人,而是选择具体的卡的时候
        if (!CollectionUtils.isEmpty(request.getCardNums())) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(cardsFilter(request.getCardNums())));
        }

        // 指定证件号码,添加证件号码过滤条件
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                request.getIdentityCard()));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);

        CombinationQueryParams secCombinationQueryParams = new CombinationQueryParams();
        secCombinationQueryParams.setType(ConditionType.must_not);
        // 指定证件号码,添加证件号码过滤条件
        secCombinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.OPPOSITE_IDENTITY_CARD,
                request.getIdentityCard()));
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(secCombinationQueryParams));
        return querySpecialParams;
    }

    @Override
    public <T, V> QuerySpecialParams buildFundsSourceAndDestinationCardResultResquest(T requestParam, V parameter) {
        FundsSourceAndDestinationStatisticsRequest request = (FundsSourceAndDestinationStatisticsRequest) requestParam;
        String caseId = parameter.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));
        if (request.getFundsSourceAndDestinationStatisticsType().equals(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT)) {
            String loanFlag = request.getFundsResultType().equals(FundsResultType.DESTINATION) ? FundTacticsAnalysisField.LOAN_FLAG_OUT : FundTacticsAnalysisField.LOAN_FLAG_IN;
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, loanFlag));
        }


        // 指定证件号码,添加证件号码过滤条件
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                request.getIdentityCard()));
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

}
