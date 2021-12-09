package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.IndividualCardTransactionStatisticsRequest;
import com.zqykj.app.service.vo.fund.IndividualCardTransactionStatisticsResponse;
import com.zqykj.app.service.vo.fund.IndividualInfoAndStatisticsRequest;
import com.zqykj.app.service.vo.fund.IndividualInfoAndStatisticsResponse;
import com.zqykj.common.core.ServerResponse;

import java.util.List;


/**
 * @author: SunChenYu
 * @date: 2021年11月30日 16:28:03
 */
public interface IIndividualPortraitStatistics {
    ServerResponse<IndividualInfoAndStatisticsResponse> accessIndividualInfoAndStatistics(IndividualInfoAndStatisticsRequest individualInfoAndStatisticsRequest);

    ServerResponse<List<IndividualCardTransactionStatisticsResponse>> accessIndividualCardTransactionStatistics(IndividualCardTransactionStatisticsRequest individualCardTransactionStatisticsRequest);
}
