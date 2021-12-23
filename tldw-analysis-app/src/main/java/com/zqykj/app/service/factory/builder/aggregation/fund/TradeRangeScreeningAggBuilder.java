/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.param.agg.TradeRangeScreeningAggParamFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <h1> 交易区间筛选聚合参数构建 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TradeRangeScreeningAggBuilder extends FundTacticsCommonAgg implements TradeRangeScreeningAggParamFactory {

}
