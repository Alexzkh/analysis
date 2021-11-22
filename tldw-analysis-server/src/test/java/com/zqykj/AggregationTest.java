package com.zqykj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zqykj.app.service.interfaze.IFundsSourceAndDestinationStatistics;
import com.zqykj.app.service.strategy.analysis.impl.FundSourceAndDestinationFactory;
import com.zqykj.app.service.strategy.analysis.impl.TransactionAmountStrategyImpl;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.enums.FundsResultType;
import com.zqykj.common.enums.FundsSourceAndDestinationStatisticsType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.*;
import com.zqykj.common.response.AggregationResult;
import com.zqykj.common.response.PersonalStatisticsResponse;
import com.zqykj.domain.Range;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.enums.AggsType;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: aggregations test .
 * @Author zhangkehou
 * @Date 2021/9/18
 */
@SpringBootTest
@Slf4j
public class AggregationTest {

    @Autowired
    EntranceRepository entranceRepository;

    /**
     * 选择个体聚合操作：聚合证件号码以及证件号码下挂的卡号的调单账号数量、交易总次数、入账笔数，入账金额、出账笔数、出账金额、交易总金额、最早交易时间、最晚交易时间
     * POST standard_bank_transaction_flow/_search?routing=7f071cdf-9197-479f-95a9-9ae46045cca9
     * {
     * "aggs": {
     * "terms_custom_identity_id": {
     * "terms": {
     * "field": "customer_identity_card",
     * "size": 25,
     * "collect_mode": "breadth_first"
     * },
     * "aggs": {
     * "terms_account_card": {
     * "terms": {
     * "field": "account_card",
     * "size": 1000,
     * "collect_mode": "breadth_first"
     * },
     * "aggs": {
     * "min_date": {
     * "min": {
     * "field": "trade_time"
     * }
     * },
     * "max_date": {
     * "max": {
     * "field": "trade_time"
     * }
     * },
     * "sum_trade_amount": {
     * "sum": {
     * "field": "trade_amount"
     * }
     * },
     * "terms_lend_mark": {
     * "terms": {
     * "field": "lend_mark"
     * },
     * "aggs": {
     * "sum_trade_amount": {
     * "sum": {
     * "field": "trade_amount"
     * }
     * }
     * }
     * }
     * }
     * },
     * "terms_lend_mark": {
     * "terms": {
     * "field": "lend_mark"
     * },
     * "aggs": {
     * "sum_trade_amount": {
     * "sum": {
     * "field": "trade_amount"
     * }
     * }
     * }
     * },
     * "min_date": {
     * "min": {
     * "field": "trade_time"
     * }
     * },
     * "max_date": {
     * "max": {
     * "field": "trade_time"
     * }
     * }
     * }
     * }
     * },
     * "size": 0
     * }
     */

    @Autowired
    private IFundsSourceAndDestinationStatistics iFundsSourceAndDestinationStatistics;

    @Test
    public void multilayerAggsTest() throws JsonProcessingException {
        AggregateBuilder aggregateBuilder1 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.min)
                .build();

