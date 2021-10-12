package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.TradeStatisticsAnalysisQueryRequestFactory;
import com.zqykj.app.service.field.TacticsAnalysisField;
import com.zqykj.app.service.interfaze.ITransactionStatistics;
import com.zqykj.app.service.vo.tarde_statistics.TimeGroupTradeAmountSum;
import com.zqykj.app.service.vo.tarde_statistics.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.TransactionStatisticsRequest;
import com.zqykj.common.response.TransactionStatisticsResponse;
import com.zqykj.common.vo.TimeTypeRequest;
import com.zqykj.core.aggregation.query.AggregateRequestFactory;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.parameters.query.CommonQueryParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 交易统计实现类
 * @Author zhangkehou
 * @Date 2021/9/28
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionStatisticsImpl implements ITransactionStatistics {

    private final EntranceRepository entranceRepository;

    @Override
    public TransactionStatisticsResponse calculateStatisticalResults(TransactionStatisticsRequest transactionStatisticsRequest) {

        return null;
    }

    @Override
    public QuerySpecialParams preQueryTransactionStatisticsAnalysis(String caseId, TradeStatisticalAnalysisPreRequest request) {
        // 构建查询参数
        return TradeStatisticsAnalysisQueryRequestFactory.createTradeAmountByTimeQuery(request, caseId);
    }

    @Override
    public ServerResponse<TimeGroupTradeAmountSum> getTradeAmountByTime(String caseId, TradeStatisticalAnalysisPreRequest request, TimeTypeRequest timeType) {

        // 构建查询参数
        QuerySpecialParams query = this.preQueryTransactionStatisticsAnalysis(caseId, request);
        // 构建  DateSpecificFormat对象
        Map<String, Object> result = entranceRepository.dateGroupAndSum(query, TacticsAnalysisField.TRADING_TIME,
                AggregateRequestFactory.convertFromTimeType(timeType.name()),
                TacticsAnalysisField.TRANSACTION_MONEY, BankTransactionFlow.class, caseId);

        TimeGroupTradeAmountSum groupTradeAmountSum = new TimeGroupTradeAmountSum();

        if (!CollectionUtils.isEmpty(result)) {

            Map<String, Object> resultNew = new LinkedHashMap<>();
            if (TimeTypeRequest.h == timeType) {
                // 需要对key 排序
                result.entrySet().stream()
                        .sorted(Comparator.comparing(x -> Integer.parseInt(x.getKey())))
                        .forEachOrdered(x -> resultNew.put(x.getKey(), x.getValue()));
                groupTradeAmountSum.setDates(resultNew.keySet());
                List<String> values = resultNew.values().stream().map(Object::toString).collect(Collectors.toList());
                groupTradeAmountSum.setTradeAmounts(values);
            } else {
                groupTradeAmountSum.setDates(result.keySet());
                List<String> values = result.values().stream().map(Object::toString).collect(Collectors.toList());
                groupTradeAmountSum.setTradeAmounts(values);
            }
        }

        return ServerResponse.createBySuccess(groupTradeAmountSum);
    }

}
