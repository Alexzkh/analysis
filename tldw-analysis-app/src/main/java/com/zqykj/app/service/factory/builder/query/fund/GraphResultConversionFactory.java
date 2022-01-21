package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.parameters.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 图结果转换工厂
 * @Author zhangkehou
 * @Date 2022/1/20
 */
@Component
public class GraphResultConversionFactory {


    /**
     * @param dateRangeRequest: 时间日期范围参数
     * @param fund:             指定的具体的交易金额
     * @param operator:         针对金额的运算符
     * @param caseId:           案件编号
     * @param ids:              图结果的关联es中总表的id
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    public QuerySpecialParams accessGraphResultConversion(DateRangeRequest dateRangeRequest, String fund, AmountOperationSymbol operator, String caseId, List<String> ids) {

        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));

        // 指定日期范围
        if (null != dateRangeRequest && StringUtils.isNotBlank(dateRangeRequest.getStart())
                & StringUtils.isNotBlank(dateRangeRequest.getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRADING_TIME
                    , new DateRange(dateRangeRequest.getStart()
                    , dateRangeRequest.getEnd())));
        }
        // 指定交易金额
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRANSACTION_MONEY, fund,
                QueryOperator.of(operator.name())
        ));
        // 指定ids集合的in 查询
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.terms, FundTacticsAnalysisField.ID, ids));
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }
}
