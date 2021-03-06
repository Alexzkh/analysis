/**
 * @作者 Mcj
 */
package com.zqykj;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IAssetTrendsTactics;
import com.zqykj.app.service.interfaze.IFastInFastOut;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.interfaze.ITransferAccountAnalysis;
import com.zqykj.app.service.vo.fund.FundAnalysisResultResponse;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.request.*;
import com.zqykj.common.response.AssetTrendsResponse;
import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.archive.PeopleCardInfo;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.domain.bank.PeopleArea;
import com.zqykj.domain.bank.TradeRangeOperationRecord;
import com.zqykj.domain.graph.EntityGraph;
import com.zqykj.domain.graph.LinkGraph;
import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.CRUDRepository;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.Consumer;

@SpringBootTest
@Slf4j
public class OriginEsOperationTest {

    @Autowired
    private EntranceRepository entranceRepository;

    @Autowired
    private CRUDRepository crudRepository;

    @Autowired
    private IAssetTrendsTactics iAssetTrendsTactics;

    @Autowired
    QueryRequestParamFactory queryRequestParamFactory;

    @Autowired
    AggregationRequestParamFactory aggregationRequestParamFactory;

    @Autowired
    IFastInFastOut iFastInFastOut;

    @Autowired
    private ITransferAccountAnalysis iTransferAccountAnalysis;


