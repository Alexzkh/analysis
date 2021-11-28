/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFastInFastOut;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.app.service.vo.fund.middle.CreditAdjustCards;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.domain.bank.BankTransactionRecord;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <h1> 战法快进快出 </h1>
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FastInFastOutImpl implements IFastInFastOut {

    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregationEntityMappingFactory aggregationEntityMappingFactory;

    private final AggregationResultEntityParseFactory aggregationResultEntityParseFactory;

    private final IFundTacticsAnalysis fundTacticsAnalysis;

    @Value("${buckets.page.initSize}")
    private int initGroupSize;

    @Value("${global.chunkSize}")
    private int globalChunkSize;

    @Value("${chunkSize}")
    private int chunkSize;

    public ServerResponse fastInFastOutAnalysis(FastInFastOutRequest request) {


        return ServerResponse.createBySuccess();
    }

    /**
     * <h2> 获取入账的调单卡号集合 </h2>
     *
     * @param caseId      案件Id
     * @param adjustCards 给定的一组调单卡号集合
     * @param singleQuota 单笔限额(指的是交易金额)
     */
    public Map<String, CreditAdjustCards> getCreditsAdjustCards(String caseId, List<String> adjustCards, int singleQuota,
                                                                int from, int size) {

        // 构建查询入账调单卡号查询请求参数
        QuerySpecialParams query = queryRequestParamFactory.buildCreditsAdjustCards(caseId, adjustCards, singleQuota);
        // 构建查询入账调单卡号聚合请求参数
        AggregationParams agg = aggregationRequestParamFactory.buildCreditsAdjustCardsAgg(initGroupSize, from, size);
        // 构建聚合名称属性映射(获取聚合值)
        Map<String, String> aggNameKeyMapping = new LinkedHashMap<>();
        aggregationEntityMappingFactory.buildTradeAnalysisResultAggMapping(aggNameKeyMapping, CreditAdjustCards.class);
        // 设置此聚合功能名称
        agg.setResultName("getCreditsAdjustCards");
        Map<String, List<List<Object>>> resultsMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        List<List<Object>> results = resultsMap.get(agg.getResultName());
        return results.stream().collect(Collectors.toMap(e -> e.get(0).toString(),
                e1 -> new CreditAdjustCards(e1.get(0).toString(), e1.get(1).toString(), BigDecimalUtil.value(e1.get(2).toString())),
                (v1, v2) -> v1
        ));
    }

    /**
     * <h2> 获取入账的调单卡号总数量 </h2>
     */
    public long getCreditsAdjustCardsTotal(String caseId, List<String> adjustCards, int singleQuota) {

        // 构建查询入账调单卡号查询请求参数
        QuerySpecialParams query = queryRequestParamFactory.buildCreditsAdjustCards(caseId, adjustCards, singleQuota);
        // 聚合查询参数
        AggregationParams agg = aggregationRequestParamFactory.buildCreditsAdjustCardsTotalAgg();
        // 构建聚合名称属性映射(获取聚合值)
        Map<String, String> mapping = aggregationEntityMappingFactory.buildDistinctTotalAggMapping(FundTacticsAnalysisField.QUERY_CARD);
        agg.setMapping(mapping);
        // 设置此聚合功能名称
        agg.setResultName("total");
        Map<String, List<List<Object>>> resultsMap = entranceRepository.compoundQueryAndAgg(query, agg, BankTransactionRecord.class, caseId);
        if (CollectionUtils.isEmpty(resultsMap)) {
            return 0;
        } else {
            List<List<Object>> total = resultsMap.get("total");
            if (CollectionUtils.isEmpty(total)) {
                return 0;
            } else {
                return Long.parseLong(total.get(0).get(0).toString());
            }
        }
    }


    /**
     * <h2> 处理调单卡号为资金来源卡号的情况 </h2>
     */
    private FastInFastOutResult processAdjustCardAsFundSourceCard() {

        return null;
    }

    /**
     * <h2> 处理调单卡号为资金中转卡号的情况 </h2>
     */
    private FastInFastOutResult processAdjustCardAsFundTransitCard() {

        // 这种情况下需要特殊考虑一种情况的计算方式(单一来源到多个沉淀: 沉淀的金额累加不能超过来源金额,如果超过后续交易记录不计算)

        return null;
    }

    /**
     * <h2> 处理调单卡号为资金沉淀卡号的情况 </h2>
     */
    private FastInFastOutResult processAdjustCardAsFundDepositCard() {

        return null;
    }


    /**
     * <h2> 计算单一来源到多个沉淀 </h2>
     */
    private FastInFastOutResult multiDepositFromSingleSource(FastInFastOutRequest request) {

        return null;
    }

    /**
     * <h2> 计算多个来源到单一沉淀 </h2>
     */
    private FastInFastOutResult multiSourceFromSingleDeposit(FastInFastOutRequest request) {

        return null;
    }

    /**
     * <h2> 计算多个来源到多个沉淀 </h2>
     */
    private FastInFastOutResult multiSourceFromMutiDeposit(FastInFastOutRequest request) {

        return null;
    }

    /**
     * <h2> 检查特征比 </h2>
     */
    private boolean checkFeatureRatio() {

        return true;
    }

    /**
     * <h2> 检查时间间隔 </h2>
     */
    private boolean checkTimeInterval() {

        return true;
    }
}
