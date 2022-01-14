package com.zqykj.app.service.vo.fund;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 时间过滤参数
 * @Author zhangkehou
 * @Date 2022/1/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeFilteringParam {

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
