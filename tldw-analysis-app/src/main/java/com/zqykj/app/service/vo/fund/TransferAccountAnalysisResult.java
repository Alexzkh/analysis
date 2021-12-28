package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.Agg;
import com.zqykj.app.service.annotation.Key;
import com.zqykj.app.service.annotation.Sort;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @Description: 调单账号elastisearch返回的结果
 * @Author zhangkehou
 * @Date 2021/12/24
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Agg(name = "local_hits")
@Key
public class TransferAccountAnalysisResult extends FundPartAnalysisResult {

    // 开户名称
    @Agg(name = "customer_name", showField = true)
    @Key(name = "customer_name")
    private String customerName;

    // 开户证件号码
    @Agg(name = "customer_identity_card", showField = true)
    @Key(name = "customer_identity_card")
    private String customerIdentityCard;

    // 开户银行
    @Agg(name = "bank", showField = true)
    @Key(name = "bank")
    private String bank;

    // 交易卡号
    @Agg(name = "query_card", showField = true)
    @Key(name = "query_card")
    private String queryCard;

    // 交易总次数
    @Agg(name = "related_account_times")
    @Key(name = "value")
    @Sort(name = "related_account_times")
    private int relatedAccountTimes;


}
