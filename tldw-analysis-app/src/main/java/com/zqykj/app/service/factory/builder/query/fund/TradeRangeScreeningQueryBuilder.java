/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.param.query.TradeRangeScreeningQueryParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.TradeRangeScreeningDataChartRequest;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <h1> 交易区间筛选查询请求参数构建 </h1>
 */
@Service
public class TradeRangeScreeningQueryBuilder extends FundTacticsCommonQuery implements TradeRangeScreeningQueryParamFactory {


    public QuerySpecialParams queryTradeAmount(TradeRangeScreeningDataChartRequest request) {

        QuerySpecialParams query = new QuerySpecialParams();
        // 组合查询参数包装
        query.addCombiningQueryParams(queryAmount(request));
        // 返回字段过滤
        query.setIncludeFields(new String[]{FundTacticsAnalysisField.CHANGE_MONEY});
        return query;
    }

    public QuerySpecialParams queryCreditOrPayoutAmount(TradeRangeScreeningDataChartRequest request, boolean isCredit) {

        QuerySpecialParams query = new QuerySpecialParams();
        // 基础查询
        CombinationQueryParams filter = queryAmount(request);
        if (isCredit) {
            // 入账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        query.addCombiningQueryParams(filter);
        // 返回字段过滤
        query.setIncludeFields(new String[]{FundTacticsAnalysisField.CHANGE_MONEY});
        return query;
    }

    public QuerySpecialParams queryCase(String caseId) {

        return queryByCaseId(caseId);
    }


    /**
     * <h2> 查询金额 </h2>
     */
    private CombinationQueryParams queryAmount(TradeRangeScreeningDataChartRequest request) {
        // 组合查询
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id 过滤
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        // 查询卡号过滤(查询卡号)
        filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNums()));
        setDateRange(request, filter);
        return filter;
    }

    /**
     * <h2> 日期范围筛选 </h2>
     */
    private void setDateRange(TradeRangeScreeningDataChartRequest request, CombinationQueryParams filter) {
        DateRangeRequest dateRange = request.getDateRange();
        if (null != dateRange) {
            String start = null;
            if (StringUtils.isNotBlank(dateRange.getStart())) {
                start = dateRange.getStart() + request.getTimeStart();
            }
            String end = null;
            if (StringUtils.isNotBlank(dateRange.getEnd())) {
                end = dateRange.getEnd() + request.getTimeStart();
            }
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, new DateRange(start, end)));
        }
    }
}
