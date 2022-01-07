/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.parameters.query.QueryOperator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * <h1> 交易统计分析前置请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class FundTacticsPartGeneralPreRequest extends FundTacticsPartGeneralRequest {

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
     * 比较符  大于、大于等于、小于、小于等于、等于   (默认是大于等于0)
     */
    private AmountOperationSymbol operator = AmountOperationSymbol.gte;

    /**
     * 交易金额
     */
    private String fund = "0";

    public static QueryOperator getOperator(AmountOperationSymbol operator) {

        return QueryOperator.of(operator.name());
    }
}
