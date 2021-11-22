/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * <h1> 战法交易汇聚查询请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TradeConvergenceAnalysisQueryRequest extends FundTacticsPartGeneralPreRequest {

    public FundTacticsPartGeneralPreRequest convertFrom(TradeConvergenceAnalysisQueryRequest from) {

        FundTacticsPartGeneralPreRequest to = new FundTacticsPartGeneralPreRequest();

        BeanUtils.copyProperties(from, to);

        return to;
    }
}
