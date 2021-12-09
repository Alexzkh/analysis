package com.zqykj.app.service.interfaze.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.IndividualPortraitAnalysisField;
import com.zqykj.app.service.interfaze.IIndividualPortraitStatistics;
import com.zqykj.app.service.vo.fund.IndividualCardTransactionStatisticsRequest;
import com.zqykj.app.service.vo.fund.IndividualCardTransactionStatisticsResponse;
import com.zqykj.app.service.vo.fund.middle.IndividualInfoAndStatistics;
import com.zqykj.app.service.vo.fund.IndividualInfoAndStatisticsRequest;
import com.zqykj.app.service.vo.fund.IndividualInfoAndStatisticsResponse;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import com.zqykj.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 个体画像
 *
 * @author: SunChenYu
 * @date: 2021年11月30日 16:15:46
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IndividualPortraitStatisticsImpl implements IIndividualPortraitStatistics {
    private final EntranceRepository entranceRepository;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    @Override
    public ServerResponse<IndividualInfoAndStatisticsResponse> accessIndividualInfoAndStatistics(IndividualInfoAndStatisticsRequest individualInfoAndStatisticsRequest) {
        String routing = individualInfoAndStatisticsRequest.getCaseId();
        // 构建query
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildIndividualInfoAndStatisticsQueryParams(individualInfoAndStatisticsRequest);
        // 构建aggregations
        AggregationParams aggregationParams = aggregationRequestParamFactory.buildIndividualInfoAndStatisticsAgg(individualInfoAndStatisticsRequest);
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        // 构造聚合名称、聚合属性、实体属性之间的映射
        aggregationEntityMappingFactory.buildIndividualInfoAndStatisticsAggMapping(aggMapping, entityMapping, IndividualInfoAndStatistics.class);
        mappingParamsSet(aggregationParams, aggMapping, entityMapping);
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, routing);
        // 解析数据、构造响应结果
        List<List<Object>> aggValueList = resultMap.get(aggregationParams.getResultName());
        List<String> entityFields = new ArrayList<>(aggregationParams.getEntityAggColMapping().keySet());
        List<Map<String, Object>> maps = aggregationResultEntityParseFactory.convertEntity(aggValueList, entityFields, IndividualInfoAndStatistics.class);
        List<IndividualInfoAndStatistics> individualInfoAndStatisticsList = JacksonUtils.parse(JacksonUtils.toJson(maps), new TypeReference<List<IndividualInfoAndStatistics>>() {
        });
        // 基本信息与统计-数据整理
        IndividualInfoAndStatisticsResponse individualInfoAndStatisticsResponse = getIndividualInfoAndStatisticsResponse(individualInfoAndStatisticsList);
        return ServerResponse.createBySuccess(individualInfoAndStatisticsResponse);
    }

    private void mappingParamsSet(AggregationParams aggregationParams, Map<String, String> aggMapping, Map<String, String> entityMapping) {
        aggregationParams.setMapping(aggMapping);
        aggregationParams.setEntityAggColMapping(entityMapping);
        aggregationParams.setResultName(IndividualPortraitAnalysisField.ResultName.CUSTOMER_IDENTITY_CARD);
    }

    @Override
    public ServerResponse<List<IndividualCardTransactionStatisticsResponse>> accessIndividualCardTransactionStatistics(IndividualCardTransactionStatisticsRequest individualCardTransactionStatisticsRequest) {
        String routing = individualCardTransactionStatisticsRequest.getCaseId();
        // 构建query
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildIndividualCardTransactionStatisticsQueryParams(individualCardTransactionStatisticsRequest);
        // 构建aggregations
        AggregationParams aggregationParams = aggregationRequestParamFactory.buildIndividualCardTransactionStatisticsAgg(individualCardTransactionStatisticsRequest);
        Map<String, String> aggMapping = new LinkedHashMap<>();
        Map<String, String> entityMapping = new LinkedHashMap<>();
        // 构造聚合名称、聚合属性、实体属性之间的映射
        aggregationEntityMappingFactory.buildIndividualCardTransactionStatisticsAggMapping(aggMapping, entityMapping, IndividualCardTransactionStatisticsResponse.class);
        mappingParamsSet(aggregationParams, aggMapping, entityMapping);
        Map<String, List<List<Object>>> resultMap = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, routing);
        // 解析数据、构造响应结果
        List<List<Object>> aggValueList = resultMap.get(aggregationParams.getResultName());
        List<String> entityFields = new ArrayList<>(aggregationParams.getEntityAggColMapping().keySet());
        List<Map<String, Object>> maps = aggregationResultEntityParseFactory.convertEntity(aggValueList, entityFields, IndividualCardTransactionStatisticsResponse.class);
        List<IndividualCardTransactionStatisticsResponse> individualCardTransactionStatisticsResponses = JacksonUtils.parse(JacksonUtils.toJson(maps), new TypeReference<List<IndividualCardTransactionStatisticsResponse>>() {
        });
        // 名下卡交易统计-数据整理
        individualCardTransactionDataFlow(routing, individualCardTransactionStatisticsResponses);
        return ServerResponse.createBySuccess(individualCardTransactionStatisticsResponses);
    }

    /**
     * 名下卡交易统计-数据整理
     */
    private void individualCardTransactionDataFlow(String routing, List<IndividualCardTransactionStatisticsResponse> individualCardTransactionStatisticsResponses) {
        // 总资产
        double currentTotalBalance = individualCardTransactionStatisticsResponses.stream().mapToDouble(response -> response.getTransactionBalance().doubleValue()).sum();
        // 金额数值类型字段保留两位小数
        for (IndividualCardTransactionStatisticsResponse response : individualCardTransactionStatisticsResponses) {
            //案件id
            response.setCaseId(routing);
            // 进账金额
            response.setTotalEntryTransactionMoney(BigDecimalUtil.value(response.getTotalEntryTransactionMoney()));
            // 出账金额
            response.setTotalOutTransactionMoney(BigDecimalUtil.value(response.getTotalOutTransactionMoney()));
            // 交易总金额
            response.setTotalTransactionMoney(BigDecimalUtil.value(response.getTotalTransactionMoney()));
            // 交易净额
            response.setNetTransactionMoney(BigDecimalUtil.value(response.getNetTransactionMoney()));
            // 当前余额
            response.setTransactionBalance(BigDecimalUtil.value(response.getTransactionBalance()));
            // 资金占比
            BigDecimal fundsProportion = response.getTransactionBalance().divide(BigDecimal.valueOf(currentTotalBalance), 4, BigDecimal.ROUND_HALF_UP);
            response.setFundsProportion(fundsProportion);
        }
    }

    /**
     * 基本信息与统计-数据整理
     */
    private IndividualInfoAndStatisticsResponse getIndividualInfoAndStatisticsResponse(List<IndividualInfoAndStatistics> individualInfoAndStatisticsList) {
        IndividualInfoAndStatisticsResponse response = new IndividualInfoAndStatisticsResponse();
        IndividualInfoAndStatistics individualInfoAndStatistics = individualInfoAndStatisticsList.get(0);
        // 计算当前总余额
        double sumBalance = individualInfoAndStatisticsList.stream().mapToDouble(IndividualInfoAndStatistics::getTransactionBalance).sum();
        BigDecimal currentTotalBalance = BigDecimalUtil.value(BigDecimal.valueOf(sumBalance));
        individualInfoAndStatistics.setCurrentTotalBalance(currentTotalBalance);
        // 金额数值类型字段保留两位小数
        individualInfoAndStatistics.setCumulativeIncome(BigDecimalUtil.value(individualInfoAndStatistics.getCumulativeIncome()));
        individualInfoAndStatistics.setCumulativeExpenditure(BigDecimalUtil.value(individualInfoAndStatistics.getCumulativeExpenditure()));
        // 交易净额
        BigDecimal cumulativeNet = individualInfoAndStatistics.getCumulativeNet();
        individualInfoAndStatistics.setCumulativeNet(BigDecimalUtil.value(cumulativeNet));
        BeanUtils.copyProperties(individualInfoAndStatistics, response);
        return response;
    }

}
