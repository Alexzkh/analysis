/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 交易区间筛选操作记录详情查看请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TradeRangeOperationDetailSeeRequest extends FundTacticsPartGeneralRequest {

    /**
     * 操作记录数据唯一标识id
     */
    private String id;

    /**
     * 查询全部标识  true: 是全部查询 false: 调单卡号查询
     */
    private boolean queryAllFlag;
}
