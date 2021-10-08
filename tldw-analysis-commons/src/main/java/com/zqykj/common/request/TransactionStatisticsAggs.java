package com.zqykj.common.request;

import com.zqykj.common.enums.DateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsAggs implements Serializable {

    private String historgramField;

    private Integer historgramNumbers;

    private DateType dateType;

}
