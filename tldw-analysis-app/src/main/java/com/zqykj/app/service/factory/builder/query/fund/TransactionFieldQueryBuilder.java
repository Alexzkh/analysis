/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.param.query.TransactionFieldQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralRequest;
import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 交易字段分析查询参数构建 </h1>
 */
@Service
public class TransactionFieldQueryBuilder implements TransactionFieldQueryParamFactory {


    public QuerySpecialParams transactionFieldType(TransactionFieldAnalysisRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        addCommonParams(filter, request);
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams transactionFieldCustomCollationQuery(TransactionFieldAnalysisRequest request, List<String> containFieldContent) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        addCommonParams(filter, request);
        filter.addCommonQueryParams(QueryParamsBuilders.terms(request.getStatisticsField(), containFieldContent));
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams transactionFieldTypeStatistics(TransactionFieldAnalysisRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        addCommonParams(filter, request);
        if (StringUtils.isNotBlank(request.getStatisticsFieldContent())) {
            filter.addCommonQueryParams(QueryParamsBuilders.term(request.getStatisticsField(), request.getStatisticsFieldContent()));
        }
        query.addCombiningQueryParams(filter);
        return query;
    }

    private void addCommonParams(CombinationQueryParams filter, TransactionFieldAnalysisRequest request) {
        // 案件id
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        // 调单卡号条件筛选
        if (!CollectionUtils.isEmpty(request.getCardNum()) && CollectionUtils.isEmpty(request.getCustomCollationQueryRequests())) {
            CombinationQueryParams filterCards = new CombinationQueryParams(ConditionType.should);
            filterCards.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNum()));
            filterCards.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, request.getCardNum()));
            filter.addCombinationQueryParams(filterCards);
        }
        // 日期筛选(开始时间-结束时间)
        DateRange dateRange = FundTacticsPartGeneralRequest.getDateRange(request.getDateRange());
        if (null != dateRange) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
    }
}
