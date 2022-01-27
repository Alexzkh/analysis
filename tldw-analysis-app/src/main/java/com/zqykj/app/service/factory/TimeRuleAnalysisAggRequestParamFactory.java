package com.zqykj.app.service.factory;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.common.enums.QueryType;
import com.zqykj.enums.AggsType;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;


/**
 * @Description: 时间规律聚合查询elasticsearch参数构建
 * @Author zhangkehou
 * @Date 2021/12/30
 */
public interface TimeRuleAnalysisAggRequestParamFactory {


    /**
     * 构建时间规律查询elastisearch
     *
     * @param request: 时间查询入参
     * @param param:   案件编号
     * @return: com.zqykj.parameters.aggregate.AggregationParams
     **/
    <T, V> AggregationParams bulidTimeRuleAnalysisAggParams(T request, V param);

    /**
     * 构建公共子聚合条件参数.
     *
     * @param root: 父聚合参数.
     * @return: void
     **/
    default void addSubAggregationParams(AggregationParams root) {
        String sum = AggsType.sum.name();

        // 交易金额
        String tradeMoneySum = "date_trade_amount_sum";
        AggregationParams subTradeMoneySumAgg = new AggregationParams(tradeMoneySum, sum, FundTacticsAnalysisField.CHANGE_AMOUNT);
        setSubAggregation(root, subTradeMoneySumAgg);

        // 交易次数
        String count = AggsType.count.name();
        String cardPer = "date_local_trade_total";
        AggregationParams transactionPerAgg = new AggregationParams(cardPer, count, FundTacticsAnalysisField.QUERY_CARD);
        setSubAggregation(root, transactionPerAgg);

        // 计算每个查询卡号的入账数据过滤(该聚合能统计入账次数)
        String filter = AggsType.filter.name();
        String payIn = "local_credits_times";
        QuerySpecialParams payInQuery = new QuerySpecialParams();
        payInQuery.setCommonQuery(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams subPayInAgg = new AggregationParams(payIn, filter, payInQuery);

        // 继续子聚合
        // 计算每个查询卡号的入账总金额
        String payInMoneySum = "local_credits_amount";
        AggregationParams payInMoneySumAgg = new AggregationParams(payInMoneySum, sum, FundTacticsAnalysisField.CHANGE_AMOUNT);
        setSubAggregation(subPayInAgg, payInMoneySumAgg);
        setSubAggregation(root, subPayInAgg);

        // 计算每个查询卡号的 出账数据过滤(该聚合能统计出账次数)

        String payOut = "local_out_times";
        QuerySpecialParams payOutQuery = new QuerySpecialParams();
        payOutQuery.setCommonQuery(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams subPayOutAgg = new AggregationParams(payOut, filter, payOutQuery);

        // 继续子聚合
        // 计算每个查询卡号的出账总金额
        String payOutMoneySum = "local_out_amount";
        AggregationParams payOutMoneySumAgg = new AggregationParams(payOutMoneySum, sum, FundTacticsAnalysisField.CHANGE_AMOUNT);
        setSubAggregation(subPayOutAgg, payOutMoneySumAgg);
        setSubAggregation(root, subPayOutAgg);
        root.setResultName("TimeRuleAnalysis");


    }

    /**
     * 将传入的子聚合加入到传入的父聚合中去.
     *
     * @param root: 父聚合参数.
     * @param sub:  子聚合参数.
     * @return: void
     **/
    default void setSubAggregation(AggregationParams root, AggregationParams sub) {
        root.setPerSubAggregation(sub);
    }

    /**
     * 设置同级聚合
     *
     * @param root:父聚合参数
     * @return: void
     **/
    default void addSiblingAggregation(AggregationParams root) {

        String sum = AggsType.sum.name();
        // 交易总次数
        String count = AggsType.count.name();
        String cardPer = "local_trade_total";
        AggregationParams transactionPerAgg = new AggregationParams(cardPer, count, FundTacticsAnalysisField.QUERY_CARD);
        root.addSiblingAggregation(transactionPerAgg);

        // 平均交易金额
        String avgTradeMoney = AggsType.avg.name();
        String avgTradeMoneyCount = "trade_avg_amount";
        AggregationParams avgTradeMoneyAgg = new AggregationParams(avgTradeMoneyCount, avgTradeMoney, FundTacticsAnalysisField.CHANGE_AMOUNT);
        root.addSiblingAggregation(avgTradeMoneyAgg);

        // 交易总金额
        String tradeMoneyCount = AggsType.sum.name();
        String tradeMoneySum = "local_trade_amount";
        AggregationParams tradeMoneyAgg = new AggregationParams(tradeMoneySum, tradeMoneyCount, FundTacticsAnalysisField.CHANGE_AMOUNT);
        root.addSiblingAggregation(tradeMoneyAgg);


        // 计算入账数据过滤(该聚合能统计入账次数)
        String filter = AggsType.filter.name();
        String payIn = "local_credits_times_total";
        QuerySpecialParams payInQuery = new QuerySpecialParams();
        payInQuery.setCommonQuery(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_IN));
        AggregationParams subPayInAgg = new AggregationParams(payIn, filter, payInQuery);

        // 继续子聚合
        // 计算入账总金额
        String payInMoneySum = "in_total_credits_amount";
        AggregationParams payInMoneySumAgg = new AggregationParams(payInMoneySum, sum, FundTacticsAnalysisField.CHANGE_AMOUNT);
        setSubAggregation(subPayInAgg, payInMoneySumAgg);
        root.addSiblingAggregation(subPayInAgg);

        // 计算出账数据过滤(该聚合能统计出账次数)

        String payOut = "local_out_times_total";
        QuerySpecialParams payOutQuery = new QuerySpecialParams();
        payOutQuery.setCommonQuery(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.LOAN_FLAG, FundTacticsAnalysisField.LOAN_FLAG_OUT));
        AggregationParams subPayOutAgg = new AggregationParams(payOut, filter, payOutQuery);

        // 继续子聚合
        // 计算出账总金额
        String payOutMoneySum = "out_total_pay_out_amount";
        AggregationParams payOutMoneySumAgg = new AggregationParams(payOutMoneySum, sum, FundTacticsAnalysisField.CHANGE_AMOUNT);
        setSubAggregation(subPayOutAgg, payOutMoneySumAgg);
        root.addSiblingAggregation(subPayOutAgg);
        root.setResultName("TimeRuleAnalysis");


    }
}