    @Test
    public void testSaveTransactionFlowAll() throws ParseException {
        StopWatch started = new StopWatch();
        started.start();
        List<BankTransactionFlow> bankTransactionFlows = new ArrayList<>();
        List<Map<String, ?>> values = new ArrayList<>();
        for (int i = 1; i < 7; i++) {
            Map<String, Object> value = new HashMap<>();
            BankTransactionFlow bankTransactionFlow = new BankTransactionFlow();
            bankTransactionFlow.setId((long) i);
            value.put("id", (long) i);
            bankTransactionFlow.setCaseId("61e9e22a-a6b1-4838-8cea-df8995bc2d8g" + i);
            value.put("case_id", "61e9e22a-a6b1-4838-8cea-df8995bc2d8g");
            bankTransactionFlow.setResourceId("a0e16cb6b48f4516aa200fca3218574c" + i);
            value.put("resource_id", "a0e16cb6b48f4516aa200fca3218574c" + i);
            bankTransactionFlow.setResourceKeyId(i + "");
            value.put("resource_key_id", i + "");
            bankTransactionFlow.setBank("中国银行");
            value.put("bank", "中国银行");
            bankTransactionFlow.setCustomerName("客户" + i);
            value.put("customer_name", "客户" + i);
            bankTransactionFlow.setQueryAccount("320123" + i);
            value.put("query_account", "320123" + i);
            bankTransactionFlow.setQueryCard("728834032432" + i);
            value.put("query_card", "728834032432" + i);
            bankTransactionFlow.setTransactionOppositeName("对方客户" + i);
            value.put("transaction_opposite_name", "对方客户" + i);
            bankTransactionFlow.setTransactionOppositeCertificateNumber("7772343" + i);
            value.put("transaction_opposite_certificate_number", "7772343" + i);
            bankTransactionFlow.setTransactionOppositeAccount("7772343" + i);
            value.put("transaction_opposite_account", "7772343" + i);
            bankTransactionFlow.setTransactionOppositeCard("4843242" + i);
            value.put("transaction_opposite_card", "4843242" + i);
            bankTransactionFlow.setTransactionType("1" + i);
            value.put("transaction_type", "4843242" + i);
            bankTransactionFlow.setLoanFlag("进");
            value.put("loan_flag", "进");
            bankTransactionFlow.setCurrency("CNY");
            value.put("currency", "CNY");
            bankTransactionFlow.setTransactionMoney(2.33 + i);
            value.put("transaction_money", 2.33 + i);
            bankTransactionFlow.setTransactionBalance(1.11 + i);
            value.put("transaction_balance", 1.11 + i);
            bankTransactionFlow.setTransactionOppositeAccountOpenBank("建设银行");
            value.put("transaction_opposite_account_open_bank", "建设银行");
            bankTransactionFlow.setTransactionSummary("test");
            value.put("transaction_summary", "test");
            bankTransactionFlow.setTransactionChannel("test");
            bankTransactionFlow.setTransactionNetworkName("test");
            bankTransactionFlow.setTransactionNetworkCode("1101");
            bankTransactionFlow.setLogNumber("1");
            bankTransactionFlow.setCertificateType("身份证");
            bankTransactionFlow.setCertificateNumber("11111");
            bankTransactionFlow.setCashFlag("111");
            bankTransactionFlow.setTerminalNumber("1111");
            bankTransactionFlow.setTransactionSuccessFlag("1");
            bankTransactionFlow.setTransactionPlace("地点");
            bankTransactionFlow.setMerchantNumber("34343");
            bankTransactionFlow.setIpAddress("127.0.0.1");
            bankTransactionFlow.setMacAddress("223232::232::11");
            bankTransactionFlow.setTransactionTellerNumber("1232131");
            bankTransactionFlow.setNote("备注");
//            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-07-04 12:09:44");
            bankTransactionFlow.setTradingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-07-04 12:09:44"));
            value.put("trading_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-07-04 12:09:44"));
            bankTransactionFlow.setDataSchemaId("21321dataSchemaId");
            EntityGraph entityGraph = new EntityGraph(132132131L, "bank_card", "");
            EntityGraph entityGraph1 = new EntityGraph(2222132132131L, "bank_card", "");
            List<EntityGraph> entityGraphs = new ArrayList<>();
            entityGraphs.add(entityGraph);
            entityGraphs.add(entityGraph1);
            LinkGraph linkGraph = new LinkGraph(2132131221324324L, "trade_bank_card", "");
            LinkGraph linkGraph1 = new LinkGraph(33332132131224L, "trade_bank_card", "");
            List<LinkGraph> linkGraphs = new ArrayList<>();
            linkGraphs.add(linkGraph);
            linkGraphs.add(linkGraph1);
            value.put("entity", JacksonUtils.parse(JacksonUtils.toJson(entityGraphs), new TypeReference<List<Map<String, Object>>>() {
            }));
            value.put("link", JacksonUtils.parse(JacksonUtils.toJson(linkGraphs), new TypeReference<List<Map<String, Object>>>() {
            }));
            bankTransactionFlow.setEntityGraphs(entityGraphs);
            bankTransactionFlow.setLinkGraphs(linkGraphs);
            bankTransactionFlows.add(bankTransactionFlow);
            values.add(value);
        }
        entranceRepository.saveAll(values, "61e9e22a-a6b1-4838-8cea-df8995bc2d8g", BankTransactionFlow.class);
        started.stop();
        log.info("save 10000 entity cost time = {} ms ", started.getTotalTimeMillis());
    }

    @Test
    public void testAssetTrendsResultQuery() {

        AssetTrendsRequest request = new AssetTrendsRequest();
        request.setCardNums(Arrays.asList("60138216660000014",
                "60138216660001414",
                "60138216660002814",
                "60138216660004214",
                "60138216660005614",
                "60138216660007014",
                "60138216660008414",
                "60138216660009814",
                "60138216660011214",
                "60138216660012614",
                "60138216660014014",
                "60138216660015414",
                "60138216660016814",
                "60138216660018214",
                "60138216660019614",
                "60138216660021014",
                "60138216660022414",
                "60138216660023814",
                "60138216660025214",
                "60138216660026614",
                "60138216660028014",
                "60138216660029414",
                "60138216660030814",
                "60138216660032214",
                "60138216660033614",
                "60138216660035014",
                "60138216660036414",
                "60138216660037814",
                "60138216660039214",
                "60138216660040614",
                "60138216660042014",
                "60138216660043414",
                "60138216660044814",
                "60138216660046214",
                "60138216660047614"));
        request.setDateRange(new DateRangeRequest("", "", "", ""));
        request.setFund("0");
        request.setOperator(AmountOperationSymbol.gte);
        request.setPaging(new PagingRequest(0, 25));
        request.setDateType("q");

//        ServerResponse serverResponse = iTransactionStatistics.getTransactionStatisticsAnalysisResult("a6cbb9f86f254a92a2e1b147b5edba39", request);

        List<AssetTrendsResponse> resutl = iAssetTrendsTactics.accessAssetTrendsTacticsResult("c94546bb87bd4b32947b576c565a94a2", request);
        System.out.println("********************");
        //        if (serverResponse.isSuccess()) {
//
//            Object data = serverResponse.getData();
//
//        }
    }

    @Test
    public void testRegionQueryAndAggs() {

        PeopleAreaRequest peopleAreaRequest = new PeopleAreaRequest();
        peopleAreaRequest.setField("province");
        peopleAreaRequest.setName("");

        peopleAreaRequest.setPaging(new PagingRequest(0, 10));
        peopleAreaRequest.setSorting(new SortingRequest("String", SortingRequest.Direction.DESC));

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.
                bulidPeopleAreaAnalysisRequest(peopleAreaRequest, "457eea4b3ebe46aabc604b9183a83920");

        AggregationParams aggregationParams = aggregationRequestParamFactory.createPeopleAreaQueryAgg(peopleAreaRequest);

        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, PeopleArea.class, "457eea4b3ebe46aabc604b9183a83920");
        // 转换结果数据然后返回给前台
        System.out.println("********************");
        //        if (serverResponse.isSuccess()) {
