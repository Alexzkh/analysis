/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.tarde_statistics;

import com.zqykj.common.request.TradeStatisticalAnalysisPreRequest;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;


/**
 * <h1> 交易统计分析查询请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradeStatisticalAnalysisQueryRequest extends TradeStatisticalAnalysisPreRequest {

    /**
     * 模糊查询
     */
    private String keyword;

    /**
     * 分页
     */
    private PageRequest pageRequest;

    /**
     * 排序
     */
    private SortRequest sortRequest;


    public TradeStatisticalAnalysisPreRequest convertFrom(TradeStatisticalAnalysisQueryRequest from) {

        TradeStatisticalAnalysisPreRequest to = new TradeStatisticalAnalysisPreRequest();

        BeanUtils.copyProperties(from, to);

        return to;
    }
}
