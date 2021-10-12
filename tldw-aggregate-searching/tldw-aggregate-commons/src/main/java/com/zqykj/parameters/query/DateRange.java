/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 日期范围
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DateRange {

    private Object from;

    private Object to;

    private String format = "yyyy-MM-dd";

//    private String timeZone = "+08:00";

    public DateRange(Object from, Object to) {
        this.from = from;
        this.to = to;
    }
}
