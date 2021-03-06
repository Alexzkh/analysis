/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.alibaba.excel.ExcelWriter;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.middle.FastInFastOutDetailRequest;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.infrastructure.core.ServerResponse;

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

    /**
     * <h2> 快进快出分析结果导出 </h2>
     */
    ServerResponse<String> fastInoutAnalysisResultExport(ExcelWriter excelWriter, FastInFastOutRequest request) throws Exception;

    /**
     * <h2> 快进快出分析结果详情导出 </h2>
     */
    ServerResponse<String> detailResultExport(ExcelWriter excelWriter, FastInFastOutDetailRequest request);
}
