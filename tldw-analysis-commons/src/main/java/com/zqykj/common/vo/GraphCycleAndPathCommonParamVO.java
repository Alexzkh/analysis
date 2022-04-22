package com.zqykj.common.vo;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.request.FundsLoopRequest;
import com.zqykj.common.request.TransactionPathRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 图回路和图路径公共参数vo
 * @Author zhangkehou
 * @Date 2022/1/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphCycleAndPathCommonParamVO {
    /**
     * 交易金额
     */
    private String fund = "0";

    /**
     * 日期范围   (时间范围固定是:  00:00:00-23:59:59)
     */
    private DateRangeRequest dateRange;

    /**
     * 比较符  大于、大于等于、小于、小于等于、等于   (默认是大于等于0)
     */
    private AmountOperationSymbol operator = AmountOperationSymbol.gte;

    public GraphCycleAndPathCommonParamVO build(FundsLoopRequest request) {
        return GraphCycleAndPathCommonParamVO.builder()
                .dateRange(request.getDateRange())
                .fund(request.getFund())
                .operator(request.getOperator())
                .build();
    }

    public GraphCycleAndPathCommonParamVO build(TransactionPathRequest request) {
        return GraphCycleAndPathCommonParamVO.builder()
                .dateRange(request.getDateRange())
                .fund(request.getFund())
                .operator(request.getOperator())
                .build();
    }
}