        AggregateBuilder aggregateBuilder2 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.max)
                .build();

        AggregateBuilder aggregateBuilder3 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FOURTH_AGGREGATE_NAME)
                .aggregateType(AggsType.sum)
                .build();

        List<AggregateBuilder> aggregateBuilders = new ArrayList<>();
        aggregateBuilders.add(aggregateBuilder3);
        AggregateBuilder aggregateBuilder4 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.THIRD_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .subAggregations(aggregateBuilders)
                .size(10)
                .build();


        List<AggregateBuilder> secondBuilders = new ArrayList<>();
        secondBuilders.add(aggregateBuilder4);
        secondBuilders.add(aggregateBuilder3);
        secondBuilders.add(aggregateBuilder2);
        secondBuilders.add(aggregateBuilder1);

        AggregateBuilder secondBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.SECOND_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(Constants.Individual.SECOND_AGGS_SIZE)
                .subAggregations(secondBuilders)
                .build();


        List<AggregateBuilder> firstBuilders = new ArrayList<>();
        firstBuilders.add(secondBuilder);
        firstBuilders.add(aggregateBuilder4);
        firstBuilders.add(aggregateBuilder3);
        firstBuilders.add(aggregateBuilder2);
        firstBuilders.add(aggregateBuilder1);

        AggregateBuilder aggregateBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIRST_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(Constants.Individual.FIRST_AGGS_SIZE)
                .subAggregations(firstBuilders)
                .build();

        Map map = entranceRepository.multilayerAggs(aggregateBuilder, StandardBankTransactionFlow.class);

        map.get("terms_customer_identity_card");


        System.out.println("********************");

    }


    @Test
    public void multilayerAggsTestWithoutCard() throws JsonProcessingException {
        AggregateBuilder aggregateBuilder1 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.min)
                .build();

        AggregateBuilder aggregateBuilder2 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.max)
                .build();

        AggregateBuilder aggregateBuilder3 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FOURTH_AGGREGATE_NAME)
                .aggregateType(AggsType.sum)
                .build();

        List<AggregateBuilder> aggregateBuilders = new ArrayList<>();
        aggregateBuilders.add(aggregateBuilder3);
        AggregateBuilder aggregateBuilder4 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.THIRD_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .subAggregations(aggregateBuilders)
                .size(10)
                .build();

        AggregateBuilder aggregateBuilder5 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.SECOND_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(10000)
                .build();


        List<AggregateBuilder> firstBuilders = new ArrayList<>();
        firstBuilders.add(aggregateBuilder5);
        firstBuilders.add(aggregateBuilder4);
        firstBuilders.add(aggregateBuilder3);
        firstBuilders.add(aggregateBuilder2);
        firstBuilders.add(aggregateBuilder1);


        QueryParams queryParams = QueryParams.builder()
                .queryType(QueryType.term)
                .value("452426198109252154")
                .field("customer_identity_card")
                .build();

        AggregateBuilder aggregateBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIRST_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(Constants.Individual.FIRST_AGGS_SIZE)
                .subAggregations(firstBuilders)
                .queryParams(queryParams)
                .build();


        Map map = entranceRepository.multilayerAggs(aggregateBuilder, StandardBankTransactionFlow.class);

        map.get("terms_customer_identity_card");


        System.out.println("********************");

    }


    @Test
    public void multilayerPeopleAggsTest() throws JsonProcessingException, ParseException {
        AggregateBuilder aggregateBuilder1 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.min)
                .build();

        AggregateBuilder aggregateBuilder2 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.max)
                .build();

        AggregateBuilder aggregateBuilder3 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FOURTH_AGGREGATE_NAME)
                .aggregateType(AggsType.sum)
                .build();

        List<AggregateBuilder> aggregateBuilders = new ArrayList<>();
        aggregateBuilders.add(aggregateBuilder3);
        AggregateBuilder aggregateBuilder4 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.THIRD_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .subAggregations(aggregateBuilders)
                .size(10)
                .build();


