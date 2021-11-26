package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.FundSourceAndDestinationAggRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.enums.FundsResultType;
import com.zqykj.common.enums.FundsSourceAndDestinationStatisticsType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.FundSourceAndDestinationCardResultRequest;
import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.common.request.SortingRequest;
import com.zqykj.common.vo.Direction;
import com.zqykj.core.aggregation.response.ElasticsearchAggregationResponseAttributes;
import com.zqykj.enums.AggsType;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.FetchSource;
import com.zqykj.parameters.aggregate.date.DateParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 *
 * @Description: 资金来源去向聚合工厂
 * @Author zhangkehou
 * @Date 2021/11/22
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundSourceAndDestinationAggFactory implements FundSourceAndDestinationAggRequestParamFactory {
    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;
    private static final String AGG_NAME_SPLIT = "_";
    private static final String DEFAULT_SORTING_FIELD = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + AggsType.sum.name();


    @Override
    public <T> AggregationParams buildFundsSourceTopNAgg(T request, FundsResultType fundsResultType) {


        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityAggKeyMapping = new LinkedHashMap<>();
        // 获取聚合Key 映射 , 聚合key 与 返回实体属性 映射
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggKeyMapping, entityAggKeyMapping, FundSourceAndDestinationBankRecord.class);

        /**
         * 分页和排序.
         * */
        FundsSourceAndDestinationStatisticsRequest topNRequest = (FundsSourceAndDestinationStatisticsRequest) request;
        String name = AggsType.multiTerms.name();
        String fields = FundTacticsAnalysisField.MERGE_IDENTITY_CARD;
//        aggKeyMapping.put(name, ElasticsearchAggregationResponseAttributes.keyAsString);
        AggregationParams root = new AggregationParams(name, AggsType.terms.name(), fields);

        // 设置聚合查询exclude
        Map<String, String[]> exclude = new HashMap<>();
        exclude.put("excludeValues", new String[]{topNRequest.getIdentityCard() + "-" + topNRequest.getIdentityCard()});
        root.setIncludeExclude(exclude);
        /**
         * 初始化script脚本.
         * 此script主要是用于分桶的结果数据进行,>大于0表示计算结果是统计来源的topN 小于0 则表示是计算去向的topN
         * */
        String script = FundTacticsAnalysisField.SELECTOR_SCRIPT_SOURCE;
        Pagination pagination = new Pagination(topNRequest.getQueryRequest().getPaging().getPage(), topNRequest.getQueryRequest().getPaging().getPageSize());
        // 默认按交易总金额降序排序(如果没有指定的话)
        String sortPath;
        Direction direction = Direction.DESC;
        /**
         * 当计算`去向`的时候，由于返回的结果都是负值,而返回的页面上统计的值都是负值的绝对值,此时负值约小反而大，反之亦反.
         *  eg：-3 和 -1 asc后 {-3,-1}.取绝对值后{3,1}是desc的.
         * */
        if (fundsResultType.equals(FundsResultType.DESTINATION) && topNRequest.getFundsSourceAndDestinationStatisticsType().
                equals(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT)) {
            direction = Direction.ASC;
        } else if (fundsResultType.equals(FundsResultType.DESTINATION) && topNRequest.getFundsSourceAndDestinationStatisticsType().
                equals(FundsSourceAndDestinationStatisticsType.NET)) {
            direction = Direction.ASC;
            script = FundTacticsAnalysisField.SELECTOR_SCRIPT_DESTINATION;

        }

        if (topNRequest.getFundsSourceAndDestinationStatisticsType().
                equals(FundsSourceAndDestinationStatisticsType.NET)) {
            /**
             * 过滤出聚合结果大于0或者小于0的数据，大于0代表来源数据,小于零代表去向数据.
             **/
            Map<String, String> bucketsPathMap = new HashMap<>();
            bucketsPathMap.put(FundTacticsAnalysisField.SUB_AGG_SUM_NAME, FundTacticsAnalysisField.TRANSACTION_MONEY_SUM);
            PipelineAggregationParams pipelineAggregationParams =
                    new PipelineAggregationParams(FundTacticsAnalysisField.PIPLINE_SELECTOR_BUCKET_NAME, AggsType.bucket_selector.name(),
                            bucketsPathMap, script);
            root.setPerSubAggregation(pipelineAggregationParams);
        }
        if (null == topNRequest.getQueryRequest().getSorting() || (topNRequest.getQueryRequest().getSorting() != null && StringUtils.isBlank(topNRequest.getQueryRequest().getSorting().getProperty()))) {
            sortPath = DEFAULT_SORTING_FIELD;
        } else {
            assert topNRequest.getQueryRequest().getSorting() != null;
            sortPath = topNRequest.getQueryRequest().getSorting().getProperty();
        }
        FieldSort fieldSort = new FieldSort(sortPath, direction.name());

        /**
         * 对桶数据排序
         * */
        String bucketSort = AggsType.bucket_sort.name();
        String sortAgg = TradeStatisticsAnalysisAggName.TRADE_RESULT_AGG_NAME + AGG_NAME_SPLIT + bucketSort;
        PipelineAggregationParams tradeResultOrderAgg = new PipelineAggregationParams(sortAgg, bucketSort, Collections.singletonList(fieldSort), pagination);
        root.setPerSubAggregation(tradeResultOrderAgg);

        setSubAggregations(root, aggKeyMapping);
        root.setMapping(aggKeyMapping);
        root.setEntityAggColMapping(entityAggKeyMapping);
        root.setResultName(FundTacticsAnalysisField.MULTI_IDENTITY_TERMS);
        return root;
    }

    @Override
    public <T> AggregationParams buildFundsSourceAndDestinationLineChartAgg(T request, FundsResultType type) {
        FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest = (FundsSourceAndDestinationStatisticsRequest) request;

        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityAggKeyMapping = new LinkedHashMap<>();
        // 获取聚合Key 映射 , 聚合key 与 返回实体属性 映射
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(
                aggKeyMapping, entityAggKeyMapping, fundsSourceAndDestinationStatisticsRequest
                .getFundsSourceAndDestinationStatisticsType()
                .equals(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT) ?
                FundSourceAndDestinationLineChart.class :
                FundSourceAndDestinationNetLineChart.class);
        // 这里可以自定义聚合名称的拼接方式
        String dateAggregateName = "date_histogram_" + FundTacticsAnalysisField.TRADING_TIME;
//        aggKeyMapping.put(dateAggregateName, ElasticsearchAggregationResponseAttributes.keyAsString);

        DateParams dateParams = new DateParams();
        String format = FundDateRequest.convertFromTimeType(fundsSourceAndDestinationStatisticsRequest.getDateType());
        dateParams.setFormat(format);
        // default
        dateParams.setMinDocCount(1);
        dateParams.addCalendarInterval(fundsSourceAndDestinationStatisticsRequest.getDateType());
        AggregationParams root = new AggregationParams(dateAggregateName, "date_histogram", FundTacticsAnalysisField.TRADING_TIME, dateParams);
        String script = FundTacticsAnalysisField.SELECTOR_SCRIPT_LINE_CHART;
        if (type.equals(FundsResultType.DESTINATION)) {
            script = FundTacticsAnalysisField.SELECTOR_SCRIPT_DESTINATION;
        }
        if (fundsSourceAndDestinationStatisticsRequest.getFundsSourceAndDestinationStatisticsType().equals(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT)) {
            // 本方入账次数
            QuerySpecialParams oppositeCreditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
            AggregationParams oppositeCreditsTimes = AggregationParamsBuilders.filter("opposite_credits_times", oppositeCreditsFilter, null);
            // 本方入账金额
            AggregationParams oppositeCreditsAmount = AggregationParamsBuilders.sum("opposite_credits_amount", FundTacticsAnalysisField.CHANGE_AMOUNT, null);
            setSubAggregation(oppositeCreditsTimes, oppositeCreditsAmount);
            setSubAggregation(root, oppositeCreditsTimes);

            // 本方出账次数
            QuerySpecialParams oppositeOutFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
            AggregationParams oppositeOutTimes = AggregationParamsBuilders.filter("opposite_out_times", oppositeOutFilter, null);
            // 本方出账金额
            AggregationParams oppositeOutAmount = AggregationParamsBuilders.sum("opposite_out_amount", FundTacticsAnalysisField.CHANGE_AMOUNT, null);
            setSubAggregation(oppositeOutTimes, oppositeOutAmount);
            setSubAggregation(root, oppositeOutTimes);
        } else {
            // 计算每个查询卡号的交易总金额
            String sum = AggsType.sum.name();
            String tradeMoneySum = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum;
            AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, sum, FundTacticsAnalysisField.TRANSACTION_MONEY);
//        mapping.put(tradeMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
            setSubAggregation(root, subTradeMoneySumAgg);

            // 桶数据过滤条件
            Map<String, String> bucketsPathMap = new HashMap<>();
            bucketsPathMap.put(FundTacticsAnalysisField.SUB_AGG_SUM_NAME, FundTacticsAnalysisField.TRANSACTION_MONEY_SUM);
            PipelineAggregationParams pipelineAggregationParams =
                    new PipelineAggregationParams(FundTacticsAnalysisField.PIPLINE_SELECTOR_BUCKET_NAME, AggsType.bucket_selector.name(),
                            bucketsPathMap, script);
            root.setPerSubAggregation(pipelineAggregationParams);
        }
        root.setMapping(aggKeyMapping);
        root.setEntityAggColMapping(entityAggKeyMapping);
        return root;
    }

    @Override
    public <T> AggregationParams buildFundsSourceAndDestinationResultListAgg(T request) {
        FundsSourceAndDestinationStatisticsRequest queryRequest = (FundsSourceAndDestinationStatisticsRequest) request;

        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityAggKeyMapping = new LinkedHashMap<>();
        // 获取聚合Key 映射 , 聚合key 与 返回实体属性 映射
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggKeyMapping, entityAggKeyMapping, FundSourceAndDestinationResultList.class);


        String name = AggsType.multiTerms.name();
        String fields = FundTacticsAnalysisField.MERGE_IDENTITY_CARD;
       // aggKeyMapping.put(name, ElasticsearchAggregationResponseAttributes.keyAsString);
        AggregationParams root = new AggregationParams(name, AggsType.terms.name(), fields);

        // 设置聚合查询exclude
        Map<String, String[]> exclude = new HashMap<>();
        exclude.put("excludeValues", new String[]{queryRequest.getIdentityCard() + "-" + queryRequest.getIdentityCard()});
        root.setIncludeExclude(exclude);

        AggregationParams oppositeTradeTotalTimes = AggregationParamsBuilders.count("opposite_trade_total",
                FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, null);
        setSubAggregation(root, oppositeTradeTotalTimes);
        // 本方交易总金额
        AggregationParams oppositeTradeTotalAmount = AggregationParamsBuilders.sum("opposite_trade_amount",
                FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        setSubAggregation(root, oppositeTradeTotalAmount);
        // 本方入账次数
        QuerySpecialParams oppositeCreditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams oppositeCreditsTimes = AggregationParamsBuilders.filter("opposite_credits_times", oppositeCreditsFilter, null);
        // 本方入账金额
        AggregationParams oppositeCreditsAmount = AggregationParamsBuilders.sum("opposite_credits_amount", FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        //
        setSubAggregation(oppositeCreditsTimes, oppositeCreditsAmount);
        //
        setSubAggregation(root, oppositeCreditsTimes);

        // 本方出账次数
        QuerySpecialParams oppositeOutFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams oppositeOutTimes = AggregationParamsBuilders.filter("opposite_out_times", oppositeOutFilter, null);
        // 本方出账金额
        AggregationParams oppositeOutAmount = AggregationParamsBuilders.sum("opposite_out_amount", FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        //
        setSubAggregation(oppositeOutTimes, oppositeOutAmount);
        //
        setSubAggregation(root, oppositeOutTimes);
        // 本方最早日期
        AggregationParams oppositeMinDate = AggregationParamsBuilders.min("opposite_min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(root, oppositeMinDate);
        // 本方最晚日期
        AggregationParams oppositeMaxDate = AggregationParamsBuilders.max("opposite_max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(root, oppositeMaxDate);
        // 本方交易净额
        Map<String, String> oppositeTradeNetPath = new HashMap<>();
        oppositeTradeNetPath.put("opposite_credits_amount", "opposite_credits_times>opposite_credits_amount");
        oppositeTradeNetPath.put("opposite_out_amount", "opposite_out_times>opposite_out_amount");
        String oppositeTradeNetScript = "params.opposite_credits_amount - params.opposite_out_amount";
        PipelineAggregationParams oppositeTradeNet = AggregationParamsBuilders.pipelineBucketScript("opposite_trade_net", oppositeTradeNetPath, oppositeTradeNetScript);
        root.setPerSubAggregation(oppositeTradeNet);
        // 本方排序
        PipelineAggregationParams oppositeSort =fundTacticsPartUniversalAggSort(queryRequest.getQueryRequest().getSorting()
                ,queryRequest.getQueryRequest().getPaging().getPage()
                ,queryRequest.getQueryRequest().getPaging().getPageSize());
        root.setPerSubAggregation(oppositeSort);

        String script = FundTacticsAnalysisField.SELECTOR_SCRIPT_LINE_CHART;
        if (queryRequest.getFundsResultType().equals(FundsResultType.DESTINATION)) {
            script = FundTacticsAnalysisField.SELECTOR_SCRIPT_DESTINATION;
        }
        if (queryRequest.getFundsSourceAndDestinationStatisticsType().equals(FundsSourceAndDestinationStatisticsType.NET)){
            // 计算每个查询卡号的交易总金额
            String sum = AggsType.sum.name();
            String tradeMoneySum = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum;
            AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, sum, FundTacticsAnalysisField.TRANSACTION_MONEY);
//        mapping.put(tradeMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
            setSubAggregation(root, subTradeMoneySumAgg);

            // 桶数据过滤条件
            Map<String, String> bucketsPathMap = new HashMap<>();
            bucketsPathMap.put(FundTacticsAnalysisField.SUB_AGG_SUM_NAME, FundTacticsAnalysisField.TRANSACTION_MONEY_SUM);
            PipelineAggregationParams pipelineAggregationParams =
                    new PipelineAggregationParams(FundTacticsAnalysisField.PIPLINE_SELECTOR_BUCKET_NAME, AggsType.bucket_selector.name(),
                            bucketsPathMap, script);
            root.setPerSubAggregation(pipelineAggregationParams);
        }
        // 本方聚合需要展示的字段
        FetchSource oppositeFetchSource = new FetchSource(FundTacticsAnalysisField.tradeStatisticalAnalysisOppositeResultShowField(), 0, 1);
        AggregationParams oppositeHits = AggregationParamsBuilders.fieldSource("opposite_hits", oppositeFetchSource);
        setSubAggregation(root, oppositeHits);
        root.setResultName("FundsSourceAndDestinationResult");
        root.setEntityAggColMapping(entityAggKeyMapping);
        root.setMapping(aggKeyMapping);
        return root;
    }


    private PipelineAggregationParams fundTacticsPartUniversalAggSort(SortingRequest sortRequest, int from, int size) {
        if (null != sortRequest) {
            List<FieldSort> fieldSorts = new ArrayList<>();
            String property = sortRequest.getProperty();
            SortingRequest.Direction order = sortRequest.getOrder();
            // 获取真实的聚合排序字段(开户名称、开户证件号码、开户银行、账号、交易卡号 不做排序,按照交易总金额排序处理)
            fieldSorts.add(new FieldSort(property, order.name()));
            return AggregationParamsBuilders.sort("sort", fieldSorts, from, size);
        }
        return null;
    }

    @Override
    public <T> AggregationParams buildFundsSourceAndDestinationCardResultListAgg(T request) {
        FundsSourceAndDestinationStatisticsRequest queryRequest = (FundsSourceAndDestinationStatisticsRequest) request;

        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityAggKeyMapping = new LinkedHashMap<>();
        // 获取聚合Key 映射 , 聚合key 与 返回实体属性 映射
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggKeyMapping, entityAggKeyMapping, FundSourceAndDestinationResultCardList.class);

        String name = AggsType.terms.name();
        String fields = FundTacticsAnalysisField.FLOW_ID;
//        aggKeyMapping.put(name, ElasticsearchAggregationResponseAttributes.keyAsString);
        AggregationParams root = new AggregationParams(name, AggsType.terms.name(), fields);

        AggregationParams oppositeTradeTotalTimes = AggregationParamsBuilders.count("opposite_trade_total",
                FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, null);
        setSubAggregation(root, oppositeTradeTotalTimes);
        // 本方交易总金额
        AggregationParams oppositeTradeTotalAmount = AggregationParamsBuilders.sum("opposite_trade_amount",
                FundTacticsAnalysisField.CHANGE_AMOUNT, null);
        setSubAggregation(root, oppositeTradeTotalAmount);
        // 本方入账次数
        QuerySpecialParams oppositeCreditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams oppositeCreditsTimes = AggregationParamsBuilders.filter("opposite_credits_times", oppositeCreditsFilter, null);
        // 本方入账金额
        AggregationParams oppositeCreditsAmount = AggregationParamsBuilders.sum("opposite_credits_amount", FundTacticsAnalysisField.CHANGE_AMOUNT, null);
        //
        setSubAggregation(oppositeCreditsTimes, oppositeCreditsAmount);
        //
        setSubAggregation(root, oppositeCreditsTimes);

        // 本方出账次数
        QuerySpecialParams oppositeOutFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams oppositeOutTimes = AggregationParamsBuilders.filter("opposite_out_times", oppositeOutFilter, null);
        // 本方出账金额
        AggregationParams oppositeOutAmount = AggregationParamsBuilders.sum("opposite_out_amount", FundTacticsAnalysisField.CHANGE_AMOUNT, null);
        //
        setSubAggregation(oppositeOutTimes, oppositeOutAmount);
        //
        setSubAggregation(root, oppositeOutTimes);
        // 本方最早日期
        AggregationParams oppositeMinDate = AggregationParamsBuilders.min("opposite_min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(root, oppositeMinDate);
        // 本方最晚日期
        AggregationParams oppositeMaxDate = AggregationParamsBuilders.max("opposite_max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(root, oppositeMaxDate);
        // 本方交易净额
        Map<String, String> oppositeTradeNetPath = new HashMap<>();
        oppositeTradeNetPath.put("opposite_credits_amount", "opposite_credits_times>opposite_credits_amount");
        oppositeTradeNetPath.put("opposite_out_amount", "opposite_out_times>opposite_out_amount");
        String oppositeTradeNetScript = "params.opposite_credits_amount - params.opposite_out_amount";
        PipelineAggregationParams oppositeTradeNet = AggregationParamsBuilders.pipelineBucketScript("opposite_trade_net", oppositeTradeNetPath, oppositeTradeNetScript);
        root.setPerSubAggregation(oppositeTradeNet);
        // 本方排序
        // 本方排序
        PipelineAggregationParams oppositeSort =fundTacticsPartUniversalAggSort(queryRequest.getQueryRequest().getSorting()
                ,queryRequest.getQueryRequest().getPaging().getPage()
                ,queryRequest.getQueryRequest().getPaging().getPageSize());
        root.setPerSubAggregation(oppositeSort);
        String script = FundTacticsAnalysisField.SELECTOR_SCRIPT_LINE_CHART;
        if (queryRequest.getFundsResultType().equals(FundsResultType.DESTINATION)) {
            script = FundTacticsAnalysisField.SELECTOR_SCRIPT_DESTINATION;
        }
        if (queryRequest.getFundsSourceAndDestinationStatisticsType().equals(FundsSourceAndDestinationStatisticsType.NET)){
            // 计算每个查询卡号的交易总金额
//            String sum = AggsType.sum.name();
//            String tradeMoneySum = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum;
//            AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, sum, FundTacticsAnalysisField.TRANSACTION_MONEY);
////        mapping.put(tradeMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
//            setSubAggregation(root, subTradeMoneySumAgg);

            // 桶数据过滤条件
            Map<String, String> bucketsPathMap = new HashMap<>();
            bucketsPathMap.put(FundTacticsAnalysisField.SUB_AGG_SUM_NAME, "opposite_trade_amount");
            PipelineAggregationParams pipelineAggregationParams =
                    new PipelineAggregationParams(FundTacticsAnalysisField.PIPLINE_SELECTOR_BUCKET_NAME, AggsType.bucket_selector.name(),
                            bucketsPathMap, script);
            root.setPerSubAggregation(pipelineAggregationParams);


        }
        // 本方聚合需要展示的字段
        FetchSource oppositeFetchSource = new FetchSource(FundTacticsAnalysisField.fundSourceAndDestinationAnalysisOppositeShowField(), 0, 1);
        AggregationParams oppositeHits = AggregationParamsBuilders.fieldSource("opposite_hits", oppositeFetchSource);
        setSubAggregation(root, oppositeHits);
        root.setResultName("FundsSourceAndDestinationCardResultList");
        root.setEntityAggColMapping(entityAggKeyMapping);
        root.setMapping(aggKeyMapping);
        return root;
    }

    @Override
    public <T> AggregationParams buildFundsSourceAndDestinationPieChartAgg(T request,FundsResultType fundsResultType) {
        FundsSourceAndDestinationStatisticsRequest queryRequest = (FundsSourceAndDestinationStatisticsRequest) request;
        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityAggKeyMapping = new LinkedHashMap<>();
        String name = AggsType.multiTerms.name();
        String fields = FundTacticsAnalysisField.MERGE_IDENTITY_CARD;
//        oppositeMapping.put(name, ElasticsearchAggregationResponseAttributes.keyAsString);
        AggregationParams root = new AggregationParams(name, AggsType.terms.name(), fields);

        // 计算每个查询卡号的交易总金额
        String sum = AggsType.sum.name();
        String tradeMoneySum = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum;
        AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, sum, FundTacticsAnalysisField.TRANSACTION_MONEY);
//        mapping.put(tradeMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
        setSubAggregation(root, subTradeMoneySumAgg);


        // 计算每个查询卡号的交易总金额
        String valueCount = AggsType.cardinality.name();
        AggregationParams cardinality = new AggregationParams(valueCount, valueCount, FundTacticsAnalysisField.OPPOSITE_IDENTITY_CARD_WILDCARD);
//        mapping.put(tradeMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
        setSubAggregation(root, cardinality);

        if (queryRequest.getFundsSourceAndDestinationStatisticsType().equals(FundsSourceAndDestinationStatisticsType.NET)){
            String script = FundTacticsAnalysisField.SELECTOR_SCRIPT_LINE_CHART;
            if(fundsResultType.equals(FundsResultType.DESTINATION)){
                script = FundTacticsAnalysisField.SELECTOR_SCRIPT_DESTINATION;
            }

            // 桶数据过滤条件
            Map<String, String> bucketsPathMap = new HashMap<>();
            bucketsPathMap.put(FundTacticsAnalysisField.SUB_AGG_SUM_NAME, FundTacticsAnalysisField.TRANSACTION_MONEY_SUM);
            PipelineAggregationParams pipelineAggregationParams =
                    new PipelineAggregationParams(FundTacticsAnalysisField.PIPLINE_SELECTOR_BUCKET_NAME, AggsType.bucket_selector.name(),
                            bucketsPathMap, script);
            root.setPerSubAggregation(pipelineAggregationParams);
        }

        // 桶数据过滤条件
        String bucketsPathMapSum = name +">"+valueCount;
        PipelineAggregationParams pipelineAggregationParamsBucketSum  =
                new PipelineAggregationParams(FundTacticsAnalysisField.PIPLINE_SUM_BUCKET, AggsType.sum_bucket.name(),
                        bucketsPathMapSum);
        aggKeyMapping.put(FundTacticsAnalysisField.PIPLINE_SUM_BUCKET,ElasticsearchAggregationResponseAttributes.valueAsString);
        root.addSiblingAggregation(pipelineAggregationParamsBucketSum,aggKeyMapping ,FundTacticsAnalysisField.PIPLINE_SUM_BUCKET);


        String bucketPathMap =name + ">"+tradeMoneySum;
        PipelineAggregationParams transactionNetAmount =
                new PipelineAggregationParams(FundTacticsAnalysisField.PIPLINE_TRANSACTION_NET_NUMBER, AggsType.sum_bucket.name(),
                        bucketPathMap);
        aggKeyMapping.put(FundTacticsAnalysisField.PIPLINE_TRANSACTION_NET_NUMBER,ElasticsearchAggregationResponseAttributes.valueAsString);
        root.addSiblingAggregation(transactionNetAmount,aggKeyMapping,FundTacticsAnalysisField.PIPLINE_TRANSACTION_NET_NUMBER);

        entityAggKeyMapping.put(FundTacticsAnalysisField.PIPLINE_SUM_BUCKET,"bucket_sum");
        entityAggKeyMapping.put(FundTacticsAnalysisField.PIPLINE_TRANSACTION_NET_NUMBER,"transaction_net_amount");
        root.setResultName("pieChart");


        // 设置聚合查询exclude
        Map<String, String[]> exclude = new HashMap<>();
        exclude.put("excludeValues", new String[]{queryRequest.getIdentityCard() + "-" + queryRequest.getIdentityCard()});
        root.setIncludeExclude(exclude);
        root.setEntityAggColMapping(aggKeyMapping);
        root.setMapping(entityAggKeyMapping);
        return root;
    }


    public void setSubAggregations(AggregationParams root, Map<String, String> mapping) {

        // 计算每个查询卡号的交易总金额
        String sum = AggsType.sum.name();
        String tradeMoneySum = FundTacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum;
        AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, sum, FundTacticsAnalysisField.TRANSACTION_MONEY);
//        mapping.put(tradeMoneySum, ElasticsearchAggregationResponseAttributes.valueAsString);
        setSubAggregation(root, subTradeMoneySumAgg);

        // 本方最早日期
        AggregationParams oppositeMinDate = AggregationParamsBuilders.min("opposite_min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(root, oppositeMinDate);
        // 本方最晚日期
        AggregationParams oppositeMaxDate = AggregationParamsBuilders.max("opposite_max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(root, oppositeMaxDate);
        // 本方聚合需要展示的字段
        FetchSource oppositeFetchSource = new FetchSource(FundTacticsAnalysisField.tradeStatisticalAnalysisOppositeShowField(), 0, 1);
        AggregationParams oppositeHits = AggregationParamsBuilders.fieldSource("opposite_hits", oppositeFetchSource);
        setSubAggregation(root, oppositeHits);
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
     * <h2>  交易统计分析部分自定义聚合名称 </h2>
     */
    interface TradeStatisticsAnalysisAggName {
        // 交易净额计算
        String TRADE_NET_AGG_NAME = "trade_net";
        // 交易结果 根据某个指标值排序
        String TRADE_RESULT_AGG_NAME = "trade_result_order";


    }
}
