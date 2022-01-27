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
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 交易区间筛选查询请求参数构建 </h1>
 */
@Service
public class TradeRangeScreeningQueryBuilder extends FundTacticsCommonQueryBuilder implements TradeRangeScreeningQueryParamFactory {


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

    public QuerySpecialParams queryAdjustCardsTradeRecord(String caseId, List<String> exportIds, List<String> adjustCards, Double minAmount, Double maxAmount, int dateType) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = queryCardsAndCase(caseId, adjustCards);
        // 可选条件参数过滤
        addOperationRecordCondition(minAmount, maxAmount, dateType, filter, exportIds);
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams queryByCaseIdAndAdjustCards(String caseId, List<String> adjustCards) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = queryCardsAndCase(caseId, adjustCards);
        query.addCombiningQueryParams(filter);
        return query;
    }

    /**
     * <h2> 当点击查看个体银行卡统计分析结果时、查看详情数据 </h2>
     * <p>
     * 应该是要根据当前保存的操作记录 的金额范围 以及 数据类别去筛选的(如果是需求就是查询/统计当前调单卡号下的全部数据 就去除这个方法)
     */
    private void addOperationRecordCondition(Double minAmount, Double maxAmount, int dateType, CombinationQueryParams filter, List<String> exportIds) {
        if (!CollectionUtils.isEmpty(exportIds)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField._ID, exportIds));
        } else {
            if (null != minAmount) {
                filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, minAmount, QueryOperator.gte));
            }
            if (null != maxAmount) {
                filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, maxAmount, QueryOperator.lte));
            }
            if (FundTacticsAnalysisField.TradeRangeScreening.CREDIT_AMOUNT == dateType) {
                filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
            } else if (FundTacticsAnalysisField.TradeRangeScreening.PAYOUT_AMOUNT == dateType) {
                filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
            }
        }
    }

    public QuerySpecialParams queryIndividualBankCardsStatistical(String caseId, List<String> ids, List<String> adjustCards, Double minAmount, Double maxAmount, int dateType) {

        return queryAdjustCardsTradeRecord(caseId, ids, adjustCards, minAmount, maxAmount, dateType);
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
                start = dateRange.getStart() + dateRange.getTimeStart();
            }
            String end = null;
            if (StringUtils.isNotBlank(dateRange.getEnd())) {
                end = dateRange.getEnd() + dateRange.getTimeStart();
            }
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, new DateRange(start, end)));
        }
    }
}
