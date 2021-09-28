/**
 * @作者 Mcj
 */
package com.zqykj;


import com.zqykj.app.service.dao.TeacherInfoDao;
import com.zqykj.core.aggregation.query.builder.AggregationMappingBuilder;
import com.zqykj.core.aggregation.query.parameters.aggregate.AggregationGeneralParameters;
import com.zqykj.core.aggregation.query.parameters.aggregate.AggregationParameters;
import com.zqykj.core.aggregation.query.parameters.aggregate.pipeline.PipelineAggregationParameters;
import com.zqykj.core.aggregation.util.aggregate.bucket.ClassNameForBeanClassOfBucket;
import com.zqykj.core.aggregation.util.aggregate.metrics.ClassNameForBeanClassOfMetrics;
import com.zqykj.core.aggregation.util.aggregate.pipeline.ClassNameForBeanClassOfPipeline;
import com.zqykj.domain.EntityClass;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.graph.EntityGraph;
import com.zqykj.domain.graph.LinkGraph;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import com.zqykj.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private ApplicationContext context;

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
            EntityGraph entityGraph = new EntityGraph(132132131L, "bank_card");
            EntityGraph entityGraph1 = new EntityGraph(2222132132131L, "bank_card");
            List<EntityGraph> entityGraphs = new ArrayList<>();
            entityGraphs.add(entityGraph);
            entityGraphs.add(entityGraph1);
            LinkGraph linkGraph = new LinkGraph(2132131221324324L, "trade_bank_card");
            LinkGraph linkGraph1 = new LinkGraph(33332132131224L, "trade_bank_card");
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
    public void testAuto() throws IOException {

        SearchRequest request = new SearchRequest("standard_bank_transaction_flow");

        request.routing("7f071cdf-9197-479f-95a9-9ae46045cca9");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        sourceBuilder.aggregation((AggregationBuilder) getClassForAggregateObject("main_account_per", "terms",
                "customer_identity_card", 3, true,
                "main_card_per",
                "terms",
                "account_card",
                5));
        request.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = search.getAggregations();
    }

    @Test
    public void testAggregationMapping() throws IOException {
        SearchRequest request = new SearchRequest("standard_bank_transaction_flow");

        request.routing("7f071cdf-9197-479f-95a9-9ae46045cca9");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        AggregationMappingBuilder aggregationMappingBuilder = new AggregationMappingBuilder(
                new ClassNameForBeanClassOfBucket(), new ClassNameForBeanClassOfPipeline(),
                new ClassNameForBeanClassOfMetrics()
        );

        AggregationParameters root = new AggregationParameters("main_account_per", "terms");
        AggregationGeneralParameters aggregationGeneralParameters = new AggregationGeneralParameters("customer_identity_card", 3);
        root.setAggregationGeneralParameters(aggregationGeneralParameters);

        AggregationParameters sub = new AggregationParameters("main_card_per", "terms");
        AggregationGeneralParameters subAggregationGeneralParameters = new AggregationGeneralParameters("account_card", 4);
        sub.setAggregationGeneralParameters(subAggregationGeneralParameters);


        AggregationParameters sub2 = new AggregationParameters("enter_bill_sum_per_card", "sum");
        AggregationGeneralParameters subAggregationGeneralParameters2 = new AggregationGeneralParameters("trade_amount");
        sub2.setAggregationGeneralParameters(subAggregationGeneralParameters2);

        PipelineAggregationParameters pipelineAggregationParameters = new PipelineAggregationParameters("total_sum_bucket", "sum_bucket", "main_card_per>enter_bill_sum_per_card");

        root.setPerPipelineAggregation(pipelineAggregationParameters);
        root.setPerSubAggregation(sub);
        sub.setPerSubAggregation(sub2);


        Object target = aggregationMappingBuilder.buildAggregationInstance(root);
        sourceBuilder.aggregation((AggregationBuilder) target);
        request.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = search.getAggregations();
    }

    public Object getClassForAggregateObject(String aggregateName, String aggregateType, String field, int size, boolean isHaveSub,
                                             String subAggregateName, String subAggregateType, String subField, int subSize) {

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

        Class<?> aggregateClass = aggregateNameForClass.get(aggregateType);

        Object target = getAggregateTarget(aggregateClass, aggregateName, aggregateType, field, size);
        if (isHaveSub) {
            Class<?> subAggregateClass = aggregateNameForClass.get(aggregateType);
            Object subTarget = getAggregateTarget(subAggregateClass, subAggregateName, subAggregateType, subField, subSize);
            setSubAggregate(target, subTarget, aggregateClass);
        }
        return target;
    }

    private Object getAggregateTarget(Class<?> aggregateClass, String aggregateName, String aggregateType, String field, int size) {

        Object target = getTargetAggregateClassViaReflection(aggregateClass, aggregateName);

        Method fieldSetMethod = ReflectionUtils.findRequiredMethod(aggregateClass, "field", String.class);
        Method collectModeSetMethod = ReflectionUtils.findRequiredMethod(aggregateClass, "collectMode", Aggregator.SubAggCollectionMode.class);
        Method sizeSetMethod = ReflectionUtils.findRequiredMethod(aggregateClass, "size", Integer.TYPE);

        org.springframework.util.ReflectionUtils.invokeMethod(fieldSetMethod, target, field);
        org.springframework.util.ReflectionUtils.invokeMethod(collectModeSetMethod, target, Aggregator.SubAggCollectionMode.BREADTH_FIRST);
        org.springframework.util.ReflectionUtils.invokeMethod(sizeSetMethod, target, size);


        return target;
    }

    private void setSubAggregate(Object target, Object subTarget, Class<?> targetClass) {

        Method subAggregation = ReflectionUtils.findRequiredMethod(targetClass, "subAggregation", AggregationBuilder.class);
        org.springframework.util.ReflectionUtils.invokeMethod(subAggregation, target, subTarget);
    }

    @SuppressWarnings("unchecked")
    protected final <R> R getTargetAggregateClassViaReflection(Class<?> baseClass, Object... constructorArguments) {
        Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(baseClass, constructorArguments);

        return constructor.map(it -> (R) BeanUtils.instantiateClass(it, constructorArguments))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "No suitable constructor found on %s to match the given arguments: %s. Make sure you implement a constructor taking these",
                        baseClass, Arrays.stream(constructorArguments).map(Object::getClass).collect(Collectors.toList()))));
    }

    @Test
    public void scanAssign() throws ClassNotFoundException {
        Set<Class<?>> scan = scan();
        for (Class<?> aClass : scan) {
            System.out.println(aClass.getSimpleName());
        }
    }

    public final Set<Class<?>> scan() throws ClassNotFoundException {
        List<String> packages = new ArrayList<>();
        packages.add("org.elasticsearch.search.aggregations.pipeline");
        if (packages.isEmpty()) {
            return Collections.emptySet();
        }
        ClassPathScanningCandidateComponentProvider scanner = createClassPathScanningCandidateComponentProvider(
                this.context);
        Set<Class<?>> entitySet = new HashSet<>();
        for (String basePackage : packages) {
            if (StringUtils.hasText(basePackage)) {
                for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                    entitySet.add(ClassUtils.forName(Objects.requireNonNull(candidate.getBeanClassName()), this.context.getClassLoader()));
                }
            }
        }
        return entitySet;
    }


    /**
     * Create a {@link ClassPathScanningCandidateComponentProvider} to scan entities based
     * on the specified {@link ApplicationContext}.
     *
     * @param context the {@link ApplicationContext} to use
     * @return a {@link ClassPathScanningCandidateComponentProvider} suitable to scan
     * entities
     * @since 2.4.0
     */
    protected ClassPathScanningCandidateComponentProvider createClassPathScanningCandidateComponentProvider(
            ApplicationContext context) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new InterfaceTypeFilter(PipelineAggregationBuilder.class));
        scanner.setEnvironment(context.getEnvironment());
        scanner.setResourceLoader(context);
        return scanner;
    }

    private static class InterfaceTypeFilter extends AssignableTypeFilter {

        /**
         *
         */
        public InterfaceTypeFilter(Class<?> targetType) {
            super(targetType);
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                throws IOException {

            return metadataReader.getClassMetadata().isInterface()

                    && super.match(metadataReader, metadataReaderFactory);
        }
    }

    public static void main(String[] args) {


//        Map<String, ? extends Class<?>> aggregateForClassName = scanList.stream().map(beanDefinition -> {
//            try {
//                return Class.forName(beanDefinition.getBeanClassName());
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }).collect(Collectors.toMap(beanClass -> {
//            assert beanClass != null;
//            Field field = ReflectionUtils.findField(beanClass, "NAME");
//            if (null == field) {
//                return "UNkNOW";
//            }
//            return field.getName();
//        }, beanClass -> beanClass));
//
//        aggregateForClassName.forEach((key, value) -> {
//            log.info("aggregate name : {}", key);
//            log.info("aggregate class name : {}", value.getSimpleName());
//        });

        // 利用上面的map 去做一个简单的测试查询
        SearchRequest request = new SearchRequest("standard_bank_transaction_flow");

        request.routing("7f071cdf-9197-479f-95a9-9ae46045cca9");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);

        // 对每个人账号进行分组
//        TermsAggregationBuilder termsAggregationBuilderByMainAccount = new TermsAggregationBuilder("main_account_per");
//        termsAggregationBuilderByMainAccount.field("customer_identity_card");
//        termsAggregationBuilderByMainAccount.collectMode(Aggregator.SubAggCollectionMode.BREADTH_FIRST);
//        termsAggregationBuilderByMainAccount.size(10);

        DateHistogramAggregationBuilder aggregationBuilder = new DateHistogramAggregationBuilder("date_test");
    }
}
