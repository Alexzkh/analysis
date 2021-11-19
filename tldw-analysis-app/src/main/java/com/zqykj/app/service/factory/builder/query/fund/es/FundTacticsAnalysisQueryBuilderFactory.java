/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund.es;

import com.zqykj.app.service.field.FundTacticsFuzzyQueryField;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.field.PeopleAreaAnalysisFuzzyQueryField;
import com.zqykj.app.service.field.SingleCardPortraitAnalysisField;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <h1> 公共查询请求参数构建工厂 </h1>
 */
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class FundTacticsAnalysisQueryBuilderFactory implements QueryRequestParamFactory {

    public <T, V> QuerySpecialParams createTradeAmountByTimeQuery(T requestParam, V other) {

        // 构建前置查询条件
        FundDateRequest request = (FundDateRequest) requestParam;
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = this.buildCommonQueryParamsViaBankTransactionFlow(requestParam, other);
        if (!CollectionUtils.isEmpty(request.getCardNums())) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(cardsFilter(request.getCardNums())));
        }
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    public <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequestByMainCards(T requestParam, V other) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) requestParam;
        // 获取前置请求
        FundTacticsPartGeneralPreRequest preRequest = request.convertFrom(request);
        CombinationQueryParams combinationQueryParams = this.buildCommonQueryParamsViaBankTransactionRecord(preRequest, other);

        CombinationQueryParams cardNumsAndFuzzyQuery = new CombinationQueryParams();

        cardNumsAndFuzzyQuery.setType(ConditionType.must);
        // 本方查询卡号(有可能是查询全部,那么卡号不为空的时候才能选用此条件)
        if (!CollectionUtils.isEmpty(request.getCardNums())) {
            cardNumsAndFuzzyQuery.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, request.getCardNums()));
        }
        // 本方需要的模糊匹配
        if (StringUtils.isNotBlank(request.getKeyword())) {
            CombinationQueryParams localFuzzy = assembleLocalFuzzy(request.getKeyword());
            localFuzzy.setDefaultQueryParam(new DefaultQueryParam());
            cardNumsAndFuzzyQuery.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }

        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(cardNumsAndFuzzyQuery));

        querySpecialParams.addCombiningQueryParams(combinationQueryParams);

        return querySpecialParams;
    }

    /**
     * <h2> 通用前置查询条件 </h2>
     */
    @Override
    public <T, V> CombinationQueryParams buildCommonQueryParamsViaBankTransactionFlow(T requestParam, V parameter) {
        FundTacticsPartGeneralPreRequest request = (FundTacticsPartGeneralPreRequest) requestParam;
        String caseId = parameter.toString();
        // 构建组合查询(多个普通查询合并)
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        // ConditionType.must 类似于and 条件
        combinationQueryParams.setType(ConditionType.filter);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定本方开户证件号码 与 对方开户证件号码
        if (request.getSearchType() == 0) {
            if (StringUtils.isNotBlank(request.getIdentityCard()) && !CollectionUtils.isEmpty(request.getCardNums())) {
                combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.multiMatch(request.getIdentityCard(),
                        FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                        FundTacticsAnalysisField.OPPOSITE_IDENTITY_CARD));
            }
        }
        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, new DateRange(request.getDateRange().getStart(), request.getDateRange().getEnd())));
        }
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRANSACTION_MONEY, request.getFund(), QueryOperator.of(request.getOperator().name())));
        // 添加组合查询
        return combinationQueryParams;
    }

    public <T, V> CombinationQueryParams buildCommonQueryParamsViaBankTransactionRecord(T t, V v) {

        FundTacticsPartGeneralPreRequest request = (FundTacticsPartGeneralPreRequest) t;
        String caseId = v.toString();
        // 构建组合查询(多个普通查询合并)
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        // ConditionType.must 类似于and 条件
        combinationQueryParams.setType(ConditionType.filter);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定调单卡号集合
        if (!CollectionUtils.isEmpty(request.getCardNums())) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNums()));
        }
        // 指定证件号码
        if (request.getSearchType() == 0) {
            if (StringUtils.isNotBlank(request.getIdentityCard()) && !CollectionUtils.isEmpty(request.getCardNums())) {
                combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD, request.getIdentityCard()));
            }
        }
        // 指定日期范围
        if (null != request.getDateRange() && StringUtils.isNotBlank(request.getDateRange().getStart())
                & StringUtils.isNotBlank(request.getDateRange().getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, new DateRange(request.getDateRange().getStart(), request.getDateRange().getEnd())));
        }
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, request.getFund(), QueryOperator.of(request.getOperator().name())));
        // 添加组合查询
        return combinationQueryParams;
    }

    private CombinationQueryParams cardsFilter(List<String> cardNums) {

        CombinationQueryParams combination = new CombinationQueryParams();
        combination.setType(ConditionType.should);

        if (!CollectionUtils.isEmpty(cardNums)) {
            combination.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, cardNums));
            combination.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, cardNums));
        }
        return combination;
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

    @Override
    public <T, V> QuerySpecialParams buildFundsSourceAndDestinationAnalysisResquest(T requestParam, V parameter) {

        FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest =
                (FundsSourceAndDestinationStatisticsRequest) requestParam;
        String caseId = parameter.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));

        // 指定证件号码,添加证件号码过滤条件
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                fundsSourceAndDestinationStatisticsRequest.getIdentityCard()));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }


    public <T, V> QuerySpecialParams buildTradeConvergenceAnalysisResultMainCardsRequest(T t, V v) {

        QuerySpecialParams convergenceQuery = new QuerySpecialParams();
        TradeConvergenceAnalysisQueryRequest request = (TradeConvergenceAnalysisQueryRequest) t;
        // 获取前置请求
        FundTacticsPartGeneralPreRequest preRequest = request.convertFrom(request);
        CombinationQueryParams combinationQueryParams = this.buildCommonQueryParamsViaBankTransactionRecord(preRequest, v);

        // 增加模糊查询条件
        if (StringUtils.isNotBlank(request.getKeyword())) {

            CombinationQueryParams localFuzzy = assembleLocalFuzzy(request.getKeyword());

            CombinationQueryParams oppositeFuzzy = assembleOppositeFuzzy(request.getKeyword());

            localFuzzy.getCommonQueryParams().addAll(oppositeFuzzy.getCommonQueryParams());

            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }

        convergenceQuery.addCombiningQueryParams(combinationQueryParams);

        return convergenceQuery;
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

    public <T, V> QuerySpecialParams filterMainCards(T request, V other, List<String> cards) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();

        CombinationQueryParams combination = new CombinationQueryParams();

        combination.setType(ConditionType.filter);

        // 设置案件id
        combination.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, other.toString()));
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
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.filter);
        // 精准词条匹配caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(SingleCardPortraitAnalysisField.CASE_ID, caseId));
        // 多条件匹配queryCard（本方卡号和对方卡号）
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.multiMatch(queryCard, SingleCardPortraitAnalysisField.QUERY_CARD));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        querySpecialParams.setPagination(new Pagination(0, 1));
        querySpecialParams.setSort(new FieldSort(SingleCardPortraitAnalysisField.TRADING_TIME, Sort.Direction.DESC.name()));

        return querySpecialParams;
    }
}
