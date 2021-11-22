package com.zqykj.common.response;

import com.zqykj.common.vo.FundsSourceAndDestinationLineChart;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Description: 资金来源去向趋势统一返回体
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundsSourceAndDestinationTrendResponse {


    /**
     * 资金来源去向趋势折线图来源的数据数据结果集
     */
    List<FundsSourceAndDestinationLineChart> sourceLineCharts;

    /**
     * 资金来源去向趋势折线图去向的数据结果集
     */
    List<FundsSourceAndDestinationLineChart> destinationLineCharts;
}
