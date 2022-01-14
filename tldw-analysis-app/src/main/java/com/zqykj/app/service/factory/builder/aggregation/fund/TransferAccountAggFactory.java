package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.param.agg.TransferAccountAggRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.TransferAccountAnalysisResult;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.SortingRequest;
import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.core.aggregation.response.ElasticsearchAggregationResponseAttributes;
import com.zqykj.enums.AggsType;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.FetchSource;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: 调单账号特征分析
 * @Author zhangkehou
 * @Date 2021/12/27
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferAccountAggFactory implements TransferAccountAggRequestParamFactory {

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final Integer PAGE = 0;

    private final Integer PAGESIZE =8_000;

    @Override
    public <T> AggregationParams buildTransferAccountAgg(T request) {
        TransferAccountAnalysisRequest queryRequest = (TransferAccountAnalysisRequest) request;
        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityAggKeyMapping = new LinkedHashMap<>();
        // 获取聚合Key 映射 , 聚合key 与 返回实体属性 映射
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggKeyMapping, entityAggKeyMapping, TransferAccountAnalysisResult.class);
        String name = AggsType.multiTerms.name();
        String fields = FundTacticsAnalysisField.QUERY_CARD;
        AggregationParams root = new AggregationParams(name, AggsType.terms.name(), fields);
        // 关联账户数
        AggregationParams relatedAccountTimes = AggregationParamsBuilders.cardinality("related_account_times",
                FundTacticsAnalysisField.TRANSACTION_OPPOSITE_CARD, null);
        setSubAggregation(root, relatedAccountTimes);
        // 交易总次数
        AggregationParams tradeTotalTimes = AggregationParamsBuilders.count("local_trade_total",
                FundTacticsAnalysisField.QUERY_CARD, null);
        setSubAggregation(root, tradeTotalTimes);
        // 交易总金额
        AggregationParams tradeTotalAmount = AggregationParamsBuilders.sum("local_trade_amount",
                FundTacticsAnalysisField.CHANGE_AMOUNT, null);
        setSubAggregation(root, tradeTotalAmount);
        // 本方入账次数
        QuerySpecialParams creditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams creditsTimes = AggregationParamsBuilders.filter("local_credits_times", creditsFilter, null);
        // 本方入账金额
        AggregationParams creditsAmount = AggregationParamsBuilders.sum("local_credits_amount", FundTacticsAnalysisField.CHANGE_AMOUNT, null);
        setSubAggregation(creditsTimes, creditsAmount);
        setSubAggregation(root, creditsTimes);
        // 本方出账次数
        QuerySpecialParams outFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams outTimes = AggregationParamsBuilders.filter("local_out_times", outFilter, null);
        // 本方出账金额
        AggregationParams outAmount = AggregationParamsBuilders.sum("local_out_amount", FundTacticsAnalysisField.CHANGE_AMOUNT, null);
        setSubAggregation(outTimes, outAmount);
        setSubAggregation(root, outTimes);
        // 本方最早日期
        AggregationParams minDate = AggregationParamsBuilders.min("local_min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(root, minDate);
        // 本方最晚日期
        AggregationParams maxDate = AggregationParamsBuilders.max("local_max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        setSubAggregation(root, maxDate);
        // 本方交易净额
        Map<String, String> tradeNetPath = new HashMap<>();
        tradeNetPath.put("local_trade_amount", "local_credits_times>local_credits_amount");
        tradeNetPath.put("local_out_amount", "local_out_times>local_out_amount");
        String tradeNetScript = "params.local_trade_amount - params.local_out_amount";
        PipelineAggregationParams tradeNet = AggregationParamsBuilders.pipelineBucketScript("local_trade_net", tradeNetPath, tradeNetScript);
        root.setPerSubAggregation(tradeNet);
        // 本方排序
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(new SortingRequest("local_trade_total", SortingRequest.Direction.DESC)
                , PAGE
                , PAGESIZE);
        root.setPerSubAggregation(sort);
        // 本方聚合需要展示的字段
        FetchSource fetchSource = new FetchSource(FundTacticsAnalysisField.transferAccountAnalysisShowField(), 0, 1);
        AggregationParams hits = AggregationParamsBuilders.fieldSource("local_hits", fetchSource);
        setSubAggregation(root, hits);
        root.setResultName("TransferAccountAnalysis");
        root.setEntityAggColMapping(entityAggKeyMapping);
        root.setMapping(aggKeyMapping);
        return root;

    }

    @Override
    public AggregationParams buildAccessAllAdjustCardsAgg() {
        String name = AggsType.multiTerms.name();
        String fields = FundTacticsAnalysisField.QUERY_CARD;
        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        aggKeyMapping.put(name, ElasticsearchAggregationResponseAttributes.keyAsString);
        AggregationParams root = new AggregationParams(name, AggsType.terms.name(), fields);
        root.setMapping(aggKeyMapping);
        root.setResultName("AllAdjustCards");
        return root;
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
     * 资金战法聚合结果统一的排序操作
     *
     * @param sortRequest: 排序请求参数
     * @param from:        分页的起始参数
     * @param size:        分页时每页条数
     * @return: com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams
     **/
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
}
