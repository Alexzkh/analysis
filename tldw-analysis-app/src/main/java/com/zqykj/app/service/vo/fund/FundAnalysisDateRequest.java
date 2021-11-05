/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.vo.TimeTypeRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 交易统计分析按时间类型汇总交易金额 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundAnalysisDateRequest extends TradeStatisticalAnalysisPreRequest {

    /**
     * 时间类型
     */
    private TimeTypeRequest timeType;

    /**
     * 例如 间隔 1d  这里填写1
     */
    private int timeValue = 1;

    /**
     * 日期字段
     */
    private String dateField;

    /**
     * 指标字段 (例如交易统计分析那边 按时间类型汇总金额, 这里的指标字段就是金额)
     */
    private String metricsField;

    /**
     * 自定义格式  eg. yyyy-MM-dd HH:mm:ss
     */
    private String format;

    /**
     * 当不指定format, 默认根据 timeType 推断出默认的
     */
    public static String convertFromTimeType(String timeType) {

        switch (timeType) {
            case "s":
                return "yyyy-MM-dd HH:mm:ss";
            case "d":
                return "yyyy-MM-dd";
            case "M":
            case "q":
                return "yyyy-MM";
            case "y":
                return "yyyy";
            default:
                return "HH";
        }
    }
}
