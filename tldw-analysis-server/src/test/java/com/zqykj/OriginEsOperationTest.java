/**
 * @作者 Mcj
 */
package com.zqykj;


import com.zqykj.app.service.dao.TeacherInfoDao;
import com.zqykj.app.service.interfaze.IAssetTrendsTactics;
import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.app.service.strategy.AggregateResultConversionAccessor;
import com.zqykj.app.service.vo.tarde_statistics.TradeStatisticalAnalysisQueryRequest;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.*;
import com.zqykj.common.response.AssetTrendsResponse;
import com.zqykj.common.response.TimeGroupTradeAmountSum;
import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.domain.EntityClass;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.PeopleArea;
import com.zqykj.domain.graph.EntityGraph;
import com.zqykj.domain.graph.LinkGraph;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.factory.QueryRequestParamFactory;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DefaultQueryParam;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import com.zqykj.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class OriginEsOperationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EntranceRepository entranceRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ITransactionStatistics iTransactionStatistics;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private IAssetTrendsTactics iAssetTrendsTactics;

    @Autowired
    private AggregateResultConversionAccessor aggregateResultConversionAccessor;

    private static Map<String, ? extends Class<?>> aggregateNameForClass;

    static {
        // true：默认TypeFilter生效，这种模式会查询出许多不符合你要求的class名
        // false：关闭默认TypeFilter
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false);
        // 接口不会被扫描，其子类会被扫描出来
        provider.addIncludeFilter(new AssignableTypeFilter(BaseAggregationBuilder.class));

        // Spring会将 .换成/  ("."-based package path to a "/"-based)
        // Spring拼接的扫描地址：classpath*:xxx/xxx/xxx/**/*.class
        // Set<BeanDefinition> scanList = provider.findCandidateComponents("com.p7.demo.scanclass");
        // org.elasticsearch.search.aggregations.metrics 指标聚合
        // org.elasticsearch.search.aggregations.bucket  桶聚合
        // org.elasticsearch.search.aggregations.pipeline 管道聚合
        Set<BeanDefinition> scanList = provider.findCandidateComponents("org.elasticsearch.search.aggregations.metrics");
        System.out.println(scanList.size());
