/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.tarde_statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * <h1> 交易统计分析查询返回 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradeStatisticalAnalysisQueryResponse {

    // 交易统计分析查询内容
    private List<TradeStatisticalAnalysisBankFlow> content;

    // 每页显示条数
    private int size;

    // 总数据量
    private long total;

    // 总页数
    private int totalPages;
}
