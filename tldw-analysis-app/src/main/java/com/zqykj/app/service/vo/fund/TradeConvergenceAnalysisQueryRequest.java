/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * <h1> 战法交易汇聚查询请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class TradeConvergenceAnalysisQueryRequest extends FundTacticsPartGeneralPreRequest {

    /**
     * 合并卡号集合过滤
     */
    private List<String> mergeCards;

    public FundTacticsPartGeneralPreRequest convertFrom(TradeConvergenceAnalysisQueryRequest from) {

        FundTacticsPartGeneralPreRequest to = new FundTacticsPartGeneralPreRequest();

        BeanUtils.copyProperties(from, to);

        return to;
    }
}
