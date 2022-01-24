/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.alibaba.excel.annotation.ExcelProperty;
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


    private int hashId;

    /**
     * 资金来源卡号
     */
    @ExcelProperty(value = "资金来源卡号", index = 0)
    private String fundSourceCard;

    /**
     * 资金来源户名
     */
    @ExcelProperty(value = "资金来源户名", index = 1)
    private String fundSourceAccountName;

    /**
     * 流入时间日期
     */
    @ExcelProperty(value = "流入时间日期", index = 2)
    private String inflowDate;

    /**
     * 流入时间日期(ms)
     */
    private long inflowDateTime;

    /**
     * 流入金额
     */
    @ExcelProperty(value = "流入金额", index = 3)
    private BigDecimal inflowAmount;

    /**
     * 资金中转卡号
     */
    @ExcelProperty(value = "资金中转卡号", index = 4)
    private String fundTransitCard;

    /**
     * 资金中转户名
     */
    @ExcelProperty(value = "资金中转户名", index = 5)
    private String fundTransitAccountName;

    /**
     * 流出时间日期
     */
    @ExcelProperty(value = "流出时间日期", index = 6)
    private String outflowDate;

    /**
     * 流出时间日期(ms)
     */
    private long outflowDateTime;

    /**
     * 流出金额
     */
    @ExcelProperty(value = "流出金额", index = 7)
    private BigDecimal outflowAmount;

    /**
     * 资金沉淀卡号
     */
    @ExcelProperty(value = "资金沉淀卡号", index = 8)
    private String fundDepositCard;

    /**
     * 资金沉淀户名
     */
    @ExcelProperty(value = "资金沉淀户名", index = 9)
    private String fundDepositAccountName;

    /**
     * 特征比: (流入金额 - 流出金额) / 流入金额
     */
    @ExcelProperty(value = "特征比", index = 10)
    private double characteristicRatio;

    public static String hashString(FastInFastOutResult result) {

        return result.getFundSourceCard() + result.getInflowDateTime() + result.getInflowAmount() +
                result.getFundTransitCard() + result.getOutflowDateTime() + result.getOutflowAmount() + result.getFundDepositCard();
    }
}
