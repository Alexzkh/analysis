/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Hits;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Local;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 调单个体分析结果
 */
@Setter
@Getter
@NoArgsConstructor
public class AdjustIndividualAnalysisResult extends FundPartAnalysisResult {

    /**
     * 账户开户名称
     */
    @Local(name = "customer_name")
    @Key(name = "hits")
    @Hits
    private String customerName;

    /**
     * 开户证件号码
     */
    @Local(name = "customer_identity_card")
    @Hits
    private String customerIdentityCard;

    /**
     * 联系号码
     */
    private String contactNumber;

    /**
     * 调单账号数量
     */
    @Local(name = "local_adjust_account_count", sortName = "local_adjust_account_count._count")
    @Key(name = "docCount")
    private int adjustAccountCount;
}
