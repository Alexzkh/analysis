/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.factory.QueryRequestParamFactory;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 交易统计分析查询参数构建工厂
 */
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class FundTacticsAnalysisQueryBuilderFactory implements QueryRequestParamFactory {

    public <T, V> QuerySpecialParams createTradeAmountByTimeQuery(T requestParam, V other) {

        TradeStatisticalAnalysisPreRequest request = (TradeStatisticalAnalysisPreRequest) requestParam;

        QuerySpecialParams querySpecialParams = this.buildCommonQuerySpecialParams(requestParam,other);

        // 构建组合查询(多个普通查询合并)
        CombinationQueryParams combinationQueryParams =querySpecialParams.getCombiningQuery().get(0);
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRANSACTION_MONEY, request.getFund(),
                QueryOperator.of(request.getOperator().name())
        ));
        // 添加组合查询
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }


    public <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequest(T requestParam, V other) {

        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) requestParam;
        // 获取前置请求
        TradeStatisticalAnalysisPreRequest preRequest = request.convertFrom(request);
        QuerySpecialParams querySpecialParams = this.createTradeAmountByTimeQuery(preRequest, other);

        // 组装模糊查询
        if (StringUtils.isNotBlank(request.getKeyword())) {

            CombinationQueryParams fuzzyCombinationQueryParams = new CombinationQueryParams();
            fuzzyCombinationQueryParams.setType(ConditionType.should);
            for (String fuzzyField : FundTacticsFuzzyQueryField.fuzzyFields) {

                fuzzyCombinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, request.getKeyword()));
            }
            querySpecialParams.addCombiningQueryParams(fuzzyCombinationQueryParams);
            querySpecialParams.setDefaultParam(new DefaultQueryParam());
        }

        // 补充查询的分页 和 排序
        PageRequest pageRequest = request.getPageRequest();
        if (null != pageRequest) {
            querySpecialParams.setPagination(new Pagination(pageRequest.getPage(), pageRequest.getPageSize()));
        }
        SortRequest sortRequest = request.getSortRequest();
        if (null != sortRequest) {
            querySpecialParams.setSort(new FieldSort(sortRequest.getProperty(), sortRequest.getOrder().name()));
        }
        return querySpecialParams;
    }

    @Override
    public <T,V> QuerySpecialParams buildCommonQuerySpecialParams(T requestParam, V parameter) {
        TradeStatisticalAnalysisPreRequest request = (TradeStatisticalAnalysisPreRequest) requestParam;
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        String caseId = parameter.toString();
        // 构建组合查询(多个普通查询合并)
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        // ConditionType.must 类似于and 条件
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定卡号
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, request.getCardNums()));
        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRADING_TIME, new DateRange(request.getDateRange().getStart(),
                    request.getDateRange().getEnd())));
        }
        // 添加组合查询
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }
}
