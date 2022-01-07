/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.query;

import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * <h1> 未调单账户分析查询参数工厂 </h1>
 */
public interface UnadjustedAccountQueryParamFactory {

    QuerySpecialParams queryUnadjusted(String caseId, List<String> adjustCards, String keyword, DateRange range);

    QuerySpecialParams queryUnadjustedExtraInfo(String caseId, List<String> unAdjustedCards);
}
