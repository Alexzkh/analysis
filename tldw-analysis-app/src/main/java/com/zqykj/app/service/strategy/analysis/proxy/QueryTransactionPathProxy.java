package com.zqykj.app.service.strategy.analysis.proxy;

import com.zqykj.domain.request.PathRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description: 查找交易路径
 * @Author zhangkehou
 * @Date 2021/12/16
 */
@Component
public class QueryTransactionPathProxy {

    @Autowired
    private BaseAPIProxy baseAPIProxy;

    /**
     * 获取案件所在的图id
     *
     * @param url: get请求的请求路径
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    public Map<String, Object> accessGraphId(String url) {
        return baseAPIProxy.request(url, Map.class);
    }

    /**
     * 获取两节点间图路径结果
     *
     * @param url: post请求路径
     * @param pathRequest: post请求的request body.这里是图数据请求参数 {@link PathRequest}
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    public Map<String, Object> accessTransactionPathResult(String url, PathRequest pathRequest) {
        return baseAPIProxy.request(url, pathRequest, Map.class);
    }
}
