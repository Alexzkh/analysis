/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisBankFlow;
import com.zqykj.app.service.vo.fund.TradeStatisticalAnalysisQueryRequest;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class FundTacticsAnalysisAggBuilderFactory implements AggregationRequestParamFactory {

    // 管道聚合 buckets_path 引用标识
    private static final String BUCKET_SCRIPT_PARAM_PREFIX = "params.";
    private static final String PIPELINE_AGG_PATH_FLAG = ">";

    private static final String AGG_NAME_SPLIT = "_";
    private static final String AGG_NAME_TOTAL = "total";
    private static final String DEFAULT_SORTING_FIELD = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + AggsType.sum.name();

    public <T> AggregationParams createTradeStatisticsAnalysisQueryAgg(T param) {

        // 需要取出的聚合结果值 key: 聚合名称 value: 聚合属性
        Map<String, String> mapping = new LinkedHashMap<>();

        // 根据查询卡号聚合
        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) param;
        String terms = AggsType.terms.name();

        String cardGroupName = FundTacticsAnalysisField.QUERY_CARD + AGG_NAME_SPLIT + terms;
        AggregationParams root = new AggregationParams(cardGroupName, terms, FundTacticsAnalysisField.QUERY_CARD);
        // 最早交易时间
        String min = AggsType.min.name();
        String tradeEarliestTime = FundTacticsAnalysisField.TRADING_TIME + AGG_NAME_SPLIT + min;
        AggregationParams tradeEarliestTimeAgg = new AggregationParams(tradeEarliestTime, min, FundTacticsAnalysisField.TRADING_TIME);
        mapping.put(tradeEarliestTime, ElasticsearchAggregationResponseAttributes.valueAsString);
        setSubAggregation(root, tradeEarliestTimeAgg);

        // 最晚交易时间
        String max = AggsType.max.name();
        String tradeLatestTime = FundTacticsAnalysisField.TRADING_TIME + AGG_NAME_SPLIT + max;
        AggregationParams tradeLatestTimeAgg = new AggregationParams(tradeLatestTime, max, FundTacticsAnalysisField.TRADING_TIME);
        mapping.put(tradeLatestTime, ElasticsearchAggregationResponseAttributes.valueAsString);
        setSubAggregation(root, tradeLatestTimeAgg);

        // 默认按交易总金额降序排序(如果没有指定的话)
        Pagination pagination = new Pagination(request.getPageRequest().getPage(), request.getPageRequest().getPageSize());

        // 默认按交易总金额降序排序(如果没有指定的话)
        String sortPath;
        Direction direction = Direction.DESC;
        if (null == request.getSortRequest() || (request.getSortRequest() != null && StringUtils.isBlank(request.getSortRequest().getProperty()))) {
            sortPath = DEFAULT_SORTING_FIELD;
        } else {
            assert request.getSortRequest() != null;
            sortPath = request.getSortRequest().getProperty();
        }
        FieldSort fieldSort = new FieldSort(sortPath, direction.name());

        // 在父聚合中的添加子聚合
        addSubAggregationParams(root, pagination, fieldSort, mapping);

        // 添加聚合桶中聚合需要显示的字段
        addTopHits(root, mapping);

        // 设置同级聚合(需要统计出当前查询卡号去重的数量)
        AggregationParams total = getTotal(FundTacticsAnalysisField.QUERY_CARD);
        mapping.put(total.getName(), ElasticsearchAggregationResponseAttributes.value);
        root.setSiblingAggregation(total);
        root.setMapping(mapping);

        // 战法交易统计分析结果 查询   聚合名称 与 结果表头属性映射
        Map<String, String> entityColMapping = getTradeStatisticsAnalysisEntityColMapping();
        root.setEntityAggColMapping(entityColMapping);
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
        root.setPerPipelineAggregation(tradeNetMoneyPipelineAgg);

        // 对交易统计分析结果排序
        String bucketSort = AggsType.bucket_sort.name();
        String sortAgg = TradeStatisticsAnalysisAggName.TRADE_RESULT_AGG_NAME + AGG_NAME_SPLIT + bucketSort;
        PipelineAggregationParams tradeResultOrderAgg = new PipelineAggregationParams(sortAgg, bucketSort, Collections.singletonList(fieldSort), pagination);
        root.setPerPipelineAggregation(tradeResultOrderAgg);
    }

    /**
     * 增加管道聚合,此功能用于获取聚合显示source中的字段.
     *
     * @param root: 父聚合参数.
     * @return: void
     **/
    private void addTopHits(AggregationParams root, Map<String, String> mapping) {

        String topHits = AggsType.top_hits.name();
        FetchSource fetchSource = new FetchSource(FundTacticsAnalysisField.tradeStatisticalAggShowField());
        String hits = TradeStatisticsAnalysisAggName.AGG_SHOW_FIELD + AGG_NAME_SPLIT + topHits;
        AggregationParams hitsAgg = new AggregationParams(hits, topHits, fetchSource);
        mapping.put(hits, ElasticsearchAggregationResponseAttributes.hits);
        root.setPerSubAggregation(hitsAgg);
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
        // 聚合结果需要展示的 字段名称
        String AGG_SHOW_FIELD = "show_fields";
    }


    /**
     * <h1> 交易统计分析聚合结果查询(聚合名称 与 实体属性 映射) 后续战法结果不会再做此映射 (后续将把聚合名称直接改成需要返回的属性名称)</h1>
     */
    private Map<String, String> getTradeStatisticsAnalysisEntityColMapping() {

        // key: 聚合名称 value: 实体属性
        Map<String, String> entityMapping = new HashMap<>();

        String min = AggsType.min.name();
        String max = AggsType.max.name();
        String sum = AggsType.sum.name();
        String count = AggsType.count.name();
        String filter = AggsType.filter.name();
        String bucketScript = AggsType.bucket_script.name();
        String topHits = AggsType.top_hits.name();
        String earliestTradingTime = TradeStatisticalAnalysisBankFlow.EntityMapping.earliestTradingTime.name();
        String latestTradingTime = TradeStatisticalAnalysisBankFlow.EntityMapping.latestTradingTime.name();
        String tradeTotalAmount = TradeStatisticalAnalysisBankFlow.EntityMapping.tradeTotalAmount.name();
        String tradeTotalTimes = TradeStatisticalAnalysisBankFlow.EntityMapping.tradeTotalTimes.name();
        String creditsTimes = TradeStatisticalAnalysisBankFlow.EntityMapping.creditsTimes.name();
        String creditsAmount = TradeStatisticalAnalysisBankFlow.EntityMapping.creditsAmount.name();
        String payOutTimes = TradeStatisticalAnalysisBankFlow.EntityMapping.payOutTimes.name();
        String payOutAmount = TradeStatisticalAnalysisBankFlow.EntityMapping.payOutAmount.name();
        String tradeNet = TradeStatisticalAnalysisBankFlow.EntityMapping.tradeNet.name();
        String source = TradeStatisticalAnalysisBankFlow.EntityMapping.source.name();

        // 最早交易时间 (TacticsAnalysisField.TRADING_TIME + AGG_NAME_SPLIT + min 来自 createTradeStatisticsAnalysisQueryAgg() 方法中描述的聚合名称)
        entityMapping.put(FundTacticsAnalysisField.TRADING_TIME + AGG_NAME_SPLIT + min, earliestTradingTime);
        // 最晚交易时间
        entityMapping.put(FundTacticsAnalysisField.TRADING_TIME + AGG_NAME_SPLIT + max, latestTradingTime);
        // 每张查询卡号的交易总金额
        entityMapping.put(FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum, tradeTotalAmount);
        // 每张查询卡号的交易总次数
        entityMapping.put(FundTacticsAnalysisField.QUERY_CARD + AGG_NAME_SPLIT + count, tradeTotalTimes);
        // 每张查询卡号的入账次数
        entityMapping.put(FundTacticsAnalysisField.LOAN_FLAG + AGG_NAME_SPLIT + filter + AGG_NAME_SPLIT + FundTacticsAnalysisField.LOAN_FLAG_IN_EN,
                creditsTimes);
        // 每张查询卡号的入账金额
        entityMapping.put(FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum + AGG_NAME_SPLIT + FundTacticsAnalysisField.LOAN_FLAG_IN_EN,
                creditsAmount);
        // 每张查询卡号的出账次数
        entityMapping.put(FundTacticsAnalysisField.LOAN_FLAG + AGG_NAME_SPLIT + filter + AGG_NAME_SPLIT + FundTacticsAnalysisField.LOAN_FLAG_OUT_EN,
                payOutTimes);
        // 每张查询卡号的出账金额
        entityMapping.put(FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum + AGG_NAME_SPLIT + FundTacticsAnalysisField.LOAN_FLAG_OUT_EN,
                payOutAmount);

        // 交易净和
        entityMapping.put(TradeStatisticsAnalysisAggName.TRADE_NET_AGG_NAME + AGG_NAME_SPLIT + bucketScript, tradeNet);

        // 聚合中需要展示的字段
        entityMapping.put(TradeStatisticsAnalysisAggName.AGG_SHOW_FIELD + AGG_NAME_SPLIT + topHits, source);

        // 数据总量
        entityMapping.put(AGG_NAME_TOTAL, AGG_NAME_TOTAL);

        return entityMapping;
    }
}
