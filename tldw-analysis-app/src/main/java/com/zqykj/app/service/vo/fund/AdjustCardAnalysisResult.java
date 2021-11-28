/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 调单卡号分析结果 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Agg(name = "local_hits")
@Key
public class AdjustCardAnalysisResult extends FundPartAnalysisResult {

    /**
     * 调单卡号
     */
    @Agg(name = "query_card", showField = true)
    @Key(name = "query_card")
    private String adjustCard;

    // 开户银行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    private String bank;

    // 开户名称
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String customerName;

    // 开户证件号码
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
    private String customerIdentityCard;
}
