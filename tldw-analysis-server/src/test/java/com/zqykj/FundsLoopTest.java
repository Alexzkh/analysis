package com.zqykj;

import com.zqykj.app.service.factory.ITransactionPathQueryRequestFactory;
import com.zqykj.app.service.factory.param.agg.FundsLoopAggParamFactory;
import com.zqykj.app.service.factory.param.query.FundsLoopQueryRequestFactory;
import com.zqykj.app.service.interfaze.impl.FundsLoopAnalysisImpl;
import com.zqykj.app.service.strategy.analysis.proxy.BaseAPIProxy;
import com.zqykj.app.service.tools.AthenaGdbOperations;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.request.FundsLoopRequest;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.infrastructure.util.RestTemplateUtil;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description: 交易回路测试类
 * @Author zhangkehou
 * @Date 2022/1/17
 */
@SpringBootTest
@Slf4j
public class FundsLoopTest {


    private final static String URI = "http://172.30.4.99:8089";


    @Autowired
    private BaseAPIProxy baseAPIProxy;
    @Autowired
    private ITransactionPathQueryRequestFactory iTransactionPathQueryRequestFactory;

    @Autowired
    private EntranceRepository entranceRepository;

    @Autowired
    private FundsLoopAggParamFactory fundsLoopAggParamFactory;

    @Autowired
    private FundsLoopQueryRequestFactory fundsLoopQueryRequestFactory;

    @Autowired
    private FundsLoopAnalysisImpl iFundsLoopAnalysis;

    @Test
    public void fundsLoopTest() {

        String caseId = "aa63682ba50642cfaa654a42d0746421";
        HttpHeaders requestHeaders = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        requestHeaders.setContentType(type);
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());
        String addBatchEdgeUrl = URI.concat(String.format("/graph/%s/%s/graphByKey", "tldw", caseId));
        ResponseEntity<String> responseEntity = RestTemplateUtil.get(addBatchEdgeUrl, String.class);
        String body = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();

        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        try {
            map = mapper.readValue(body, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        LinkedHashMap linkedHashMap = (LinkedHashMap) map.get("data");
        System.out.println();
        long graphId = Long.valueOf((String)linkedHashMap.get("graphId"));
        Map<String, String> requestParam = new HashMap<>();
        List<String> cardList = new ArrayList<>();

        // 查找所有的调单卡号
//        List<String> cardsCollection =
        FundsLoopRequest fundsLoopRequest = FundsLoopRequest.builder()
                .operator(AmountOperationSymbol.gte)
                .fund("0")
                .build();
        QuerySpecialParams allAdjustCardsQuery = fundsLoopQueryRequestFactory.buildQueryAccountQueryRequest(fundsLoopRequest, caseId);
        AggregationParams aggregationParams = fundsLoopAggParamFactory.buildAccessAllAdjustCardsAgg();
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(allAdjustCardsQuery, aggregationParams, BankTransactionFlow.class, caseId);
        List<List<Object>> lists = result.get(aggregationParams.getResultName());
        List<String> allCards = lists.stream().map(m -> m.stream().findFirst().get().toString()).collect(Collectors.toList());

        cardList = allCards.stream().filter(Objects::nonNull).map(card ->
                String.valueOf(AthenaGdbOperations.createKeyId(Constants.AthenaGdbConstants.SCHEMA
                        , graphId
                        , Constants.AthenaGdbConstants.BANK_CARD
                        , new StringBuilder(Constants.AthenaGdbConstants.BANK_CARD)
                                .append(Constants.AthenaGdbConstants.OLD_GRAPH_ID_CONNECTOR)
                                .append(card).toString()))

        ).collect(Collectors.toList());

        CycleRequest cycleRequest = CycleRequest.builder()
                .depth(10)
                .elpTypeKeys(Arrays.asList("bank_card_trade"))
                .vfromVKeyId(cardList)
                .build();

//        System.out.println(JsonUtils.obj2String(PathRequest.builder().vfromVKeyId(fromKeyId).vtoVKeyId(toKeyId).depth(1).elpTypeKeys(Arrays.asList("bank_card_trade").build()));

        // /graph/{gid}/allpaths
        String allPathUri = URI.concat(String.format("/graph/%s/cycleDetect", "9195565582025766553"));

        Map<String, Object> objectMap = baseAPIProxy.request(allPathUri, cycleRequest, Map.class);
        iFundsLoopAnalysis.parseAthenaGdbResult(objectMap,caseId,fundsLoopRequest);
        requestParam.put("graphId", "7746113266265363756");

        System.out.println("*****");

    }
}
