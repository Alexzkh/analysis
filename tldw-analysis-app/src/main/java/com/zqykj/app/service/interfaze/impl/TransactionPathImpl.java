package com.zqykj.app.service.interfaze.impl;

import com.xkzhangsan.time.calculator.DateTimeCalculatorUtil;
import com.zqykj.app.service.common.service.GraphCycleAndPathCommonService;
import com.zqykj.app.service.factory.ITransactionPathQueryRequestFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.ITransactionPath;
import com.zqykj.app.service.strategy.analysis.proxy.AthenaGraphOperatorProxy;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.request.GraphQueryDetailRequest;
import com.zqykj.common.request.TransactionPathRequest;
import com.zqykj.common.vo.GraphCycleAndPathCommonParamVO;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.request.PathRequest;
import com.zqykj.domain.response.TransactionPathResponse;
import com.zqykj.domain.vo.TransactionPathResultVO;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Description: 交易路径分析业务
 * @Author zhangkehou
 * @Date 2021/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionPathImpl implements ITransactionPath {

    private final AthenaGraphOperatorProxy athenaGraphOperatorProxy;

    private final ITransactionPathQueryRequestFactory iTransactionPathQueryRequestFactory;

    private final EntranceRepository entranceRepository;

    private final GraphCycleAndPathCommonService graphCycleAndPathCommonService;

    @Override
    public TransactionPathResponse accessPathAnalysisResult(TransactionPathRequest request, String caseId) {
        TransactionPathResponse transactionPathResponse = new TransactionPathResponse();
        try {
            // 1、获取图id。
            long graphId = athenaGraphOperatorProxy.accessGraphId(caseId);

            // 2、构建左右顶点集合id
            List<String> left = convertCardToKeyId(graphId, request.getLeft());
            List<String> right = convertCardToKeyId(graphId, request.getRight());

            // 3、构建路径查找请求体
            PathRequest pathRequest = PathRequest.builder()
                    .depth(request.getDepth())
                    .direction(request.getDirection().toString())
                    .vfromVKeyId(left)
                    .vtoVKeyId(right)
                    .elpTypeKeys(Arrays.asList(Constants.AthenaGdbConstants.BANK_CARD_TRADE))
                    .build();

            // 4、获取图路径结果
            Map<String, Object> accessPathResult = athenaGraphOperatorProxy.accessTransactionPathResult(graphId, pathRequest);
            List<TransactionPathResultVO> results = parseAthenaGdbResult(accessPathResult, request, caseId);
            // 需要对数据进行内存排序和分页
            transactionPathResponse.setResults(results);
            transactionPathResponse.doOrderOrPaging(request.getQueryRequest().getPaging().getPage()
                    , request.getQueryRequest().getPaging().getPageSize()
                    , request.getQueryRequest().getSorting().getProperty()
                    , request.getQueryRequest().getSorting().getOrder().toString()
            );

        } catch (Exception e) {
            log.error(" transaction path analysis error {}", e);
            throw new RuntimeException(" transaction path analysis error");
        }

        return transactionPathResponse;
    }

    @Override
    public Page<BankTransactionFlow> accessPathAnalysisDetailResult(GraphQueryDetailRequest request, String caseId) {
        QuerySpecialParams querySpecialParams = iTransactionPathQueryRequestFactory.accessTransactionPathDetailRequest(request, caseId);
        PageRequest destPageRequest = new PageRequest(request.getQueryRequest().getPaging().getPage()
                , request.getQueryRequest().getPaging().getPageSize()
                , Sort.by(request.getQueryRequest().getSorting().getOrder().isDescending()
                        ? Sort.Direction.DESC : Sort.Direction.ASC
                , request.getQueryRequest().getSorting().getProperty()));
        Page<BankTransactionFlow> result = entranceRepository.findAll(destPageRequest, caseId, BankTransactionFlow.class, querySpecialParams);
        return result;
    }


    /***
     * 解析图路径分析结果
     *
     * @param result: 图返回的数据集，待解析的原始数据
     * @param request: 交易路径分析请求参数
     * @param caseId: 案件编号
     * @return: java.util.List<com.zqykj.domain.vo.TransactionPathResultVO>
     **/
    private List<TransactionPathResultVO> parseAthenaGdbResult(Map<String, Object> result, TransactionPathRequest request, String caseId) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        List<TransactionPathResultVO> results = new ArrayList<>();
        try {
            LinkedHashMap<String, Object> dataResult = mapper.readValue((String) result.get(Constants.AthenaGdbConstants.DATA), new org.codehaus.jackson.type.TypeReference<LinkedHashMap<String, Object>>() {
            });
            for (Map.Entry<String, Object> entry : dataResult.entrySet()) {
                List<List<LinkedHashMap<String, Object>>> path = (List<List<LinkedHashMap<String, Object>>>) entry.getValue();
                path.stream().forEach(pat -> {
                    /**
                     * 获取每一条路径下的对应总表数据的id,用于查找路径详情使用
                     * */
                    List<String> rowIdsResult = graphCycleAndPathCommonService.getRowIds(pat);
                    /**
                     * 交易路径分析需要展示的结果是路径上起始节点和结束节点的相关数据，因此此时就需要拿到返回的结果中的第一个元素和最后一个元素。
                     * 由于返回的图路径结果是逆序的。所以起始节点为列表中最后一个元素，而结束节点为列表中第一个元素。
                     * eg: 路径为 A->C->D->B  需要展示结果为 A->B 而图路径返回的列表数据：{D->B,C->D,A-C}
                     * */
                    LinkedHashMap<String, Object> firstNode = pat.stream().findFirst().orElse(pat.get(pat.size() - 1));
                    LinkedHashMap<String, Object> lastNode = pat.stream().skip(pat.size() - 1).findFirst().orElse(null);
                    GraphCycleAndPathCommonParamVO graphCycleAndPathCommonParamVO = new GraphCycleAndPathCommonParamVO();
                    Page<BankTransactionFlow> sourceFlow = graphCycleAndPathCommonService.getHeadAndTailNode(firstNode, graphCycleAndPathCommonParamVO.build(request), caseId);
                    Page<BankTransactionFlow> destFlow = graphCycleAndPathCommonService.getHeadAndTailNode(lastNode, graphCycleAndPathCommonParamVO.build(request), caseId);
                    if (!ObjectUtils.isEmpty(sourceFlow.getContent().get(0)) && !ObjectUtils.isEmpty(destFlow.getContent().get(0))) {
                        long timeSpan = Math.abs(DateTimeCalculatorUtil.betweenTotalDays(sourceFlow.getContent().get(0).getTradingTime(), destFlow.getContent().get(0).getTradingTime()));
                        TransactionPathResultVO transactionPathResultVO = this.builder(sourceFlow.getContent().get(0), destFlow.getContent().get(0), timeSpan, rowIdsResult);
                        results.add(transactionPathResultVO);
                    }
                });
            }
        } catch (Exception e) {
            log.error("parse athenaGdb result error {}", e);
            throw new RuntimeException("parse athenaGdb result error");
        }

        return results;
    }

    /**
     * 构建交易路径对象
     *
     * @param source:       起始路径
     * @param dest:         终点路径
     * @param timeSpan:     时间间隔
     * @param rowIdResults: 每条路径对应原始数据的集合
     * @return: com.zqykj.domain.vo.TransactionPathResultVO
     **/
    public TransactionPathResultVO builder(BankTransactionFlow source, BankTransactionFlow dest, long timeSpan, List<String> rowIdResults) {

        return TransactionPathResultVO.builder()
                .destAccount(dest.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? dest.getTransactionOppositeAccount()
                        : dest.getQueryAccount())
                .destTransactionTime(dest.getTradingTime())
                .destIdentityCard(dest.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? dest.getTransactionOppositeCertificateNumber()
                        : dest.getCustomerIdentityCard())
                .destName(dest.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? dest.getTransactionOppositeName()
                        : dest.getCustomerName())
                .destTransactionMoney(new BigDecimal(dest.getTransactionMoney()).setScale(2, RoundingMode.HALF_UP))
                .sourceAccount(source.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? source.getQueryAccount()
                        : source.getTransactionOppositeAccount())
                .sourceIdentityCard(source.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? source.getCustomerIdentityCard()
                        : source.getTransactionOppositeCertificateNumber())
                .sourceName(source.getLoanFlag().equals(FundTacticsAnalysisField.LOAN_FLAG_OUT) ? source.getCustomerName()
                        : source.getTransactionOppositeName())
                .sourceTransactionMoney(new BigDecimal(source.getTransactionMoney()).setScale(2, RoundingMode.HALF_UP))
                .sourceTransactionTime(source.getTradingTime())
                .timeSpan(timeSpan)
                .ids(rowIdResults)
                .build();
    }

}
