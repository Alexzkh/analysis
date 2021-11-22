package com.zqykj.app.service.strategy.analysis.impl;

import com.zqykj.app.service.strategy.analysis.FundSourceAndDestinationStrategy;
import com.zqykj.common.enums.FundsSourceAndDestinationStatisticsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 资金来源去向工厂类
 * @Author zhangkehou
 * @Date 2021/11/18
 */
@Component
public class FundSourceAndDestinationFactory {

    /**
     * 自定义工厂map
     */
    private static final Map<FundsSourceAndDestinationStatisticsType, FundSourceAndDestinationStrategy> fundFactoryMap = new ConcurrentHashMap<>(4);

    @Autowired
    private TransactionNetSumStrategyImpl transactionNetSumStrategy;

    @Autowired
    private TransactionAmountStrategyImpl transactionAmountStrategy;

    /**
     * @param type: 统计类型枚举类,主要按照交易净和、交易金额统计
     * @return: com.zqykj.app.service.strategy.analysis.FundSourceAndDestinationStrategy
     **/
    public FundSourceAndDestinationStrategy access(FundsSourceAndDestinationStatisticsType type) throws Exception {
        fundFactoryMap.put(FundsSourceAndDestinationStatisticsType.NET, transactionNetSumStrategy);
        fundFactoryMap.put(FundsSourceAndDestinationStatisticsType.TRANSACTION_AMOUNT,transactionAmountStrategy);
        if (null == fundFactoryMap.get(type)) {
            throw new Exception("No Strategy exist with type{}" + type.toString());
        }
        return fundFactoryMap.get(type);
    }
}
