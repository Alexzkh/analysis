/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.*;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.common.core.ServerResponse;

/**
 * <h1> 战法交易汇聚分析 业务层 </h1>
 */
public interface ITransactionConvergenceAnalysis {

    /**
     * <h2> 交易汇聚分析结果 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TradeConvergenceAnalysisResult>> convergenceAnalysisResult(TradeConvergenceAnalysisQueryRequest request, String caseId) throws Exception;

    /**
     * <h2> 交易汇聚分析结果详情 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> getDetail(FundTacticsPartGeneralRequest request);
}
