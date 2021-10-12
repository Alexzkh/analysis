package com.zqykj.common.request;

import com.zqykj.common.enums.DateType;
import com.zqykj.common.enums.HistogramField;
import com.zqykj.common.vo.TimeTypeRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description: 交易统计-聚合统计请求
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsAggs implements Serializable {

    /**
     * 柱状图统计依据的字段
     */
    private HistogramField historgramField;

    /**
     * 横坐标区间个数
     */
    private Integer historgramNumbers;

    /**
     * 折线图分桶依据的时间类型
     */
    private TimeTypeRequest dateType;

}
