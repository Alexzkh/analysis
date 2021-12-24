/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QueryOperator;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.stereotype.Service;

/**
 * <h1> 资金战法部分通用查询构建 </h1>
 */
@Service
public class FundTacticsCommonQuery {

    public QuerySpecialParams queryDataByCaseId(String caseId) {
        QuerySpecialParams query = new QuerySpecialParams();
        query.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        return query;
    }

    public QuerySpecialParams queryByIdAndCaseId(String caseId, String id) {

        QuerySpecialParams query = new QuerySpecialParams();

        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.ID, id));
        query.addCombiningQueryParams(filter);
        return query;
    }

    /**
     * <h2> 查询调单卡号 </h2>
     * <p>
     * 根据交易金额过滤 、交易日期过滤
     */
    public QuerySpecialParams queryAdjustNumberByAmountAndDate(String caseId, Double minAmount, Double maxAmount, DateRange dateRange) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        if (null != minAmount) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, minAmount, QueryOperator.gte));
        }
        if (null != maxAmount) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, maxAmount, QueryOperator.lte));
        }
        if (null != dateRange) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
        query.setIncludeFields(new String[]{FundTacticsAnalysisField.QUERY_CARD});
        query.addCombiningQueryParams(filter);
        return query;
    }
}
