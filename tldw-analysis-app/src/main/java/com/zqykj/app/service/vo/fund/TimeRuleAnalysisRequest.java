package com.zqykj.app.service.vo.fund;

import com.zqykj.common.enums.LineChartDisplayType;
import com.zqykj.common.enums.StatisticType;
import com.zqykj.common.request.PagingRequest;
import com.zqykj.common.request.SortingRequest;
import com.zqykj.common.vo.DateRangeRequest;
import lombok.*;


import java.util.List;

/**
 * @Description: 时间规律分析请求体
 * @Author zhangkehou
 * @Date 2021/12/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRuleAnalysisRequest {

    /**
     * 时间规律分析选择的本端对象
     */
    private List<String> source;

    /**
     * 时间规律分析选择的对端对象
     */
    private List<String> dest;

    /**
     * 统计类型：汇总、单一
     */
    private StatisticType statisticType;

    /**
     * 折线图展示类型：交易金额 、交易次数
     */
    private LineChartDisplayType lineChartDisplayType;

    /**
     * 统计方式(按月m、按周w、按日d、按小时h).
     */
    private String dateType;

    /**
     * 日期范围   (时间范围固定是:  00:00:00-23:59:59)
     */
    private DateRangeRequest dateRange;

    /**
     * 分页请求体
     */
    private PagingRequest paging;

    /**
     * 排序请求体
     */
    private SortingRequest sorting;


    /**
     * 工作日
     */
    private Boolean weekdays;

    /**
     * 节假日
     */
    private Boolean holidays;

    /**
     * 周末
     */
    private Boolean weekend;



}
