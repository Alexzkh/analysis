package com.zqykj.common.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;


/**
 * @Description: 资产趋势请求体
 * @Author zhangkehou
 * @Date 2021/10/19
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssetTrendsRequest extends TradeStatisticalAnalysisPreRequest {


    /**
     * 统计方式(按年y、按季度q、按月m).
     */
    private String dateType;

    /**
     * 列表查询分页参数.
     */
    private PagingRequest paging;

    /**
     * 列表排序查询参数.
     */
    private SortingRequest sorting;


    public TradeStatisticalAnalysisPreRequest convertFrom(AssetTrendsRequest from) {

        TradeStatisticalAnalysisPreRequest to = new TradeStatisticalAnalysisPreRequest();

        BeanUtils.copyProperties(from, to);

        return to;
    }


}
