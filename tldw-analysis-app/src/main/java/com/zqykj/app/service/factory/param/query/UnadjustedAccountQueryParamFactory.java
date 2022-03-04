/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.query;

import com.zqykj.app.service.vo.fund.FundTacticsPartGeneralRequest;
import com.zqykj.app.service.vo.fund.UnadjustedAccountAnalysisRequest;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * <h1> 未调单账户分析查询参数工厂 </h1>
 */
public interface UnadjustedAccountQueryParamFactory {

    /**
     * <h2> 查询未调单数据 </h2>
     */
    QuerySpecialParams queryUnadjusted(UnadjustedAccountAnalysisRequest request, List<String> adjustCards);

    /**
     * <h2> 查询未调单分析数据的缺失信息 </h2>
     */
    QuerySpecialParams queryUnadjustedExtraInfo(String caseId, List<String> unAdjustedCards);

    /**
     * <h2> 查询建议调单账号数据 </h2>
     */
    QuerySpecialParams querySuggestAdjustAccount(FundTacticsPartGeneralRequest request);

    /**
     * <h2> 刪除建议调单账号数据 </h2>
     */
    QuerySpecialParams deleteSuggestAdjustAccount(FundTacticsPartGeneralRequest request);
}
