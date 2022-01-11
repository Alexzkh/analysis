/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.common.vo.PageRequest;
import com.zqykj.common.vo.SortRequest;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.parameters.query.DateRange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 资金战法部分通用请求参数
 */
@Setter
@Getter
@NoArgsConstructor
public class FundTacticsPartGeneralRequest {

    /**
     * 数据标识唯一id
     */
    private String id;

    /**
     * 数据标识唯一id集合
     */
    private List<String> ids;

    /**
     * 案件Id
     */
    private String caseId;

    /**
     * 模糊查询
     */
    private String keyword;

    /**
     * 日期范围
     */
    private DateRangeRequest dateRange;

    /**
     * 分页
     */
    private PageRequest pageRequest;

    /**
     * 排序
     */
    private SortRequest sortRequest;


    private int groupInitFrom = 0;

    private int groupInitSize = 60000;

    public static DateRange getDateRange(DateRangeRequest dateRangeRequest) {

        if (null == dateRangeRequest) {
            return null;
        }
        if (StringUtils.isBlank(dateRangeRequest.getStart()) || StringUtils.isBlank(dateRangeRequest.getEnd())) {
            return null;
        }
        String start = dateRangeRequest.getStart() + dateRangeRequest.getTimeStart();
        String end = dateRangeRequest.getEnd() + dateRangeRequest.getTimeEnd();
        return new DateRange(start, end);
    }
}
