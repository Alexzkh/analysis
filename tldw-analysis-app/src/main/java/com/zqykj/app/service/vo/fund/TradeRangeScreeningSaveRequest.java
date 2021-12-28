/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * <h1> 交易范围筛选保存请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TradeRangeScreeningSaveRequest {

    private String caseId;

    /**
     * 调单卡号(全部查询的时候为空)
     */
    private List<String> cardNum;

    /**
     * 操作人
     */
    private String operationPeople;

    /**
     * 最小金额
     */
    private double fundMin;

    /**
     * 最大金额
     */
    private double fundMax;

    /**
     * 账户开户名称
     */
    private String accountOpeningName;

    /**
     * 账户开户证件号码
     */
    private String accountOpeningNumber;

    /**
     * 数据类别
     */
    private String saveType;

    /**
     * 备注
     */
    private String remark;
}
