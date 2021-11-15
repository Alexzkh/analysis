/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * <h1> 交易汇聚分析结果返回 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradeConvergenceAnalysisResultResponse {

    private List<TradeConvergenceAnalysisResult> content;

    // 每页显示条数
    private int size;

    // 总数据量
    private long total;

    // 总页数
    private int totalPages;
}
