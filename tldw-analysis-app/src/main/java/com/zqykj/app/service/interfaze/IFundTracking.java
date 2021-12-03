package com.zqykj.app.service.interfaze;

import com.zqykj.common.request.FundTrackingRequest;
import com.zqykj.common.request.GraduallyTrackingRequest;
import com.zqykj.common.response.FundTrackingResponse;
import com.zqykj.common.response.GraduallyTrackingResponse;
import com.zqykj.domain.Page;

import java.util.List;

/**
 * @Description: 资金追踪业务接口类
 * @Author zhangkehou
 * @Date 2021/11/29
 */
public interface IFundTracking {


    /**
     * 资金追踪列表数据显示
     *
     * @param request: 资金追踪列表数据
     * @param caseId:  案件编号
     * @return: java.util.List<com.zqykj.common.response.FundTrackingResponse>
     **/
    Page<FundTrackingResponse> accessFundTrackingResult(FundTrackingRequest request, String caseId) throws Exception;

    /**
     * 逐步追踪，返回源节点、下一节点，以及追踪出来的点信息{@link com.zqykj.common.vo.TrackingNode}
     *
     * @param request: 逐步追踪请求体
     * @param caseId:  案件编号
     * @return: com.zqykj.common.response.GraduallyTrackingResponse
     **/
    GraduallyTrackingResponse accessGraduallyTrackingResult(GraduallyTrackingRequest request, String caseId) throws Exception;
}
