/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * <h1> 战法快进快出请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class FastInFastOutRequest extends FundTacticsPartGeneralRequest {

    /**
     * 特征比: (入账金额 - 出账金额) / 入账金额
     */
    private int characteristicRatio;

    /**
     * 调单卡号集合
     */
    private List<String> cardNum;

    /**
     * 单笔限额(元) >= 最小0元 最大1000000000元 int够用
     */
    private int singleQuota;

    /**
     * 时间间隔(分钟) >= 最小0 最大1000000000
     */
    private long timeInterval;

}
