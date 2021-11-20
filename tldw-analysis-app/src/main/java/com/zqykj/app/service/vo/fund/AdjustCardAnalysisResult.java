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
 * <h1> 调单卡号分析结果 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class AdjustCardAnalysisResult extends FundPartAnalysisResult {

    /**
     * 调单卡号
     */
    @Local(name = "query_card")
    @Hits
    @Key(name = "hits")
    private String adjustCard;

    // 开户银行
    @Local(name = "bank")
    @Hits
    private String bank;

    // 开户名称
    @Local(name = "customer_name")
    @Hits
    private String customerName;

    // 开户证件号码
    @Local(name = "customer_identity_card")
    @Hits
    private String customerIdentityCard;
}
