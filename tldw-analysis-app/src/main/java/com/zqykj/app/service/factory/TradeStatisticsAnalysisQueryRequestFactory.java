/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.app.service.field.TacticsAnalysisField;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.parameters.query.*;

/**
 * 交易统计分析查询参数构建工厂
 */
public class TradeStatisticsAnalysisQueryRequestFactory {

    public static QuerySpecialParams createTradeAmountByTimeQuery(TradeStatisticalAnalysisPreRequest request, String caseId) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        // 构建组合查询(多个普通查询合并)
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        // ConditionType.must 类似于and 条件
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, TacticsAnalysisField.CASE_ID, caseId));
        // 指定卡号
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.terms, TacticsAnalysisField.QUERY_CARD, request.getCardNums()));
        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, TacticsAnalysisField.TRADING_TIME, new DateRange(request.getDateRange().getStart(),
                    request.getDateRange().getEnd())));
        }
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, TacticsAnalysisField.TRANSACTION_MONEY, request.getFund(),
                QueryOperator.of(request.getOperator().name())
        ));
        // 添加组合查询
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }
}
