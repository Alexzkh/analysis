package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.SingleCardPortraitRequest;
import com.zqykj.app.service.vo.fund.SingleCardPortraitResponse;
import com.zqykj.infrastructure.core.ServerResponse;

/**
 * @author: SunChenYu
 * @date: 2021年11月15日 11:42:28
 */
public interface ISingleCardPortraitStatistics {
    /**
     * 获取单卡画像统计分析结果
     *
     * @param singleCardPortraitRequest 请求体
     * @return 返回体
     */
    ServerResponse<SingleCardPortraitResponse> accessSingleCardPortraitStatistics(SingleCardPortraitRequest singleCardPortraitRequest);
}
