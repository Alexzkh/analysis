/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.middle.FastInFastOutDetailRequest;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.common.core.ServerResponse;

import java.util.concurrent.ExecutionException;

/**
 * <h1> 战法快进快出 </h1>
 */
public interface IFastInFastOut {

    /**
     * <h2> 快进快出分析 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<FastInFastOutResult>> fastInFastOutAnalysis(FastInFastOutRequest request) throws Exception;

    /**
     * <h2> 快进快出分析结果详情 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> detailResult(FastInFastOutDetailRequest request);
}
