/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.alibaba.excel.ExcelWriter;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.app.service.vo.fund.TransactionFieldTypeProportionResults;
import com.zqykj.app.service.vo.fund.TransactionFieldTypeStatisticsResult;
import com.zqykj.app.service.vo.fund.middle.TradeAnalysisDetailResult;
import com.zqykj.infrastructure.core.ServerResponse;

import java.util.List;

/**
 * <h1> 交易字段分析 </h1>
 */
public interface ITransactionField {

    /**
     * <h2> 交易字段类型占比(页面上柱状图数据来源) </h2>
     */
    ServerResponse<List<TransactionFieldTypeProportionResults>> fieldTypeProportionHistogram(TransactionFieldAnalysisRequest request) throws Exception;

    /**
     * <h2> 交易字段类型统计 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TransactionFieldTypeStatisticsResult>> fieldTypeStatistics(int from, int size, TransactionFieldAnalysisRequest request) throws Exception;

    /**
     * <h2> 自定义归类(交易字段分组的内容) </h2>
     */
    ServerResponse<FundAnalysisResultResponse<Object>> customCollationContainField(TransactionFieldAnalysisRequest request);

    /**
     * <h2> 交易字段类型统计数据详情 </h2>
     */
    ServerResponse<FundAnalysisResultResponse<TradeAnalysisDetailResult>> getDetail(TransactionFieldAnalysisRequest request, int from, int size);

    /**
     * <h2> 交易字段类型统计数据详情导出 </h2>
     */
    ServerResponse<String> detailExport(ExcelWriter excelWriter, TransactionFieldAnalysisRequest request) throws Exception;

    /**
     * <h2> 字段类型统计结果导出 </h2>
     */
    ServerResponse<String> fieldTypeStatisticsExport(ExcelWriter excelWriter, TransactionFieldAnalysisRequest request) throws Exception;
}
