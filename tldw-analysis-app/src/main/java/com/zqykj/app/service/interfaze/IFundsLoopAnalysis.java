package com.zqykj.app.service.interfaze;

import com.zqykj.common.request.FundsLoopRequest;
import com.zqykj.domain.response.FundsLoopResponse;

/**
 * @Description: 资金回路分析业务接口定义
 * @Author zhangkehou
 * @Date 2022/1/17
 */
public interface IFundsLoopAnalysis {


    /**
     * 资金回路分析获取分析结果
     *
     * @param request: 资金回路请求体
     * @param caseId:  案件编号
     * @return: com.zqykj.domain.response.FundsLoopResponse
     **/
    FundsLoopResponse accessFundsLoopAnalysisResult(FundsLoopRequest request, String caseId);




}
