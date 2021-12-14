/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;

/**
 * <h1> 快进快出结果 </h1>
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
     * 特征比: (入账金额 - 出账金额) / 入账金额
     */
    private String characteristicRatio;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.fundSourceCard);
        sb.append(this.inflowDate);
        sb.append(this.inflowAmount.toString());
        sb.append(this.fundTransitCard);
        sb.append(this.outflowDate);
        sb.append(this.outflowAmount.toString());
        sb.append(this.fundDepositCard);
        return sb.toString();
    }

    public static String md5(String str) {

        return DigestUtils.md5Hex(str);
    }

    public static String hash(String str) {

        return DigestUtils.md5Hex(str);
    }
}
