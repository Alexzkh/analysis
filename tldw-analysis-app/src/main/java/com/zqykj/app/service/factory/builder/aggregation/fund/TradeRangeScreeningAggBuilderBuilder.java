/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.param.agg.TradeRangeScreeningAggParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.builder.AggregationParamsBuilders;
import com.zqykj.common.vo.Direction;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <h1> 交易区间筛选聚合参数构建 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeRangeScreeningAggBuilderBuilder extends FundTacticsCommonAggBuilder implements TradeRangeScreeningAggParamFactory {

    public AggregationParams individualBankCardsStatisticalAgg(int from, int size, String property, String direction, int groupSize) {

        // 根据查询卡号分组
        AggregationParams groupBy = AggregationParamsBuilders.terms("groupByQueryCard", FundTacticsAnalysisField.QUERY_CARD);
        // 交易笔数统计
        AggregationParams tradeTimes = AggregationParamsBuilders.count("trade_times", FundTacticsAnalysisField.QUERY_CARD, null);
        groupBy.setPerSubAggregation(tradeTimes);
        // 交易金额
        AggregationParams tradeAmount = AggregationParamsBuilders.sum("trade_amount", FundTacticsAnalysisField.CHANGE_MONEY);
        groupBy.setPerSubAggregation(tradeAmount);
        // 最早交易日期
        AggregationParams minDate = AggregationParamsBuilders.min("min_date", FundTacticsAnalysisField.TRADING_TIME, null);
        groupBy.setPerSubAggregation(minDate);
        // 最晚交易日期
        AggregationParams maxDate = AggregationParamsBuilders.min("max_date", FundTacticsAnalysisField.TRADING_TIME, null);
        groupBy.setPerSubAggregation(maxDate);
        // 聚合展示字段
        String[] showFields = new String[]{FundTacticsAnalysisField.QUERY_CARD, FundTacticsAnalysisField.BANK};
        AggregationParams hits = fundTacticsPartUniversalAggShowFields(showFields, "hits", null);
        groupBy.setPerSubAggregation(hits);
        // 排序与分页
        SortRequest sortRequest = new SortRequest(property, Direction.valueOf(direction));
        PipelineAggregationParams sort = fundTacticsPartUniversalAggSort(sortRequest, from, size);
        groupBy.setPerSubAggregation(sort);
        groupBy.setCollectMode("BREADTH_FIRST");
        groupBy.setSize(groupSize);
        return groupBy;
    }
}
