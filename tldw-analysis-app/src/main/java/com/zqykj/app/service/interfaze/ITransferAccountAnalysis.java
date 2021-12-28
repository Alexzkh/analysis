package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;

/**
 * @Description: 调单账号特征分析接口
 * @Author zhangkehou
 * @Date 2021/12/24
 */
public interface ITransferAccountAnalysis {


    /**
     * 获取调单账号特征分析战法结果
     *
     * @param request: 调单账号特征分析请求体
     * @param caseId:  案件编号
     * @return: com.zqykj.app.service.vo.fund.FundAnalysisResultResponse<com.zqykj.app.service.vo.fund.TransferAccountAnalysisResult>
     **/
    FundAnalysisResultResponse<TransferAccountAnalysisResultVO> accessTransferAccountAnalysis(TransferAccountAnalysisRequest request, String caseId);
}
