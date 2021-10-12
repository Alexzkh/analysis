package com.zqykj.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description: 交易统计返回数据
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatisticsResponse implements Serializable {

    /**
     * 交易统计直方图数据集.
     * Map<String,Double> key: 对应交易金额、入账金额、出账金额的区间;value 对应各区间下的交易笔数.
     */
    private HistogramStatisticResponse histogram;

    /**
     * 交易统计折线图数据集.
     * Map<String,Double> key: 对应时间类型（年、月、日、时）区间 ;value 对应各区间交易金额总和.
     */
    private TimeGroupTradeAmountSum lineChart;

    /**
     * 交易统计个体列表数据.
     * Map<String,Object> key: 列表的列头 ;value:统计结果及交易流水中相关信息.
     */
    private List<Map<String, Object>> dataLists;


}
