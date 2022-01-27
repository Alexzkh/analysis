package com.zqykj.app.service.factory.builder.aggregation.fund;

import com.zqykj.app.service.factory.param.agg.FundsLoopAggParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.core.aggregation.response.ElasticsearchAggregationResponseAttributes;
import com.zqykj.enums.AggsType;
import com.zqykj.parameters.aggregate.AggregationParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 资金回路聚合请求参数
 * @Author zhangkehou
 * @Date 2022/1/17
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundsLoopAggFactory implements FundsLoopAggParamFactory {
    @Override
    public AggregationParams buildAccessAllAdjustCardsAgg() {
        String name = AggsType.multiTerms.name();
        String fields = FundTacticsAnalysisField.QUERY_CARD;
        Map<String, String> aggKeyMapping = new LinkedHashMap<>();
        aggKeyMapping.put(name, ElasticsearchAggregationResponseAttributes.keyAsString);
        AggregationParams root = new AggregationParams(name, AggsType.terms.name(), fields);
        root.setMapping(aggKeyMapping);
        root.setResultName("AllAdjustCards");
        return root;
    }
}
