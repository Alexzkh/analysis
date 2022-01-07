/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.UnadjustedAccountAnalysisRequest;
import com.zqykj.app.service.vo.fund.UnadjustedAccountAnalysisResult;
import com.zqykj.common.core.ServerResponse;

import java.util.concurrent.ExecutionException;

/**
 * <h1> 未调单账户分析 </h1>
 */
public interface IUnadjustedAccountsAnalysis {

    /**
     * <h1> 未调单账号分析结果 </h1>
     */
    ServerResponse<FundAnalysisResultResponse<UnadjustedAccountAnalysisResult>> unAdjustedAnalysis(UnadjustedAccountAnalysisRequest request) throws ExecutionException, InterruptedException;
}
