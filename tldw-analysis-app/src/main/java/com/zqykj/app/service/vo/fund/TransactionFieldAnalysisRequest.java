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
public class TransactionFieldAnalysisRequest extends FundTacticsPartGeneralRequest {

    /**
     * 统计字段
     * {@link com.zqykj.domain.bank.BankTransactionFlow 来自于他的属性字段} <br>
     */
    private String statisticsField;

    /**
     * 统计字段内容
     */
    private String statisticsFieldContent;

    /**
     * 自定义归类查询请求
     */
    private List<CustomCollationQueryRequest> customCollationQueryRequests;


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
