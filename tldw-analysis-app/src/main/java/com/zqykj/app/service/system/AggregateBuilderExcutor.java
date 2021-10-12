package com.zqykj.app.service.system;

import com.zqykj.common.constant.Constants;
import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.common.request.IndividualRequest;
import com.zqykj.common.request.QueryParams;
import com.zqykj.enums.AggsType;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 聚合查询构建执行器
 * @Author zhangkehou
 * @Date 2021/9/23
 */
public class AggregateBuilderExcutor {


    /**
     * @Description: 构建聚合查询为人和卡一起查询出来的聚合参数
     * @return: com.zqykj.common.request.AggregateBuilder
     **/
    public static AggregateBuilder buildPeopleWithCardAggregateBuilder() {

        /**
         * 聚合最早交易时间
         * */
        AggregateBuilder aggregateBuilder1 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.min)
                .build();

        /**
         * 聚合最晚交易时间
         * */
        AggregateBuilder aggregateBuilder2 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.max)
                .build();

        /**
         * 聚合交易总金额
         * */
        AggregateBuilder aggregateBuilder3 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FOURTH_AGGREGATE_NAME)
                .aggregateType(AggsType.sum)
                .build();

        /**
         * 聚合借贷标志并分别计算聚合结果的交易总金额
         * */
        List<AggregateBuilder> aggregateBuilders = new ArrayList<>();
        aggregateBuilders.add(aggregateBuilder3);
        AggregateBuilder aggregateBuilder4 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.THIRD_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .subAggregations(aggregateBuilders)
                .build();


        /**
         * 聚合计算人下面卡的最早交易时间、最晚交易时间、交易总金额、借贷标志以及各借贷标志下交易总金额
         * */
        List<AggregateBuilder> secondBuilders = new ArrayList<>();
        secondBuilders.add(aggregateBuilder4);
        secondBuilders.add(aggregateBuilder3);
        secondBuilders.add(aggregateBuilder2);
        secondBuilders.add(aggregateBuilder1);

        AggregateBuilder secondBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.SECOND_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(Constants.Individual.SECOND_AGGS_SIZE)
                .subAggregations(secondBuilders)
                .build();


        List<AggregateBuilder> firstBuilders = new ArrayList<>();
        firstBuilders.add(secondBuilder);
        firstBuilders.add(aggregateBuilder4);
        firstBuilders.add(aggregateBuilder3);
        firstBuilders.add(aggregateBuilder2);
        firstBuilders.add(aggregateBuilder1);

        /**
         * 聚合个体统计数据
         * */
        AggregateBuilder aggregateBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIRST_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(Constants.Individual.FIRST_AGGS_SIZE)
                .subAggregations(firstBuilders)
                .build();

        return aggregateBuilder;

    }

    /**
     * @Description: 构建人的聚合查询请求入参
     * @return: com.zqykj.common.request.AggregateBuilder
     **/
    public static AggregateBuilder buildPeopleAggregateBuilder(IndividualRequest individualRequest) {

        /**
         * 聚合最早交易时间
         * */
        AggregateBuilder aggregateBuilder1 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.min)
                .build();

        /**
         * 聚合最晚交易时间
         * */
        AggregateBuilder aggregateBuilder2 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.max)
                .build();

        /**
         * 聚合交易总金额
         * */
        AggregateBuilder aggregateBuilder3 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FOURTH_AGGREGATE_NAME)
                .aggregateType(AggsType.sum)
                .build();

        /**
         * 聚合借贷标志并分别计算聚合结果的交易总金额
         * */
        List<AggregateBuilder> aggregateBuilders = new ArrayList<>();
        aggregateBuilders.add(aggregateBuilder3);
        AggregateBuilder aggregateBuilder4 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.THIRD_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .subAggregations(aggregateBuilders)
                .size(10)
                .build();

        /**
         * 聚合调单个体个数
         * */
        AggregateBuilder secondBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.SECOND_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(Constants.Individual.SECOND_AGGS_SIZE)
                .build();

        /**
         * 聚合结果的分页、排序
         * */
        AggregateBuilder aggregateBuilder5 = AggregateBuilder.builder()
                .aggregateName("bucket_sort")
                .from(individualRequest.getPagingRequest().getPage())
                .size(individualRequest.getPagingRequest().getPageSize())
                .aggregateType(AggsType.bucket_sort)
                .build();


        List<AggregateBuilder> firstBuilders = new ArrayList<>();
        firstBuilders.add(secondBuilder);
        firstBuilders.add(aggregateBuilder5);
        firstBuilders.add(aggregateBuilder4);
        firstBuilders.add(aggregateBuilder3);
        firstBuilders.add(aggregateBuilder2);
        firstBuilders.add(aggregateBuilder1);

        /**
         * 模糊查询参数
         * */
        QueryParams queryParam = QueryParams.builder().value(individualRequest.getKeyword()).build();
        /**
         * 聚合个体统计数据
         * */
        AggregateBuilder aggregateBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIRST_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(Constants.Individual.FIRST_AGGS_SIZE)
                .routing(individualRequest.getCaseId())
                .queryParams(queryParam)
                .subAggregations(firstBuilders)
                .build();


        return aggregateBuilder;

    }


    /**
     * @param individualRequest: query所需要的查询字段的值
     * @return: com.zqykj.common.request.AggregateBuilder
     **/
    public static AggregateBuilder buildCardAggregateBuilder(IndividualRequest individualRequest) {

        /**
         * 聚合最早交易时间
         * */
        AggregateBuilder aggregateBuilder1 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.min)
                .build();

        /**
         * 聚合最晚交易时间
         * */
        AggregateBuilder aggregateBuilder2 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FIFTH_AGGREGATE_NAME)
                .aggregateType(AggsType.max)
                .build();

        /**
         * 聚合交易总金额
         * */
        AggregateBuilder aggregateBuilder3 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.FOURTH_AGGREGATE_NAME)
                .aggregateType(AggsType.sum)
                .build();

        /**
         * 聚合借贷标志并分别计算聚合结果的交易总金额
         * */
        List<AggregateBuilder> aggregateBuilders = new ArrayList<>();
        aggregateBuilders.add(aggregateBuilder3);
        AggregateBuilder aggregateBuilder4 = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.THIRD_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .subAggregations(aggregateBuilders)
                .size(10)
                .build();

        /**
         * 聚合结果的分页排序
         * */
//        AggregateBuilder aggregateBuilder5 = AggregateBuilder.builder()
//                .aggregateName("bucket_sort")
//                .from(individualRequest.getPagingRequest().getPage())
//                .size(individualRequest.getPagingRequest().getPageSize())
//                .aggregateType(AggsType.bucket_sort)
//                .build();
        List<AggregateBuilder> firstBuilders = new ArrayList<>();
//        firstBuilders.add(aggregateBuilder5);
        firstBuilders.add(aggregateBuilder4);
        firstBuilders.add(aggregateBuilder3);
        firstBuilders.add(aggregateBuilder2);
        firstBuilders.add(aggregateBuilder1);


        /**
         * 聚合之外的查询参数
         * */
        QueryParams queryParams = QueryParams.builder()
                .queryType(QueryType.term)
                .value(individualRequest.getValue())
                .field(individualRequest.getField())
                .build();

        /**
         * 聚合card的统计信息
         * */
        AggregateBuilder aggregateBuilder = AggregateBuilder.builder()
                .aggregateName(Constants.Individual.SECOND_AGGREGATE_NAME)
                .aggregateType(AggsType.terms)
                .size(10000)
                .subAggregations(firstBuilders)
                .queryParams(queryParams)
                .build();


        return aggregateBuilder;

    }
}
