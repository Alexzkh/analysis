package com.zqykj.common.response;

import java.util.Date;
import java.util.List;

/**
 * @Description: 调单个体统计响应实体
 * @Author zhangkehou
 * @Date 2021/9/22
 */
public class PersonalStatisticsResponse {

    /**
     * 账户开户名称
     * */
    private String customerName;
    private String customerIdentityId;
    private String phoneNumber;
    private Long accountCardNums;
    private Long transactionTotalNums;
    private Long entriesNums;
    private Double entriesAmount;
    private Long outGoingNums;
    private Double outGoingAmount;
    private Double transactionNetAmount;
    private Double transactionTotalAmount;
    private Date earliestTradingTime;
    private Date LatestTradingTime;
    private List<CardStatisticsResponse> cardStatisticsResponseList;
}
