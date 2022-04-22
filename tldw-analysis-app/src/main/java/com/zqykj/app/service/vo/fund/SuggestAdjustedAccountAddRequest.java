/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.domain.bank.SuggestAdjusted;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * <h1> 建议调单账号添加请求 </h1>
 */
@Setter
@Getter
public class SuggestAdjustedAccountAddRequest extends  FundTacticsPartGeneralRequest{

    /** 建议调单账号数据 */
    private List<SuggestAdjusted> suggestAdjusted;
}
