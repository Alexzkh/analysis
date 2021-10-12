package com.zqykj.app.service.interfaze;

import com.zqykj.common.request.IndividualRequest;
import com.zqykj.common.response.CardStatisticsResponse;
import com.zqykj.common.response.PersonalStatisticsResponse;
import com.zqykj.infrastructure.core.ServerResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 资金战法业务接口
 * @Author zhangkehou
 * @Date 2021/9/23
 */
@Component
public interface IBankTransaction {

    /**
     * 获取个体人的统计信息
     */
    <T> ServerResponse<List<PersonalStatisticsResponse>> accessPeopleIndividualStatistics(IndividualRequest individualRequest);

    /**
     * 获取卡的统计信息
     */
    <T> ServerResponse<List<CardStatisticsResponse>> accessCardIndividualStatistics(IndividualRequest individualRequest);



//    <T> ServerResponse<List<HistogramStatisticResponse>> accessHistogramStatistics();


}
