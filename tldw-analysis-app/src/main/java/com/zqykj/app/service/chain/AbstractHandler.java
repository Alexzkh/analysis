package com.zqykj.app.service.chain;

import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;

/**
 * @Description: 抽象调单账号特征分析处理器
 * @Author zhangkehou
 * @Date 2021/12/27
 */
public abstract class AbstractHandler {

    /**
     * @param context: 需要处理的调单账号数据
     * @param chain: 调单账号特征数据处理器链
     * @return: void
     **/
    protected abstract void doHandle(TransferAccountAnalysisRequest request, TransferAccountAnalysisResultVO context, TransferAccountAnalysisResultHandlerChain chain);

    /**
     * 特征比处理器的权重
     */
    protected abstract int weight();
}
