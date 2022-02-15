/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.param.query.TransactionFieldQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralRequest;
import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1> 交易字段分析查询参数构建 </h1>
 */
@Service
public class TransactionFieldQueryBuilder implements TransactionFieldQueryParamFactory {


    public QuerySpecialParams transactionFieldType(TransactionFieldAnalysisRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        addCommonParams(query, request);
        return query;
    }

    public QuerySpecialParams transactionFieldCustomCollationQuery(TransactionFieldAnalysisRequest request, List<String> containFieldContent) {

        QuerySpecialParams query = new QuerySpecialParams();
        addCommonParams(query, request);
        query.addCommonQueryParams(QueryParamsBuilders.terms(request.getStatisticsField(), containFieldContent));
        return query;
    }

    public QuerySpecialParams transactionFieldTypeStatistics(TransactionFieldAnalysisRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        addCommonParams(query, request);
        if (StringUtils.isNotBlank(request.getStatisticsFieldContent())) {
            query.addCommonQueryParams(QueryParamsBuilders.term(request.getStatisticsField(), request.getStatisticsFieldContent()));
        }
        return query;
    }

    private void addCommonParams(QuerySpecialParams query, TransactionFieldAnalysisRequest request) {
        // 案件id
        query.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        // 日期筛选(开始时间-结束时间)
        DateRange dateRange = FundTacticsPartGeneralRequest.getDateRange(request.getDateRange());
        if (null != dateRange) {
            query.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
    }
}
