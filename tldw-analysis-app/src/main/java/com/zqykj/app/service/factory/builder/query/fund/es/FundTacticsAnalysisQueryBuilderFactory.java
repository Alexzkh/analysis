/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund.es;

import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.PeopleAreaAnalysisFuzzyQueryField;
import com.zqykj.app.service.strategy.PeopleAreaAnalysisFieldStrategy;
import com.zqykj.app.service.transform.PeopleAreaConversion;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.request.*;
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
import org.springframework.util.CollectionUtils;

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
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(assemblePostFilter(requestParam, request.getSearchTag())));
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
        if (request.getSearchType() == 0) {
            if (StringUtils.isNotBlank(request.getIdentityCard()) && !CollectionUtils.isEmpty(request.getCardNums())) {
                combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.multi_match, request.getIdentityCard(),
                        FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                        FundTacticsAnalysisField.OPPOSITE_IDENTITY_CARD));
            }
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
    public <T> CombinationQueryParams assemblePostFilter(T param, String tag) {

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
        if (tag.equals("local") && request.getSearchType() == 1) {
            firstCombination.addCommonQueryParams(new CommonQueryParams(secondCombinationOne));
        } else if (tag.equals("opposite") && request.getSearchType() == 1) {
            firstCombination.addCommonQueryParams(new CommonQueryParams(secondCombinationTwo));
        } else {
            firstCombination.addCommonQueryParams(new CommonQueryParams(secondCombinationOne));
            firstCombination.addCommonQueryParams(new CommonQueryParams(secondCombinationTwo));
        }
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

    @Override
    public <T, V> QuerySpecialParams bulidPeopleAreaAnalysisRequest(T requestParam, V parameter) {
        PeopleAreaRequest peopleAreaRequest = (PeopleAreaRequest) requestParam;
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        String caseId = parameter.toString();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        // ConditionType.must 类似于and 条件
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定卡号
        if (StringUtils.isNotEmpty(peopleAreaRequest.getName())) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term,
                    PeopleAreaConversion.REGION_NAME.get(peopleAreaRequest.getField()), peopleAreaRequest.getName()));
        }

        // 添加组合查询
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    @Override
    public <T, V> QuerySpecialParams bulidPeopleAreaDetailAnalysisRequest(T requestParam, V parameter) {
        PeopleAreaDetailRequest peopleAreaDetailRequest = (PeopleAreaDetailRequest) requestParam;
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        String caseId = parameter.toString();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定具体地区查询
        if (StringUtils.isNotEmpty(peopleAreaDetailRequest.getRegionName())) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term,
                    PeopleAreaAnalysisFieldStrategy.PEOPLE_AREA_MAP.get(peopleAreaDetailRequest.getField()), peopleAreaDetailRequest.getRegionName()));

        }
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        String keyword = peopleAreaDetailRequest.getQueryRequest().getKeyword();

        /**
         * 构建模糊查询参数
         * */
        if (StringUtils.isNotBlank(keyword)) {

            CombinationQueryParams fuzzyCombinationQueryParams = new CombinationQueryParams();
            fuzzyCombinationQueryParams.setType(ConditionType.should);
            for (String fuzzyField : PeopleAreaAnalysisFuzzyQueryField.fuzzyFields) {

                fuzzyCombinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, keyword));
            }
            querySpecialParams.addCombiningQueryParams(fuzzyCombinationQueryParams);
            querySpecialParams.setDefaultParam(new DefaultQueryParam());

        }

        PagingRequest pageRequest = peopleAreaDetailRequest.getQueryRequest().getPaging();
        if (null != pageRequest) {
            querySpecialParams.setPagination(new Pagination(pageRequest.getPage(), pageRequest.getPageSize()));
        }
        SortingRequest sortRequest = peopleAreaDetailRequest.getQueryRequest().getSorting();
        /**
         * 默认按照省份排序
         * */
        if (null != sortRequest) {
            querySpecialParams.setSort(new FieldSort(PeopleAreaAnalysisFieldStrategy.PEOPLE_AREA_MAP.
                    get(sortRequest.getProperty() == null ? "province" :
                            sortRequest.getProperty()),
                    sortRequest.getOrder().name()));
        }
        return querySpecialParams;
    }
}
