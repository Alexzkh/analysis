/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund.middle;

import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * <h1> 快进快出结果详情请求 </h1>
 */
@Setter
@Getter
public class FastInFastOutDetailRequest extends FundTacticsPartGeneralRequest {

    /** 资金来源卡号 */
    private String fundSourceCard;

    /** 流入日期时间 */
    private String flowInDateTime;

    /** 流入金额 */
    private double flowInAmount;

    /** 资金中专卡号 */
    private String fundTransitCard;

    /** 流出日期时间 */
    private String flowOutDateTime;

    /** 流出金额 */
    private double flowOutAmount;

    /** 沉淀卡号 */
    private String depositCard;
}
