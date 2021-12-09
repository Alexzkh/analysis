/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.builder.query.fund.FundTacticsAnalysisQueryBuilderFactory;
import com.zqykj.app.service.field.IndividualPortraitAnalysisField;
import com.zqykj.app.service.field.SingleCardPortraitAnalysisField;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.transform.PeopleAreaConversion;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.request.PeopleAreaRequest;
import com.zqykj.app.service.vo.fund.SingleCardPortraitRequest;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.domain.Sort;
import com.zqykj.parameters.query.CombinationQueryParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.AssetTrendsRequest;
import com.zqykj.common.vo.Direction;
import com.zqykj.core.aggregation.response.ElasticsearchAggregationResponseAttributes;
import com.zqykj.enums.AggsType;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.FetchSource;
import com.zqykj.parameters.aggregate.date.DateParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundTacticsAnalysisAggBuilderFactory implements AggregationRequestParamFactory {


    private final FundTacticsAnalysisQueryBuilderFactory queryBuilderFactory;

    // 管道聚合 buckets_path 引用标识
    private static final String BUCKET_SCRIPT_PARAM_PREFIX = "params.";
    private static final String PIPELINE_AGG_PATH_FLAG = ">";

    private static final String AGG_NAME_SPLIT = "_";
    private static final String DEFAULT_SORTING_FIELD = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + AggsType.sum.name();
    private static final String DEPTH_FIRST = "DEPTH_FIRST";
    private static final String BREADTH_FIRST = "BREADTH_FIRST";

    /**
     * <h2> 聚合排序 </h2>
     */
    private PipelineAggregationParams fundTacticsPartUniversalAggSort(SortRequest sortRequest, int from, int size) {
        if (null != sortRequest) {
            List<FieldSort> fieldSorts = new ArrayList<>();
            String property = sortRequest.getProperty();
            Direction order = sortRequest.getOrder();
            // 获取真实的聚合排序字段(开户名称、开户证件号码、开户银行、账号、交易卡号 不做排序,按照交易总金额排序处理)
            fieldSorts.add(new FieldSort(property, order.name()));
            return AggregationParamsBuilders.sort("sort", fieldSorts, from, size);
        }
        return null;
    }

    public <T> AggregationParams buildTradeStatisticsAnalysisByMainCards(T request, int from, int size) {

        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        AggregationParams cardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.QUERY_CARD);
        cardTerms.setSize(queryRequest.getGroupInitSize());
        cardTerms.setCollectMode(BREADTH_FIRST);
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.QUERY_CARD, null);
        setSubAggregation(cardTerms, tradeTotalTimes);
        fundTacticsPartUniversalAgg(cardTerms, queryRequest);
        // 排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(queryRequest.getSortRequest(), from, size);
        if (null != sort) {
            cardTerms.setPerSubAggregation(sort);
        }
        return cardTerms;
    }

    /**
     * <h2> 构建交易统计分析聚合展示字段聚合 </h2>
     */
    public AggregationParams buildTradeStatisticalAnalysisHitsAgg(int groupSize) {

        AggregationParams cardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.QUERY_CARD);
        cardTerms.setSize(groupSize);
        AggregationParams showFields = fundTacticsPartUniversalAggShowFields(FundTacticsAnalysisField.tradeStatisticalAnalysisLocalShowField(), "local_hits", null);
        cardTerms.setPerSubAggregation(showFields);
        return cardTerms;
    }

    public <T> AggregationParams buildTradeStatisticalQueryCardsAgg(T request, int from, int size) {
        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        AggregationParams cardTerms = AggregationParamsBuilders.terms("groupBy_" + FundTacticsAnalysisField.QUERY_CARD, FundTacticsAnalysisField.QUERY_CARD);
        cardTerms.setSize(queryRequest.getGroupInitSize());
        cardTerms.setCollectMode(BREADTH_FIRST);
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.QUERY_CARD, null);
        setSubAggregation(cardTerms, tradeTotalTimes);
        fundTacticsPartUniversalAgg(cardTerms, queryRequest);
        // 排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(queryRequest.getSortRequest(), from, size);
        if (null != sort) {
            cardTerms.setPerSubAggregation(sort);
        }
        return cardTerms;
    }

    /**
     * <h2> 资金战法分析部分通用聚合查询(适用于用户指定了一组调单卡号集合) </h2>
     * <p>
     * 目前适用于 交易统计分析结果查询、交易汇聚结果查询等
     * <p>
     * 操作的是表 {@link com.zqykj.domain.bank.BankTransactionRecord}
     */
    private void fundTacticsPartUniversalAgg(AggregationParams cardTerms, @Nullable FundTacticsPartGeneralPreRequest preRequest) {

        // 交易总金额
        AggregationParams tradeTotalAmount = AggregationParamsBuilders.sum("local_trade_amount",
                FundTacticsAnalysisField.CHANGE_MONEY, null);
        setSubAggregation(cardTerms, tradeTotalAmount);

        // 入账次数
        QuerySpecialParams creditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams creditsTimes = AggregationParamsBuilders.filter("local_credits_times", creditsFilter, null);
        // 入账金额
        AggregationParams creditsAmount = AggregationParamsBuilders.sum("local_credits_amount", FundTacticsAnalysisField.CHANGE_MONEY, null);
        //
        setSubAggregation(creditsTimes, creditsAmount);
        //
        setSubAggregation(cardTerms, creditsTimes);

        // 出账次数
        QuerySpecialParams outFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams outTimes = AggregationParamsBuilders.filter("local_out_times", outFilter, null);
        // 出账金额
        AggregationParams outAmount = AggregationParamsBuilders.sum("local_out_amount", FundTacticsAnalysisField.CHANGE_MONEY, null);
        //
        setSubAggregation(outTimes, outAmount);
        //
        setSubAggregation(cardTerms, outTimes);

        // 最早日期
        AggregationParams minDate = AggregationParamsBuilders.min("local_min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(cardTerms, minDate);
        // 最晚日期
        AggregationParams maxDate = AggregationParamsBuilders.max("local_max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(cardTerms, maxDate);
        // 交易净额
        AggregationParams tradeNet = AggregationParamsBuilders.sum("local_trade_net", FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        setSubAggregation(cardTerms, tradeNet);
    }

    /**
     * <h2> 聚合展示字段 </h2>
     */
    private AggregationParams fundTacticsPartUniversalAggShowFields(String[] fields, String hitsAggName, @Nullable FieldSort sort) {
        FetchSource fetchSource = new FetchSource(fields, 0, 1);
        if (null != sort) {
            fetchSource.setSort(sort);
        }
        return AggregationParamsBuilders.fieldSource(hitsAggName, fetchSource);
    }

    public <T> AggregationParams buildTradeStatisticsAnalysisTotalAgg(T request) {

        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        // 前置过滤条件
        CombinationQueryParams combinationOne = new CombinationQueryParams();
        combinationOne.setType(ConditionType.filter);
        // 本方查询卡号(有可能是查询全部,那么卡号不为空的时候才能选用此条件)
        if (!CollectionUtils.isEmpty(queryRequest.getCardNums())) {
            combinationOne.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, queryRequest.getCardNums()));
        }
        if (StringUtils.isNotBlank(queryRequest.getKeyword())) {
            // 本方需要的模糊匹配
            combinationOne.addCommonQueryParams(new CommonQueryParams(queryBuilderFactory.assembleLocalFuzzy(queryRequest.getKeyword())));
        }
        QuerySpecialParams prefixFilter = new QuerySpecialParams();
        prefixFilter.addCombiningQueryParams(combinationOne);
        AggregationParams root = new AggregationParams("total", AggsType.filter.name(), prefixFilter);
        AggregationParams total;
        total = new AggregationParams("distinct_" + FundTacticsAnalysisField.QUERY_CARD, AggsType.cardinality.name(), FundTacticsAnalysisField.QUERY_CARD);
        setSubAggregation(root, total);
        return root;
    }

    public <T> AggregationParams buildTradeStatisticsAnalysisFundByTimeType(T request) {

        FundDateRequest fundAnalysisDateRequest = (FundDateRequest) request;

        fundAnalysisDateRequest.setDateField(FundTacticsAnalysisField.TRADING_TIME);
        fundAnalysisDateRequest.setMetricsField(FundTacticsAnalysisField.TRANSACTION_MONEY);
        DateParams dateParams = new DateParams();
        // 设置format
        if (StringUtils.isNotBlank(fundAnalysisDateRequest.getFormat())) {
            dateParams.setFormat(fundAnalysisDateRequest.getFormat());
        } else {
            dateParams.setFormat(FundDateRequest.convertFromTimeType(fundAnalysisDateRequest.getTimeType().name()));
        }
        if (fundAnalysisDateRequest.getTimeValue() > 1) {
            // 代表的是固定间隔 fixed
            dateParams.addFixedInterval(fundAnalysisDateRequest.getTimeValue(), fundAnalysisDateRequest.getTimeType().name());
        } else {
            // 默认间隔是1
            dateParams.addCalendarInterval(fundAnalysisDateRequest.getTimeType().name());
        }
        AggregationParams root = new AggregationParams("date_group", AggsType.date_histogram.name(), fundAnalysisDateRequest.getDateField(), dateParams);

        AggregationParams sub = new AggregationParams("trade_amount_sum", AggsType.sum.name(), fundAnalysisDateRequest.getMetricsField());

        root.setPerSubAggregation(sub);

        Map<String, String> bucketsPathMap = new HashMap<>();
        bucketsPathMap.put("final_sum", "trade_amount_sum");
        PipelineAggregationParams pipelineAggregationParams =
                new PipelineAggregationParams("sum_bucket_selector", AggsType.bucket_selector.name(),
                        bucketsPathMap, "params.final_sum > 0");
        root.setPerSubAggregation(pipelineAggregationParams);
        return root;
    }


    @Override
    public <T> AggregationParams createAssetTrendsAnalysisQueryAgg(T request) {

        // 需要取出的聚合结果值 key: 聚合名称 value: 聚合属性
        Map<String, String> mapping = new LinkedHashMap<>();
        AssetTrendsRequest assetTrendsRequest;
        assetTrendsRequest = (AssetTrendsRequest) request;
        // 这里可以自定义聚合名称的拼接方式
        String dateAggregateName = "date_histogram_" + FundTacticsAnalysisField.TRADING_TIME;
        mapping.put(dateAggregateName, ElasticsearchAggregationResponseAttributes.keyAsString);
        DateParams dateParams = new DateParams();
        String format = FundDateRequest.convertFromTimeType(assetTrendsRequest.getDateType());
        dateParams.setFormat(format);
        // default
        dateParams.setMinDocCount(1);
        dateParams.addCalendarInterval(assetTrendsRequest.getDateType());
        AggregationParams root = new AggregationParams(dateAggregateName, "date_histogram", FundTacticsAnalysisField.TRADING_TIME, dateParams);
        Pagination pagination = new Pagination(assetTrendsRequest.getPaging().getPage(), assetTrendsRequest.getPaging().getPageSize());

        // 默认按交易总金额降序排序(如果没有指定的话)
        String sortPath;
        Direction direction = Direction.DESC;
        if (null == assetTrendsRequest.getSorting() || (assetTrendsRequest.getSorting() != null && StringUtils.isBlank(assetTrendsRequest.getSorting().getProperty()))) {
            sortPath = DEFAULT_SORTING_FIELD;
        } else {
            assert assetTrendsRequest.getSorting() != null;
            sortPath = assetTrendsRequest.getSorting().getProperty();
        }
        FieldSort fieldSort = new FieldSort(sortPath, direction.name());
        addSubAggregationParams(root, pagination, fieldSort, mapping);
        root.setMapping(mapping);
        return root;
    }

    @Override
    public <T> AggregationParams createPeopleAreaQueryAgg(T request) {
        Map<String, String> mapping = new LinkedHashMap<>();
        PeopleAreaRequest peopleAreaRequest = (PeopleAreaRequest) request;
        String field = peopleAreaRequest.getField();
        String terms = AggsType.terms.name();
        String areaGroupName = field + "_" + terms;
        mapping.put(areaGroupName, ElasticsearchAggregationResponseAttributes.keyAsString);
        AggregationParams root = new AggregationParams(areaGroupName, terms, PeopleAreaConversion.REGION_NAME.get(field));
        root.setMapping(mapping);
        setSubAggregations(root, peopleAreaRequest);
        return root;
    }


    /**
     * @param root:              根聚合参数
     * @param peopleAreaRequest: 人员地域请求体
     * @return: void
     **/
    public void setSubAggregations(AggregationParams root, PeopleAreaRequest peopleAreaRequest) {
        // 计算地区个数
        String count = AggsType.count.name();
        String regionCount = peopleAreaRequest.getField() + AGG_NAME_SPLIT + count;
        AggregationParams subRegionTotalAgg = new AggregationParams(regionCount, count, PeopleAreaConversion.REGION_NAME.get(peopleAreaRequest.getField()));
        root.getMapping().put(regionCount, ElasticsearchAggregationResponseAttributes.value);
        setSubAggregation(root, subRegionTotalAgg);

        // 按照地区个数排序
        Direction direction = peopleAreaRequest.getSorting().getOrder().isAscending() ? Direction.ASC : Direction.DESC;
        Pagination pagination = new Pagination(peopleAreaRequest.getPaging().getPage(), peopleAreaRequest.getPaging().getPageSize());
        FieldSort fieldSort = new FieldSort(regionCount, direction.name());
        // 对交易统计分析结果排序
        String bucketSort = AggsType.bucket_sort.name();
        String sortAgg = TradeStatisticsAnalysisAggName.TRADE_RESULT_AGG_NAME + AGG_NAME_SPLIT + bucketSort;
        PipelineAggregationParams regionNumberOrderAgg = new PipelineAggregationParams(sortAgg, bucketSort, Collections.singletonList(fieldSort), pagination);
        root.setPerSubAggregation(regionNumberOrderAgg);
    }

    /**
     * 将传入的子聚合加入到传入的父聚合中去.
     *
     * @param root: 父聚合参数.
     * @param sub:  子聚合参数.
     * @return: void
     **/
    private void setSubAggregation(AggregationParams root, AggregationParams sub) {
        root.setPerSubAggregation(sub);
    }

    /**
     * 增加管道聚合，用于计算交易净额。
     *
     * @param root:                父聚合参数.
     * @param tradeNetBucketsPath: 交易金额桶路径.
     * @param tradeNetScript:      交易金额管道聚合脚本.
     * @param pagination:          分页参数.
     * @param fieldSort:           排序参数.
     * @return: void
     **/
    private void setSubPipelineAggregation(AggregationParams root,
                                           Map<String, String> tradeNetBucketsPath, String tradeNetScript,
                                           Pagination pagination, FieldSort fieldSort, Map<String, String> mapping) {


        // 计算交易净和
        String bucketScript = AggsType.bucket_script.name();
        String tradeNet = TradeStatisticsAnalysisAggName.TRADE_NET_AGG_NAME + AGG_NAME_SPLIT + bucketScript;
        PipelineAggregationParams tradeNetMoneyPipelineAgg = new PipelineAggregationParams(tradeNet, bucketScript, tradeNetBucketsPath, tradeNetScript);
        mapping.put(tradeNet, ElasticsearchAggregationResponseAttributes.valueAsString);
        root.setPerSubAggregation(tradeNetMoneyPipelineAgg);

        // 对交易统计分析结果排序
        String bucketSort = AggsType.bucket_sort.name();
        String sortAgg = TradeStatisticsAnalysisAggName.TRADE_RESULT_AGG_NAME + AGG_NAME_SPLIT + bucketSort;
        PipelineAggregationParams tradeResultOrderAgg = new PipelineAggregationParams(sortAgg, bucketSort, Collections.singletonList(fieldSort), pagination);
        root.setPerSubAggregation(tradeResultOrderAgg);
    }

    /**
     * 构建公共子聚合条件参数.
     *
     * @param root:       父聚合参数.
     * @param pagination: 分页参数.
     * @param fieldSort:  排序参数.
     * @return: void
     **/
    private void addSubAggregationParams(AggregationParams root, Pagination pagination, FieldSort fieldSort, Map<String, String> mapping) {
        // 计算每个查询卡号的交易总金额
        String sum = AggsType.sum.name();
        String tradeMoneySum = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum;
        AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, sum, FundTacticsAnalysisField.TRANSACTION_MONEY);
        mapping.put(tradeMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
        setSubAggregation(root, subTradeMoneySumAgg);

        // 计算每个查询卡号的交易总次数
        String count = AggsType.count.name();
        String tradeCount = FundTacticsAnalysisField.QUERY_CARD + AGG_NAME_SPLIT + count;
        AggregationParams subTradeTotalAgg = new AggregationParams(tradeCount, count, FundTacticsAnalysisField.QUERY_CARD);
        mapping.put(tradeCount, ElasticsearchAggregationResponseAttributes.value);
        setSubAggregation(root, subTradeTotalAgg);

        // 计算每个查询卡号的入账数据过滤(该聚合能统计入账次数)
        String filter = AggsType.filter.name();
        String payIn = FundTacticsAnalysisField.LOAN_FLAG + AGG_NAME_SPLIT + filter + AGG_NAME_SPLIT + FundTacticsAnalysisField.LOAN_FLAG_IN_EN;
        QuerySpecialParams payInQuery = new QuerySpecialParams();
        payInQuery.setCommonQuery(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams subPayInAgg = new AggregationParams(payIn, filter, payInQuery);
        mapping.put(payIn, ElasticsearchAggregationResponseAttributes.docCount);

        // 继续子聚合
        // 计算每个查询卡号的入账总金额
        String payInMoneySum = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum + AGG_NAME_SPLIT + FundTacticsAnalysisField.LOAN_FLAG_IN_EN;
        AggregationParams payInMoneySumAgg = new AggregationParams(payInMoneySum, sum, FundTacticsAnalysisField.TRANSACTION_MONEY);
        mapping.put(payInMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
        setSubAggregation(subPayInAgg, payInMoneySumAgg);
        setSubAggregation(root, subPayInAgg);

        // 计算每个查询卡号的 出账数据过滤(该聚合能统计出账次数)

        String payOut = FundTacticsAnalysisField.LOAN_FLAG + AGG_NAME_SPLIT + filter + AGG_NAME_SPLIT + FundTacticsAnalysisField.LOAN_FLAG_OUT_EN;
        QuerySpecialParams payOutQuery = new QuerySpecialParams();
        payOutQuery.setCommonQuery(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams subPayOutAgg = new AggregationParams(payOut, filter, payOutQuery);
        mapping.put(payOut, ElasticsearchAggregationResponseAttributes.docCount);

        // 继续子聚合
        // 计算每个查询卡号的出账总金额
        String payOutMoneySum = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum + AGG_NAME_SPLIT + FundTacticsAnalysisField.LOAN_FLAG_OUT_EN;
        AggregationParams payOutMoneySumAgg = new AggregationParams(payOutMoneySum, sum, FundTacticsAnalysisField.TRANSACTION_MONEY);
        mapping.put(payOutMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
        setSubAggregation(subPayOutAgg, payOutMoneySumAgg);
        setSubAggregation(root, subPayOutAgg);


        // 管道聚合构建
        Map<String, String> tradeNetBucketsPath = new LinkedHashMap<>();
        tradeNetBucketsPath.put("pay_in_total", payIn + PIPELINE_AGG_PATH_FLAG + payInMoneySum);
        tradeNetBucketsPath.put("pay_out_total", payOut + PIPELINE_AGG_PATH_FLAG + payOutMoneySum);
        String tradeNetScript = BUCKET_SCRIPT_PARAM_PREFIX + "pay_in_total" + "-" + BUCKET_SCRIPT_PARAM_PREFIX + "pay_out_total";
        setSubPipelineAggregation(root, tradeNetBucketsPath, tradeNetScript, pagination, fieldSort, mapping);

    }


    public <T> AggregationParams buildTradeConvergenceAnalysisResultMainCardsAgg(T request, int from, int size) {

        TradeConvergenceAnalysisQueryRequest convergenceRequest = (TradeConvergenceAnalysisQueryRequest) request;

        AggregationParams cardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.MERGE_CARD);
        cardTerms.setSize(convergenceRequest.getGroupInitSize());
        cardTerms.setCollectMode(BREADTH_FIRST);
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.MERGE_CARD, null);

        setSubAggregation(cardTerms, tradeTotalTimes);
        fundTacticsPartUniversalAgg(cardTerms, convergenceRequest);
        // 设置分页 与 排序
        // 排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(convergenceRequest.getSortRequest(), from, size);
        if (null != sort) {
            cardTerms.setPerSubAggregation(sort);
        }
        return cardTerms;
    }

    public <T> AggregationParams buildTradeConvergenceQueryAndMergeCardsAgg(T request, int from, int size) {
        TradeConvergenceAnalysisQueryRequest convergenceRequest = (TradeConvergenceAnalysisQueryRequest) request;

        AggregationParams cardTerms = AggregationParamsBuilders.terms("groupBy_" + FundTacticsAnalysisField.MERGE_CARD, FundTacticsAnalysisField.MERGE_CARD);
        cardTerms.setSize(convergenceRequest.getGroupInitSize());
        cardTerms.setCollectMode(BREADTH_FIRST);
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.MERGE_CARD, null);

        setSubAggregation(cardTerms, tradeTotalTimes);
        fundTacticsPartUniversalAgg(cardTerms, convergenceRequest);
        // 设置分页 与 排序
        // 排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(convergenceRequest.getSortRequest(), from, size);
        if (null != sort) {
            cardTerms.setPerSubAggregation(sort);
        }
        return cardTerms;
    }

    public AggregationParams buildTradeConvergenceAnalysisHitsAgg(int groupSize) {

        AggregationParams cardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.MERGE_CARD);
        cardTerms.setSize(groupSize);
        AggregationParams showFields = fundTacticsPartUniversalAggShowFields(FundTacticsAnalysisField.tradeConvergenceAnalysisShowField(), "local_hits", null);
        cardTerms.setPerSubAggregation(showFields);
        return cardTerms;
    }

    public <T> AggregationParams buildTradeConvergenceAnalysisResultTotalAgg(T request) {

        TradeConvergenceAnalysisQueryRequest queryRequest = (TradeConvergenceAnalysisQueryRequest) request;

        AggregationParams filter = null;
        // 前置过滤条件
        CombinationQueryParams combinationOne = new CombinationQueryParams();
        combinationOne.setType(ConditionType.filter);
        // 需要的模糊匹配
        if (StringUtils.isNotBlank(queryRequest.getKeyword())) {
            CombinationQueryParams localFuzzy = queryBuilderFactory.assembleLocalFuzzy(queryRequest.getKeyword());
            CombinationQueryParams oppositeFuzzy = queryBuilderFactory.assembleOppositeFuzzy(queryRequest.getKeyword());
            localFuzzy.getCommonQueryParams().addAll(oppositeFuzzy.getCommonQueryParams());
            combinationOne.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }
        if (!CollectionUtils.isEmpty(combinationOne.getCommonQueryParams())) {
            QuerySpecialParams prefixFilter = new QuerySpecialParams();
            prefixFilter.addCombiningQueryParams(combinationOne);
            filter = new AggregationParams("total", AggsType.filter.name(), prefixFilter);
        }
        AggregationParams total = new AggregationParams("distinct_" + FundTacticsAnalysisField.MERGE_CARD, AggsType.cardinality.name(), FundTacticsAnalysisField.MERGE_CARD);

        if (null != filter) {
            setSubAggregation(filter, total);
            return filter;
        }
        return total;
    }

    /**
     * <h2> 批量获取调单卡号集合 </h2>
     */
    public AggregationParams buildGetCardNumsInBatchesAgg(int from, int size) {

        // 按照查询卡号去重
        AggregationParams groupQueryCard = AggregationParamsBuilders.terms("groupBy_" + FundTacticsAnalysisField.QUERY_CARD, FundTacticsAnalysisField.QUERY_CARD);
        // 批量返回
        PipelineAggregationParams sortAndPage = AggregationParamsBuilders.sort("sortAndPage", from, size);
        // 设置子聚合
        groupQueryCard.setPerSubAggregation(sortAndPage);
        return groupQueryCard;
    }

    public AggregationParams groupByField(String field, int groupSize, @Nullable Pagination pagination) {
        AggregationParams groupBy = AggregationParamsBuilders.terms("groupBy_" + field, field);
        groupBy.setSize(groupSize);
        // 设置分页
        if (null != pagination) {
            int from = pagination.getFrom();
            Integer size = pagination.getSize();
            PipelineAggregationParams page = AggregationParamsBuilders.sort("page", from, size);
            groupBy.setPerSubAggregation(page);
        }
        return groupBy;
    }

    public AggregationParams showFields(@Nullable FieldSort sort, String... fields) {
        return fundTacticsPartUniversalAggShowFields(fields, "local_hits", sort);
    }

    /**
     * <h2>  交易统计分析部分自定义聚合名称 </h2>
     */
    interface TradeStatisticsAnalysisAggName {
        // 交易净额计算
        String TRADE_NET_AGG_NAME = "trade_net";
        // 交易结果 根据某个指标值排序
        String TRADE_RESULT_AGG_NAME = "trade_result_order";
    }

    /**
     * 构建单卡画像最早交易时间聚合查询参数
     */
    @Override
    public <T> AggregationParams buildSingleCardPortraitEarliestTimeAgg(T request) {
        return AggregationParamsBuilders.min(SingleCardPortraitAnalysisField.AggResultName.EARLIEST_TRADING_TIME, SingleCardPortraitAnalysisField.TRADING_TIME, null);
    }

    /**
     * 构建单卡画像最晚交易时间聚合查询参数
     */
    @Override
    public <T> AggregationParams buildSingleCardPortraitLatestTimeAgg(T request) {
        return AggregationParamsBuilders.max(SingleCardPortraitAnalysisField.AggResultName.LATEST_TRADING_TIME, SingleCardPortraitAnalysisField.TRADING_TIME, null);
    }

    /**
     * 构建单卡画像查询卡号分桶聚合查询参数
     */
    @Override
    public <T> AggregationParams buildSingleCardPortraitAgg(T request) {
        SingleCardPortraitRequest singleCardPortraitRequest = (SingleCardPortraitRequest) request;
        String queryCard = singleCardPortraitRequest.getQueryCard();
        // 查询卡号分桶参数
        AggregationParams localCardTermsAggregationParams = AggregationParamsBuilders.terms(SingleCardPortraitAnalysisField.AggResultName.LOCAL_CARD_TERMS, SingleCardPortraitAnalysisField.QUERY_CARD, queryCard);
        // 当前查询卡号进账交易金额查询参数（统计结果是正数）
        QuerySpecialParams localInTransactionMoneyQuerySpecialParams = new QuerySpecialParams();
        CommonQueryParams localInTransactionTermCommonQueryParams = QueryParamsBuilders.term(SingleCardPortraitAnalysisField.LOAN_FLAG, SingleCardPortraitAnalysisField.Value.LOAN_FLAG_IN);
        localInTransactionMoneyQuerySpecialParams.addCommonQueryParams(localInTransactionTermCommonQueryParams);
        AggregationParams localInTransactionMoneySum = AggregationParamsBuilders.filter(SingleCardPortraitAnalysisField.AggResultName.LOCAL_IN_TRANSACTION_MONEY_SUM, localInTransactionMoneyQuerySpecialParams, null);
        AggregationParams localTransactionMoneySum1 = AggregationParamsBuilders.sum(SingleCardPortraitAnalysisField.AggResultName.LOCAL_IN_TRANSACTION_MONEY, SingleCardPortraitAnalysisField.TRANSACTION_MONEY);
        localInTransactionMoneySum.setPerSubAggregation(localTransactionMoneySum1);
        localCardTermsAggregationParams.setPerSubAggregation(localInTransactionMoneySum);
        // 当前查询卡号出账交易金额查询参数（统计结果为负数）
        QuerySpecialParams localOutTransactionMoneyQuerySpecialParams = new QuerySpecialParams();
        CommonQueryParams localOutTransactionTermCommonQueryParams = QueryParamsBuilders.term(SingleCardPortraitAnalysisField.LOAN_FLAG, SingleCardPortraitAnalysisField.Value.LOAN_FLAG_OUT);
        localOutTransactionMoneyQuerySpecialParams.addCommonQueryParams(localOutTransactionTermCommonQueryParams);
        AggregationParams localOutTransactionMoneySum = AggregationParamsBuilders.filter(SingleCardPortraitAnalysisField.AggResultName.LOCAL_OUT_TRANSACTION_MONEY_SUM, localOutTransactionMoneyQuerySpecialParams, null);
        AggregationParams localTransactionMoneySum2 = AggregationParamsBuilders.sum(SingleCardPortraitAnalysisField.AggResultName.LOCAL_OUT_TRANSACTION_MONEY, SingleCardPortraitAnalysisField.TRANSACTION_MONEY);
        localOutTransactionMoneySum.setPerSubAggregation(localTransactionMoneySum2);
        localCardTermsAggregationParams.setPerSubAggregation(localOutTransactionMoneySum);
        // 当前查询卡号交易净额（进账交易金额 - 出账交易金额（结果需转换成正数值））
        AggregationParams localTotalTransactionMoneySum = AggregationParamsBuilders.sum(SingleCardPortraitAnalysisField.AggResultName.LOCAL_TOTAL_TRANSACTION_MONEY_SUM, SingleCardPortraitAnalysisField.TRANSACTION_MONEY);
        localCardTermsAggregationParams.setPerSubAggregation(localTotalTransactionMoneySum);
        // 构建作为本方卡号top_hits
        FieldSort fieldSort = new FieldSort(SingleCardPortraitAnalysisField.TRADING_TIME, Sort.Direction.DESC.name());
        FetchSource localFetchSource = new FetchSource(SingleCardPortraitAnalysisField.Value.LOCAL_INCLUDES_TOP_HITS, 0, 1, fieldSort);
        AggregationParams localTopHits = AggregationParamsBuilders.fieldSource(SingleCardPortraitAnalysisField.AggResultName.LOCAL_HITS, localFetchSource);
        localCardTermsAggregationParams.setPerSubAggregation(localTopHits);

        return localCardTermsAggregationParams;
    }

    public <T> AggregationParams buildAdjustIndividualAgg(T request) {

        // 调单卡号请求
        AdjustIndividualRequest adjustIndividualRequest = (AdjustIndividualRequest) request;

        // 按照开户证件号码分组
        AggregationParams accountTerms = AggregationParamsBuilders.terms("accountGroupBy", FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD);
        accountTerms.setSize(adjustIndividualRequest.getGroupInitSize());

        // 调单账号次数(统计的是查询卡号去重次数)
        AggregationParams distinctCards = AggregationParamsBuilders.cardinality("adjustAccountCount", FundTacticsAnalysisField.QUERY_CARD);
        accountTerms.setPerSubAggregation(distinctCards);
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("tradeTotal",
                FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD, null);
        accountTerms.setPerSubAggregation(tradeTotalTimes);
        // 统计入账笔数、入账金额、出账笔数、出账金额、交易净额、交易总金额、最早交易时间、最晚交易时间
        fundTacticsPartUniversalAgg(accountTerms, null);

        // 设置聚合展示字
        String[] showFields = new String[]{FundTacticsAnalysisField.CUSTOMER_NAME, FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD};
        AggregationParams showFieldsAgg = fundTacticsPartUniversalAggShowFields(showFields, "local_hits", null);
        accountTerms.setPerSubAggregation(showFieldsAgg);
        // 排序
        PageRequest pageRequest = adjustIndividualRequest.getPageRequest();
        PipelineAggregationParams sortAgg = fundTacticsPartUniversalAggSort(adjustIndividualRequest.getSortRequest(),
                PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize()), pageRequest.getPageSize());
        accountTerms.setPerSubAggregation(sortAgg);
        return accountTerms;
    }

    public AggregationParams buildDistinctViaField(String distinctField) {

        return AggregationParamsBuilders.cardinality("distinct_" + distinctField, distinctField);
    }

    public <T> AggregationParams buildAdjustCardsAgg(T request) {

        // 调单卡号请求
        AdjustIndividualRequest adjustIndividualRequest = (AdjustIndividualRequest) request;
        // 按照查询卡号分组
        AggregationParams queryCardTerms = AggregationParamsBuilders.terms("queryCardBy", FundTacticsAnalysisField.QUERY_CARD);
        queryCardTerms.setSize(adjustIndividualRequest.getGroupInitSize());

        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("tradeTotal",
                FundTacticsAnalysisField.QUERY_CARD, null);
        queryCardTerms.setPerSubAggregation(tradeTotalTimes);
        // 统计入账笔数、入账金额、出账笔数、出账金额、交易净额、交易总金额、最早交易时间、最晚交易时间
        fundTacticsPartUniversalAgg(queryCardTerms, null);

        // 设置聚合展示字
        String[] showFields = new String[]{FundTacticsAnalysisField.QUERY_CARD, FundTacticsAnalysisField.BANK,
                FundTacticsAnalysisField.CUSTOMER_NAME, FundTacticsAnalysisField.CUSTOMER_IDENTITY_CARD};
        AggregationParams aggShowFields = fundTacticsPartUniversalAggShowFields(showFields, "local_hits", null);
        queryCardTerms.setPerSubAggregation(aggShowFields);
        // 排序
        PageRequest pageRequest = adjustIndividualRequest.getPageRequest();
        PipelineAggregationParams sortAgg = fundTacticsPartUniversalAggSort(adjustIndividualRequest.getSortRequest(),
                PageRequest.getOffset(pageRequest.getPage(), pageRequest.getPageSize()), pageRequest.getPageSize());
        queryCardTerms.setPerSubAggregation(sortAgg);
        return queryCardTerms;
    }

    public AggregationParams buildCreditsAdjustCardsAgg(int initGroupSize, int from, int size) {

        // 过滤出借贷标志为进
        QuerySpecialParams filter = new QuerySpecialParams();
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams root = AggregationParamsBuilders.filter("filter_credits", filter, null);
        AggregationParams cardTerms = AggregationParamsBuilders.terms("query_card_terms", FundTacticsAnalysisField.QUERY_CARD);
        root.setPerSubAggregation(cardTerms);

        AggregationParams creditsAmountSum = AggregationParamsBuilders.sum("money_sum", FundTacticsAnalysisField.CHANGE_MONEY);
        // 设置分组数量
        cardTerms.setSize(initGroupSize);
        cardTerms.setPerSubAggregation(creditsAmountSum);
        // 设置分页
        PipelineAggregationParams sortAgg = AggregationParamsBuilders.sort("sort", from, size);
        cardTerms.setPerSubAggregation(sortAgg);
        return root;
    }

    public AggregationParams buildCreditsAdjustCardsTotalAgg() {

        // 过滤出借贷标志为进
        QuerySpecialParams filter = new QuerySpecialParams();
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams root = AggregationParamsBuilders.filter("filter_credits", filter, null);
        // 对查询卡号去重
        AggregationParams total = AggregationParamsBuilders.cardinality("distinct_" + FundTacticsAnalysisField.QUERY_CARD, FundTacticsAnalysisField.QUERY_CARD);
        root.setPerSubAggregation(total);
        return root;
    }

    @Override
    public <T> AggregationParams buildIndividualInfoAndStatisticsAgg(T request) {
        IndividualPortraitCommonParams individualPortraitCommonParams = new IndividualPortraitCommonParams().invoke();
        AggregationParams customerIdentityCardTerms = individualPortraitCommonParams.getCustomerIdentityCardTerms();
        AggregationParams queryCardTerms = individualPortraitCommonParams.getQueryCardTerms();

        // 1.2 成功累计收入金额
        String cumulativeIncomeBucketsPath = "queryCardTerms>totalEntryTransactionMoney";
        PipelineAggregationParams cumulativeIncome = new PipelineAggregationParams(IndividualPortraitAnalysisField.AggResultName.CUMULATIVE_INCOME,
                AggsType.sum_bucket.name(), cumulativeIncomeBucketsPath);

        // 1.3 成功累计支出金额
        String cumulativeExpenditureBucketsPath = "queryCardTerms>totalOutTransactionMoney";
        PipelineAggregationParams cumulativeExpenditure = new PipelineAggregationParams(IndividualPortraitAnalysisField.AggResultName.CUMULATIVE_EXPENDITURE,
                AggsType.sum_bucket.name(), cumulativeExpenditureBucketsPath);

        // 1.4 成功累计交易净额
        String cumulativeNetBucketsPath = "queryCardTerms>netTransactionMoney";
        PipelineAggregationParams cumulativeNet = new PipelineAggregationParams(IndividualPortraitAnalysisField.AggResultName.CUMULATIVE_NET,
                AggsType.sum_bucket.name(), cumulativeNetBucketsPath);

        customerIdentityCardTerms.setPerSubAggregation(queryCardTerms);
        customerIdentityCardTerms.setPerSubAggregation(cumulativeIncome);
        customerIdentityCardTerms.setPerSubAggregation(cumulativeExpenditure);
        customerIdentityCardTerms.setPerSubAggregation(cumulativeNet);

        return customerIdentityCardTerms;
    }

    @Override
    public <T> AggregationParams buildIndividualCardTransactionStatisticsAgg(T request) {
        IndividualCardTransactionStatisticsRequest individualCardTransactionStatisticsRequest = (IndividualCardTransactionStatisticsRequest) request;
        Pagination pagination = individualCardTransactionStatisticsRequest.getPagination();
        SortRequest sortRequest = individualCardTransactionStatisticsRequest.getSortRequest();

        IndividualPortraitCommonParams individualPortraitCommonParams = new IndividualPortraitCommonParams().invoke();
        AggregationParams customerIdentityCardTerms = individualPortraitCommonParams.getCustomerIdentityCardTerms();
        AggregationParams queryCardTerms = individualPortraitCommonParams.getQueryCardTerms();
        // 1.1.8 单卡交易最早时间
        queryCardTerms.setPerSubAggregation(earliestTradingTimeAgg());
        // 1.1.9 单卡最晚交易时间
        queryCardTerms.setPerSubAggregation(latestTradingTimeAgg());
        // 单卡交易总次数
        queryCardTerms.setPerSubAggregation(totalTransactionTimesAgg());

        // 分页、排序
        PipelineAggregationParams pipelineAggregationParams;
        // 默认按照交易总次数降序
        String defaultSortProperty = "totalTransactionTimes";
        // 排除排序字段
        List<String> excludeSortProperties = Arrays.asList("transactionBalance", "fundsProportion");
        FieldSort fieldSort;
        if (!ObjectUtils.isEmpty(sortRequest) && pagination.getSize() != null && !excludeSortProperties.contains(sortRequest.getProperty())) {
            switch (sortRequest.getProperty()) {
                case "entryTransactionTimes":
                    fieldSort = new FieldSort("entryFilter.entryTransactionTimes", sortRequest.getOrder().name());
                    break;
                case "outTransactionTimes":
                    fieldSort = new FieldSort("outFilter.outTransactionTimes", sortRequest.getOrder().name());
                    break;
                default:
                    fieldSort = new FieldSort(sortRequest.getProperty(), sortRequest.getOrder().name());
                    break;
            }
            List<FieldSort> fieldSorts = Collections.singletonList(fieldSort);
            pipelineAggregationParams = new PipelineAggregationParams(IndividualPortraitAnalysisField.AggResultName.BUCKET_SORT_NAME,
                    AggsType.bucket_sort.name(), fieldSorts, pagination);
        } else {
            fieldSort = new FieldSort(defaultSortProperty, Direction.DESC.name());
            List<FieldSort> fieldSorts = Collections.singletonList(fieldSort);
            pipelineAggregationParams = new PipelineAggregationParams(IndividualPortraitAnalysisField.AggResultName.BUCKET_SORT_NAME,
                    AggsType.bucket_sort.name(), fieldSorts, pagination);
        }
        queryCardTerms.setPerSubAggregation(pipelineAggregationParams);

        customerIdentityCardTerms.setPerSubAggregation(queryCardTerms);

        return customerIdentityCardTerms;
    }

    private AggregationParams loanFlagInFilterAgg() {
        QuerySpecialParams loanFlagInQuerySpecialParams = new QuerySpecialParams();
        CommonQueryParams loanFlagInTermsQuery = QueryParamsBuilders.term(IndividualPortraitAnalysisField.LOAN_FLAG,
                IndividualPortraitAnalysisField.Value.LOAN_FLAG_IN);
        loanFlagInQuerySpecialParams.addCommonQueryParams(loanFlagInTermsQuery);
        AggregationParams loanFlagInFilter = AggregationParamsBuilders.filter(IndividualPortraitAnalysisField.AggResultName.ENTRY_FILTER,
                loanFlagInQuerySpecialParams, null);
        AggregationParams loanFlagInSumAggregation = AggregationParamsBuilders.sum(IndividualPortraitAnalysisField.AggResultName.ENTRY_TRANSACTION_MONEY_SUM,
                IndividualPortraitAnalysisField.TRANSACTION_MONEY);
        AggregationParams entryCountAggregation = AggregationParamsBuilders.count(IndividualPortraitAnalysisField.AggResultName.ENTRY_TRANSACTION_TIMES,
                IndividualPortraitAnalysisField.LOAN_FLAG, null);
        loanFlagInFilter.setPerSubAggregation(loanFlagInSumAggregation);
        loanFlagInFilter.setPerSubAggregation(entryCountAggregation);
        return loanFlagInFilter;
    }

    private AggregationParams loanFlagOutFilterAgg() {
        QuerySpecialParams loanFlagOutQuerySpecialParams = new QuerySpecialParams();
        CommonQueryParams loanFlagOutTermsQuery = QueryParamsBuilders.term(IndividualPortraitAnalysisField.LOAN_FLAG,
                IndividualPortraitAnalysisField.Value.LOAN_FLAG_OUT);
        loanFlagOutQuerySpecialParams.addCommonQueryParams(loanFlagOutTermsQuery);
        AggregationParams loanFlagOutFilter = AggregationParamsBuilders.filter(IndividualPortraitAnalysisField.AggResultName.OUT_FILTER,
                loanFlagOutQuerySpecialParams, null);
        AggregationParams loanFlagOutSumAggregation = AggregationParamsBuilders.sum(IndividualPortraitAnalysisField.AggResultName.OUT_TRANSACTION_MONEY_SUM,
                IndividualPortraitAnalysisField.TRANSACTION_MONEY);
        AggregationParams outCountAggregation = AggregationParamsBuilders.count(IndividualPortraitAnalysisField.AggResultName.OUT_TRANSACTION_TIMES,
                IndividualPortraitAnalysisField.LOAN_FLAG, null);
        loanFlagOutFilter.setPerSubAggregation(loanFlagOutSumAggregation);
        loanFlagOutFilter.setPerSubAggregation(outCountAggregation);
        return loanFlagOutFilter;
    }

    private PipelineAggregationParams totalTransactionMoneyAgg() {
        Map<String, String> bucketsPathMap = new HashMap<>(16);
        bucketsPathMap.put("totalEntryTransactionMoneyPath", "entryFilter>entryTransactionMoneySum");
        bucketsPathMap.put("totalOutgoingTransactionMoneyPath", "outFilter>outTransactionMoneySum");
        String script = "params.totalEntryTransactionMoneyPath - params.totalOutgoingTransactionMoneyPath";
        return new PipelineAggregationParams(IndividualPortraitAnalysisField.AggResultName.TOTAL_TRANSACTION_MONEY,
                AggsType.bucket_script.name(), bucketsPathMap, script);
    }

    private PipelineAggregationParams totalEntryTransactionMoneyAgg() {
        Map<String, String> bucketsPathMap = new HashMap<>(16);
        bucketsPathMap.put("entryTransactionMoneyPath", "entryFilter.entryTransactionMoneySum");
        String script = "params.entryTransactionMoneyPath";
        return new PipelineAggregationParams(IndividualPortraitAnalysisField.AggResultName.TOTAL_ENTRY_TRANSACTION_MONEY,
                AggsType.bucket_script.name(), bucketsPathMap, script);
    }

    private PipelineAggregationParams totalOutTransactionMoneyAgg() {
        Map<String, String> bucketsPathMap = new HashMap<>(16);
        bucketsPathMap.put("outTransactionMoneyPath", "outFilter.outTransactionMoneySum");
        String script = "Math.abs(params.outTransactionMoneyPath)";
        return new PipelineAggregationParams(IndividualPortraitAnalysisField.AggResultName.TOTAL_OUT_TRANSACTION_MONEY,
                AggsType.bucket_script.name(), bucketsPathMap, script);
    }

    private AggregationParams netTransactionMoneyAgg() {
        return AggregationParamsBuilders.sum(IndividualPortraitAnalysisField.AggResultName.NET_TRANSACTION_MONEY, IndividualPortraitAnalysisField.TRANSACTION_MONEY);
    }

    private AggregationParams earliestTradingTimeAgg() {
        return AggregationParamsBuilders.min(IndividualPortraitAnalysisField.AggResultName.EARLIEST_TRADING_TIME, IndividualPortraitAnalysisField.TRADING_TIME, null);
    }

    private AggregationParams latestTradingTimeAgg() {
        return AggregationParamsBuilders.max(IndividualPortraitAnalysisField.AggResultName.LATEST_TRADING_TIME, IndividualPortraitAnalysisField.TRADING_TIME, null);
    }

    private AggregationParams queryCardTopHits() {
        FetchSource fetchSource = new FetchSource();
        fetchSource.setIncludes(IndividualPortraitAnalysisField.Value.INCLUDES_TOP_HITS);
        fetchSource.setSize(1);
        fetchSource.setSort(new FieldSort(IndividualPortraitAnalysisField.TRADING_TIME, Direction.DESC.name()));
        return AggregationParamsBuilders.fieldSource(IndividualPortraitAnalysisField.AggResultName.QUERY_CARD_TOP_HITS, fetchSource);
    }

    private AggregationParams totalTransactionTimesAgg() {
        return AggregationParamsBuilders.count(IndividualPortraitAnalysisField.AggResultName.TOTAL_TRANSACTION_TIMES, IndividualPortraitAnalysisField.QUERY_CARD, null);
    }

    private class IndividualPortraitCommonParams {
        private AggregationParams customerIdentityCardTerms;
        private AggregationParams queryCardTerms;

        private AggregationParams getCustomerIdentityCardTerms() {
            return customerIdentityCardTerms;
        }

        private AggregationParams getQueryCardTerms() {
            return queryCardTerms;
        }

        private IndividualPortraitCommonParams invoke() {
            // 1 按身份证号分桶
            customerIdentityCardTerms = AggregationParamsBuilders.terms(IndividualPortraitAnalysisField.AggResultName.CUSTOMER_IDENTITY_CARD_TERMS,
                    IndividualPortraitAnalysisField.CUSTOMER_IDENTITY_CARD);
            // 1.1 按个人卡号分桶
            queryCardTerms = AggregationParamsBuilders.terms(IndividualPortraitAnalysisField.AggResultName.QUERY_CARD_TERMS,
                    IndividualPortraitAnalysisField.QUERY_CARD);
            // 1.1.1 单卡进账过滤
            queryCardTerms.setPerSubAggregation(loanFlagInFilterAgg());
            // 1.1.2 单卡出账过滤
            queryCardTerms.setPerSubAggregation(loanFlagOutFilterAgg());
            // 1.1.3 单卡交易净额
            queryCardTerms.setPerSubAggregation(netTransactionMoneyAgg());
            // 1.1.4 单卡交易总金额
            queryCardTerms.setPerSubAggregation(totalTransactionMoneyAgg());
            // 1.1.5 单卡交易进账总金额
            queryCardTerms.setPerSubAggregation(totalEntryTransactionMoneyAgg());
            // 1.1.6 单卡交易出账总金额
            queryCardTerms.setPerSubAggregation(totalOutTransactionMoneyAgg());
            // 1.1.7 单卡基本信息top_hits
            queryCardTerms.setPerSubAggregation(queryCardTopHits());
            return this;
        }
    }
}
