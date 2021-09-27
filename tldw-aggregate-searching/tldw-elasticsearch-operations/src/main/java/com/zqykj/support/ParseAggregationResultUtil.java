package com.zqykj.support;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.ParsedMin;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: parse the result to map
 * @Author zhangkehou
 * @Date 2021/9/24
 */
public class ParseAggregationResultUtil {


    /**
     * @param searchResponse: aggregations result
     * @param name:           terms name
     * @return: java.util.Map
     **/
    public static Map parse(SearchResponse searchResponse, String name) {

        Aggregations aggregations = searchResponse.getAggregations();
        Terms customerIdentityCardTerms = aggregations.get(name);
        List resultMapList = new ArrayList();
        customerIdentityCardTerms.getBuckets().stream().forEach(bucket -> {
            Map singleAggregationResult = new HashMap();
            ParsedStringTerms.ParsedBucket parentBucket = (ParsedStringTerms.ParsedBucket) bucket;
            singleAggregationResult.put(AggregationResultConstants.Multilayer.CARD,parentBucket.getKey());
            singleAggregationResult.put(AggregationResultConstants.Multilayer.TRANSACTION_TOTAL_NUMS, parentBucket.getDocCount());
            List<Aggregation> statisticResultList = bucket.getAggregations().asList();
            for (Aggregation aggregation : statisticResultList) {
                /**
                 * 解析最早交易时间
                 * */
                if (aggregation instanceof ParsedMin) {
                    ParsedMin parsedMin = (ParsedMin) aggregation;
                    singleAggregationResult.put(AggregationResultConstants.Multilayer.EARLIEST_TRADING_TIME, parsedMin.getValueAsString());
                }

                /**
                 * 解析最晚交易时间
                 * */
                if (aggregation instanceof ParsedMax) {
                    ParsedMax parsedMax = (ParsedMax) aggregation;
                    singleAggregationResult.put(AggregationResultConstants.Multilayer.LATEST_TRADING_TIME, parsedMax.getValueAsString());
                }
                /**
                 * 解析交易总金额
                 * */
                if (aggregation instanceof ParsedSum) {
                    ParsedSum parsedSum = (ParsedSum) aggregation;
                    singleAggregationResult.put(AggregationResultConstants.Multilayer.TRANSACTION_TOTAL_AMOUNT, parsedSum.getValue());
                }
                /**
                 * 解析借贷标志聚合结果
                 * */
                if (aggregation instanceof ParsedStringTerms) {

                    ParsedStringTerms termAggs = (ParsedStringTerms) aggregation;
                    /***/
                    if (termAggs.getName().equals(AggregationResultConstants.Multilayer.TERMS_ACCOUNT_CARD)){
                        singleAggregationResult.put(AggregationResultConstants.Multilayer.ACCOUNT_CARD_NUMS,termAggs.getBuckets().size());
                        continue;
                    }
                    termAggs.getBuckets().stream().forEach(lendMarkTerms -> {
                        ParsedStringTerms.ParsedBucket sumsAamount = (ParsedStringTerms.ParsedBucket) lendMarkTerms;
                        sumsAamount.getAggregations().asList().stream().forEach(parsedSum -> {
                            if (parsedSum instanceof ParsedSum) {
                                ParsedSum lendMarkSum = (ParsedSum) parsedSum;
                                /**
                                 * 解析入账笔数、出账笔数、入账金额、出账金额
                                 * */
                                if (sumsAamount.getKey().equals(AggregationResultConstants.Multilayer.ENTRIES)) {
                                    singleAggregationResult.put(AggregationResultConstants.Multilayer.ENTRIES_AMOUNT, lendMarkSum.getValue());
                                    singleAggregationResult.put(AggregationResultConstants.Multilayer.ENTRIES_NUMS, sumsAamount.getDocCount());
                                } else if (sumsAamount.getKey().equals(AggregationResultConstants.Multilayer.OUTGOING)) {
                                    singleAggregationResult.put(AggregationResultConstants.Multilayer.OUTGOING_AMOUNT, lendMarkSum.getValue());
                                    singleAggregationResult.put(AggregationResultConstants.Multilayer.OUTGOING_NUMS, sumsAamount.getDocCount());
                                }

                            }
                        });
                    });

                    /**
                     * 解析交易净额
                     * */
                    singleAggregationResult.put(AggregationResultConstants.Multilayer.TRANSACTION_NET_AMOUNT,
                            (Double) singleAggregationResult.get(AggregationResultConstants.Multilayer.ENTRIES_AMOUNT) -
                                    (Double) singleAggregationResult.get(AggregationResultConstants.Multilayer.OUTGOING_AMOUNT));
                }
            }
            resultMapList.add(singleAggregationResult);
        });

        Map result = new HashMap();
        result.put(name, resultMapList);
        return result;
    }
}
