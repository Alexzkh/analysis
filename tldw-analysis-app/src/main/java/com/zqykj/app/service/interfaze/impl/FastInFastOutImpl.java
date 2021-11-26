/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.AggregationEntityMappingFactory;
import com.zqykj.app.service.factory.AggregationRequestParamFactory;
import com.zqykj.app.service.factory.AggregationResultEntityParseFactory;
import com.zqykj.app.service.factory.QueryRequestParamFactory;
import com.zqykj.app.service.interfaze.IFastInFastOut;
import com.zqykj.app.service.interfaze.IFundTacticsAnalysis;
import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.app.service.vo.fund.FastInFastOutResult;
import com.zqykj.common.core.ServerResponse;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
