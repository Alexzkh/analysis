/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * <h1> 交易字段分析请求 </h1>
 */
@Setter
@Getter
public class TransactionFieldAnalysisRequest extends FundTacticsPartGeneralPreRequest {

    /**
     * 调单卡号集合
     */
    private List<String> cardNum;

    /**
     * 统计字段
     * {@link com.zqykj.domain.bank.BankTransactionFlow 来自于他的属性字段} <br>
     */
    private String statisticsField;

    /**
     * 统计字段内容(查询详情的时候需要)
     */
    private String statisticsFieldContent;

    /**
     * 自定义归类查询请求
     */
    private List<CustomCollationQueryRequest> customCollationQueryRequests;

    /**
     * 定义是 交易字段类型占比聚合查询: 1 / 交易字段类型统计聚合查询: 2
     */
    private int AggQueryType = 1;

    /**
     * <h2> 自定义归类查询请求 </h2>
     */
    @Setter
    @Getter
    public static class CustomCollationQueryRequest {

        /**
         * 类别名称
         */
        private String classificationName;

        /**
         * 指的是包含字段内容
         */
        private List<String> containField;
    }
}
