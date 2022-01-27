/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.alibaba.excel.ExcelWriter;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.core.ServerResponse;

import java.util.concurrent.ExecutionException;

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
    ServerResponse<FundAnalysisResultResponse<SuggestAdjustedAccountResult>> suggestAdjustedAccounts(FundTacticsPartGeneralRequest request, int from, int size);

    /**
     * <h2> 建议调单账号列表下载 </h2>
     */
    ServerResponse<String> suggestAdjustedAccountDownload(ExcelWriter writer, FundTacticsPartGeneralRequest request) throws ExecutionException, InterruptedException;

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

    /**
     * <h2> 未调单账户分析数据导出 </h2>
     */
    ServerResponse<String> unAdjustedAnalysisDownload(ExcelWriter writer, UnadjustedAccountAnalysisRequest request) throws Exception;
}
