package com.zqykj.app.service.common.service;

import com.zqykj.app.service.factory.ITransactionPathQueryRequestFactory;
import com.zqykj.common.request.GraphQueryDetailRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: 交易路径和资金回路
 * @Author zhangkehou
 * @Date 2022/1/20
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GraphCycleAndPathDetailService {

    private final ITransactionPathQueryRequestFactory iTransactionPathQueryRequestFactory;

    private final EntranceRepository entranceRepository;

    /**
     * @param request: 图结果列表中对应详情查找的入参
     * @param caseId:  案件编号
     * @return: com.zqykj.domain.Page<com.zqykj.domain.bank.BankTransactionFlow>
     **/
    public Page<BankTransactionFlow> accessPathAnalysisDetailResult(GraphQueryDetailRequest request, String caseId) throws Exception {
        QuerySpecialParams querySpecialParams = iTransactionPathQueryRequestFactory.accessTransactionPathDetailRequest(request, caseId);
        PageRequest destPageRequest = new PageRequest(request.getQueryRequest().getPaging().getPage()
                , request.getQueryRequest().getPaging().getPageSize()
                , Sort.by(request.getQueryRequest().getSorting().getOrder().isDescending()
                        ? Sort.Direction.DESC : Sort.Direction.ASC
                , request.getQueryRequest().getSorting().getProperty()));
        Page<BankTransactionFlow> result = entranceRepository.findAll(destPageRequest, caseId, BankTransactionFlow.class, querySpecialParams);
        return result;
    }
}
