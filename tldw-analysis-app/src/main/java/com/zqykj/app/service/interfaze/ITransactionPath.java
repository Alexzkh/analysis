package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.tools.AthenaGdbOperations;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.request.GraphQueryDetailRequest;
import com.zqykj.common.request.TransactionPathRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.response.TransactionPathResponse;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 交易路径分析调用图全路径分析接口
 * @Author zhangkehou
 * @Date 2021/12/11
 */
public interface ITransactionPath {

    /**
     * 获取交易路径分析调用图接口生成的路径结果
     *
     * @param request: 交易路径分析请求体（这其中包括源实体id、目标实体集合以及全路径搜索的深度、路径边的类型）
     * @param caseId:  案件编号
     * @return: com.zqykj.domain.response.TransactionPathResponse
     **/
    TransactionPathResponse accessPathAnalysisResult(TransactionPathRequest request, String caseId);

    /**
     * 根据交易流水数据的id获取详细的路径信息
     *
     * @param request: 交易路径详情查找请求体
     * @param caseId:  案件编号
     * @return: com.zqykj.domain.response.TransactionPathDetailResponse
     **/
    Page<BankTransactionFlow> accessPathAnalysisDetailResult(GraphQueryDetailRequest request, String caseId);

    /**
     * 将选择的卡号集合转换为图实体的keyid 即获取hash后的结果值.
     *
     * @param graphId:  图id编号
     * @param cardList: 卡号的集合
     * @return: java.util.List<java.lang.String>
     **/
    default List<String> convertCardToKeyId(long graphId, List<String> cardList) {
        return cardList.stream().filter(Objects::nonNull).map(card ->
                String.valueOf(AthenaGdbOperations.createKeyId(Constants.AthenaGdbConstants.SCHEMA
                        , graphId
                        , Constants.AthenaGdbConstants.BANK_CARD
                        , new StringBuilder(Constants.AthenaGdbConstants.BANK_CARD)
                                .append(Constants.AthenaGdbConstants.OLD_GRAPH_ID_CONNECTOR)
                                .append(card).toString()))

        ).collect(Collectors.toList());
    }


}
