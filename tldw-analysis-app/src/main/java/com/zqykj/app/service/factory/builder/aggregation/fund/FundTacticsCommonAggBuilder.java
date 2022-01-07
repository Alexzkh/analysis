/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralPreRequest;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.FetchSource;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1> 资金战法部分通用聚合构建 </h1>
 */
public abstract class FundTacticsCommonAggBuilder {

    /**
     * <h2> 资金战法分析部分通用聚合查询(适用于用户指定了一组调单卡号集合) </h2>
     * <p>
     * 目前适用于 交易统计分析结果查询、交易汇聚结果查询等
     * <p>
     * 操作的是表 {@link com.zqykj.domain.bank.BankTransactionRecord}
     */
    protected void fundTacticsPartUniversalAgg(AggregationParams cardTerms, @Nullable FundTacticsPartGeneralPreRequest preRequest) {

        // 交易总金额
        AggregationParams tradeTotalAmount = AggregationParamsBuilders.sum("local_trade_amount",
                FundTacticsAnalysisField.CHANGE_MONEY, null);
        cardTerms.setPerSubAggregation(tradeTotalAmount);

        // 入账次数
        QuerySpecialParams creditsFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams creditsTimes = AggregationParamsBuilders.filter("local_credits_times", creditsFilter, null);
        // 入账金额
        AggregationParams creditsAmount = AggregationParamsBuilders.sum("local_credits_amount", FundTacticsAnalysisField.CHANGE_MONEY, null);
        creditsTimes.setPerSubAggregation(creditsAmount);
        cardTerms.setPerSubAggregation(creditsTimes);

        // 出账次数
        QuerySpecialParams outFilter = new QuerySpecialParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams outTimes = AggregationParamsBuilders.filter("local_out_times", outFilter, null);
        // 出账金额
        AggregationParams outAmount = AggregationParamsBuilders.sum("local_out_amount", FundTacticsAnalysisField.CHANGE_MONEY, null);
        outTimes.setPerSubAggregation(outAmount);
        cardTerms.setPerSubAggregation(outTimes);

        // 最早日期
        AggregationParams minDate = AggregationParamsBuilders.min("local_min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        cardTerms.setPerSubAggregation(minDate);
        // 最晚日期
        AggregationParams maxDate = AggregationParamsBuilders.max("local_max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        cardTerms.setPerSubAggregation(maxDate);
        // 交易净额
        AggregationParams tradeNet = AggregationParamsBuilders.sum("local_trade_net", FundTacticsAnalysisField.TRANSACTION_MONEY, null);
        cardTerms.setPerSubAggregation(tradeNet);
    }

    /**
     * <h2> 聚合排序 </h2>
     */
    protected PipelineAggregationParams fundTacticsPartUniversalAggSort(SortRequest sortRequest, int from, int size) {
        if (null != sortRequest) {
            List<FieldSort> fieldSorts = new ArrayList<>();
            String property = sortRequest.getProperty();
            Direction order = sortRequest.getOrder();
            // 获取真实的聚合排序字段(开户名称、开户证件号码、开户银行、账号、交易卡号 不做排序,按照交易总金额排序处理)
            fieldSorts.add(new FieldSort(property, order.name()));
            return AggregationParamsBuilders.sort("sort", fieldSorts, from, size);
        } else {
            return AggregationParamsBuilders.sort("sort", from, size);
        }
    }

    /**
     * <h2> 聚合排序 </h2>
     */
    protected PipelineAggregationParams fundTacticsPartUniversalAggSort(int from, int size) {

        // 获取真实的聚合排序字段(开户名称、开户证件号码、开户银行、账号、交易卡号 不做排序,按照交易总金额排序处理)
        return fundTacticsPartUniversalAggSort(null, from, size);
    }

    /**
     * <h2> 聚合展示字段 </h2>
     * <p>
     * 默认只取出一条
     */
    protected AggregationParams fundTacticsPartUniversalAggShowFields(String[] fields, String hitsAggName) {
        return fundTacticsPartUniversalAggShowFields(fields, hitsAggName, 0, 1, null);
    }

    /**
     * <h2> 聚合展示字段 </h2>
     * <p>
     * 默认只取出一条
     */
    protected AggregationParams fundTacticsPartUniversalAggShowFields(String[] fields, String hitsAggName, @Nullable FieldSort sort) {
        return fundTacticsPartUniversalAggShowFields(fields, hitsAggName, 0, 1, sort);
    }

    /**
     * <h2> 聚合展示字段 </h2>
     * <p>
     * 自定义需要展示的字段记录的条数
     */
    protected AggregationParams fundTacticsPartUniversalAggShowFields(String[] fields, String hitsAggName, int from, int size, @Nullable FieldSort sort) {
        FetchSource fetchSource = new FetchSource(fields, from, size);
        if (null != sort) {
            fetchSource.setSort(sort);
        }
        return AggregationParamsBuilders.fieldSource(hitsAggName, fetchSource);
    }
}
