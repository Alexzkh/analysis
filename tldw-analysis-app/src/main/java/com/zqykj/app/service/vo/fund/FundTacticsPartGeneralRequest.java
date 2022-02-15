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
import lombok.Setter;

import java.util.List;

/**
 * 资金战法部分通用请求参数
 */
@Setter
@Getter
public class FundTacticsPartGeneralRequest {

    /**
     * 数据标识唯一id
     */
    private String id;

    /**
     * 查询卡号
     */
    private String queryCard;

    /**
     * 对方卡号
     */
    private String oppositeCard;

    /**
     * 数据标识唯一id集合
     */
    private List<String> ids;

    /**
     * 数据标识唯一id集合(数据下载导出的时候会选择某几条数据,直接根据id即可)
     */
    private List<String> exportIds;

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

    /**
     * 选择范围: top
     */
    private Integer topRange;

    /**
     * 选择范围: 账号数量前 百分比 eg. 符合条件的未调单数量: 1000, 设置20%, 相当于取200个
     */
    private Double percentageOfAccountNumber;

    /**
     * excel导出名称
     */
    private String exportFileName;

    private int groupInitFrom = 0;

    private int groupInitSize = 60000;

    public static DateRange getDateRange(DateRangeRequest dateRangeRequest) {

        if (null == dateRangeRequest) {
            return null;
        }
        if (StringUtils.isBlank(dateRangeRequest.getStart()) || StringUtils.isBlank(dateRangeRequest.getEnd())) {
            return null;
        }
        DateRange dateRange = new DateRange();
        if (StringUtils.isNotBlank(dateRangeRequest.getStart())) {
            dateRange.setFrom(dateRangeRequest.getStart() + dateRangeRequest.getTimeStart());
        }
        if (StringUtils.isNotBlank(dateRangeRequest.getEnd())) {
            dateRange.setTo(dateRangeRequest.getEnd() + dateRangeRequest.getTimeEnd());
        }
        return dateRange;
    }
}
