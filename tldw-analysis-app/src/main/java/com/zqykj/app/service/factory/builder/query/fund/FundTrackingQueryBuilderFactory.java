package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.IFundTrackingQueryRequestFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.tools.FundTrackingDateConditionTools;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.request.FundTrackingRequest;
import com.zqykj.common.request.GraduallyTrackingRequest;
import com.zqykj.domain.Sort;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Description: 资金追踪
 * @Author zhangkehou
 * @Date 2021/11/30
 */
@Service
public class FundTrackingQueryBuilderFactory implements IFundTrackingQueryRequestFactory {

    @Value("${tracking.default.size:1000}")
    private int trackingDefaultSize;

    // 资金追踪分析结果需要展示的字段
    static String[] fundSourceAndDestinationAnalysisOppositeShowField() {

        return new String[]{FundTacticsAnalysisField.QUERY_CARD,
                FundTacticsAnalysisField.QUERY_ACCOUNT,
                FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD,
                FundTacticsAnalysisField.OPPOSITE_ACCOUNT,
                FundTacticsAnalysisField.TRADING_TIME,
                FundTacticsAnalysisField.TRANSACTION_MONEY,
                FundTacticsAnalysisField.LOAN_FLAG,
                FundTacticsAnalysisField.TRANSACTION_TYPE,
                FundTacticsAnalysisField.TRANSACTION_SUMMARY,
                FundTacticsAnalysisField.CUSTOMER_NAME,
                FundTacticsAnalysisField.TRANSACTION_OPPOSITE_NAME
        };
    }


    @Override
    public <T, V> QuerySpecialParams accessFundTrackingList(T request, V param) {

        FundTrackingRequest fundTrackingRequest = (FundTrackingRequest) request;
        String caseId = param.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.should);

        CommonQueryParams innerLocalQueryParams = new CommonQueryParams();
        CommonQueryParams innerOppositeQueryParams = new CommonQueryParams();

        // 指定交易金额
        CommonQueryParams amountQueryParams = QueryParamsBuilders.range(FundTacticsAnalysisField.TRANSACTION_MONEY, fundTrackingRequest.getFund()
                , QueryOperator.of(fundTrackingRequest.getOperator().name()));
        CommonQueryParams caseIdQueryParams = QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId);
        CommonQueryParams queryCardParams = QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, fundTrackingRequest.getLeftCard());
        CommonQueryParams oppositeCardParams = QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, fundTrackingRequest.getRightCard());
        CommonQueryParams secondQueryCardParams = QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, fundTrackingRequest.getLeftCard());
        CommonQueryParams secondOppositeCardParams = QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, fundTrackingRequest.getRightCard());
        /**
         * 资金追踪选择的两端的调单个体不是说左边选的就是查询卡号，右边选择的就是对方卡号,换句话说,选择的两端调单个体既可能在本方,又有可能在对方，一来一会就是两种情况。
         * 其中，innerLocalParams指的是按照选择的方向找符合条件的数据。那么innerOppositeParams则指代按照选择个体的逆向来查找数据。
         * */
        CombinationQueryParams innerLocalParams = new CombinationQueryParams();
        innerLocalParams.setType(ConditionType.must);
        innerLocalParams.addCommonQueryParams(caseIdQueryParams);
        innerLocalParams.addCommonQueryParams(queryCardParams);
        innerLocalParams.addCommonQueryParams(oppositeCardParams);
        innerLocalParams.addCommonQueryParams(amountQueryParams);

        CombinationQueryParams innerOppositeParams = new CombinationQueryParams();
        innerOppositeParams.setType(ConditionType.must);
        innerOppositeParams.addCommonQueryParams(caseIdQueryParams);
        innerOppositeParams.addCommonQueryParams(secondQueryCardParams);
        innerOppositeParams.addCommonQueryParams(secondOppositeCardParams);
        innerOppositeParams.addCommonQueryParams(amountQueryParams);

        // 指定日期范围
        if (null != fundTrackingRequest.getDateRange() && StringUtils.isNotBlank(fundTrackingRequest.getDateRange().getStart())
                & StringUtils.isNotBlank(fundTrackingRequest.getDateRange().getEnd())
        ) {
            CommonQueryParams tradingTimeParams = QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME,
                    new DateRange(fundTrackingRequest.getDateRange().getStart(), fundTrackingRequest.getDateRange().getEnd()));
            innerLocalParams.addCommonQueryParams(tradingTimeParams);
            innerOppositeParams.addCommonQueryParams(tradingTimeParams);
        }

        innerOppositeQueryParams.setCompoundQueries(innerOppositeParams);
        innerLocalQueryParams.setCompoundQueries(innerLocalParams);
        combinationQueryParams.addCommonQueryParams(innerLocalQueryParams);
        combinationQueryParams.addCommonQueryParams(innerOppositeQueryParams);
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);

        querySpecialParams.setIncludeFields(fundSourceAndDestinationAnalysisOppositeShowField());
        return querySpecialParams;
    }

    @Override
    public <T, V> QuerySpecialParams accessFundTrackingResult(T request, V param) {

        GraduallyTrackingRequest req = (GraduallyTrackingRequest) request;
        String time = req.getNext().getTradingTime();
        String caseId = param.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.QUERY_CARD, req.getNext().getCardNumber()));
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, new DateRange(time,
                FundTrackingDateConditionTools.transfer(time, req.getDateInterval(), req.getUnit()))));
        querySpecialParams.setPagination(new Pagination(0, trackingDefaultSize));
        querySpecialParams.setSort(new FieldSort(FundTacticsAnalysisField.TRADING_TIME, Sort.Direction.ASC.name()));
        querySpecialParams.setIncludeFields(fundSourceAndDestinationAnalysisOppositeShowField());
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

}
