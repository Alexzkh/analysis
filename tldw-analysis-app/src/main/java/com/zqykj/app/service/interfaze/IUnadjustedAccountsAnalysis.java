/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.core.ServerResponse;

/**
 * <h1> 未调单账户分析 </h1>
 */
public interface IUnadjustedAccountsAnalysis {

    /**
     * <h2> 未调单账号分析结果 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<UnadjustedAccountAnalysisResult>> unAdjustedAnalysis(UnadjustedAccountAnalysisRequest request) throws Exception;

    /**
     * <h2> 建议调单账号列表 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<SuggestAdjustedAccountResult>> suggestAdjustedAccounts(FundTacticsPartGeneralRequest request);

    /**
     * <h2> 删除/批量删除调单账号列表 </h2>
     */
    ServerResponse<String> deleteSuggestAdjusted(FundTacticsPartGeneralRequest request);

    /**
     * <h2> 添加建议调单账号(手动保存) </h2>
     */
    ServerResponse<String> suggestAdjustedAccountManualSave(SuggestAdjustedAccountAddRequest request);

    /**
     * <h2> 添加建议调单账号(自动保存) </h2>
     */
    ServerResponse<String> suggestAdjustedAccountAutoSave(UnadjustedAccountAnalysisRequest request) throws Exception;
}
