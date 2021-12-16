/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <h1> 快进快出结果(包含调单卡号作为来源的、中转的、沉淀的) </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class FastInFastOutResult {

    /**
     * 资金来源卡号
     */
    private String fundSourceCard;

    /**
     * 资金来源户名
     */
    private String fundSourceAccountName;

    /**
     * 流入时间日期
     */
    private String inflowDate;

    /**
     * 流入金额
     */
    private BigDecimal inflowAmount;

    /**
     * 资金中转卡号
     */
    private String fundTransitCard;

    /**
     * 资金中转户名
     */
    private String fundTransitAccountName;

    /**
     * 流出日期
     */
    private String outflowDate;

    /**
     * 流出金额
     */
    private BigDecimal outflowAmount;

    /**
     * 资金沉淀卡号
     */
    private String fundDepositCard;

    /**
     * 资金沉淀户名
     */
    private String fundDepositAccountName;

    /**
     * 特征比: (流入金额 - 流出金额) / 流入金额
     */
    private int characteristicRatio;
}
