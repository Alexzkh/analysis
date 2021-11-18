package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.interfaze.IFundsSourceAndDestinationStatistics;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.common.request.FundsSourceAndDestinationStatisticsRequest;
import com.zqykj.common.response.FundsSourceAndDestinationStatisticsResponse;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * @Description: 资金来源去向战法业务接口
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundsSourceAndDestinationStatisticsImpl implements IFundsSourceAndDestinationStatistics {

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final EntranceRepository entranceRepository;

    @Override
    public FundsSourceAndDestinationStatisticsResponse accessFundsSourceAndDestinationStatisticsResult(FundsSourceAndDestinationStatisticsRequest fundsSourceAndDestinationStatisticsRequest, String caseId) {

        /**
         * 构建公共查询参数.
         * */
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.buildFundsSourceAndDestinationAnalysisResquest(fundsSourceAndDestinationStatisticsRequest,caseId);

        AggregationParams aggregationParams = aggregationRequestParamFactory.buildFundsSourceTopNAgg(fundsSourceAndDestinationStatisticsRequest);
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, BankTransactionRecord.class, caseId);

        return null;
    }
}
