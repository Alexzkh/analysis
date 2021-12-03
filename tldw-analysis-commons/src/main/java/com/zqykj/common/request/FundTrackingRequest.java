package com.zqykj.common.request;

import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.vo.DateRangeRequest;
import lombok.*;

import java.util.List;

/**
 * @Description: 资金追踪请求体
 * @Author zhangkehou
 * @Date 2021/11/29
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTrackingRequest {

    /**
     * 左边选中的个体的卡号
     */
    private List<String> leftCard;

    /**
     * 右边选中的个体的卡号
     */
    private List<String> rightCard;

    /**
     * 列表查询分页参数.
     */
    private PagingRequest paging;

    /**
     * 列表排序查询参数.
     */
    private SortingRequest sorting;

    /**
     * 日期范围   (时间范围固定是:  00:00:00-23:59:59)
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
}
