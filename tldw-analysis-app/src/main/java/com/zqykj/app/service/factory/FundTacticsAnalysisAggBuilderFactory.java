/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.app.service.factory.builder.AggregationParamsBuilders;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.Local;
import com.zqykj.app.service.vo.fund.Opposite;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisBankFlow;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.util.ReflectionUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.AssetTrendsRequest;
import com.zqykj.common.vo.Direction;
import com.zqykj.core.aggregation.response.ElasticsearchAggregationResponseAttributes;
import com.zqykj.core.aggregation.factory.AggregateRequestFactory;
import com.zqykj.enums.AggsType;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.FetchSource;
import com.zqykj.parameters.aggregate.date.DateParams;
import com.zqykj.parameters.aggregate.date.DateSpecificFormat;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private static final String AGG_NAME_TOTAL = "total";
    private static final String DEFAULT_SORTING_FIELD = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + AggsType.sum.name();

    @Deprecated
    public <T> List<AggregationParams> createTradeStatisticsAnalysisQueryAgg(T request) {

        return null;
    }

    /**
     * <h2> 构建交易统计分析本方查询卡号分组聚合 </h2>
     */
    public <T> AggregationParams buildTradeStatisticsAnalysisQueryCardAgg(T request) {

        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        // 如果是全部查询条件,需要增加过滤条件
        AggregationParams localFilter = null;
        if (CollectionUtils.isEmpty(queryRequest.getCardNums()) || StringUtils.isBlank(queryRequest.getIdentityCard())) {

            // 本方聚合的时候调单卡号的过滤条件
            QuerySpecialParams querySpecialParams = new QuerySpecialParams();
            querySpecialParams.addCombiningQueryParams(queryBuilderFactory.assembleLocalFuzzy(queryRequest.getKeyword()));
            localFilter = new AggregationParams("local_filter", AggsType.filter.name(), querySpecialParams);
        }
        // 查询卡号分组
        AggregationParams queryCardTerms;
        if (CollectionUtils.isEmpty(queryRequest.getCardNums())) {
            queryCardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.QUERY_CARD);
        } else {
            String[] cardNums = queryRequest.getCardNums().toArray(new String[0]);
            queryCardTerms = AggregationParamsBuilders.terms("local_card_terms", FundTacticsAnalysisField.QUERY_CARD, cardNums);
        }
        if (null != localFilter) {

            // 过滤出调单卡号之后再去group by
            setSubAggregation(localFilter, queryCardTerms);
        }

        // 下面聚合都在是该分组之下(属于子聚合)
        // 本方交易总次数
        AggregationParams localTradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.QUERY_CARD, null);
        setSubAggregation(queryCardTerms, localTradeTotalTimes);
        // 本方交易总金额
        AggregationParams localTradeTotalAmount = AggregationParamsBuilders.sum("local_trade_amount",
                FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        setSubAggregation(queryCardTerms, localTradeTotalAmount);
        // 本方入账次数
        QuerySpecialParams localCreditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams localCreditsTimes = AggregationParamsBuilders.filter("local_credits_times", localCreditsFilter, null);
        // 本方入账金额
        AggregationParams localCreditsAmount = AggregationParamsBuilders.sum("local_credits_amount", FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        //
        setSubAggregation(localCreditsTimes, localCreditsAmount);
        //
        setSubAggregation(queryCardTerms, localCreditsTimes);

        // 本方出账次数
        QuerySpecialParams localOutFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams localOutTimes = AggregationParamsBuilders.filter("local_out_times", localOutFilter, null);
        // 本方出账金额
        AggregationParams localOutAmount = AggregationParamsBuilders.sum("local_out_amount", FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        //
        setSubAggregation(localOutTimes, localOutAmount);
        //
        setSubAggregation(queryCardTerms, localOutTimes);
        // 本方最早日期
        AggregationParams localMinDate = AggregationParamsBuilders.min("local_min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(queryCardTerms, localMinDate);
        // 本方最晚日期
        AggregationParams localMaxDate = AggregationParamsBuilders.max("local_max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(queryCardTerms, localMaxDate);
        // 本方交易净额
        Map<String, String> localTradeNetPath = new HashMap<>();
        localTradeNetPath.put("local_credits_amount", "local_credits_times>local_credits_amount");
        localTradeNetPath.put("local_out_amount", "local_out_times>local_out_amount");
        String tradeNetScript = "params.local_credits_amount - params.local_out_amount";
        PipelineAggregationParams localTradeNet = AggregationParamsBuilders.pipelineBucketScript("local_trade_net", localTradeNetPath, tradeNetScript);
        queryCardTerms.setPerSubAggregation(localTradeNet);
        // 本方排序
        if (null != queryRequest.getSortRequest()) {
            List<FieldSort> fieldSorts = new ArrayList<>();
            String property = queryRequest.getSortRequest().getProperty();
            String order = "DESC";
            Local local = ReflectionUtils.findRequiredField(TradeStatisticalAnalysisBankFlow.class, property).getAnnotation(Local.class);
            if (null == local || StringUtils.isBlank(local.sortName())) {

                // 这加个排序字段 是属于 索引中字段, 聚合中无法排序(聚合只能根据某个聚合下的度量值排序,必须是数值类型的)
                // 因此还是默认按照交易统计金额排序
                property = "local_trade_amount";
            } else {
                String sortName = local.sortName();
                if (StringUtils.isBlank(sortName)) {
                    property = "local_trade_amount";
                } else {
                    property = sortName;
                    order = queryRequest.getSortRequest().getOrder().name();
                }
            }
            // 获取真实的聚合排序字段(开户名称、开户证件号码、开户银行、账号、交易卡号 不做排序,按照交易总金额排序处理)
            fieldSorts.add(new FieldSort(property, order));
            PipelineAggregationParams localSort = AggregationParamsBuilders.sort("local_sort", fieldSorts, queryRequest.getGroupInitPage(), queryRequest.getGroupInitSize());
            queryCardTerms.setPerSubAggregation(localSort);
        } else {
            // 默认排序
            FieldSort fieldSort = new FieldSort("local_trade_amount", "DESC");
            PipelineAggregationParams localSort = AggregationParamsBuilders.sort("local_sort", Collections.singletonList(fieldSort), queryRequest.getGroupInitPage(), queryRequest.getGroupInitSize());
            queryCardTerms.setPerSubAggregation(localSort);
        }
        // 本方聚合需要展示的字段
        FetchSource localFetchSource = new FetchSource(FundTacticsAnalysisField.tradeStatisticalAnalysisLocalShowField(), 0, 1);
        AggregationParams localHits = AggregationParamsBuilders.fieldSource("local_hits", localFetchSource);
        setSubAggregation(queryCardTerms, localHits);
        return null == localFilter ? queryCardTerms : localFilter;
    }

    /**
     * <h2> 构建对方查询卡号分组聚合 </h2>
     */
    public <T> AggregationParams buildTradeStatisticsAnalysisOppositeCardAgg(T request) {

        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        // 如果是全部查询条件,需要增加过滤条件
        AggregationParams oppositeFilter = null;
        if (CollectionUtils.isEmpty(queryRequest.getCardNums()) || StringUtils.isBlank(queryRequest.getIdentityCard())) {

            // 本方聚合的时候调单卡号的过滤条件
            QuerySpecialParams querySpecialParams = new QuerySpecialParams();
            querySpecialParams.addCombiningQueryParams(queryBuilderFactory.assembleOppositeFuzzy(queryRequest.getKeyword()));
            oppositeFilter = new AggregationParams("opposite_filter", AggsType.filter.name(), querySpecialParams);
        }
        AggregationParams oppositeCardTerms;
        // 查询卡号分组
        if (CollectionUtils.isEmpty(queryRequest.getCardNums())) {

            oppositeCardTerms = AggregationParamsBuilders.terms("opposite_card_terms", FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD);
        } else {
            String[] cardNums = queryRequest.getCardNums().toArray(new String[0]);
            oppositeCardTerms = AggregationParamsBuilders.terms("opposite_card_terms", FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, cardNums);
        }

        // 过滤出调单卡号之后分组
        if (null != oppositeFilter) {

            setSubAggregation(oppositeFilter, oppositeCardTerms);
        }

        // 下面聚合都在是该分组之下(属于子聚合)
        // 本方交易总次数
        AggregationParams oppositeTradeTotalTimes = AggregationParamsBuilders.count("opposite_trade_total",
                FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, null);
        setSubAggregation(oppositeCardTerms, oppositeTradeTotalTimes);
        // 本方交易总金额
        AggregationParams oppositeTradeTotalAmount = AggregationParamsBuilders.sum("opposite_trade_amount",
                FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        setSubAggregation(oppositeCardTerms, oppositeTradeTotalAmount);
        // 本方入账次数
        QuerySpecialParams oppositeCreditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams oppositeCreditsTimes = AggregationParamsBuilders.filter("opposite_credits_times", oppositeCreditsFilter, null);
        // 本方入账金额
        AggregationParams oppositeCreditsAmount = AggregationParamsBuilders.sum("opposite_credits_amount", FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        //
        setSubAggregation(oppositeCreditsTimes, oppositeCreditsAmount);
        //
        setSubAggregation(oppositeCardTerms, oppositeCreditsTimes);

        // 本方出账次数
        QuerySpecialParams oppositeOutFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams oppositeOutTimes = AggregationParamsBuilders.filter("opposite_out_times", oppositeOutFilter, null);
        // 本方出账金额
        AggregationParams oppositeOutAmount = AggregationParamsBuilders.sum("opposite_out_amount", FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        //
        setSubAggregation(oppositeOutTimes, oppositeOutAmount);
        //
        setSubAggregation(oppositeCardTerms, oppositeOutTimes);
        // 本方最早日期
        AggregationParams oppositeMinDate = AggregationParamsBuilders.min("opposite_min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(oppositeCardTerms, oppositeMinDate);
        // 本方最晚日期
        AggregationParams oppositeMaxDate = AggregationParamsBuilders.max("opposite_max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(oppositeCardTerms, oppositeMaxDate);
        // 本方交易净额
        Map<String, String> oppositeTradeNetPath = new HashMap<>();
        oppositeTradeNetPath.put("opposite_credits_amount", "opposite_credits_times>opposite_credits_amount");
        oppositeTradeNetPath.put("opposite_out_amount", "opposite_out_times>opposite_out_amount");
        String oppositeTradeNetScript = "params.opposite_credits_amount - params.opposite_out_amount";
        PipelineAggregationParams oppositeTradeNet = AggregationParamsBuilders.pipelineBucketScript("opposite_trade_net", oppositeTradeNetPath, oppositeTradeNetScript);
        oppositeCardTerms.setPerSubAggregation(oppositeTradeNet);
        // 本方排序
        if (null != queryRequest.getSortRequest()) {
            List<FieldSort> fieldSorts = new ArrayList<>();
            String property = queryRequest.getSortRequest().getProperty();
            String order = "DESC";
            Opposite opposite = ReflectionUtils.findRequiredField(TradeStatisticalAnalysisBankFlow.class, property).getAnnotation(Opposite.class);
            if (null == opposite || StringUtils.isBlank(opposite.sortName())) {

                // 这加个排序字段 是属于 索引中字段, 聚合中无法排序(聚合只能根据某个聚合下的度量值排序,必须是数值类型的)
                // 因此还是默认按照交易统计金额排序
                property = "opposite_trade_amount";
            } else {
                String sortName = opposite.sortName();
                if (StringUtils.isBlank(sortName)) {
                    property = "opposite_trade_amount";
                } else {
                    property = sortName;
                }
                order = queryRequest.getSortRequest().getOrder().name();
            }
            fieldSorts.add(new FieldSort(property, order));
            PipelineAggregationParams oppositeSort = AggregationParamsBuilders.sort("opposite_sort", fieldSorts, queryRequest.getGroupInitPage(), queryRequest.getGroupInitSize());
            oppositeCardTerms.setPerSubAggregation(oppositeSort);
        } else {
            // 默认排序
            FieldSort fieldSort = new FieldSort("opposite_trade_amount", "DESC");
            PipelineAggregationParams oppositeSort = AggregationParamsBuilders.sort("opposite_sort", Collections.singletonList(fieldSort), queryRequest.getGroupInitPage(), queryRequest.getGroupInitSize());
            oppositeCardTerms.setPerSubAggregation(oppositeSort);
        }
        // 本方聚合需要展示的字段
        FetchSource oppositeFetchSource = new FetchSource(FundTacticsAnalysisField.tradeStatisticalAnalysisOppositeShowField(), 0, 1);
        AggregationParams oppositeHits = AggregationParamsBuilders.fieldSource("opposite_hits", oppositeFetchSource);
        setSubAggregation(oppositeCardTerms, oppositeHits);
        return null == oppositeFilter ? oppositeCardTerms : oppositeFilter;
    }


    public <T> AggregationParams buildTradeStatisticsAnalysisTotalAgg(T request) {

        TradeStatisticalAnalysisQueryRequest queryRequest = (TradeStatisticalAnalysisQueryRequest) request;

        // 前置过滤条件
        CombinationQueryParams combinationOne = new CombinationQueryParams();
        combinationOne.setType(ConditionType.must);
        // 本方查询卡号(有可能是查询全部,那么卡号不为空的时候才能选用此条件)
        if (!CollectionUtils.isEmpty(queryRequest.getCardNums())) {
            combinationOne.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.QUERY_CARD, queryRequest.getCardNums()));
        }
        // 本方需要的模糊匹配
        combinationOne.addCommonQueryParams(new CommonQueryParams(queryBuilderFactory.assembleLocalFuzzy(queryRequest.getKeyword())));
        QuerySpecialParams prefixFilter = new QuerySpecialParams();
        prefixFilter.addCombiningQueryParams(combinationOne);

        AggregationParams root = new AggregationParams("total", AggsType.filter.name(), prefixFilter);
        AggregationParams total;
        total = new AggregationParams("cardinality_total", AggsType.cardinality.name(), FundTacticsAnalysisField.QUERY_CARD);
        setSubAggregation(root, total);
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
        DateSpecificFormat specificFormat = AggregateRequestFactory.convertFromTimeType(assetTrendsRequest.getDateType());
        dateParams.setFormat(specificFormat.getFormat());
        // default
        dateParams.setMinDocCount(1);
        if (StringUtils.isNotBlank(specificFormat.getFixedInterval()) && StringUtils.isNotBlank(specificFormat.getCalendarInterval())) {

            throw new IllegalArgumentException("Cannot use [fixedInterval] with [calendarInterval] configuration option.");
        } else if (StringUtils.isNotBlank(specificFormat.getFixedInterval())) {
            dateParams.setFixedInterval(specificFormat.getFixedInterval());
        } else {

            dateParams.setCalendarInterval(specificFormat.getCalendarInterval());
        }
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

    public <T> AggregationParams getTotal(T field) {
        String cardinality = AggsType.cardinality.name();
        return new AggregationParams(AGG_NAME_TOTAL, cardinality, field.toString());
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

    /**
     * <h2>  交易统计分析部分自定义聚合名称 </h2>
     */
    interface TradeStatisticsAnalysisAggName {
        // 交易净额计算
        String TRADE_NET_AGG_NAME = "trade_net";
        // 交易结果 根据某个指标值排序
        String TRADE_RESULT_AGG_NAME = "trade_result_order";
    }
}
