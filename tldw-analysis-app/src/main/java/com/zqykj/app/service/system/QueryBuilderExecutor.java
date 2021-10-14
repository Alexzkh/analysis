package com.zqykj.app.service.system;

import com.zqykj.common.constant.Constants;
import com.zqykj.common.enums.HistogramField;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 查询构建执行器
 * @Author zhangkehou
 * @Date 2021/10/11
 */
public class QueryBuilderExecutor {


//    /**
//     * @param transactionStatisticsRequest: 交易统计请求体
//     * @return: List<QueryParams>
//     **/
//    public static List<QueryParams> buildTransactionStatisticsQuery(TransactionStatisticsRequest transactionStatisticsRequest) {
//
//
//        TransactionStatisticsQuery transactionStatisticsQuery = transactionStatisticsRequest.getTradeStatisticalAnalysisPreRequest();
//
//        TransactionStatisticsAggs transactionStatisticsAggs = transactionStatisticsRequest.getTransactionStatisticsAggs();
//
//        /**
//         * 查询条件类似与sql语句中,where `customerIdentityCard` =  `value`
//         * */
//        QueryParams termQuery = new QueryParams();
//        termQuery.setField(Constants.Individual.FIRST_AGGREGATE_NAME);
//        termQuery.setQueryType(QueryType.term);
//        termQuery.setValue(transactionStatisticsQuery.getIdentityCard());
//
//        /**
//         * 查询条件类似与sql语句中,where `caseId` =  `value`
//         * */
//        QueryParams termQuery1 = new QueryParams();
//        termQuery1.setField(Constants.Individual.CASE_ID);
//        termQuery1.setQueryType(QueryType.term);
//        termQuery1.setValue(transactionStatisticsRequest.getCaseId());
//
//        /**
//         * 查询条件类似与sql语句中范围查询>= 、<= 、>、<
//         * */
//        QueryParams rangeQuery = new QueryParams();
//        rangeQuery.setQueryType(QueryType.range);
//        rangeQuery.setField(Constants.Individual.FOURTH_AGGREGATE_NAME);
//
//        /**
//         * 查询条件类似与sql语句中in查询
//         * */
//        QueryParams termsQuery = new QueryParams();
//        termsQuery.setQueryType(QueryType.terms);
//        termsQuery.setField(Constants.Individual.SECOND_AGGREGATE_NAME);
//        termsQuery.setTermsValues(transactionStatisticsQuery.getIndividualCard());
//
//
//        OperatorParam operatorParam = new OperatorParam();
//        operatorParam.setOperator(Operator.from);
//        operatorParam.setInclude(true);
//        operatorParam.setOperatorValue(transactionStatisticsQuery.getAmountValue());
//
//        List<OperatorParam> operatorParams = new ArrayList<>();
//        operatorParams.add(operatorParam);
//        rangeQuery.setOperatorParams(operatorParams);
//
//        List<QueryParams> queryParams = new ArrayList<>();
//        /**
//         * 根据借贷标志区分入账金额or进账金额。
//         * 当借贷标志为`进`时,则此时表示统计图纵坐标统计的是入账金额的区间范围值
//         * 当借贷标志为`出`时,则此时标识统计图纵坐标统计的是出账金额的区间范围值
//         * */
//        if (transactionStatisticsAggs.getHistorgramField().equals(HistogramField.RECORDED_AMOUNT)) {
//            QueryParams loanTermQuery = new QueryParams();
//            termQuery1.setField(Constants.Individual.THIRD_AGGREGATE_NAME);
//            termQuery1.setQueryType(QueryType.term);
//            termQuery1.setValue("进");
//            queryParams.add(loanTermQuery);
//        }
//
//        if (transactionStatisticsAggs.getHistorgramField().equals(HistogramField.OUTGOING_AMOUNT)) {
//            QueryParams loanTermQuery = new QueryParams();
//            termQuery1.setField(Constants.Individual.THIRD_AGGREGATE_NAME);
//            termQuery1.setQueryType(QueryType.term);
//            termQuery1.setValue("出");
//            queryParams.add(loanTermQuery);
//        }
//        queryParams.add(termQuery);
//        queryParams.add(termQuery1);
//        queryParams.add(rangeQuery);
//        queryParams.add(termsQuery);
//        return queryParams;
//    }
}
