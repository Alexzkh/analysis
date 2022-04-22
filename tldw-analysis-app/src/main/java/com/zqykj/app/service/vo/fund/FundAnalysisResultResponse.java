/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.domain.PageRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1> 资金分析分页结果信息 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class FundAnalysisResultResponse<T> {

    private List<T> content;

    // 每页显示条数
    private int size;

    // 总数据量
    private long total;

    // 总页数
    private int totalPages;

    public static <T> FundAnalysisResultResponse<T> empty() {
        FundAnalysisResultResponse<T> fundAnalysisResultResponse = new FundAnalysisResultResponse<>();
        fundAnalysisResultResponse.setContent(new ArrayList<>());
        return fundAnalysisResultResponse;
    }

    public static <T> FundAnalysisResultResponse<T> build(List<T> content, long total, int pageSize) {
        FundAnalysisResultResponse<T> fundAnalysisResultResponse = new FundAnalysisResultResponse<>();
        fundAnalysisResultResponse.setContent(content);
        fundAnalysisResultResponse.setTotalPages(PageRequest.getTotalPages(total, pageSize));
        fundAnalysisResultResponse.setTotal(total);
        fundAnalysisResultResponse.setSize(pageSize);
        return fundAnalysisResultResponse;
    }
}
