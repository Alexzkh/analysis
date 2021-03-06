/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.field.*;
import com.zqykj.app.service.strategy.PeopleAreaAnalysisFieldStrategy;
import com.zqykj.app.service.transform.PeopleAreaConversion;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.request.*;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.domain.Sort;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * <h1> 资金公共查询请求参数构建工厂 </h1>
 */
@Service
public class FundTacticsAnalysisQueryBuilderFactory extends FundTacticsCommonQueryBuilder implements QueryRequestParamFactory {

    /**
     * <h2> 通用前置查询条件 </h2>
     */
    @Override
    public <T, V> CombinationQueryParams buildCommonQueryParamsViaBankTransactionFlow(T requestParam, V parameter) {
        FundTacticsPartGeneralPreRequest request = (FundTacticsPartGeneralPreRequest) requestParam;
        String caseId = parameter.toString();
        // 构建组合查询(多个普通查询合并)
        CombinationQueryParams filter = new CombinationQueryParams();
        // ConditionType.must 类似于and 条件
        filter.setType(ConditionType.filter);
        // 指定caseId
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 选择个体查询的时候
        if (request.getAnalysisType() == SELECT_INDIVIDUAL) {
            filter.addCommonQueryParams(QueryParamsBuilders.multiMatch(request.getIdentityCard(),
                    FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                    FundTacticsAnalysisField.OPPOSITE_IDENTITY_CARD));
        }
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            filter.addCombinationQueryParams(cardsFilter(request.getCardNum()));
        }
        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, new DateRange(request.getDateRange().getStart(), request.getDateRange().getEnd())));
        }
        // 指定交易金额
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRANSACTION_MONEY, request.getFund(), QueryOperator.of(request.getOperator().name())));
        // 添加组合查询
        return filter;
    }

    public <T, V> CombinationQueryParams buildCommonQueryParamsViaBankTransactionRecord(T t, V v) {

        FundTacticsPartGeneralPreRequest request = (FundTacticsPartGeneralPreRequest) t;
        String caseId = v.toString();
        // 构建组合查询(多个普通查询合并)
        CombinationQueryParams filter = new CombinationQueryParams();
        // ConditionType.must 类似于and 条件
        filter.setType(ConditionType.filter);
        // 指定caseId
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 选择个体查询的时候
        if (request.getAnalysisType() == SELECT_INDIVIDUAL) {
            String identityCard = StringUtils.isBlank(request.getIdentityCard()) ? "" : request.getIdentityCard();
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD, identityCard));
        }
        if (!CollectionUtils.isEmpty(request.getCardNum())) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNum()));
        }
        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, new DateRange(request.getDateRange().getStart(), request.getDateRange().getEnd())));
        }
        // 指定交易金额
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, request.getFund(), QueryOperator.of(request.getOperator().name())));
        // 添加组合查询
        return filter;
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

    public <T, V> QuerySpecialParams buildBasicParamQueryViaCase(T request, V other) {

        //TODO 后续可能会加一些request的参数作为 查询条件

        FundTacticsPartGeneralPreRequest preRequest = (FundTacticsPartGeneralPreRequest) request;

        QuerySpecialParams basicQuery = new QuerySpecialParams();

        CombinationQueryParams combination = new CombinationQueryParams();

        combination.setType(ConditionType.filter);

        // 设置过滤案件id
        combination.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, other.toString()));

        basicQuery.addCombiningQueryParams(combination);

        return basicQuery;
    }

    public QuerySpecialParams filterMainCards(String caseId, List<String> cards) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        CombinationQueryParams combination = new CombinationQueryParams();

        combination.setType(ConditionType.filter);

        // 设置案件id
        combination.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 设置查询卡号
        combination.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, cards));
        querySpecialParams.addCombiningQueryParams(combination);
        querySpecialParams.setIncludeFields(new String[]{FundTacticsAnalysisField.QUERY_CARD});
        return querySpecialParams;
    }

    @Override
    public <T> QuerySpecialParams buildSingleCardPortraitQueryParams(T request) {
        // 请求参数转换成单卡画像请求体类型
        SingleCardPortraitRequest singleCardPortraitRequest = (SingleCardPortraitRequest) request;
        // 案件id
        String caseId = singleCardPortraitRequest.getCaseId();
        // 查询卡号（调单卡号）
        String queryCard = singleCardPortraitRequest.getQueryCard();

        // 使用自定义封装的ES构建器构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams(ConditionType.filter);
        // 精准词条匹配caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(SingleCardPortraitAnalysisField.CASE_ID, caseId));
        // 多条件匹配queryCard（本方卡号和对方卡号）
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.multiMatch(queryCard, SingleCardPortraitAnalysisField.QUERY_CARD));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        querySpecialParams.setPagination(new Pagination(0, 1));
        querySpecialParams.setSort(new FieldSort(SingleCardPortraitAnalysisField.TRADING_TIME, Sort.Direction.DESC.name()));

        return querySpecialParams;
    }

    public <T> QuerySpecialParams buildAdjustIndividualQuery(T request) {

        AdjustIndividualRequest adjustIndividualRequest = (AdjustIndividualRequest) request;
        // 案件Id
        String caseId = adjustIndividualRequest.getCaseId();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 指定caseId
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定开户证件号码
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                adjustIndividualRequest.getCustomerIdentityCard()));
        // 去除证件号码为空的
        // 过滤 查询卡号 和 对方卡号为空的交易记录
