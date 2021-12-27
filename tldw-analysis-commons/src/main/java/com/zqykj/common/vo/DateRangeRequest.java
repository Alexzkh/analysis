/**
 * @作者 Mcj
 */
package com.zqykj.common.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 日期范围请求
 *
 * @author Administrator
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeRequest {

    /**
     * 开始时间(若2者都为空,则代表全部)
     */
    private String start;

    /**
     * 结束时间
     */
    private String end;

    /**
     * 时间范围
     */
    private String timeStart = " 00:00:00";
    private String timeEnd = " 23:59:59";
}
