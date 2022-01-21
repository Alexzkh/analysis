package com.zqykj.app.service.strategy.analysis.proxy;

import com.zqykj.app.service.tools.AthenaGdbOperations;
import com.zqykj.common.constant.Constants;
import com.zqykj.domain.request.CycleRequest;
import com.zqykj.domain.request.PathRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 查找交易路径
 * @Author zhangkehou
 * @Date 2021/12/16
 */
@Component
public class AthenaGraphOperatorProxy {

    @Autowired
    private BaseAPIProxy baseAPIProxy;

    @Value("${athena.gdb.uri:http://172.30.4.55:8089}")
    private String URI;

    /**
     * 获取案件所在的图id
     *
     * @param url: get请求的请求路径
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    public Map<String, Object> accessGraphDataResult(String url) {
        return baseAPIProxy.request(url, Map.class);
    }

    /**
     * 获取两节点间图路径结果
     *
     * @param graphId:     图id
     * @param pathRequest: post请求的request body.这里是图数据请求参数 {@link PathRequest}
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    public Map<String, Object> accessTransactionPathResult(long graphId, PathRequest pathRequest) {

        String pathUrl = URI.concat(String.format(Constants.AthenaGdbConstants.Path_API, graphId));
        return baseAPIProxy.request(pathUrl, pathRequest, Map.class);
    }

    /**
     * 获取回路结果
     *
     * @param graphId:      图id
     * @param cycleRequest: 回路查找请求参数
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    public Map<String, Object> accessCycleDetect(long graphId, CycleRequest cycleRequest) {

        String cycleUrl = URI.concat(String.format(Constants.AthenaGdbConstants.CYCLE_DETECT, graphId));
        return baseAPIProxy.request(cycleUrl, cycleRequest, Map.class);
    }

    /**
     * 获取图id
     *
     * @param caseId: 案件id
     * @return: long
     **/
    public long accessGraphId(String caseId) {
        String url = URI.concat(String.format(Constants.AthenaGdbConstants.GRAPH_ID_API,
                Constants.AthenaGdbConstants.SCHEMA, caseId));
        Map<String, Object> accessGraphIdRep = accessGraphDataResult(url);
        LinkedHashMap linkedHashMap = (LinkedHashMap) accessGraphIdRep.get("data");
        long graphId = Long.valueOf((String)linkedHashMap.get("graphId")) ;
        return graphId;
    }

    /**
     * 将卡号转换为图数据存储的id
     *
     * @param graphId:     图id
     * @param cardNumbers: 卡号的集合
     * @return: java.util.List<java.lang.String>
     **/
    public List<String> transferGraphKeyId(long graphId, List<String> cardNumbers) {

        return cardNumbers.stream().filter(Objects::nonNull).map(card ->
                String.valueOf(AthenaGdbOperations.createKeyId(Constants.AthenaGdbConstants.SCHEMA
                        , graphId
                        , Constants.AthenaGdbConstants.BANK_CARD
                        , new StringBuilder(Constants.AthenaGdbConstants.BANK_CARD)
                                .append(Constants.AthenaGdbConstants.OLD_GRAPH_ID_CONNECTOR)
                                .append(card).toString()))

        ).collect(Collectors.toList());
    }
}