//        List<AggregateBuilder> secondBuilders = new ArrayList<>();
//        secondBuilders.add(aggregateBuilder4);
//        secondBuilders.add(aggregateBuilder3);
//        secondBuilders.add(aggregateBuilder2);
//        secondBuilders.add(aggregateBuilder1);
//
//        AggregateBuilder secondBuilder = AggregateBuilder.builder()
//                .aggregateName(Constants.Individual.SECOND_AGGREGATE_NAME)
//                .aggregateType(AggsType.terms)
//                .size(Constants.Individual.SECOND_AGGS_SIZE)
//                .subAggregations(secondBuilders)
//                .build();
        AggregateBuilder aggregateBuilder5 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.SECOND_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(10000)
                .build();
        AggregateBuilder aggregateBuilder6 = AggregateBuilder.builder()
                .aggregateName("bucket_sort")
                .aggregateType(AggsType.terms)
                .size(10000)
                .build();

        List<AggregateBuilder> firstBuilders = new ArrayList<>();
        firstBuilders.add(aggregateBuilder5);
        firstBuilders.add(aggregateBuilder4);
        firstBuilders.add(aggregateBuilder3);
        firstBuilders.add(aggregateBuilder2);
        firstBuilders.add(aggregateBuilder1);

        AggregateBuilder aggregateBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIRST_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(Constants.Individual.FIRST_AGGS_SIZE)
                .subAggregations(firstBuilders)
                .build();

        Map map = entranceRepository.multilayerAggs(aggregateBuilder, StandardBankTransactionFlow.class);
        List list = (List) map.get("terms_customer_identity_card");
        List<AggregationResult> personalStatisticsResponses = new ArrayList<>();
        List<PersonalStatisticsResponse> responses = new ArrayList<>();
        list.stream().forEach(map1 -> {
            ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            ;
            AggregationResult aggregationResult = objectMapper.convertValue(map1, AggregationResult.class);
            personalStatisticsResponses.add(aggregationResult);
            QueryParams queryParams = QueryParams.builder().field(Constants.Individual.FIRST_AGGREGATE_NAME)
                    .value(aggregationResult.getCard())
                    .build();
            StandardBankTransactionFlow standardBankTransactionFlow = entranceRepository.query(queryParams, StandardBankTransactionFlow.class);
            PersonalStatisticsResponse personalStatisticsResponse;
            try {
                personalStatisticsResponse = new PersonalStatisticsResponse(aggregationResult);
                personalStatisticsResponse.setCustomerName(standardBankTransactionFlow.getCustomer_name());
                personalStatisticsResponse.setCustomerName(standardBankTransactionFlow.getCustomer_name());
                personalStatisticsResponse.setCustomerIdentityId(aggregationResult.getCard());
                responses.add(personalStatisticsResponse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void histogram() {
        Map map = new LinkedHashMap();

        map = entranceRepository.histogramAggs("transactionMoney", "", 8996.17, 5015.66, 49996.49, BankTransactionFlow.class);
        System.out.println("***");
    }

    @Test
    public void range5() {

        List<Range> list = new ArrayList<>();
        Range range = new Range(0.0, 9983d);
        Range range1 = new Range(9983d, 19967d);
        Range range2 = new Range(19967d, 29951d);
        Range range3 = new Range(29951d, 39935d);
        Range range4 = new Range(39935d, 49918d);
        list.add(range);
        list.add(range1);
        list.add(range2);
        list.add(range3);
        list.add(range4);

        QueryParams termQuery = new QueryParams();
        termQuery.setField("customerIdentityCard");
        termQuery.setQueryType(QueryType.term);
        termQuery.setValue("371601198702200014");

        QueryParams termQuery1 = new QueryParams();
        termQuery1.setField("caseId");
        termQuery1.setQueryType(QueryType.term);
        termQuery1.setValue("100376eb69614df4a7cd63ca6884827b");

        QueryParams rangeQuery = new QueryParams();
        rangeQuery.setQueryType(QueryType.range);
        rangeQuery.setField("transactionMoney");
        OperatorParam operatorParam = new OperatorParam();
        operatorParam.setOperator(Operator.from);
        operatorParam.setInclude(true);
        operatorParam.setOperatorValue(0.0);

        List<OperatorParam> operatorParams = new ArrayList<>();
        operatorParams.add(operatorParam);
        rangeQuery.setOperatorParams(operatorParams);

        List<QueryParams> queryParams = new ArrayList<>();
        queryParams.add(termQuery);
        queryParams.add(termQuery1);
        queryParams.add(rangeQuery);


        StopWatch stopWatch = new StopWatch();
        stopWatch.start("rangeAggs5");
//        Map map1 = entranceRepository.rangeAggs(queryParams,"transactionMoney","100376eb69614df4a7cd63ca6884827b",list,BankTransactionFlow.class);
        stopWatch.stop();

//        Map map1 = entranceRepository.rangeAggs("transactionMoney","",list,BankTransactionFlow.class);
        System.out.println("stopWatch.prettyPrint()~~~~~~~~~~~~~~~~~:" + stopWatch.getTotalTimeSeconds());
        System.out.println(stopWatch.prettyPrint());
    }

    @Test
    public void range10() {

        List<Range> list = new ArrayList<>();
        Range range = new Range(0.0, 4958d);
        Range range1 = new Range(4958d, 9917d);
        Range range2 = new Range(9917d, 14875d);
        Range range3 = new Range(14875d, 19834d);
        Range range4 = new Range(19834d, 24793d);
        Range range5 = new Range(24793d, 29751d);
        Range range6 = new Range(29751d, 34710d);
        Range range7 = new Range(34710d, 39669d);
        Range range8 = new Range(39669d, 44627d);
        Range range9 = new Range(44627d, 49586d);
        list.add(range);
        list.add(range1);
        list.add(range2);
        list.add(range3);
        list.add(range4);
        list.add(range5);
        list.add(range6);
        list.add(range7);
        list.add(range8);
        list.add(range9);

        QueryParams termQuery = new QueryParams();
        termQuery.setField("customerIdentityCard");
        termQuery.setQueryType(QueryType.term);
        termQuery.setValue("371601198702200014");

        QueryParams termQuery1 = new QueryParams();
        termQuery1.setField("caseId");
        termQuery1.setQueryType(QueryType.term);
        termQuery1.setValue("100376eb69614df4a7cd63ca6884827b");

        QueryParams rangeQuery = new QueryParams();
        rangeQuery.setQueryType(QueryType.range);
        rangeQuery.setField("transactionMoney");
        OperatorParam operatorParam = new OperatorParam();
        operatorParam.setOperator(Operator.from);
        operatorParam.setInclude(true);
        operatorParam.setOperatorValue(0.0);

        List<OperatorParam> operatorParams = new ArrayList<>();
        operatorParams.add(operatorParam);
        rangeQuery.setOperatorParams(operatorParams);

        List<QueryParams> queryParams = new ArrayList<>();
        queryParams.add(termQuery);
        queryParams.add(termQuery1);
        queryParams.add(rangeQuery);


        StopWatch stopWatch = new StopWatch();
        stopWatch.start("rangeAggs10");
//        Map map1 = entranceRepository.rangeAggs(queryParams,"transactionMoney","100376eb69614df4a7cd63ca6884827b",list,BankTransactionFlow.class);
        stopWatch.stop();

//        Map map1 = entranceRepository.rangeAggs("transactionMoney","",list,BankTransactionFlow.class);
        System.out.println("stopWatch.prettyPrint()~~~~~~~~~~~~~~~~~:" + stopWatch.getTotalTimeSeconds());
        System.out.println(stopWatch.prettyPrint());
    }


    @Test
    public void histogramTest() {
        List<DateHistogramBuilder> list = new ArrayList<>();
        DateHistogramBuilder dateHistogramBuilder1 = DateHistogramBuilder.builder()
                .aggsType(AggsType.sum)
                .field("transactionMoney")
                .build();
        list.add(dateHistogramBuilder1);
        DateHistogramBuilder dateHistogramBuilder = DateHistogramBuilder.builder()
                .aggsType(AggsType.date_histogram)
                .dateIntervalUnit("day")
                .field("tradingTime")
                .minDocCount(1)
                .format("yyyy-MM-dd")
                .childDateHistogramBuilders(list)
                .build();


        Map map1 = entranceRepository.dateHistogramAggs(dateHistogramBuilder, "", BankTransactionFlow.class);
        System.out.println("***");
    }


    @Test
    public void statsAggs() {

        List<String> list = new ArrayList<>();

        list.add("60138216660012614");
        list.add("60138216660042014");


        QueryParams termQuery = new QueryParams();
        termQuery.setField("customerIdentityCard");
        termQuery.setQueryType(QueryType.term);
        termQuery.setValue("371601198702200014");

        QueryParams termQuery1 = new QueryParams();
        termQuery1.setField("caseId");
        termQuery1.setQueryType(QueryType.term);
        termQuery1.setValue("100376eb69614df4a7cd63ca6884827b");

        QueryParams rangeQuery = new QueryParams();
        rangeQuery.setQueryType(QueryType.range);
        rangeQuery.setField("transactionMoney");


        QueryParams termsQuery = new QueryParams();
        termsQuery.setQueryType(QueryType.terms);
        termsQuery.setField("queryCard");
        termsQuery.setTermsValues(list);

        OperatorParam operatorParam = new OperatorParam();
        operatorParam.setOperator(Operator.from);
        operatorParam.setInclude(true);
        operatorParam.setOperatorValue(0.0);

        List<OperatorParam> operatorParams = new ArrayList<>();
        operatorParams.add(operatorParam);
        rangeQuery.setOperatorParams(operatorParams);

        List<QueryParams> queryParams = new ArrayList<>();
        queryParams.add(termQuery);
        queryParams.add(termQuery1);
        queryParams.add(rangeQuery);
        queryParams.add(termsQuery);


        StopWatch stopWatch = new StopWatch();
        stopWatch.start("rangeAggs5");
//        Map map1 = entranceRepository.statsAggs(queryParams,"transactionMoney","100376eb69614df4a7cd63ca6884827b",BankTransactionFlow.class);
        stopWatch.stop();


//        Map map1 = entranceRepository.rangeAggs("transactionMoney","",list,BankTransactionFlow.class);
        System.out.println("stopWatch.prettyPrint()~~~~~~~~~~~~~~~~~:" + stopWatch.getTotalTimeSeconds() + "s");
        System.out.println(stopWatch.prettyPrint());
    }


    @Test
    public void testFundsSourceAndDestination() {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundsSourceAndDestinationStatisticsRequest request = new FundsSourceAndDestinationStatisticsRequest();
        request.setIdentityCard("322125198702200000");
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.NET);
        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        iFundsSourceAndDestinationStatistics.accessFundsSourceAndDestinationStatisticsResult(request, "abe45225e276423a96ce68c43d9e91f3");


    }

    @Test
    public void testFundsSourceAndDestinationResultList() {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundsSourceAndDestinationStatisticsRequest request = new FundsSourceAndDestinationStatisticsRequest();
        request.setIdentityCard("322125198702200000");
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.NET);

        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        iFundsSourceAndDestinationStatistics.accessFundsSourceAndDestinationStatisticsResultList(request, "abe45225e276423a96ce68c43d9e91f3");


    }

    @Autowired
    TransactionAmountStrategyImpl transactionAmountStrategyImpl;

    @Test
    public void testAccessFundSourceAndDestinationTopN_交易金额() throws Exception {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundsSourceAndDestinationStatisticsRequest request = new FundsSourceAndDestinationStatisticsRequest();
        request.setIdentityCard("322125198702200000");
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT);

        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationTopN(request, "a24c5d1d7bf743cfba1b0120aa0a172c");


    }


    @Autowired
    private FundSourceAndDestinationFactory fundSourceAndDestinationFactory;

    @Test
    public void testAccessFundSourceAndDestinationTopN_交易净和() {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundsSourceAndDestinationStatisticsRequest request = new FundsSourceAndDestinationStatisticsRequest();
        request.setIdentityCard("322125198702200000");
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.NET);

        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        try {
            fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationTopN(request, "a24c5d1d7bf743cfba1b0120aa0a172c");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testAccessFundSourceAndDestinationTrend_交易金额() {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundsSourceAndDestinationStatisticsRequest request = new FundsSourceAndDestinationStatisticsRequest();
        request.setIdentityCard("322125198702200000");
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT);

        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        try {
            fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationTrend(request, "a24c5d1d7bf743cfba1b0120aa0a172c");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testAccessFundSourceAndDestinationTrend_交易净和() {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundsSourceAndDestinationStatisticsRequest request = new FundsSourceAndDestinationStatisticsRequest();
        request.setIdentityCard("322125198702200000");
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.NET);

        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        try {
            fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationTrend(request, "a24c5d1d7bf743cfba1b0120aa0a172c");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testAccessFundSourceAndDestinationListResult_交易金额() {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundsSourceAndDestinationStatisticsRequest request = new FundsSourceAndDestinationStatisticsRequest();
        request.setIdentityCard("322125198702200000");
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT);

        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        try {
            fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationList(request, "a24c5d1d7bf743cfba1b0120aa0a172c");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAccessFundSourceAndDestinationListResult_交易净和() {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundsSourceAndDestinationStatisticsRequest request = new FundsSourceAndDestinationStatisticsRequest();
        request.setIdentityCard("322125198702200000");
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.NET);
        request.setFundsResultType(FundsResultType.SOURCE);

        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        try {
            fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationList(request, "a24c5d1d7bf743cfba1b0120aa0a172c");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testaccessFundSourceAndDestinationCardList_交易净和() {


        // caseID :834da065584948318c359b8f5d5fe49d
        FundSourceAndDestinationCardResultRequest request = new FundSourceAndDestinationCardResultRequest();
        request.setFundsSourceAndDestinationStatisticsType(FundsSourceAndDestinationStatisticsType.NET);
        request.setFundsResultType(FundsResultType.SOURCE);
        request.setCustomerIdentityCard("452632198702200766");

        QueryRequest queryRequest = new QueryRequest();
        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setPage(0);
        pagingRequest.setPageSize(10);
        queryRequest.setPaging(pagingRequest);
        request.setQueryRequest(queryRequest);

        try {
            fundSourceAndDestinationFactory.access(request.getFundsSourceAndDestinationStatisticsType()).accessFundSourceAndDestinationCardList(request, "a24c5d1d7bf743cfba1b0120aa0a172c");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
