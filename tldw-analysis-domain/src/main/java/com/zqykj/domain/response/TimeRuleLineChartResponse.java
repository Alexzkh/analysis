package com.zqykj.domain.response;

import com.zqykj.domain.vo.TimeRuleLineChartVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 时间规律折线图返回body
 * @Author zhangkehou
 * @Date 2021/12/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeRuleLineChartResponse {

    /**
     * 折线图列表数据
     */
    private List<TimeRuleLineChartVO> result;

    /**
     * 交易总金额
     */
    private BigDecimal transactionTotalMoney;

    /**
     * 交易总次数
     */
    private Long transactionTotalFrequency;

    /**
     * 平均交易金额
     */
    private BigDecimal avgTransactionMoney;
}
