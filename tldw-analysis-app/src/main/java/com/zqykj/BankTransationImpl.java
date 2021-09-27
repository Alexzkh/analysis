package com.zqykj;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zqykj.app.service.interfaze.IBankTransaction;
import com.zqykj.app.service.system.AggregateBuilderExcutor;
import com.zqykj.common.Constants;
import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.common.request.IndividualRequest;
import com.zqykj.common.request.QueryParams;
import com.zqykj.common.response.AggregationResult;
import com.zqykj.common.response.CardStatisticsResponse;
import com.zqykj.common.response.PersonalStatisticsResponse;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.infrastructure.core.ResponseCode;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 资金交易实现
 * @Author zhangkehou
 * @Date 2021/9/23
 */
@Service
@Slf4j
public class BankTransationImpl implements IBankTransaction {


    @Autowired
    private EntranceRepository entranceRepository;


    @Override
    public <T> ServerResponse<List<PersonalStatisticsResponse>> accessPeopleIndividualStatistics(IndividualRequest individualRequest) {

        try {
            Map map = entranceRepository.multilayerAggs(AggregateBuilderExcutor.buildPeopleAggregateBuilder(individualRequest), BankTransactionFlow.class);
            List list = (List) map.get(Constants.Individual.TERMS_CUSTOM_IDENTITY_CARD);
            List<AggregationResult> personalStatisticsResponses = new ArrayList<>();
            List<PersonalStatisticsResponse> responses = new ArrayList<>();
            list.stream().forEach(map1 -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    AggregationResult aggregationResult = objectMapper.convertValue(map1, AggregationResult.class);
                    personalStatisticsResponses.add(aggregationResult);
                    QueryParams queryParams = QueryParams.builder().field(Constants.Individual.FIRST_AGGREGATE_NAME)
                            .value(aggregationResult.getCard())
                            .build();
                    BankTransactionFlow bankTransactionFlow = entranceRepository.query(queryParams, BankTransactionFlow.class);
                    PersonalStatisticsResponse personalStatisticsResponse;

                    personalStatisticsResponse = new PersonalStatisticsResponse(aggregationResult);
                    personalStatisticsResponse.setCustomerName(bankTransactionFlow.getCustomerName());
                    personalStatisticsResponse.setCustomerIdentityId(aggregationResult.getCard());
                    responses.add(personalStatisticsResponse);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            });
            return ServerResponse.createBySuccess(responses);
        } catch (Exception e) {
            log.error("获取elasticsearch数据集失败{}", e);
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ACCESS_DATA.getCode(), e.getMessage());
        }
    }

    @Override
    public <T> ServerResponse<List<CardStatisticsResponse>> accessCardIndividualStatistics(IndividualRequest individualRequest) {

        try {
            Map map = entranceRepository.multilayerAggs(AggregateBuilderExcutor.buildCardAggregateBuilder(individualRequest), BankTransactionFlow.class);
            List list = (List) map.get("terms_account_card");
            List<AggregationResult> personalStatisticsResponses = new ArrayList<>();
            List<CardStatisticsResponse> responses = new ArrayList<>();
            list.stream().forEach(map1 -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    AggregationResult aggregationResult = objectMapper.convertValue(map1, AggregationResult.class);
                    personalStatisticsResponses.add(aggregationResult);
                    QueryParams queryParams = QueryParams.builder().field(Constants.Individual.SECOND_AGGREGATE_NAME)
                            .value(aggregationResult.getCard())
                            .build();
                    BankTransactionFlow bankTransactionFlow = entranceRepository.query(queryParams, BankTransactionFlow.class);
                    CardStatisticsResponse cardStatisticsResponse;

                    cardStatisticsResponse = new CardStatisticsResponse(aggregationResult);
                    cardStatisticsResponse.setBank(bankTransactionFlow.getBank());
                    cardStatisticsResponse.setCustomerName(bankTransactionFlow.getCustomerName());
                    cardStatisticsResponse.setQueryCard(aggregationResult.getCard());
                    cardStatisticsResponse.setCustomerIdentityId(bankTransactionFlow.getCustomerIdentityCard());
                    responses.add(cardStatisticsResponse);
                } catch (ParseException e) {
                    log.error("转换elasticsearch返回的结果失败{}", e);
                }
            });
            return ServerResponse.createBySuccess(responses);
        } catch (Exception e) {
            log.error("获取elasticsearch数据集失败{}", e);
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ACCESS_DATA.getCode(), e.getMessage());
        }

    }

}