//        for (BeanDefinition beanDefinition : scanList) {
//            System.out.println(beanDefinition.getBeanClassName());
//        }

        Set<? extends Class<?>> beanClasses = scanList.stream().map(beanDefinition -> {
            try {
                return Class.forName(beanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toSet());

        aggregateNameForClass = beanClasses.stream().collect(Collectors.toMap(
                beanClass2 -> {
                    Field field = ReflectionUtils.findRequiredField(beanClass2, "NAME");
                    if (field.getType().isAssignableFrom(String.class)) {
                        try {
                            Object value = field.get(beanClass2);
                            if (value == null) {
                                return "UNKNOWN";
                            }
                            return value.toString();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            return "UNKNOWN";
                        }
                    }
                    return "UNKNOWN";
                },
                beanClass2 -> beanClass2,
                (v1, v2) -> v1
        ));

//        aggregateNameAndClass.forEach((key, value) -> {
//            log.info("aggregate name : {} , class name : {}", key, value.getSimpleName());
//        });
    }


    @Test
    public void testTeacherById() throws Exception {

        EntranceRepository entranceRepository = applicationContext.getBean(EntranceRepository.class);
        Optional<TeacherInfo> teacherInfo = entranceRepository.findById("110", "22", TeacherInfo.class);
        log.info(JacksonUtils.toJson(teacherInfo.orElse(null)));
    }

    @Test
    public void testQueryAnn() {

        TeacherInfoDao teacherInfoDao = applicationContext.getBean(TeacherInfoDao.class);
        Page<TeacherInfo> teacherInfo = teacherInfoDao.matchAll(new PageRequest(0, 20, Sort.unsorted()),
                new EntityClass(TeacherInfo.class));
        log.info(JacksonUtils.toJson(teacherInfo.getContent()));
    }

    @Test
    public void testSave() {
        TeacherInfo teacherInfo = new TeacherInfo();
        teacherInfo.setAge(1);
        teacherInfo.setId("1");
        teacherInfo.setJob("test job");
        teacherInfo.setName("test name");
        teacherInfo.setSalary(new BigDecimal("1.00"));
        teacherInfo.setSex(1);
        entranceRepository.save(teacherInfo, "82c3e52e-019b-4d02-a4a3-e4fecc7f347b", TeacherInfo.class);
    }

    @Test
    public void testSaveAll() {
        List<TeacherInfo> teacherInfos = new ArrayList<>();
        for (int i = 2; i < 30; i++) {
            TeacherInfo teacherInfo = new TeacherInfo();
            teacherInfo.setAge(i);
            teacherInfo.setId("1" + i);
            teacherInfo.setJob("test job " + i);
            teacherInfo.setName("test name " + i);
            teacherInfo.setSalary(new BigDecimal("1.00"));
            teacherInfo.setSex(i);
            teacherInfos.add(teacherInfo);
        }
        entranceRepository.saveAll(teacherInfos, "61e9e22a-a6b1-4838-8cea-df8995bc2d8c", TeacherInfo.class);
    }

    @Test
    public void testSaveTransactionFlowAll() throws ParseException {
        StopWatch started = new StopWatch();
        started.start();
        List<BankTransactionFlow> bankTransactionFlows = new ArrayList<>();
        for (int i = 1; i < 7; i++) {
            BankTransactionFlow bankTransactionFlow = new BankTransactionFlow();
            bankTransactionFlow.setId((long) i);
            bankTransactionFlow.setCaseId("61e9e22a-a6b1-4838-8cea-df8995bc2d8g" + i);
            bankTransactionFlow.setResourceId("a0e16cb6b48f4516aa200fca3218574c" + i);
            bankTransactionFlow.setResourceKeyId(i + "");
            bankTransactionFlow.setBank("中国银行");
            bankTransactionFlow.setCustomerName("客户" + i);
            bankTransactionFlow.setQueryAccount("320123" + i);
            bankTransactionFlow.setQueryCard("728834032432" + i);
            bankTransactionFlow.setTransactionOppositeName("对方客户" + i);
            bankTransactionFlow.setTransactionOppositeCertificateNumber("7772343" + i);
            bankTransactionFlow.setTransactionOppositeAccount("7772343" + i);
            bankTransactionFlow.setTransactionOppositeCard("4843242" + i);
            bankTransactionFlow.setTransactionType("1" + i);
            bankTransactionFlow.setLoanFlag("进");
            bankTransactionFlow.setCurrency("CNY");
            bankTransactionFlow.setTransactionMoney(2.33 + i);
            bankTransactionFlow.setTransactionBalance(1.11 + i);
            bankTransactionFlow.setTransactionOppositeAccountOpenBank("建设银行");
            bankTransactionFlow.setTransactionSummary("test");
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
            bankTransactionFlow.setEntityGraphs(entityGraphs);
            bankTransactionFlow.setLinkGraphs(linkGraphs);
            bankTransactionFlows.add(bankTransactionFlow);
        }
        entranceRepository.saveAll(bankTransactionFlows, "61e9e22a-a6b1-4838-8cea-df8995bc2d8g", BankTransactionFlow.class);
        started.stop();
        log.info("save 10000 entity cost time = {} ms ", started.getTotalTimeMillis());
    }

    public void testAggregateClass() {


    }

    @Test
    public void scriptAggregationTest() throws IOException {

        SearchRequest request = new SearchRequest("standard_bank_transaction_flow");

        request.routing("7f071cdf-9197-479f-95a9-9ae46045cca9");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        // 对每个人账号进行分组
        TermsAggregationBuilder termsAggregationBuilderByMainAccount = new TermsAggregationBuilder("main_account_per");
        termsAggregationBuilderByMainAccount.field("customer_identity_card");
        termsAggregationBuilderByMainAccount.collectMode(Aggregator.SubAggCollectionMode.BREADTH_FIRST);
        termsAggregationBuilderByMainAccount.size(10);

        // 对每个人的卡号进行分组
        TermsAggregationBuilder termsAggregationBuilderByMainCard = new TermsAggregationBuilder("main_card_per");
        termsAggregationBuilderByMainCard.field("account_card");
        termsAggregationBuilderByMainCard.collectMode(Aggregator.SubAggCollectionMode.BREADTH_FIRST);
        termsAggregationBuilderByMainCard.size(5);

        // 筛选借贷标识为 进的
        FilterAggregationBuilder filterInBillAggregationBuilder = new FilterAggregationBuilder("in_bill",
                new TermQueryBuilder("lend_mark", "进"));
        // 借贷标识为 进的 入账总额
        SumAggregationBuilder sumInBillAggregationBuilder = new SumAggregationBuilder("in_bill_sum");
        sumInBillAggregationBuilder.field("trade_amount");
        filterInBillAggregationBuilder.subAggregation(sumInBillAggregationBuilder);


        // 筛选借贷标识为 出的
        FilterAggregationBuilder filterOutBillAggregationBuilder = new FilterAggregationBuilder("out_bill",
                new TermQueryBuilder("lend_mark", "出"));
        // 借贷标识为 出的  出账总额
        SumAggregationBuilder sumOutBillAggregationBuilder = new SumAggregationBuilder("out_bill_sum");
        sumOutBillAggregationBuilder.field("trade_amount");
        filterOutBillAggregationBuilder.subAggregation(sumOutBillAggregationBuilder);

        termsAggregationBuilderByMainCard.subAggregation(filterInBillAggregationBuilder);
        termsAggregationBuilderByMainCard.subAggregation(filterOutBillAggregationBuilder);

        Script script = new Script("params.in_amount_total + params.out_amount_total");

        Map<String, String> bucketsPath = new HashMap<>();
        bucketsPath.put("in_amount_total", "in_bill>in_bill_sum");
        bucketsPath.put("out_amount_total", "out_bill>out_bill_sum");

        // 管道聚合 bucket_script
        BucketScriptPipelineAggregationBuilder bucketScriptPipelineAggregationBuilder =
                new BucketScriptPipelineAggregationBuilder("trade_total_amount_per_card", bucketsPath, script);

        termsAggregationBuilderByMainAccount.subAggregation(bucketScriptPipelineAggregationBuilder);

        sourceBuilder.aggregation(termsAggregationBuilderByMainAccount);
        sourceBuilder.aggregation(bucketScriptPipelineAggregationBuilder);

        request.source(sourceBuilder);

        TermQueryBuilder queryBuilder = new TermQueryBuilder("test", "value");

        MaxAggregationBuilder maxAggregationBuilder = new MaxAggregationBuilder("test_max");
        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }

    @Test
    public void testDateHistogramService() {

        // 732c350f-3a2b-46d0-b9cc-0bdcc52fca93
        TradeStatisticalAnalysisPreRequest request = new TradeStatisticalAnalysisPreRequest();
        request.setCardNums(Arrays.asList("60138216660037818", "60138216660042019", "60138216660023809"));
        request.setDateRange(new DateRangeRequest("2019-04-05", "2020-03-13"));
        request.setFund("0");
        request.setOperator(AmountOperationSymbol.gte);
        TimeGroupTradeAmountSum tradeAmountByTime =
                iTransactionStatistics.getTradeAmountByTime("c94546bb87bd4b32947b576c565a94a2", request, TimeTypeRequest.h);

        log.info(JacksonUtils.toJson(tradeAmountByTime));

    }

    @Test
    public void testTradeStatisticalResultQuery() {

        TradeStatisticalAnalysisQueryRequest request = new TradeStatisticalAnalysisQueryRequest();
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
        request.setDateRange(new DateRangeRequest("2020-01-01", "2021-10-14"));
        request.setFund("0");
        request.setOperator(AmountOperationSymbol.gte);
        request.setPageRequest(new com.zqykj.common.vo.PageRequest(0, 25));
        request.setKeyword("*平安*");

        ServerResponse serverResponse = iTransactionStatistics.getTransactionStatisticsAnalysisResult("a6cbb9f86f254a92a2e1b147b5edba39", request);

        if (serverResponse.isSuccess()) {

            Object data = serverResponse.getData();

        }
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
        request.setDateRange(new DateRangeRequest("", ""));
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

    @Autowired
    QueryRequestParamFactory queryRequestParamFactory;

    @Autowired
    AggregationRequestParamFactory aggregationRequestParamFactory;
    @Test
    public void testRegionQueryAndAggs() {

        PeopleAreaRequest peopleAreaRequest = new PeopleAreaRequest();
        peopleAreaRequest.setField("province");
        peopleAreaRequest.setName("");

        peopleAreaRequest.setPaging(new PagingRequest(0,10));
        peopleAreaRequest.setSorting(new SortingRequest("String", SortingRequest.Direction.DESC));

        QuerySpecialParams querySpecialParams = queryRequestParamFactory.
                bulidPeopleAreaAnalysisRequest(peopleAreaRequest,"457eea4b3ebe46aabc604b9183a83920");

        AggregationParams aggregationParams =aggregationRequestParamFactory.createPeopleAreaQueryAgg(peopleAreaRequest);

        List<List<Object>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, PeopleArea.class, "457eea4b3ebe46aabc604b9183a83920");
        // 转换结果数据然后返回给前台
        System.out.println("********************");
        //        if (serverResponse.isSuccess()) {
//
//            Object data = serverResponse.getData();
//
//        }
    }


    @Test
    public void testRegionDetailsQueryAndAggs() {

//        PeopleAreaRequest peopleAreaRequest = new PeopleAreaRequest();
//        peopleAreaRequest.setField("province");
//        peopleAreaRequest.setName("");
//
//
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("customer_name", "b690f6b8f960462e8bb4c0f609d04830"))
//                .should(QueryBuilders.regexpQuery("account_card", "*马*"))
//                .minimumShouldMatch(1);
//
//        peopleAreaRequest.setPaging(new PagingRequest(0,10));
//        peopleAreaRequest.setSorting(new SortingRequest("String", SortingRequest.Direction.DESC));
//
//        QuerySpecialParams querySpecialParams = queryRequestParamFactory.
//                bulidPeopleAreaAnalysisRequest(peopleAreaRequest,"457eea4b3ebe46aabc604b9183a83920");
//
//        AggregationParams aggregationParams =aggregationRequestParamFactory.createPeopleAreaQueryAgg(peopleAreaRequest);
//
//        List<List<Object>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, PeopleArea.class, "457eea4b3ebe46aabc604b9183a83920");
//        // 转换结果数据然后返回给前台
//        System.out.println("********************");
//        //        if (serverResponse.isSuccess()) {
////
////            Object data = serverResponse.getData();
////
////        }


        QuerySpecialParams querySpecialParams1 = new QuerySpecialParams();

        List<CombinationQueryParams> combiningQuery = new ArrayList<>();

        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        CommonQueryParams commonQueryParams1 = new CommonQueryParams();
        commonQueryParams1.setType(QueryType.term);
        commonQueryParams1.setField("case_id");
        commonQueryParams1.setValue("b690f6b8f960462e8bb4c0f609d04830");
        combinationQueryParams.addCommonQueryParams(commonQueryParams1);
        combiningQuery.add(combinationQueryParams);

        CombinationQueryParams combinationQueryParams1 = new CombinationQueryParams();
        combinationQueryParams1.setType(ConditionType.should);
        CommonQueryParams commonQueryParams111 = new CommonQueryParams();
        commonQueryParams111.setType(QueryType.wildcard);
        commonQueryParams111.setField("province.province_wildcard");
        commonQueryParams111.setValue("*安*");
        combinationQueryParams1.addCommonQueryParams(commonQueryParams111);
        combiningQuery.add(combinationQueryParams1);

        querySpecialParams1.setCombiningQuery(combiningQuery);

        querySpecialParams1.setDefaultParam(new DefaultQueryParam());


//        entranceRepository.compoundQueryWithoutAgg(querySpecialParams1,PeopleArea.class,"b690f6b8f960462e8bb4c0f609d04830");




    }
}
