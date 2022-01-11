package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.chain.TransferAccountAnalysisResultHandlerChain;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.param.agg.TransferAccountAggRequestParamFactory;
import com.zqykj.app.service.factory.param.query.TransferAccountQueryRequestFactory;
import com.zqykj.app.service.interfaze.ITransferAccountAnalysis;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.app.service.vo.fund.TransferAccountAnalysisResult;
import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.response.TransferAccountAnlysisResultResponse;
import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 调单账号特征分析业务请求接口实现类
 * @Author zhangkehou
 * @Date 2021/12/24
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferAccountAnalysisImpl implements ITransferAccountAnalysis {

    private final TransferAccountAggRequestParamFactory transferAccountAggRequestParamFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final TransferAccountQueryRequestFactory transferAccountQueryRequestFactory;

    private final EntranceRepository entranceRepository;

    private final TransferAccountAnalysisResultHandlerChain chain;


    @Override
    public FundAnalysisResultResponse<TransferAccountAnalysisResultVO> accessTransferAccountAnalysis(TransferAccountAnalysisRequest request, String caseId) {

        FundAnalysisResultResponse<TransferAccountAnalysisResultVO> response = new FundAnalysisResultResponse<>();
        try {
            // 获取给定条件下所以的调单卡号
            List<String> allCards = accessAllTransferAccount(request, caseId);
            QuerySpecialParams querySpecialParams = transferAccountQueryRequestFactory.buildTransferAccountAnalysisQueryRequest(request, caseId, allCards);
            AggregationParams aggregationParams = transferAccountAggRequestParamFactory.buildTransferAccountAgg(request);
            Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);
            List<String> resultListTitles = new ArrayList<>(aggregationParams.getEntityAggColMapping().keySet());
            List<Map<String, Object>> resultListEntityMapping = aggregationResultEntityParseFactory.convertEntity(
                    result.get(aggregationParams.getResultName()), resultListTitles, TransferAccountAnalysisResult.class
            );
            // 获取调单卡号聚合结果
            List<TransferAccountAnalysisResult> fundsResultList = JacksonUtils.parse(JacksonUtils.toJson(resultListEntityMapping), new TypeReference<List<TransferAccountAnalysisResult>>() {
            });
            List<TransferAccountAnalysisResultVO> resultVOList = new ArrayList<>();
            fundsResultList.stream().forEach(fund -> {
                TransferAccountAnalysisResultVO vo = new TransferAccountAnalysisResultVO();
                fund.setCreditsAmount(fund.getCreditsAmount().setScale(2, RoundingMode.HALF_UP));
                fund.setPayOutAmount(fund.getPayOutAmount().setScale(2, RoundingMode.HALF_UP));
                fund.setTradeNet(fund.getTradeNet().setScale(2, RoundingMode.HALF_UP));
                fund.setTradeTotalAmount(fund.getTradeTotalAmount().setScale(2, RoundingMode.HALF_UP));
                BeanUtils.copyProperties(fund, vo);
                resultVOList.add(vo);
            });

            // 计算调单账号特征
            resultVOList.stream().forEach(v -> chain.handle(v, request));

            // 对调单账号特征分析结果进行分页、排序
            TransferAccountAnlysisResultResponse resultResponse = TransferAccountAnlysisResultResponse.builder()
                    .results(resultVOList)
                    .build();
            Integer size = request.getQueryRequest().getPaging().getPageSize();
            Integer page = request.getQueryRequest().getPaging().getPage();
            Integer total = fundsResultList.size();
            resultResponse.doOrderOrPaging(page, size
                    , request.getQueryRequest().getSorting().getProperty()
                    , request.getQueryRequest().getSorting().getOrder().toString());

            response.setContent(resultResponse.provideSortList());
            response.setSize(size);
            response.setTotal(total);
            response.setTotalPages(PageRequest.getTotalPages(total, size));
        } catch (Exception e) {
            log.error("调单账号特征分析结果失败：{}", e);
        }

        return response;
    }


    /**
     * 获取案件域给定条件下所有的调单账号数据
     *
     * @param request: 调单账号特征分析请求体
     * @param caseId:  案件编号
     * @return: java.util.List<java.lang.String>
     **/
    private List<String> accessAllTransferAccount(TransferAccountAnalysisRequest request, String caseId) throws Exception {
        QuerySpecialParams allAdjustCardsQuery = transferAccountQueryRequestFactory.buildTransaferAccountQueryRequest(request, caseId);
        AggregationParams aggregationParams = transferAccountAggRequestParamFactory.buildAccessAllAdjustCardsAgg();
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(allAdjustCardsQuery, aggregationParams, BankTransactionFlow.class, caseId);
        List<List<Object>> lists = result.get(aggregationParams.getResultName());
        List<String> allCards = lists.stream().map(m -> m.stream().findFirst().get().toString()).collect(Collectors.toList());
        return allCards;
    }

}
