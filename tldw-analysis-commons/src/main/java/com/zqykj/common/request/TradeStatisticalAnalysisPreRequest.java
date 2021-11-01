/**
 * @作者 Mcj
 */
package com.zqykj.common.request;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.vo.DateRangeRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <h1> 交易统计分析前置请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TradeStatisticalAnalysisPreRequest {

    /**
     * 分析对象描述
     */
    private String analysisSubject;

    /**
     * 身份证
     */
    private String identityCard;

    /**
     * 卡号集合
     */
    private List<String> cardNums;

    /**
     * 日期范围   (时间范围固定是:  00:00-23:59)
     */
    private DateRangeRequest dateRange;

    /**
     * 比较符  大于、大于等于、小于、小于等于、等于   (默认是大于等于0)
     */
    private AmountOperationSymbol operator = AmountOperationSymbol.gte;

    /**
     * 交易金额
     */
    private String fund = "0";
}
