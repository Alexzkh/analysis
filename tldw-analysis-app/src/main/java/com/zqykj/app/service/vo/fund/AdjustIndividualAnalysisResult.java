/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 调单个体分析结果
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Agg(name = "local_hits")
@Key
public class AdjustIndividualAnalysisResult extends FundPartAnalysisResult {

    /**
     * 账户开户名称
     */
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String customerName;

    /**
     * 开户证件号码
     */
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
    private String customerIdentityCard;

    /**
     * 联系号码
     */
    private String contactNumber;

    /**
     * 调单账号数量
     */
    @Agg(name = "local_adjust_account_count")
    @Key(name = "docCount")
    @Sort(name = "local_adjust_account_count._count")
    private int adjustAccountCount;
}
