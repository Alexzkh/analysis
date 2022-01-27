package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.TimeRuleAnalysisAggRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.vo.fund.FundDateRequest;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisRequest;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.date.DateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @Description: 时间规律聚合
 * @Author zhangkehou
 * @Date 2021/12/30
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TimeRuleAnalysisAggFactory implements TimeRuleAnalysisAggRequestParamFactory {

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;
    @Override
    public <T, V> AggregationParams bulidTimeRuleAnalysisAggParams(T request, V param) {
        TimeRuleAnalysisRequest timeRuleAnalysisRequest = (TimeRuleAnalysisRequest) request;
        // 需要取出的聚合结果值 key: 聚合名称 value: 聚合属性
        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        Map<String, String> entityAggKeyMapping = new LinkedHashMap<>();
        // 获取聚合Key 映射 , 聚合key 与 返回实体属性 映射
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggKeyMapping, entityAggKeyMapping, TimeRuleAnalysisResult.class);
        // 这里可以自定义聚合名称的拼接方式
        String dateAggregateName = "date_histogram_" + FundTacticsAnalysisField.TRADING_TIME;
//        aggKeyMapping.put(dateAggregateName, ElasticsearchAggregationResponseAttributes.keyAsString);
        DateParams dateParams = new DateParams();
        String format = FundDateRequest.convertFromTimeType(timeRuleAnalysisRequest.getDateType());
        dateParams.setFormat(format);
        // default
        dateParams.setMinDocCount(1);
        dateParams.addCalendarInterval(timeRuleAnalysisRequest.getDateType());
        AggregationParams root = new AggregationParams(dateAggregateName, "date_histogram", FundTacticsAnalysisField.TRADING_TIME, dateParams);
        root.setMapping(aggKeyMapping);
        root.setEntityAggColMapping(entityAggKeyMapping);
        // 设置子聚合
        addSubAggregationParams(root);
        // 设置同级聚合
        addSiblingAggregation(root);

        return root;
    }
}
