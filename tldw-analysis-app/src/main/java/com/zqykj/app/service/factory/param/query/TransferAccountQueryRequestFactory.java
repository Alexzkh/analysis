package com.zqykj.app.service.factory.requestparam.query;

import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.common.enums.ConditionType;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.DateRange;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

/**
 * @Description: 调单账号查询elasticsearch查询参数
 * @Author zhangkehou
 * @Date 2021/12/24
 */
public interface TransferAccountQueryRequestFactory {


    /**
     * @param request: 调单账号特征分析参数
     * @param param:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildTransferAccountAnalysisQueryRequest(T request, V param, List<String> cards);

    /**
     * @param request: 获取批量调单账号的请求参数
     * @param param:   案件编号
     * @return: com.zqykj.parameters.query.QuerySpecialParams
     **/
    <T, V> QuerySpecialParams buildTransaferAccountQueryRequest(T request, V param);


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