//        CombinationQueryParams mustNot = new CombinationQueryParams();
//        mustNot.setType(ConditionType.must_not);
//        mustNot.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD, ""));
//        filter.addCombinationQueryParams(mustNot);
        // 模糊查询
        if (!StringUtils.isBlank(adjustIndividualRequest.getKeyword())) {

            CombinationQueryParams fuzzyQuery = new CombinationQueryParams(ConditionType.should);
            fuzzyQuery.addCommonQueryParams(QueryParamsBuilders.fuzzy(adjustIndividualRequest.getKeyword(),
                    FundTacticsAnalysisField.CUSTOMER_NAME_WILDCARD));
            fuzzyQuery.addCommonQueryParams(QueryParamsBuilders.fuzzy(adjustIndividualRequest.getKeyword(),
                    FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD_WILDCARD));
            // 增加模糊查询
            filter.addCommonQueryParams(new CommonQueryParams(fuzzyQuery));
        }
        querySpecialParams.addCombiningQueryParams(filter);
        return querySpecialParams;
    }

    @Override
    public <T> QuerySpecialParams buildIndividualInfoAndStatisticsQueryParams(T request) {
        IndividualInfoAndStatisticsRequest individualInfoAndStatisticsRequest = (IndividualInfoAndStatisticsRequest) request;
        String caseId = individualInfoAndStatisticsRequest.getCaseId();
        String customerIdentityCard = individualInfoAndStatisticsRequest.getCustomerIdentityCard();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams(ConditionType.filter);
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(IndividualPortraitAnalysisField.CASE_ID, caseId));
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(IndividualPortraitAnalysisField.CUSTOMER_IDENTITY_CARD, customerIdentityCard));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    @Override
    public <T> QuerySpecialParams buildIndividualCardTransactionStatisticsQueryParams(T request) {
        IndividualCardTransactionStatisticsRequest individualCardTransactionStatisticsRequest = (IndividualCardTransactionStatisticsRequest) request;
        String caseId = individualCardTransactionStatisticsRequest.getCaseId();
        String customerIdentityCard = individualCardTransactionStatisticsRequest.getCustomerIdentityCard();
        List<String> queryCards = individualCardTransactionStatisticsRequest.getQueryCards();
        String keyword = individualCardTransactionStatisticsRequest.getKeyword();
        DateRange dateRange = individualCardTransactionStatisticsRequest.getDateRange();

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams(ConditionType.filter);
        combinationQueryParams.setType(ConditionType.filter);
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(IndividualPortraitAnalysisField.CASE_ID, caseId));
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(IndividualPortraitAnalysisField.CUSTOMER_IDENTITY_CARD, customerIdentityCard));
        if (!CollectionUtils.isEmpty(queryCards)) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(IndividualPortraitAnalysisField.QUERY_CARD, queryCards));
        }
        if (StringUtils.isNotBlank(keyword)) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.fuzzy(IndividualPortraitAnalysisField.QUERY_CARD, "*".concat(keyword).concat("*")));
        }
        if (!ObjectUtils.isEmpty(dateRange)) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.range(IndividualPortraitAnalysisField.TRADING_TIME, dateRange));
        }
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    public QuerySpecialParams queryDataByCaseId(String caseId) {
        QuerySpecialParams query = new QuerySpecialParams();
        query.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        return query;
    }

    public QuerySpecialParams queryByIdAndCaseId(String caseId, String id) {

        QuerySpecialParams query = new QuerySpecialParams();

        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField._ID, id));
        query.addCombiningQueryParams(filter);
        return query;
    }

    /**
     * <h2> 查询调单卡号 </h2>
     * <p>
     * 根据交易金额过滤 、交易日期过滤
     */
    public QuerySpecialParams queryAdjustNumberByAmountAndDate(String caseId, Double startAmount, QueryOperator startOperator,
                                                               Double endAmount, QueryOperator endOperator, DateRange dateRange) {

        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        if (null != startAmount) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRANSACTION_MONEY, startAmount, startOperator));
        }
        if (null != endAmount) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRANSACTION_MONEY, endAmount, endOperator));
        }
        if (null != dateRange) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
        query.setIncludeFields(new String[]{FundTacticsAnalysisField.QUERY_CARD});
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams queryAdjustNumberByAmountAndDate(String caseId, Double startAmount, QueryOperator startOperator, DateRange dateRange) {
        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        if (null != startAmount) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRANSACTION_MONEY, startAmount, startOperator));
        }
        if (null != dateRange) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
        query.setIncludeFields(new String[]{FundTacticsAnalysisField.QUERY_CARD});
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams queryAdjustNumberByDate(String caseId, DateRange dateRange) {
        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        if (null != dateRange) {
            filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, dateRange));
        }
        query.setIncludeFields(new String[]{FundTacticsAnalysisField.QUERY_CARD});
        query.addCombiningQueryParams(filter);
        return query;
    }

    public QuerySpecialParams queryTradeAnalysisDetail(FundTacticsPartGeneralRequest request, String... detailFuzzyFields) {
        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        if (StringUtils.isNotBlank(request.getQueryCard())) {
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.QUERY_CARD, request.getQueryCard()));
        }
        if (StringUtils.isNotBlank(request.getOppositeCard())) {
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, request.getOppositeCard()));
        }
        if (!CollectionUtils.isEmpty(request.getIds())) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField._ID, request.getIds()));
        }
        // 增加模糊查询
        if (StringUtils.isNotBlank(request.getKeyword())) {
            CombinationQueryParams detailFuzzy = new CombinationQueryParams(ConditionType.should);
            for (String fuzzyField : detailFuzzyFields) {
                detailFuzzy.addCommonQueryParams(new CommonQueryParams(QueryType.wildcard, fuzzyField, request.getKeyword()));
            }
            filter.addCombinationQueryParams(detailFuzzy);
        }
        query.addCombiningQueryParams(filter);
        return query;
    }
}
