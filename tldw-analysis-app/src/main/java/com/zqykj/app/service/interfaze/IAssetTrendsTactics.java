package com.zqykj.app.service.interfaze;

import com.zqykj.common.request.AssetTrendsRequest;
import com.zqykj.common.response.AssetTrendsResponse;

/**
 * @Description: 资产趋势战法接口定义.
 * @Author zhangkehou
 * @Date 2021/10/19
 */
public interface IAssetTrendsTactics {


    /**
     * 获取资产趋势统计结果.
     *
     * @param caseId:             案件编号.
     * @param assetTrendsRequest: 资产趋势请求头.
     * @return: com.zqykj.common.response.AssetTrendsResponse
     **/
    AssetTrendsResponse accessAssetTrendsTacticsResult(String caseId, AssetTrendsRequest assetTrendsRequest);
}
