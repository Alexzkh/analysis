package com.zqykj.common.request;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.common.vo.GraphCycleAndPathCommonParamVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 交易回路请求体
 * @Author zhangkehou
 * @Date 2022/1/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundsLoopRequest {

    /**
     * 选择的个体数,如果此项为空,则代表是全部调单卡号
     */
    private List<String> individual;

    /**
     * 交易金额
     */
    private String fund = "0";

    /**
     * 日期范围   (时间范围固定是:  00:00:00-23:59:59)
     */
    private DateRangeRequest dateRange;

    /**
     * 查询请求参数（查询、分页）
     */
    private QueryRequest queryRequest;

    /**
     * 比较符  大于、大于等于、小于、小于等于、等于   (默认是大于等于0)
     */
    private AmountOperationSymbol operator = AmountOperationSymbol.gte;


    public GraphCycleAndPathCommonParamVO build() {
        return GraphCycleAndPathCommonParamVO.builder()
                .dateRange(this.dateRange)
                .fund(this.fund)
                .operator(this.getOperator())
                .build();
    }


}
