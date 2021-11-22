package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.SingleCardPortraitAnalysisField;
import com.zqykj.app.service.interfaze.ISingleCardPortraitStatistics;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.app.service.vo.fund.SingleCardPortraitRequest;
import com.zqykj.app.service.vo.fund.SingleCardPortraitResponse;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author: SunChenYu
 * @date: 2021年11月15日 14:44:52
 */

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SingleCardPortraitStatisticsImpl implements ISingleCardPortraitStatistics {
    private final EntranceRepository entranceRepository;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    @Override
    public ServerResponse<SingleCardPortraitResponse> accessSingleCardPortraitStatistics(SingleCardPortraitRequest singleCardPortraitRequest) {
        // routing对应caseId
        String routing = singleCardPortraitRequest.getCaseId();

        // 构建单卡画像查询参数
        QuerySpecialParams singleCardPortraitQuery = queryRequestParamFactory.buildSingleCardPortraitQueryParams(singleCardPortraitRequest);

        // 构建单卡画像聚合查询-查询卡号分桶聚合参数
        AggregationParams queryCardTermsAgg = aggregationRequestParamFactory.buildSingleCardPortraitAgg(singleCardPortraitRequest);

        // 构建单卡画像聚合查询-最早交易时间聚合参数
        AggregationParams earliestTimeAgg = aggregationRequestParamFactory.buildSingleCardPortraitEarliestTimeAgg(singleCardPortraitRequest);
        Map<String, String> earliestTimeAggMap = new LinkedHashMap<>();
        earliestTimeAggMap.put(SingleCardPortraitAnalysisField.AggResultName.EARLIEST_TRADING_TIME, SingleCardPortraitAnalysisField.AggResultField.VALUE_AS_STRING);
        earliestTimeAgg.setMapping(earliestTimeAggMap);
        earliestTimeAgg.setResultName(SingleCardPortraitAnalysisField.ResultName.EARLIEST_TRADING_TIME);

        // 构建单卡画像聚合查询-最晚交易时间聚合参数
        AggregationParams latestTimeAgg = aggregationRequestParamFactory.buildSingleCardPortraitLatestTimeAgg(singleCardPortraitRequest);
        Map<String, String> latestTimeAggMap = new LinkedHashMap<>();
        latestTimeAggMap.put(SingleCardPortraitAnalysisField.AggResultName.LATEST_TRADING_TIME, SingleCardPortraitAnalysisField.AggResultField.VALUE_AS_STRING);
        latestTimeAgg.setMapping(latestTimeAggMap);
        latestTimeAgg.setResultName(SingleCardPortraitAnalysisField.ResultName.LATEST_TRADING_TIME);

        // 设置同级聚合
        queryCardTermsAgg.addSiblingAggregation(earliestTimeAgg);
        queryCardTermsAgg.addSiblingAggregation(latestTimeAgg);

        // 构建 mapping (聚合名称 -> 聚合属性)  , (实体属性 -> 聚合名称)
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildSingleCardPortraitResultAggMapping(aggMapping, entityMapping, SingleCardPortraitResponse.class);
        queryCardTermsAgg.setMapping(aggMapping);
        queryCardTermsAgg.setEntityAggColMapping(entityMapping);
        queryCardTermsAgg.setResultName(SingleCardPortraitAnalysisField.ResultName.QUERY_CARD_TERMS);

        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(singleCardPortraitQuery, queryCardTermsAgg, BankTransactionRecord.class, routing);
        if (CollectionUtils.isEmpty(resultMap)) {
            return ServerResponse.createByErrorMessage("单卡画像查询结果为空！");
        }

        // 解析指定结构的返回数据
        SingleCardPortraitResponse singleCardPortraitResponseFinal = new SingleCardPortraitResponse();
        resultMap.forEach((resultName, aggValueList) -> {
            if (!CollectionUtils.isEmpty(aggValueList)) {
                if (SingleCardPortraitAnalysisField.ResultName.QUERY_CARD_TERMS.equals(resultName)) {
                    // 当前卡号相关基础信息
                    List<Object> objectList = (List<Object>) aggValueList.get(0).get(2);
                    Object targetObject = objectList.get(0);
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                    SingleCardPortraitResponse singleCardPortraitResponse;
                    try {
                        singleCardPortraitResponse = objectMapper.readValue(JacksonUtils.toJson(targetObject), SingleCardPortraitResponse.class);
                        String[] ignoreProperties = {"earliestTradingTime", "latestTradingTime"};
                        BeanUtils.copyProperties(singleCardPortraitResponse, singleCardPortraitResponseFinal, ignoreProperties);
                    } catch (IOException e) {
                        log.error("反序列化单卡画像查询结果失败：", e);
                    }
                    singleCardPortraitResponseFinal.setEntriesAmount((Double) aggValueList.get(0).get(0));
                    // 查询 bank_transaction_record 表，出账交易金额为负数,取绝对值
                    singleCardPortraitResponseFinal.setOutGoingAmount(!ObjectUtils.isEmpty(aggValueList.get(0).get(1)) && (Double) aggValueList.get(0).get(1) < 0
                            ? -(Double) aggValueList.get(0).get(1) : 0.00);
                    singleCardPortraitResponseFinal.setTransactionTotalAmount((Double) aggValueList.get(0).get(0) - (Double) aggValueList.get(0).get(1));
                }
                if (SingleCardPortraitAnalysisField.ResultName.EARLIEST_TRADING_TIME.equals(resultName)) {
                    String tradingTimeString = (String) aggValueList.get(0).get(0);
                    singleCardPortraitResponseFinal.setEarliestTradingTime("Infinity".equals(tradingTimeString) || "-Infinity".equals(tradingTimeString) ? null : tradingTimeString);
                }
                if (SingleCardPortraitAnalysisField.ResultName.LATEST_TRADING_TIME.equals(resultName)) {
                    String tradingTimeString = (String) aggValueList.get(0).get(0);
                    singleCardPortraitResponseFinal.setLatestTradingTime("Infinity".equals(tradingTimeString) || "-Infinity".equals(tradingTimeString) ? null : tradingTimeString);
                }
            }
        });
        log.info("单卡画像返回结果：{}", JacksonUtils.toJson(singleCardPortraitResponseFinal));
        return ServerResponse.createBySuccess(singleCardPortraitResponseFinal);
    }
}
