package com.zqykj.app.service.factory.builder.query.fund;

import com.zqykj.app.service.factory.param.query.FundsLoopQueryRequestFactory;
import com.zqykj.common.request.FundsLoopRequest;
import com.zqykj.parameters.query.CombinationQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @Description: 资金回路查询elasticsearch构建查询请求
 * @Author zhangkehou
 * @Date 2022/1/17
 */
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundsLoopQueryBuilderFactory implements FundsLoopQueryRequestFactory {


    @Override
    public <T, V> QuerySpecialParams buildQueryAccountQueryRequest(T request, V param) {
        FundsLoopRequest req = (FundsLoopRequest)request;
        String caseId = param.toString();
        QuerySpecialParams querySpecialParams = new QuerySpecialParams();
        CombinationQueryParams combinationQueryParams = buildCommomParams(req.getDateRange(), caseId);
        querySpecialParams.addCombiningQueryParams(combinationQueryParams);
        return querySpecialParams;
    }
}
