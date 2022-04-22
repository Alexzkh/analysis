package com.zqykj.app.service.vo.fund;

import com.zqykj.common.vo.SortRequest;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.DateRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 个体画像-名下卡交易情况统计请求体
 *
 * @author: SunChenYu
 * @date: 2021年11月30日 10:46:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndividualCardTransactionStatisticsRequest {
    /**
     * 案件id
     */
    private String caseId;

    /**
     * 身份证号
     */
    private String customerIdentityCard;

    /**
     * 银行卡号
     */
    private List<String> queryCards;

    /**
     * 关键字搜索
     */
    private String keyword;

    /**
     * 时间范围
     */
    DateRange dateRange;

    /**
     * 分页
     */
    private Pagination pagination;

    /**
     * 排序
     */
    private SortRequest sortRequest;

}
