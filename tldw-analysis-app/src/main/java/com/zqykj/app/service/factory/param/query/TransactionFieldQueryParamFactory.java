/**
 * @作者 Mcj
 */
package com.zqykj.app.service.factory.param.query;

import com.zqykj.app.service.vo.fund.TransactionFieldAnalysisRequest;
import com.zqykj.parameters.query.QuerySpecialParams;

import java.util.List;

/**
 * <h1> 交易字段分析查询参数工厂 </h1>
 */
public interface TransactionFieldQueryParamFactory {

    /**
     * <h2> 交易字段类型公共查询 </h2>
     */
    QuerySpecialParams transactionFieldTypeQuery(TransactionFieldAnalysisRequest request);

    /**
     * <h2> 交易字段类型占比自定义归类查询 </h2>
     */
    QuerySpecialParams fieldTypeCustomCollationQuery(TransactionFieldAnalysisRequest request, List<String> containFieldContent);

    /**
     * <h2> 交易字段类型统计详情数据查询 </h2>
     */
    QuerySpecialParams fieldTypeStatisticsDetailQuery(TransactionFieldAnalysisRequest request, String... detailFuzzyFields);
}
