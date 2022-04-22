package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.TimeRuleAnalysisDetailRequest;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisRequest;
import com.zqykj.domain.Page;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.response.TimeRuleLineChartResponse;
import com.zqykj.domain.response.TimeRuleResultListResponse;

/**
 * @Description: 时间规律分析业务接口
 * @Author zhangkehou
 * @Date 2022/1/6
 */
public interface ITimeRuleAnalysisStatistics {


    /**
     * 时间规律分析获取折线图展示结果
     *
     * @param request: 时间规律战法分析请求
     * @param caseId:  案件编号
     * @return: com.zqykj.domain.response.TimeRuleLineChartResponse
     **/
    TimeRuleLineChartResponse accessLineChartResult(TimeRuleAnalysisRequest request, String caseId);


    /**
     * 时间规律分析获取分析结果列表数据
     *
     * @param request: 时间规律战法分析请求
     * @param caseId:  案件编号
     * @return: com.zqykj.domain.response.TimeRuleResultListResponse
     **/
    TimeRuleResultListResponse accessTimeRuleResultList(TimeRuleAnalysisRequest request, String caseId);


    /**
     * 时间规律分析详情接口: 根据时间范围筛选出符合条件的时间日期范围的银行交易流水数据
     *
     * @param request: 交易路径详情查找请求体
     * @param caseId:  案件编号
     * @return: com.zqykj.domain.response.TransactionPathDetailResponse
     **/
    Page<BankTransactionFlow> accessTimeRuleAnalysisResultDetail(TimeRuleAnalysisDetailRequest request, String caseId);

}
