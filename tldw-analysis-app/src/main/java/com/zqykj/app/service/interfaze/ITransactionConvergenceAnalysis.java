/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.TradeConvergenceAnalysisQueryRequest;
import com.zqykj.common.core.ServerResponse;

import java.util.concurrent.ExecutionException;

/**
 * <h1> 战法交易汇聚分析 业务层 </h1>
 */
public interface ITransactionConvergenceAnalysis {

    ServerResponse<TradeConvergenceAnalysisResultResponse> convergenceAnalysisResult(TradeConvergenceAnalysisQueryRequest request, String caseId) throws ExecutionException, InterruptedException;
}
