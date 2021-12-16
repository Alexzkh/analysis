/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.query.fund;

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
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <h1> 公共查询请求参数构建工厂 </h1>
 */
@Service
public class FundTacticsAnalysisQueryBuilderFactory implements QueryRequestParamFactory {

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    public <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequestByMainCards(T requestParam, V other,
                                                                                           Class<?> queryTable) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) requestParam;
        // 获取前置请求
        FundTacticsPartGeneralPreRequest preRequest = request.convertFrom(request);
        CombinationQueryParams combinationQueryParams;
        if (BankTransactionFlow.class.isAssignableFrom(queryTable)) {
            combinationQueryParams = this.buildCommonQueryParamsViaBankTransactionFlow(preRequest, other);
        } else {
            combinationQueryParams = this.buildCommonQueryParamsViaBankTransactionRecord(preRequest, other);
        }

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

    public QuerySpecialParams buildTradeStatisticalAnalysisHitsQuery(List<String> queryCards, String caseId) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.filter);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定合并卡号集合过滤
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, queryCards));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        // 设置需要返回的字段
        querySpecialParams.setIncludeFields(FundTacticsAnalysisField.tradeStatisticalAnalysisLocalShowField());

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


    public <T, V> QuerySpecialParams buildTradeConvergenceAnalysisResultMainCardsRequest(T t, V v) {

        QuerySpecialParams convergenceQuery = new QuerySpecialParams();
        TradeConvergenceAnalysisQueryRequest request = (TradeConvergenceAnalysisQueryRequest) t;
        // 获取前置请求
        FundTacticsPartGeneralPreRequest preRequest = request.convertFrom(request);
        CombinationQueryParams combinationQueryParams = this.buildCommonQueryParamsViaBankTransactionRecord(preRequest, v);
        // 合并卡号集合过滤
        if (!CollectionUtils.isEmpty(request.getMergeCards())) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.MERGE_CARD, request.getMergeCards()));
        }
        // 增加模糊查询条件
        if (StringUtils.isNotBlank(request.getKeyword())) {

            CombinationQueryParams localFuzzy = assembleLocalFuzzy(request.getKeyword());

            CombinationQueryParams oppositeFuzzy = assembleOppositeFuzzy(request.getKeyword());

            localFuzzy.getCommonQueryParams().addAll(oppositeFuzzy.getCommonQueryParams());

            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }
        // 过滤 查询卡号 和 对方卡号为空的交易记录
        CombinationQueryParams mustNot = new CombinationQueryParams();
        mustNot.setType(ConditionType.must_not);
        mustNot.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.QUERY_CARD, ""));
        mustNot.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, ""));
        convergenceQuery.addCombiningQueryParams(mustNot);
        convergenceQuery.addCombiningQueryParams(combinationQueryParams);
        return convergenceQuery;
    }

    public QuerySpecialParams buildTradeConvergenceAnalysisHitsQuery(List<String> mergeCards, String caseId) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.filter);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定合并卡号集合过滤
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.MERGE_CARD, mergeCards));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        // 设置需要返回的字段
        querySpecialParams.setIncludeFields(FundTacticsAnalysisField.tradeConvergenceAnalysisShowField());

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

    public <T> QuerySpecialParams buildAdjustIndividualQuery(T request) {

        AdjustIndividualRequest adjustIndividualRequest = (AdjustIndividualRequest) request;
        // 案件Id
        String caseId = adjustIndividualRequest.getCaseId();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.filter);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.CASE_ID, caseId));
        // 指定开户证件号码
        if (StringUtils.isNotBlank(adjustIndividualRequest.getCustomerIdentityCard())) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD,
                    adjustIndividualRequest.getCustomerIdentityCard()));
        }
        // 模糊查询
        if (!StringUtils.isBlank(adjustIndividualRequest.getKeyword())) {

            CombinationQueryParams fuzzyQuery = new CombinationQueryParams();
            fuzzyQuery.setType(ConditionType.should);
            fuzzyQuery.addCommonQueryParams(QueryParamsBuilders.fuzzy(adjustIndividualRequest.getKeyword(),
                    FundTacticsAnalysisField.CUSTOMER_NAME));
            fuzzyQuery.addCommonQueryParams(QueryParamsBuilders.fuzzy(adjustIndividualRequest.getKeyword(),
                    FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD));
            // 增加模糊查询
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(fuzzyQuery));
        }
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

    public QuerySpecialParams buildCreditsAdjustCards(String caseId, List<String> adjustCards, int singleQuota) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        if (!CollectionUtils.isEmpty(adjustCards)) {

            // 调单卡号集合过滤
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, adjustCards));
        }
        // 借贷标志进
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        querySpecialParams.addCombiningQueryParams(filter);
        return querySpecialParams;
    }

    public QuerySpecialParams buildCreditAndPayoutRecordsNoSuchCards(FastInFastOutRequest request, int size,
                                                                     boolean isCredits, String... includeFields) {
        // 案件Id
        String caseId = request.getCaseId();
        // 单笔限额(交易金额)
        int singleQuota = request.getSingleQuota();
        // 构建查询参数
        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id过滤
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 单笔限额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        if (isCredits) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        // not in 给定的一组卡号
        CombinationQueryParams noIn = new CombinationQueryParams(ConditionType.must_not);
        noIn.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNum()));
        // 添加一个单个组合查询
        filter.addCombinationQueryParams(noIn);
        // 包装查询参数
        query.addCombiningQueryParams(filter);
        // 设置查询包含的字段
        query.setIncludeFields(includeFields);
        return query;
    }

    public QuerySpecialParams buildCreditAndPayOutViaLocalAndOpposite(String caseId, List<String> queryCards, List<String> oppositeCards,
                                                                      int singleQuota, boolean isCredits, @Nullable String... includeFields) {
        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id过滤
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, caseId));
        // 调单卡号(查询卡号)集合过滤
        if (!CollectionUtils.isEmpty(queryCards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, queryCards));
        }
        // 对方卡号过滤
        if (!CollectionUtils.isEmpty(oppositeCards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, oppositeCards));
        }
        // 单笔限额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        if (isCredits) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        querySpecialParams.addCombiningQueryParams(filter);
        // 设置查询包含的字段
        if (null != includeFields && includeFields.length > 0) {
            querySpecialParams.setIncludeFields(includeFields);
        }
        return querySpecialParams;
    }

    public QuerySpecialParams queryAsAdjustOppositeNoSuchAdjust(FastInFastOutRequest request, String loanFlag) {

        // 构建查询参数
        QuerySpecialParams query = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, request.getCaseId()));
        // 交易金额
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, request.getSingleQuota(), QueryOperator.gte));
        // 借贷标志
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, loanFlag));
        // 查询卡号
        filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, request.getCardNum()));
        // !=
        CombinationQueryParams mustNot = new CombinationQueryParams(ConditionType.must_not);
        // 对方卡号不在这些卡号之内的
        mustNot.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, request.getCardNum()));

        query.addCombiningQueryParams(filter);
        query.addCombiningQueryParams(mustNot);
        // 设置source
        query.setIncludeFields(FundTacticsAnalysisField.fastInFastOutFields());
        return query;
    }

    public QuerySpecialParams getFastInOutTradeRecordsByCondition(String caseId, int singleQuota, List<String> queryCards,
                                                                  boolean isQueryCredits, Date tradeDate, QueryOperator operator,
                                                                  @Nullable String... includeFields) {
        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id 过滤
        filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.CASE_ID, caseId));
        // 查询卡号集合过滤
        if (!CollectionUtils.isEmpty(queryCards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, queryCards));
        }
        // 单笔限额过滤
        filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.CHANGE_MONEY, singleQuota, QueryOperator.gte));
        if (isQueryCredits) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        // 日期范围过滤
        if (null != tradeDate && null != operator) {
            String date = format.format(tradeDate);
            if (operator == QueryOperator.gte) {
                // 大于等于first的交易日期
                filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, DateRange.gte(date)));
            } else if (operator == QueryOperator.lte) {
                // 小于等于first的交易日期
                filter.addCommonQueryParams(QueryParamsBuilders.range(FundTacticsAnalysisField.TRADING_TIME, DateRange.lte(date)));
            }
        }
        querySpecialParams.addCombiningQueryParams(filter);
        // 设置查询包含的字段
        querySpecialParams.setIncludeFields(includeFields);

        return querySpecialParams;
    }

    public QuerySpecialParams getFastInOutTradeRecordsByLocalOpposite(String caseId, int singleQuota, List<String> queryCards, List<String> oppositeCards,
                                                                      boolean isQueryCredits, Date tradeDate, QueryOperator operator, String... includeFields) {
        QuerySpecialParams params = getFastInOutTradeRecordsByCondition(caseId, singleQuota, queryCards, isQueryCredits, tradeDate, operator, includeFields);
        CombinationQueryParams filter = params.getCombiningQuery().get(0);
        // 加上 对方卡号过滤
        if (!CollectionUtils.isEmpty(oppositeCards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, oppositeCards));
        }
        return params;
    }

    public QuerySpecialParams getInoutRecordsViaAdjustCards(List<String> cards, String caseId, boolean isIn) {
        // 构建查询参数
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        // 案件Id 过滤
        filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.CASE_ID, caseId));
        // 查询卡号集合过滤
        if (!CollectionUtils.isEmpty(cards)) {
            filter.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, cards));
        }
        if (isIn) {
            // 进账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        } else {
            // 出账
            filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        }
        return querySpecialParams;
    }
}
