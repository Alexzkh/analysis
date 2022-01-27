package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.common.service.GraphCycleAndPathCommonService;
import com.zqykj.app.service.factory.param.agg.FundsLoopAggParamFactory;
import com.zqykj.app.service.factory.param.query.FundsLoopQueryRequestFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFundsLoopAnalysis;
import com.zqykj.app.service.strategy.analysis.proxy.AthenaGraphOperatorProxy;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.request.FundsLoopRequest;
import com.zqykj.common.vo.GraphCycleAndPathCommonParamVO;
import com.zqykj.domain.Page;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.request.CycleRequest;
import com.zqykj.domain.response.FundsLoopResponse;
import com.zqykj.domain.vo.FundsLoopResultListVO;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 资金回路分析业务实现类
 * @Author zhangkehou
 * @Date 2022/1/18
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundsLoopAnalysisImpl implements IFundsLoopAnalysis {

    private final FundsLoopQueryRequestFactory fundsLoopQueryRequestFactory;

    private final FundsLoopAggParamFactory fundsLoopAggParamFactory;

    private final EntranceRepository entranceRepository;

    private final AthenaGraphOperatorProxy proxy;

    private final GraphCycleAndPathCommonService graphCycleAndPathCommonService;

    @Override
    public FundsLoopResponse accessFundsLoopAnalysisResult(FundsLoopRequest request, String caseId) {

        FundsLoopResponse fundsLoopResponse = new FundsLoopResponse();
        try {
            // 1、获取图id
            long graphId = proxy.accessGraphId(caseId);

            // 查询全部调单卡号时就是一个卡号都不选择的情况
            List<String> cards = request.getIndividual();
            if (CollectionUtils.isEmpty(cards)) {
                // 2、获取所有的调单卡号
                cards = accessAllIndividualCards(request, caseId, graphId);
            }

            // 3、将所有卡转为图的key id
            List<String> graphKeys = proxy.transferGraphKeyId(graphId, cards);

            // 4、构建图查找参数
            CycleRequest cycleRequest = CycleRequest.builder()
                    .depth(10)
                    .vfromVKeyId(graphKeys)
                    .elpTypeKeys(Arrays.asList(Constants.AthenaGdbConstants.BANK_CARD_TRADE))
                    .build();

            Map<String, Object> objectMap = proxy.accessCycleDetect(graphId, cycleRequest);
            List<FundsLoopResultListVO> result = parseAthenaGdbResult(objectMap, caseId, request);
            // 5、需要对数据进行内存排序和分页
            fundsLoopResponse.setResults(result);
            fundsLoopResponse.doOrderOrPaging(request.getQueryRequest().getPaging().getPage()
                    , request.getQueryRequest().getPaging().getPageSize()
                    , request.getQueryRequest().getSorting().getProperty()
                    , request.getQueryRequest().getSorting().getOrder().toString()
            );

        } catch (Exception e) {
            log.error("获取资金回路结果出错：{}", e);

        }
        return fundsLoopResponse;
    }


    /***
     * 解析图路径分析结果
     *
     * @param result: 图返回的数据集，待解析的原始数据
     * @return: java.util.List<com.zqykj.domain.vo.TransactionPathResultVO>
     **/
    public List<FundsLoopResultListVO> parseAthenaGdbResult(Map<String, Object> result, String caseId, FundsLoopRequest request) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Set<FundsLoopResultListVO> noRepeatValue = new HashSet<>();
        try {
            LinkedHashMap<String, Object> dataResult = mapper.readValue((String) result.get(Constants.AthenaGdbConstants.DATA), new org.codehaus.jackson.type.TypeReference<LinkedHashMap<String, Object>>() {
            });
            for (Map.Entry<String, Object> entry : dataResult.entrySet()) {
                List<List<LinkedHashMap<String, Object>>> path = (List<List<LinkedHashMap<String, Object>>>) entry.getValue();
                path.stream().forEach(pat -> {
                    List<String> rowIdsResult = graphCycleAndPathCommonService.getRowIds(pat);
                    LinkedHashMap<String, Object> firstNode = pat.stream().findFirst().orElse(pat.get(pat.size() - 1));
                    LinkedHashMap<String, Object> lastNode = pat.stream().skip(pat.size() - 1).findFirst().orElse(null);
                    // 执行查询操作，并获取到分页结果
                    GraphCycleAndPathCommonParamVO graphCycleAndPathCommonParamVO = new GraphCycleAndPathCommonParamVO();
                    Page<BankTransactionFlow> sourceFlow = graphCycleAndPathCommonService.getHeadAndTailNode(firstNode, graphCycleAndPathCommonParamVO.build(request), caseId);
                    Page<BankTransactionFlow> destFlow = graphCycleAndPathCommonService.getHeadAndTailNode(lastNode, graphCycleAndPathCommonParamVO.build(request), caseId);
                    if (!ObjectUtils.isEmpty(sourceFlow.getContent().get(0)) && !ObjectUtils.isEmpty(destFlow.getContent().get(0))) {
                        FundsLoopResultListVO fundsLoopResultListVO = this.builder(sourceFlow.getContent().get(0), destFlow.getContent().get(0), rowIdsResult);
                        noRepeatValue.add(fundsLoopResultListVO);
                    }
                });
            }
        } catch (Exception e) {
            log.error("parse athenaGdb result error {}", e);
            throw new RuntimeException("parse athenaGdb result error");
        }
        List<FundsLoopResultListVO> results = noRepeatValue.stream().collect(Collectors.toList());
        return results;
    }


    /**
     * 构建交易路径对象
     *
     * @param source:       起始路径
     * @param dest:         终点路径
     * @param rowIdResults: 每条路径对应原始数据的集合
     * @return: com.zqykj.domain.vo.TransactionPathResultVO
     **/
    public FundsLoopResultListVO builder(BankTransactionFlow source, BankTransactionFlow dest, List<String> rowIdResults) {

        return FundsLoopResultListVO.builder()
                .latestTransactionAccount(dest.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? dest.getTransactionOppositeAccount()
                        : dest.getQueryAccount())
                .latestTransactionTradingTime(dest.getTradingTime())
                .latestTransactionIdentityCard(dest.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? dest.getTransactionOppositeCertificateNumber()
                        : dest.getCustomerIdentityCard())
                .latestTransactionName(dest.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? dest.getTransactionOppositeName()
                        : dest.getCustomerName())
                .latestTransactionMoney(new BigDecimal(dest.getTransactionMoney()).setScale(2, RoundingMode.HALF_UP))
                .earliestTransactionAccount(source.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? source.getQueryAccount()
                        : source.getTransactionOppositeAccount())
                .earliestTransactionIdentityCard(source.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? source.getCustomerIdentityCard()
                        : source.getTransactionOppositeCertificateNumber())
                .earliestTransactionName(source.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? source.getCustomerName()
                        : source.getTransactionOppositeName())
                .earliestTransactionMoney(new BigDecimal(source.getTransactionMoney()).setScale(2, RoundingMode.HALF_UP))
                .earliestTransactionTradingTime(source.getTradingTime())
                .ids(rowIdResults)
                .build();
    }

    /**
     * 获取案件域下所有的调单卡号
     *
     * @param request: 资金回路请求体
     * @param caseId:  案件编号
     * @param graphId: 图id
     * @return: java.util.List<java.lang.String>
     **/
    private List<String> accessAllIndividualCards(FundsLoopRequest request, String caseId, long graphId) {
        QuerySpecialParams allAdjustCardsQuery = fundsLoopQueryRequestFactory.buildQueryAccountQueryRequest(request, caseId);
        AggregationParams aggregationParams = fundsLoopAggParamFactory.buildAccessAllAdjustCardsAgg();
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(allAdjustCardsQuery, aggregationParams, BankTransactionFlow.class, caseId);
        List<List<Object>> lists = result.get(aggregationParams.getResultName());
        List<String> allCards = lists.stream().map(m -> m.stream().findFirst().get().toString()).collect(Collectors.toList());
        return allCards;
    }
}
