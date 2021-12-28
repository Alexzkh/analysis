package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.factory.requestparam.query.TransferAccountQueryRequestFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.builder.QueryParamsBuilders;
import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description: 调单账号特征分析
 * @Author zhangkehou
 * @Date 2021/12/24
 */
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferAccountQueryBuilderFactory implements TransferAccountQueryRequestFactory {

    private final QueryRequestParamFactory queryRequestParamFactory;

    @Override
    public <T, V> QuerySpecialParams buildTransferAccountAnalysisQueryRequest(T request, V param, List<String> cards) {
        TransferAccountAnalysisRequest transferAccountAnalysisRequest = (TransferAccountAnalysisRequest) request;
        String caseId = param.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = buildCommomParams(transferAccountAnalysisRequest.getDateRange(), caseId);
        String keyword = transferAccountAnalysisRequest.getQueryRequest().getKeyword();
        // 增加模糊查询条件
        if (StringUtils.isNotBlank(keyword)) {
            CombinationQueryParams localFuzzy = queryRequestParamFactory.assembleLocalFuzzy(keyword);
            combinationQueryParams.addCommonQueryParams(new CommonQueryParams(localFuzzy));
        }
        if (!CollectionUtils.isEmpty(cards)) {
            combinationQueryParams.addCommonQueryParams(QueryParamsBuilders.terms(FundTacticsAnalysisField.QUERY_CARD, cards));
        }
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);

        return querySpecialParams;
    }

    @Override
    public <T, V> QuerySpecialParams buildTransaferAccountQueryRequest(T request, V param) {
        TransferAccountAnalysisRequest transferAccountAnalysisRequest = (TransferAccountAnalysisRequest) request;
        String caseId = param.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = buildCommomParams(transferAccountAnalysisRequest.getDateRange(), caseId);
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }

}
