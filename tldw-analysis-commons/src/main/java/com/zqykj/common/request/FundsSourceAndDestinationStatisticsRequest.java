package com.zqykj.common.request;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.enums.FundsResultType;
import com.zqykj.common.enums.FundsSourceAndDestinationStatisticsType;
import com.zqykj.common.enums.FundsSourceAndDestinationTrendType;
import com.zqykj.common.vo.DateRangeRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @Description: 资金来源去向战法请求体
 * @Author zhangkehou
 * @Date 2021/11/6
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundsSourceAndDestinationStatisticsRequest {


    /**
     * 身份证
     */
    private String identityCard;

    /**
     * 卡号集合
     */
    private List<String> cardNums;

    /**
     * 日期范围   (时间范围固定是:  00:00-23:59)
     */
    private DateRangeRequest dateRange;

    /**
     * 比较符  大于、大于等于、小于、小于等于、等于   (默认是大于等于0)
     */
    private AmountOperationSymbol operator = AmountOperationSymbol.gte;

    /**
     * 交易金额
     */
    private String fund = "0";

    /**
     * 来源和去向取值top N ,默认10.
     */
    private Integer top = 10;

    /**
     * 资金来源去向统计依据（交易金额、交易净和）
     */
    private FundsSourceAndDestinationStatisticsType fundsSourceAndDestinationStatisticsType;

    /**
     * 列表数据来源、去向
     */
    private FundsResultType fundsResultType;

    /**
     * 资金来源去向趋势依据
     */
    private FundsSourceAndDestinationTrendType fundsSourceAndDestinationTrendType;

    /**
     * 查询参数（这其中包括模糊搜索,分页和排序参数）
     */
    private QueryRequest queryRequest;

    /**
     * 时间排序（1d）
     */
    private String dateType = "d";


}
