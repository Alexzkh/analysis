package com.zqykj.app.service.factory.param.query;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 资金回路查询请求工厂
 * @Author zhangkehou
 * @Date 2022/1/17
 */
public interface FundsLoopQueryRequestFactory  {

    /**
     * 构建获取查询所有调单账号的查询请求
     *
     * @param request: 获取批量调单账号的请求参数
     * @param param:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildQueryAccountQueryRequest(T request, V param);

    /**
     * 构建公共查询参数（主要是过滤时间范围和案件编号）
     *
     * @param request: 日期范围参数
     * @param caseId:  案件编号
     * @return: com.zqykj.parameters.query.CombinationQueryParams
     **/
    default CombinationQueryParams buildCommomParams(DateRangeRequest request, String caseId) {
        CombinationQueryParams combinationQueryParams = new CombinationQueryParams();
        combinationQueryParams.setType(ConditionType.must);
        // 指定caseId
        combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.term, FundTacticsAnalysisField.CASE_ID, caseId));

        // 指定日期范围
        if (null != request && StringUtils.isNotBlank(request.getStart())
                & StringUtils.isNotBlank(request.getEnd())
        ) {
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(QueryType.range, FundTacticsAnalysisField.TRADING_TIME
                    , new DateRange(request.getStart()
                    , request.getEnd())));
        }

        return combinationQueryParams;
    }

}
