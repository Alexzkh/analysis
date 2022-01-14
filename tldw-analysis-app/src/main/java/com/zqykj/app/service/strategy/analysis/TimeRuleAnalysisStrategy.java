package com.zqykj.app.service.strategy.analysis;

import com.zqykj.app.service.vo.fund.TimeRuleAnalysisResult;
import com.zqykj.app.service.vo.fund.TimeRuleAnalysisRequest;
import com.zqykj.domain.response.TimeRuleLineChartResponse;
import com.zqykj.domain.response.TimeRuleResultListResponse;
import com.zqykj.domain.vo.TimeRuleResultListVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @Description: 时间规律分析
 * @Author zhangkehou
 * @Date 2021/12/30
 */
public interface TimeRuleAnalysisStrategy {

    /**
     * 根据统计类型（单一或汇总）计算折线图统计结果
     *
     * @param request: 时间规律分析请求体
     * @param results: elasticsearch聚合统计分析结果
     * @return: com.zqykj.domain.response.TimeRuleLineChartResponse
     **/
    TimeRuleLineChartResponse accessTimeRuleAnalysisLineChartResult(TimeRuleAnalysisRequest request, List<TimeRuleAnalysisResult> results);


    /**
     * 根据统计类型（单一或汇总）计算列表数据统计结果
     *
     * @param request: 时间规律分析请求体
     * @param list:    案件编号
     * @return: com.zqykj.domain.response.TimeRuleResultListResponse
     **/
    TimeRuleResultListResponse accessTimeRuleAnalysisResultList(TimeRuleAnalysisRequest request, List<TimeRuleAnalysisResult> list);



    /**
     * 计算金额占比
     * 分别是交易金额占比 = 当前记录的交易金额/总的交易金额
     * 入账金额占比 = 当前金额的入账金额 / 总的入账金额
     * 出账金额占比 = 当前金额的出账金额 / 总的出账金额
     *
     * @param divisor:  除数
     * @param dividend: 被除数
     * @return: java.lang.String
     **/
    default String accumulationAmountRatio(BigDecimal divisor, BigDecimal dividend) {

        return dividend.divide(divisor, 2, RoundingMode.HALF_UP).toString() + "%";
    }

    /**
     * 处理elasticsearch返回的原始数据转换为业务层需要的结果
     *
     * @param result:     elastisearch返回的原始结果
     * @param timePeriod: 时间段
     * @return: com.zqykj.domain.vo.TimeRuleResultListVO
     **/
    default TimeRuleResultListVO accessResultListVO(TimeRuleAnalysisResult result, String timePeriod) {

        return TimeRuleResultListVO.builder()
                .timePeriod(timePeriod)
                .transcationFrequency(result.getDateTradeTotalTimes())
                .transationMoney(result.getDatetradeAmountSum())
                .transactionMoneyRatio(accumulationAmountRatio(result.getTradeTotalAmount(), result.getDatetradeAmountSum().multiply(new BigDecimal(100))))
                .inTranscationFrequency(result.getCreditsTimes())
                .inTransactionMoney(result.getCreditsAmount())
                .inTransactionMoneyRatio(accumulationAmountRatio(result.getInTotalCreditsAmount(), result.getCreditsAmount().multiply(new BigDecimal(100))))
                .outTranscationFrequency(result.getPayOutTimes())
                .outTransactionMoney(result.getPayOutAmount())
                .outTransactionMoneyRatio(accumulationAmountRatio(result.getOutTotalPayOutAmount(), result.getPayOutAmount().multiply(new BigDecimal(100))))
                .build();
    }

    /**
     * 同类型的时间段的数据做累加
     *
     * @param originalVo: 缓存的数据，拿出来用于做累加
     * @param result:     elasticsearch返回的原始数据结果
     * @param timePeriod: 时间段
     * @return: com.zqykj.domain.vo.TimeRuleResultListVO
     **/
    default TimeRuleResultListVO accumulationResult(TimeRuleResultListVO originalVo, TimeRuleAnalysisResult result, String timePeriod) {

        return TimeRuleResultListVO.builder()
                .timePeriod(timePeriod)
                .transcationFrequency(result.getDateTradeTotalTimes() + originalVo.getTranscationFrequency())
                .transationMoney(result.getDatetradeAmountSum().add(originalVo.getTransationMoney()))
                .transactionMoneyRatio(accumulationAmountRatio(result.getTradeTotalAmount(), result.getDatetradeAmountSum().add(originalVo.getTransationMoney())
                        .multiply(new BigDecimal(100))))
                .inTranscationFrequency(result.getCreditsTimes() + originalVo.getInTranscationFrequency())
                .inTransactionMoney(result.getCreditsAmount().add(originalVo.getInTransactionMoney()))
                .inTransactionMoneyRatio(accumulationAmountRatio(result.getInTotalCreditsAmount(), result.getCreditsAmount().add(originalVo.getInTransactionMoney())
                        .multiply(new BigDecimal(100))))
                .outTranscationFrequency(result.getPayOutTimes() + originalVo.getOutTranscationFrequency())
                .outTransactionMoney(result.getPayOutAmount().add(originalVo.getOutTransactionMoney()))
                .outTransactionMoneyRatio(accumulationAmountRatio(result.getOutTotalPayOutAmount(), result.getPayOutAmount().add(originalVo.getOutTransactionMoney())
                        .multiply(new BigDecimal(100))))
                .build();
    }


}
