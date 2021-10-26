package com.zqykj.common.request;

import com.zqykj.common.vo.DateRangeRequest;

/**
 * @Description: 调单卡号（查询卡号）特征分析
 * @Author zhangkehou
 * @Date 2021/10/26
 */
public class QueryCardFeatureAnalysisRequest {

    /**
     * 日期范围   (时间范围固定是:  00:00-23:59)
     */
    private DateRangeRequest dateRange;

    /**
     * 特征比参数
     */
    private CharacteristicRatio characteristicRatio;

}
