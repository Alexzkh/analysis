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
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.search.MultiMatchQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 交易统计分析查询参数构建工厂
 */
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class FundTacticsAnalysisQueryBuilderFactory implements QueryRequestParamFactory {

    public <T, V> QuerySpecialParams createTradeAmountByTimeQuery(T requestParam, V other) {

        // 构建前置查询条件
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        querySpecialParams.addCombiningQueryParams(this.buildCommonQueryParams(requestParam, other));
        return querySpecialParams;
    }

    /**
     * <h2> 交易统计分析结果查询请求构建 </h2>
     */
    public <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequest(T requestParam, V other) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) requestParam;
        // 获取前置请求
        TradeStatisticalAnalysisPreRequest preRequest = request.convertFrom(request);
        CombinationQueryParams combinationQueryParams = this.buildCommonQueryParams(preRequest, other);

        // 组装结果筛选条件(包括模糊搜索条件等)
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(assemblePostFilter(requestParam)));
        // 补充查询的分页 和 排序
        PageRequest pageRequest = request.getPageRequest();
        if (null != pageRequest) {
            querySpecialParams.setPagination(new Pagination(pageRequest.getPage(), pageRequest.getPageSize()));
        }
        SortRequest sortRequest = request.getSortRequest();
        if (null != sortRequest) {
            querySpecialParams.setSort(new FieldSort(sortRequest.getProperty(), sortRequest.getOrder().name()));
        }
        // 组装复合查询
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    /**
     * <h2> 通用前置查询条件 </h2>
     */
    @Override
    public <T, V> CombinationQueryParams buildCommonQueryParams(T requestParam, V parameter) {
        TradeStatisticalAnalysisPreRequest request = (TradeStatisticalAnalysisPreRequest) requestParam;
        String caseId = parameter.toString();
        // 构建组合查询(多个普通查询合并)
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        // ConditionType.must 类似于and 条件
        combinationQueryParams.setType(ConditionType.filter);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定本方开户证件号码 与 对方开户证件号码
        if (StringUtils.isNotBlank(request.getIdentityCard())) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.multi_match, request.getIdentityCard(),
                    FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                    FundTacticsAnalysisField.OPPOSITE_IDENTITY_CARD));
        }
        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRADING_TIME, new DateRange(request.getDateRange().getStart(),
                    request.getDateRange().getEnd())));
        }
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRANSACTION_MONEY, request.getFund(),
                QueryOperator.of(request.getOperator().name())
        ));
        // 添加组合查询
        return combinationQueryParams;
    }

    /**
     * <h2> 组装后置过滤参数(复合查询条件) </h2>
     */
    public <T> CombinationQueryParams assemblePostFilter(T param) {

        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) param;
        // 第一层嵌套
        CombinationQueryParams firstCombination = new CombinationQueryParams();
        firstCombination.setType(ConditionType.should);

        // 第二层嵌套第一个
        CombinationQueryParams secondCombinationOne = new CombinationQueryParams();
        secondCombinationOne.setType(ConditionType.must);
        // 本方查询卡号(有可能是查询全部,那么卡号不为空的时候才能选用此条件)
        if (!CollectionUtils.isEmpty(request.getCardNums())) {
            secondCombinationOne.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, request.getCardNums()));
        }
        // 本方需要的模糊匹配
        secondCombinationOne.addCommonQueryParams(new CommonQueryParams(assembleLocalFuzzy(request.getKeyword())));

        // 第二层嵌套第二个
        CombinationQueryParams secondCombinationTwo = new CombinationQueryParams();
        secondCombinationTwo.setType(ConditionType.must);
        // 对方卡号
        if (!CollectionUtils.isEmpty(request.getCardNums())) {
            secondCombinationTwo.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, request.getCardNums()));
        }
        // 本方需要的模糊匹配
        secondCombinationTwo.addCommonQueryParams(new CommonQueryParams(assembleOppositeFuzzy(request.getKeyword())));

        // 第一层嵌套 设置 第二层嵌套
        firstCombination.addCommonQueryParams(new CommonQueryParams(secondCombinationOne));
        firstCombination.addCommonQueryParams(new CommonQueryParams(secondCombinationTwo));

        return firstCombination;
    }

    /**
     * <h2> 组装本方模糊查询 </h2>
     */
    public CombinationQueryParams assembleLocalFuzzy(String keyword) {

        CombinationQueryParams localFuzzy = new CombinationQueryParams();
        localFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.localFuzzyFields) {

            localFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
        }
        return localFuzzy;
    }

    /**
     * <h2> 组装对方模糊查询 </h2>
     */
    public CombinationQueryParams assembleOppositeFuzzy(String keyword) {

        CombinationQueryParams oppositeFuzzy = new CombinationQueryParams();
        oppositeFuzzy.setType(ConditionType.should);
        for (String fuzzyField : FundTacticsFuzzyQueryField.oppositeFuzzyFields) {

            oppositeFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
        }
        return oppositeFuzzy;
    }
}
