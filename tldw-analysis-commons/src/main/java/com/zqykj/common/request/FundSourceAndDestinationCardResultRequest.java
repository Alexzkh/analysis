package com.zqykj.common.request;

import com.zqykj.common.enums.FundsResultType;
import com.zqykj.common.enums.FundsSourceAndDestinationStatisticsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 资金来源去向卡明细请求体
 * @Author zhangkehou
 * @Date 2021/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundSourceAndDestinationCardResultRequest {

    /**
     * 资金请求类型
     */
    private FundsResultType fundsResultType;

    /**
     * 需要查询明细的证件号码
     */
    private String customerIdentityCard;

    /**
     * 查询参数（这其中包括模糊搜索,分页和排序参数）
     */
    private QueryRequest queryRequest;

    private FundsSourceAndDestinationStatisticsType fundsSourceAndDestinationStatisticsType;
}
