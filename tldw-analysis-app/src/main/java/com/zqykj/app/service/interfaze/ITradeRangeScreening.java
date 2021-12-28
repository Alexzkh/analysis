/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.*;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.domain.bank.TradeRangeOperationRecord;

import java.util.List;

/**
 * <h1> 交易区间筛选 </h1>
 */
public interface ITradeRangeScreening {

    /**
     * <h2> 获取交易区间筛选数据图表结果 </h2>
     */
    ServerResponse<TradeRangeScreeningDataChartResult> getDataChartResult(TradeRangeScreeningDataChartRequest request);

    /**
     * <h2> 保存交易区间筛选操作记录 </h2>
     */
    ServerResponse<String> saveOperationRecord(TradeRangeScreeningSaveRequest request);

    /**
     * <h2> 刪除交易区间筛选操作记录 </h2>
     */
    ServerResponse<String> deleteOperationRecord(String caseId, String id);

    /**
     * <h2> 交易区间筛选操作记录列表 </h2>
     */
    ServerResponse<List<TradeRangeOperationRecord>> operationRecordsList(FundTacticsPartGeneralRequest request);

    /**
     * <h2> 查看交易区间筛选操作记录详细列表数据 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TradeRangeOperationDetailSeeResult>> seeOperationRecordsDetailList(FundTacticsPartGeneralRequest request);

    /**
     * <h2> 查看操作记录中个体银行卡的部分统计结果 </h2>
     */
    ServerResponse<List<TradeOperationIndividualBankCardsStatistical>> seeIndividualBankCardsStatisticalResult(FundTacticsPartGeneralRequest request);
}
