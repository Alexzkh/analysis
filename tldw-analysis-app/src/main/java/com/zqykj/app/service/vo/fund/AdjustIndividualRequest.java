/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 选择调单个体分析请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class AdjustIndividualRequest extends FundTacticsPartGeneralRequest {

    /**
     * 案件Id
     */
    private String caseId;

    /**
     * 开户证件号码
     */
    private String customerIdentityCard;
}
