package com.zqykj.common.request;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.enums.TransactionStatisticsDateRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description: 交易统计查询参数
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionStatisticsQuery implements Serializable {

    /**
     * 选择的调单个体(身份证件号或卡号)
     */
    private List<String> condition;

    /**
     * 交易统计日期范围
     */
    private TransactionStatisticsDateRange transactionStatisticsDateRange;

    /**
     * 日期范围的起始值
     */
    private Map<String, String> dateRangeValue;

    /**
     * 交易金额操作符
     */
    private AmountOperationSymbol amountOperationSymbol;

    /**
     * 交易金额值
     */
    private Double amountValue;


}
