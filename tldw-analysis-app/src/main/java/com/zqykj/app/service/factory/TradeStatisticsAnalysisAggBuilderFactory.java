/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.app.service.field.TacticsAnalysisField;
import com.zqykj.app.service.vo.tarde_statistics.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.vo.Direction;
import com.zqykj.enums.AggsType;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.FetchSource;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
public class TradeStatisticsAnalysisAggBuilderFactory implements AggregationRequestParamFactory {

    // 管道聚合 buckets_path 引用标识
    private static final String BUCKET_SCRIPT_PARAM_PREFIX = "params.";
    private static final String PIPELINE_AGG_PATH_FLAG = ">";
    private static final String AGG_NAME_SPLIT = "_";


    public <T> AggregationParams createTradeStatisticsAnalysisQueryAgg(T param) {

        Map<String, String> mapping = new LinkedHashMap<>();
        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) param;
        String terms = AggsType.terms.name();
        String cardGroupName = TacticsAnalysisField.QUERY_CARD + AGG_NAME_SPLIT + terms;
        AggregationParams root = new AggregationParams(cardGroupName, terms, TacticsAnalysisField.QUERY_CARD);

        // 计算每个查询卡号的交易总金额
        String sum = AggsType.sum.name();
        String tradeMoneySum = TacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum;
        AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, sum, TacticsAnalysisField.TRANSACTION_MONEY);
        setSubAggregation(root, subTradeMoneySumAgg);

        // 计算每个查询卡号的交易总次数
        String count = AggsType.count.name();
        String tradeCount = TacticsAnalysisField.QUERY_CARD + AGG_NAME_SPLIT + count;
        AggregationParams subTradeTotalAgg = new AggregationParams(tradeCount, count, TacticsAnalysisField.QUERY_CARD);
        setSubAggregation(root, subTradeTotalAgg);

        // 计算每个查询卡号的入账数据过滤(该聚合能统计入账次数)
        String filter = AggsType.filter.name();
        String payIn = TacticsAnalysisField.LOAN_FLAG + AGG_NAME_SPLIT + filter + AGG_NAME_SPLIT + TacticsAnalysisField.LOAN_FLAG_IN_EN;
        QuerySpecialParams payInQuery = new QuerySpecialParams();
        payInQuery.setCommonQuery(new CommonQueryParams(QueryType.term, TacticsAnalysisField.LOAN_FLAG, TacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams subPayInAgg = new AggregationParams(payIn, filter, payInQuery);
        // 继续子聚合
        // 计算每个查询卡号的入账总金额

        String payInMoneySum = TacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum + AGG_NAME_SPLIT + TacticsAnalysisField.LOAN_FLAG_IN_EN;
        AggregationParams payInMoneySumAgg = new AggregationParams(payInMoneySum, sum, TacticsAnalysisField.TRANSACTION_MONEY);
        setSubAggregation(subPayInAgg, payInMoneySumAgg);
        setSubAggregation(root, subPayInAgg);

        // 计算每个查询卡号的 出账数据过滤(该聚合能统计出账次数)

        String payOut = TacticsAnalysisField.LOAN_FLAG + AGG_NAME_SPLIT + filter + AGG_NAME_SPLIT + TacticsAnalysisField.LOAN_FLAG_OUT_EN;
        QuerySpecialParams payOutQuery = new QuerySpecialParams();
        payOutQuery.setCommonQuery(new CommonQueryParams(QueryType.term, TacticsAnalysisField.LOAN_FLAG, TacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams subPayOutAgg = new AggregationParams(payOut, filter, payOutQuery);
        // 继续子聚合
        // 计算每个查询卡号的出账总金额
        String payOutMoneySum = TacticsAnalysisField.TRANSACTION_MONEY + AGG_NAME_SPLIT + sum + AGG_NAME_SPLIT + TacticsAnalysisField.LOAN_FLAG_OUT_EN;
        AggregationParams payOutMoneySumAgg = new AggregationParams(payOutMoneySum, sum, TacticsAnalysisField.TRANSACTION_MONEY);
        setSubAggregation(subPayOutAgg, payOutMoneySumAgg);
        setSubAggregation(root, subPayOutAgg);

        // 最早交易时间
        String min = AggsType.min.name();
        String tradeEarliestTime = TacticsAnalysisField.TRADING_TIME + AGG_NAME_SPLIT + min;
        AggregationParams tradeEarliestTimeAgg = new AggregationParams(tradeEarliestTime, min, TacticsAnalysisField.TRADING_TIME);
        setSubAggregation(root, tradeEarliestTimeAgg);

        // 最晚交易时间
        String max = AggsType.max.name();
        String tradeLatestTime = TacticsAnalysisField.TRADING_TIME + AGG_NAME_SPLIT + max;
        AggregationParams tradeLatestTimeAgg = new AggregationParams(tradeLatestTime, max, TacticsAnalysisField.TRADING_TIME);
        setSubAggregation(root, tradeLatestTimeAgg);

        // 管道聚合构建
        Map<String, String> tradeNetBucketsPath = new LinkedHashMap<>();
        tradeNetBucketsPath.put("credits_amount", payIn + PIPELINE_AGG_PATH_FLAG + payInMoneySum);
        tradeNetBucketsPath.put("pay_out_amount", payOut + PIPELINE_AGG_PATH_FLAG + payOutMoneySum);
        String tradeNetScript = BUCKET_SCRIPT_PARAM_PREFIX + "credits_amount" + "-" + BUCKET_SCRIPT_PARAM_PREFIX + "pay_out_amount";
        // 默认按交易总金额降序排序(如果没有指定的话)
        String sortPath = null;
        Direction direction = Direction.DESC;
        if (null == request.getSortRequest() || (request.getSortRequest() != null && StringUtils.isBlank(request.getSortRequest().getProperty()))) {
            sortPath = tradeMoneySum;
        }
        setSubPipelineAggregation(request, root, tradeNetBucketsPath, tradeNetScript, sortPath, direction);

        // 添加聚合桶中聚合需要显示的字段
        addTopHits(root);
        return root;
    }

    private void setSubAggregation(AggregationParams root, AggregationParams sub) {
        root.setPerSubAggregation(sub);
    }

    private void setSubPipelineAggregation(TradeStatisticalAnalysisQueryRequest request, AggregationParams root,
                                           Map<String, String> tradeNetBucketsPath, String tradeNetScript,
                                           String sortField, Direction direction) {


        // 计算交易净和
        String bucketScript = AggsType.bucket_script.name();
        PipelineAggregationParams tradeNetMoneyPipelineAgg =
                new PipelineAggregationParams(TradeStatisticsAnalysisAggName.TRADE_NET_AGG_NAME + AGG_NAME_SPLIT + bucketScript, bucketScript,
                        tradeNetBucketsPath, tradeNetScript);
        root.setPerPipelineAggregation(tradeNetMoneyPipelineAgg);

        // 对交易统计分析结果排序
        FieldSort fieldSort = new FieldSort(sortField, direction.name());
        Pagination pagination = new Pagination(request.getPageRequest().getPage(), request.getPageRequest().getPageSize());
        String bucketSort = AggsType.bucket_sort.name();
        PipelineAggregationParams tradeResultOrderAgg =
                new PipelineAggregationParams(TradeStatisticsAnalysisAggName.TRADE_RESULT_AGG_NAME + AGG_NAME_SPLIT + bucketSort,
                        bucketSort, Collections.singletonList(fieldSort), pagination);
        root.setPerPipelineAggregation(tradeResultOrderAgg);
    }

    private void addTopHits(AggregationParams root) {

        String topHits = AggsType.top_hits.name();
        FetchSource fetchSource = new FetchSource(TacticsAnalysisField.tradeStatisticalAggShowField());
        AggregationParams hitsAgg =
                new AggregationParams(TradeStatisticsAnalysisAggName.AGG_SHOW_FIELD + AGG_NAME_SPLIT + topHits,
                        topHits, fetchSource);
        root.setPerSubAggregation(hitsAgg);
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
}