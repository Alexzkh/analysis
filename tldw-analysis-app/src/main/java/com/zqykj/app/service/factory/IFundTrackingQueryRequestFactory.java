package com.zqykj.app.service.factory;

import com.zqykj.parameters.query.QuerySpecialParams;

/**
 * @Description: 资金追踪查询elasticsearch工厂类
 * @Author zhangkehou
 * @Date 2021/11/30
 */
public interface IFundTrackingQueryRequestFactory {

    /**
     * 获取资金追踪列表数据
     *
     * @param request: 资金追踪前置请求体
     * @param param:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams accessFundTrackingList(T request, V param);

    /**
     * 获取逐步追踪结果
     *
     * @param request: 逐步追踪条件（时间间隔和金额偏差）
     * @param param:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams accessFundTrackingResult(T request, V param);

}
