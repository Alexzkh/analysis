package com.zqykj.controller;

import com.gitee.starblues.realize.PluginUtils;
import com.zqykj.app.service.interfaze.IBankTransaction;
import com.zqykj.common.request.IndividualRequest;
import com.zqykj.common.response.CardStatisticsResponse;
import com.zqykj.common.response.PersonalStatisticsResponse;
import com.zqykj.infrastructure.core.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description: 交易统计
 * @Author zhangkehou
 * @Date 2021/9/25
 */
@RestController
@RequestMapping(path = "individual")
@Slf4j
public class BankTransactionController {


    private IBankTransaction iBankTransaction;

    public BankTransactionController(PluginUtils pluginUtils) {
        iBankTransaction = pluginUtils.getMainBean(IBankTransaction.class);

    }


    @RequestMapping(value = "/case/{case-id}/bill/people", method = RequestMethod.POST, produces = {"application/json"})
    public ServerResponse<List<PersonalStatisticsResponse>> getPeopleStatisticsInfoList(@PathVariable("case-id") String caseId,
                                                                                        @RequestBody IndividualRequest individualRequest, HttpServletRequest request, HttpServletResponse httpResponse) {

        try {
            return iBankTransaction.accessPeopleIndividualStatistics(individualRequest);
        } catch (Exception e) {
            log.error("获取调单个体统计数据出错{}", e);
            return ServerResponse.createByErrorMessage(e.getMessage());
        }
    }


    @RequestMapping(value = "/case/{case-id}/bill/card", method = RequestMethod.POST, produces = {"application/json"})
    public ServerResponse<List<CardStatisticsResponse>> getCardStatisticslInfoList(@PathVariable("case-id") String caseId,
                                                                                   @RequestBody IndividualRequest individualRequest, HttpServletRequest request, HttpServletResponse httpResponse) {

        try {
            return iBankTransaction.accessCardIndividualStatistics(individualRequest);
        } catch (Exception e) {
            log.error("获取指定调单个体下调单卡号的统计数据出错{}", e);
            return ServerResponse.createByErrorMessage(e.getMessage());
        }
    }

}
