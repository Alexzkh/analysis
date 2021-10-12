package com.zqykj.common.response;

import com.zqykj.common.enums.HistogramStatistic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 交易统计-柱状图统计结果的返回
 * @Author zhangkehou
 * @Date 2021/10/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistogramStatisticResponse {


    /**
     * 交易统计柱状图返回结果(这其中包括横坐标和纵坐标的值)
     */
    private List<HistogramStatistic> histogramStatisticList;



}
