/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.alibaba.excel.ExcelWriter;
import com.zqykj.app.service.vo.fund.*;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.infrastructure.core.ServerResponse;

/**
 * <h1> 战法交易汇聚分析 业务层 </h1>
 */
public interface ITransactionConvergenceAnalysis {

    /**
     * <h2> 交易汇聚分析结果 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TradeConvergenceAnalysisResult>> convergenceAnalysisResult(TradeConvergenceAnalysisQueryRequest request, int from, int size,
                                                                                                         boolean isComputeTotal) throws Exception;

    /**
     * <h2> 交易汇聚分析结果详情 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> getDetail(FundTacticsPartGeneralRequest request);

    /**
     * <h2> 交易汇聚分析结果详情导出 </h2>
     */
    ServerResponse<String> detailExport(ExcelWriter excelWriter, FundTacticsPartGeneralRequest request) throws Exception;

    /**
     * <h2> 交易汇聚分析结果导出 </h2>
     */
    ServerResponse<String> convergenceAnalysisResultExport(ExcelWriter excelWriter, TradeConvergenceAnalysisQueryRequest request) throws Exception;
}
