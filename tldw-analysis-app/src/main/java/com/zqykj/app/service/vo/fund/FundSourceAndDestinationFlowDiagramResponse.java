package com.zqykj.app.service.vo.fund;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 资金来源去向流向图返回结果
 * @Author zhangkehou
 * @Date 2021/11/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundSourceAndDestinationFlowDiagramResponse {
    /**
     * 流向图来源数据
     */
    List<FundSourceAndDestinationBankRecord> localResults;

    /**
     * 流向图去向数据
     */
    List<FundSourceAndDestinationBankRecord> destinationResults;
}
