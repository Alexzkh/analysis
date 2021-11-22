/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;


/**
 * <h1> 交易统计分析查询请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TradeStatisticalAnalysisQueryRequest extends FundTacticsPartGeneralPreRequest {

    public FundTacticsPartGeneralPreRequest convertFrom(TradeStatisticalAnalysisQueryRequest from) {

        FundTacticsPartGeneralPreRequest to = new FundTacticsPartGeneralPreRequest();

        BeanUtils.copyProperties(from, to);

        return to;
    }
}
