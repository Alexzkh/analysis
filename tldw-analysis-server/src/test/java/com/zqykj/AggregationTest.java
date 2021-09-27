package com.zqykj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zqykj.common.Constants;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.common.request.QueryParams;
import com.zqykj.common.response.AggregationResult;
import com.zqykj.common.response.PersonalStatisticsResponse;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.enums.AggsType;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.util.ArrayList;
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
    public void multilayerPeopleAggsTest() throws JsonProcessingException,ParseException {
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
        List list =(List) map.get("terms_customer_identity_card");
        List<AggregationResult> personalStatisticsResponses = new ArrayList<>();
        List<PersonalStatisticsResponse> responses = new ArrayList<>();
        list.stream().forEach(map1->{
            ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);;
            AggregationResult aggregationResult =objectMapper.convertValue(map1, AggregationResult.class);
            personalStatisticsResponses.add(aggregationResult);
            QueryParams queryParams = QueryParams.builder().field(Constants.Individual.FIRST_AGGREGATE_NAME)
                    .value(aggregationResult.getCard())
                    .build();
            StandardBankTransactionFlow standardBankTransactionFlow =entranceRepository.query(queryParams,StandardBankTransactionFlow.class);
            PersonalStatisticsResponse personalStatisticsResponse;
            try {
                personalStatisticsResponse  = new PersonalStatisticsResponse(aggregationResult);
                personalStatisticsResponse.setCustomerName(standardBankTransactionFlow.getCustomer_name());
                personalStatisticsResponse.setCustomerName(standardBankTransactionFlow.getCustomer_name());
                personalStatisticsResponse.setCustomerIdentityId(aggregationResult.getCard());
                responses.add(personalStatisticsResponse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

}
