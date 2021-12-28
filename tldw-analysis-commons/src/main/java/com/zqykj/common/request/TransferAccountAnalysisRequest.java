package com.zqykj.common.request;

import com.zqykj.common.vo.DateRangeRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 调单账号分析请求体
 * @Author zhangkehou
 * @Date 2021/12/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferAccountAnalysisRequest {


    /**
     * 日期范围   (时间范围固定是:  00:00:00-23:59:59)
     */
    private DateRangeRequest dateRange;

    /**
     * 特征比参数
     */
    private CharacteristicRatio characteristicRatio;

    /**
     * 模糊查询、分页排序参数
     */
    private QueryRequest queryRequest;

    /**
     * 是否来源.
     */
    private Boolean source;

    /**
     * 是否中转.
     */
    private Boolean transfer;

    /**
     * 是否沉淀.
     */
    private Boolean precipitate;

    /**
     * 其他
     * */
    private Boolean other;
}
