/**
 * @作者 Mcj
 */
package com.zqykj.factory;

import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;

/**
 * <h1> 公共查询请求参数构建工厂 </h1>
 */
public interface QueryRequestParamFactory {

    <T, V> QuerySpecialParams createTradeAmountByTimeQuery(T request, V other);

    <T, V> QuerySpecialParams createTradeStatisticalAnalysisQueryRequest(T request, V other);

    <T, V> CombinationQueryParams buildCommonQueryParams(T request, V parameter);

    <T> CombinationQueryParams assemblePostFilter(T request);
}