//
//            Object data = serverResponse.getData();
//
//        }
    }

    @Test
    public void testFindAllByPage() {

        // ES 默认就是 分页参数是 from + size <= 10000, 如果超过10000 相当于深度分页,
        // 需要修改集群的配置  (
        //   PUT /bank_transaction_flow/_settings
        //{
        //  "index":{
        //    "max_result_window": size
        //  }
        //}
        // )
        Page<BankTransactionFlow> bankTransactionFlows = entranceRepository.findAll(PageRequest.of(10000, 1),
                "f9ed6f2b58bf4fbc87204d4bb2d57d45", BankTransactionFlow.class);

        List<BankTransactionFlow> content = bankTransactionFlows.getContent();
    }

    @Test
    public void testFindAllByConditional() {

        // ES 默认就是 分页参数是 from + size <= 10000, 如果超过10000 相当于深度分页,
        // 需要修改集群的配置  (
        //   PUT /bank_transaction_flow/_settings
        //{
        //  "index":{
        //    "max_result_window": size
        //  }
        //}
        // )
        List<String> cards = new ArrayList<>();
        cards.add("60138216660037814");
        cards.add("60138216660018214");
        QuerySpecialParams params = new QuerySpecialParams(QueryParamsBuilders.terms("query_card", cards));
        Iterable<BankTransactionFlow> bankTransactionFlow2 = entranceRepository.findAll(BankTransactionFlow.class,
                "05e39cf6f3464d4c9b91dca359adbd97", params);
    }

    @Test
    public void testMapInsert() {

        List<Map<String, ?>> maps = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();

        map.put("person_ssn_id", "3201131994103234");
        map.put("person_name", "小马");
        map.put("person_gender", "男");
        map.put("person_nationality", "中国");
        map.put("person_ethnicity", "汉族");
        map.put("person_height", "173cm");
        map.put("person_bloodtype", "B");
        map.put("begin_time", new Date());
        map.put("end_time", new Date());
        map.put("dataSource_id", "1");

        maps.add(map);

        entranceRepository.saveAll(maps, null, PeopleCardInfo.class);
    }

    @Test
    public void specifyIndexNameInsertData() {

        Map<String, Object> map = new HashMap<>();
        map.put("adjust_card", Arrays.asList("0111120103290272049", "011112900214791", "6210676862224490842"));
        map.put("operation_date", "2022-03-01 15:04:41");
        map.put("operation_people", "测试数据插入22");
        map.put("min_amount", 7.0);
        map.put("max_amount", 8000.0);
        map.put("account_name", "测试数据插入22");
        map.put("account_id_number", "132530198111095616");
        map.put("individual_bankCards_number", 6);
        map.put("data_cateGory", 2);
        map.put("remark", "");
        map.put("case_id", "ef8307a204b24a7b9034e139c6d75252");
        entranceRepository.save(map, "ef8307a204b24a7b9034e139c6d75252", "trade_range");
    }

    @Test
    public void testCount() {

        QuerySpecialParams params = new QuerySpecialParams();

        CommonQueryParams commonQueryParams = QueryParamsBuilders.term("case_id", "7013cc43232f4127b6c95da0bf26e925");
        params.setCommonQuery(commonQueryParams);
        long count = entranceRepository.count("7013cc43232f4127b6c95da0bf26e925", BankTransactionFlow.class, params);
        System.out.println(count);
    }

    @Test
    public void testFindAll() {

        QuerySpecialParams params = new QuerySpecialParams();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term("person_name", "于*彬"));
        combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.term("bank_card_no", "622849**89510105777-转存"));

        params.addCombiningQueryParams(combinationQueryParams);
        Iterable<PeopleCardInfo> all = entranceRepository.findAll(PeopleCardInfo.class, null, params);
    }

    @Test
    public void testaccessTransferAccountAnalysis() {

        String caseId = "ffe471270eb6429ebfcc9dba92e06cf5";
        TransferAccountAnalysisRequest request = new TransferAccountAnalysisRequest();
        request.setCharacteristicRatio(new CharacteristicRatio(80, 5, 80));
        request.setOther(true);
        request.setPrecipitate(true);
        request.setTransfer(true);
        request.setSource(true);
        request.setQueryRequest(new QueryRequest(null, new PagingRequest(1, 25), new SortingRequest("tradeTotalAmount", SortingRequest.Direction.DESC)));
        FundAnalysisResultResponse<TransferAccountAnalysisResultVO> response11 = iTransferAccountAnalysis.accessTransferAccountAnalysis(request, caseId);

    }

    @Test
    public void deleteAllByCondition() {

        // 61e9e22a-a6b1-4838-8cea-df8995bc2d8g 这个案件下一共导入了6条样例数据

        // 条件查询参数构建  eg.  需要 查询 case_id = 61e9e22a-a6b1-4838-8cea-df8995bc2d8g
        QuerySpecialParams condition = QuerySpecialParams.builder()
                .commonQuery(QueryParamsBuilders.term("case_id", "61e9e22a-a6b1-4838-8cea-df8995bc2d8g"))
                .build();

        // 分页条件查询获取全部数据
        Page<BankTransactionFlow> page = entranceRepository.findAll(PageRequest.of(5, 1),
                "61e9e22a-a6b1-4838-8cea-df8995bc2d8g", BankTransactionFlow.class, condition);
        System.out.println("实际获取数据量:  " + page.getContent().size());

        // 条件查询过滤后计算总数量
        long count = entranceRepository.count("61e9e22a-a6b1-4838-8cea-df8995bc2d8g", BankTransactionFlow.class, condition);
        System.out.println("删除前总数量: " + count);

        // 条件过滤后删除
        entranceRepository.deleteAll("61e9e22a-a6b1-4838-8cea-df8995bc2d8g", BankTransactionFlow.class, condition);

        // 再次条件查询过滤后计算总数量
        long afterCount = entranceRepository.count("61e9e22a-a6b1-4838-8cea-df8995bc2d8g", BankTransactionFlow.class, condition);

        System.out.println("删除后总数量: " + afterCount);
    }

    @Test
    public void scriptQuery() {

        QuerySpecialParams query = new QuerySpecialParams();

        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, "d5a1a617103a4a61a809dd5b142b931e"));
        filter.addCommonQueryParams(QueryParamsBuilders.script("doc['trading_time'].value.get(ChronoField.ALIGNED_WEEK_OF_YEAR)== 47"));
        query.addCombiningQueryParams(filter);
        Page<BankTransactionRecord> page =
                entranceRepository.findAll(PageRequest.of(0, 10, Sort.Direction.ASC, FundTacticsAnalysisField.TRADING_TIME),
                        "d5a1a617103a4a61a809dd5b142b931e", BankTransactionRecord.class, query);
        System.out.println(page.getTotalElements());
    }

    @Test
    public void scriptQuery2() {

        QuerySpecialParams query = new QuerySpecialParams();

        CombinationQueryParams filter = new CombinationQueryParams(ConditionType.filter);
        filter.addCommonQueryParams(QueryParamsBuilders.term(FundTacticsAnalysisField.CASE_ID, "d5a1a617103a4a61a809dd5b142b931e"));
        filter.addCommonQueryParams(QueryParamsBuilders.script("doc['trading_time'].value.get(ChronoField.HOUR_OF_DAY) == " + 00));
        query.addCombiningQueryParams(filter);
        Page<BankTransactionRecord> page =
                entranceRepository.findAll(PageRequest.of(0, 10, Sort.Direction.ASC, FundTacticsAnalysisField.TRADING_TIME),
                        "d5a1a617103a4a61a809dd5b142b931e", BankTransactionRecord.class, query);
        System.out.println(page.getTotalElements());
    }

    @Test
    public void testTime() {
        LocalDateTime today = LocalDateTime.now();
        int day;
        day = today.get(ChronoField.DAY_OF_MONTH);
        System.out.println("今天是本月的第" + day + "天");
        int month;
        month = today.get(ChronoField.MONTH_OF_YEAR);
        System.out.println("今天是本年的第" + month + "月");
        int h;
        h = today.get(ChronoField.HOUR_OF_DAY);
        System.out.println("当天时间点(小时): " + h);
    }

    /**
     * <h2> 测试文本查询 </h2>
     */
    @Test
    public void queryEsTest() throws Exception {
        QuerySpecialParams query = new QuerySpecialParams();
        query.addCommonQueryParams(QueryParamsBuilders.term("case_id", "a4f80e60-0aef-4d56-adb9-fd6443fb4f2e"));
        query.addCommonQueryParams(QueryParamsBuilders.term("_id", "68315276"));
        Iterable<BankTransactionFlow> all = entranceRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "transaction_money")), "a4f80e60-0aef-4d56-adb9-fd6443fb4f2e", BankTransactionFlow.class, query);
        Optional<BankTransactionFlow> byId = entranceRepository.findById("68315276", "a4f80e60-0aef-4d56-adb9-fd6443fb4f2e", BankTransactionFlow.class);
    }

    @Test
    public void testForEach() {

    }
}
