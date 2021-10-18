/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory;

import com.zqykj.app.service.field.TacticsAnalysisField;
import com.zqykj.app.service.vo.tarde_statistics.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.vo.Direction;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.FetchSource;
import com.zqykj.parameters.aggregate.date.DateParams;
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

    public <T> AggregationParams createTradeStatisticsAnalysisQueryAgg(T param) {

        TradeStatisticalAnalysisQueryRequest request = (TradeStatisticalAnalysisQueryRequest) param;
        String cardGroupName = "terms_" + TacticsAnalysisField.QUERY_CARD;
        AggregationParams root = new AggregationParams(cardGroupName, "terms", TacticsAnalysisField.QUERY_CARD);

        // 计算每个查询卡号的交易总金额
        String tradeMoneySum = "sum_" + TacticsAnalysisField.TRANSACTION_MONEY;
        AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, "sum", TacticsAnalysisField.TRANSACTION_MONEY);
        setSubAggregation(root, subTradeMoneySumAgg);

        // 计算每个查询卡号的交易总次数
        String tradeCount = "count_" + TacticsAnalysisField.QUERY_CARD;
        AggregationParams subTradeTotalAgg = new AggregationParams(tradeCount, "value_count", TacticsAnalysisField.QUERY_CARD);
        setSubAggregation(root, subTradeTotalAgg);

        // 计算每个查询卡号的入账数据过滤(该聚合能统计入账次数)
        String payIn = "filter_pay_in_" + TacticsAnalysisField.LOAN_FLAG;
        QuerySpecialParams payInQuery = new QuerySpecialParams();
        payInQuery.setCommonQuery(new CommonQueryParams(QueryType.term, TacticsAnalysisField.LOAN_FLAG, TacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams subPayInAgg = new AggregationParams(payIn, "filter", payInQuery);
        // 继续子聚合
        // 计算每个查询卡号的入账总金额
        String payInMoneySum = "sum_pay_in_" + TacticsAnalysisField.TRANSACTION_MONEY;
        AggregationParams payInMoneySumAgg = new AggregationParams(payInMoneySum, "sum", TacticsAnalysisField.TRANSACTION_MONEY);
        setSubAggregation(subPayInAgg, payInMoneySumAgg);
        setSubAggregation(root, subPayInAgg);

        // 计算每个查询卡号的 出账数据过滤(该聚合能统计出账次数)

        String payOut = "filter_pay_out_" + TacticsAnalysisField.LOAN_FLAG;
        QuerySpecialParams payOutQuery = new QuerySpecialParams();
        payOutQuery.setCommonQuery(new CommonQueryParams(QueryType.term, TacticsAnalysisField.LOAN_FLAG, TacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams subPayOutAgg = new AggregationParams(payOut, "filter", payOutQuery);
        // 继续子聚合
        // 计算每个查询卡号的出账总金额
        String payOutMoneySum = "sum_pay_out" + TacticsAnalysisField.TRANSACTION_MONEY;
        AggregationParams payOutMoneySumAgg = new AggregationParams(payOutMoneySum, "sum", TacticsAnalysisField.TRANSACTION_MONEY);
        setSubAggregation(subPayOutAgg, payOutMoneySumAgg);
        setSubAggregation(root, subPayOutAgg);

        // 最早交易时间
        String tradeEarliestTime = "min_" + TacticsAnalysisField.TRADING_TIME;
        AggregationParams tradeEarliestTimeAgg = new AggregationParams(tradeEarliestTime, "min", TacticsAnalysisField.TRADING_TIME);
        setSubAggregation(root, tradeEarliestTimeAgg);

        // 最晚交易时间
        String tradeLatestTime = "max_" + TacticsAnalysisField.TRADING_TIME;
        AggregationParams tradeLatestTimeAgg = new AggregationParams(tradeLatestTime, "max", TacticsAnalysisField.TRADING_TIME);
        setSubAggregation(root, tradeLatestTimeAgg);

        // 管道聚合构建
        Map<String, String> tradeNetBucketsPath = new LinkedHashMap<>();
        tradeNetBucketsPath.put("pay_in_total", payIn + PIPELINE_AGG_PATH_FLAG + payInMoneySum);
        tradeNetBucketsPath.put("pay_out_total", payOut + PIPELINE_AGG_PATH_FLAG + payOutMoneySum);
        String tradeNetScript = BUCKET_SCRIPT_PARAM_PREFIX + "pay_in_total" + "-" + BUCKET_SCRIPT_PARAM_PREFIX + "pay_out_total";
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
        PipelineAggregationParams tradeNetMoneyPipelineAgg = new PipelineAggregationParams("tradeNetMoney", "bucket_script",
                tradeNetBucketsPath, tradeNetScript);
        root.setPerPipelineAggregation(tradeNetMoneyPipelineAgg);

        // 对交易统计分析结果排序
        FieldSort fieldSort = new FieldSort(sortField, direction.name());
        Pagination pagination = new Pagination(request.getPageRequest().getPage(), request.getPageRequest().getPageSize());
        PipelineAggregationParams tradeResultOrderAgg = new PipelineAggregationParams("trade_result_order",
                "bucket_sort", Collections.singletonList(fieldSort), pagination);
        root.setPerPipelineAggregation(tradeResultOrderAgg);
    }

    private void addTopHits(AggregationParams root) {

        FetchSource fetchSource = new FetchSource(TacticsAnalysisField.tradeStatisticalAggShowField());
        AggregationParams hitsAgg = new AggregationParams("agg_show_fields", "top_hits", fetchSource);
        root.setPerSubAggregation(hitsAgg);
    }


    public static void main(String[] args) {
        AggregationParams date = new AggregationParams("", "", new DateParams());


        AggregationParams sum = new AggregationParams();

    }
}
