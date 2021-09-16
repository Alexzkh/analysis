/**
 * @作者 Mcj
 */
package com.zqykj.repository;

import com.zqykj.annotations.NoRepositoryBean;
import com.zqykj.common.request.DateHistogramParams;
import com.zqykj.common.response.Stats;
import com.zqykj.enums.AggsType;

import java.util.Map;

/**
 * <h1> 提供给外部使用的公共入口Repository </h1>
 */
@NoRepositoryBean
public interface EntranceRepository<T, ID> extends CrudRepository<T, ID> {


//    public <T> T aggsStat(String merticName,Class<T> tClass,String... indexes) throws Exception;


    /**
     * @param metricsName:
     * @param aggsType:
     * @param clazz:
     * @param indexes:
     * @return: S
     **/
    <S extends T> S metricsAggs(String metricsName, AggsType aggsType, Class<S> clazz, String... indexes);

    /**
     * @param metricsName:
     * @param clazz:
     * @param indexes:
     * @return: com.zqykj.common.response.Stats
     **/
    <S extends T> Stats statsAggs(String metricsName, Class<S> clazz, String... indexes);

    /**
     * @param metricsName:
     * @param clazz:
     * @param bucketName:
     * @param indexes:
     * @return: java.util.Map<java.lang.String, com.zqykj.common.response.Stats>
     **/
    <S extends T> Map<String, Stats> statsAggs(String metricsName, Class<S> clazz, String bucketName, String... indexes);

    /**
     * @param metricsName:
     * @param clazz:
     * @param customSegment:
     * @param indexes:
     * @return: java.util.Map<java.lang.Double, java.lang.Double>
     **/
    <S extends T> Map<Double, Double> percentilesAggs(String metricsName, Class<S> clazz, double[] customSegment, String... indexes);

    /**
     * @param metricsName:
     * @param clazz:
     * @param customSegment:
     * @param indexes:
     * @return: java.util.Map<java.lang.Double, java.lang.Double>
     **/
    <S extends T> Map<Double, Double> percentilesRanksAggs(String metricsName, Class<S> clazz, double[] customSegment, String... indexes);

    /**
     * @param metricsName:
     * @param aggsType:
     * @param clazz:
     * @param bucketName:
     * @param interval:
     * @param indexes:
     * @return: java.util.Map
     **/
    <S extends T> Map histogramAggs(String metricsName, AggsType aggsType, Class<S> clazz, String bucketName, double interval, String... indexes);

    /**
     * @param metricsName:
     * @param aggsType:
     * @param clazz:
     * @param bucketName:
     * @param dateHistogramParams:
     * @param indexes:
     * @return: java.util.Map
     **/
    <S extends T> Map dateHistogramAggs(String metricsName, AggsType aggsType, Class<S> clazz, String bucketName, DateHistogramParams dateHistogramParams, String... indexes);

    /**
     * @param metricName:
     * @param precisionThreshold:
     * @param clazz:
     * @param indexes:
     * @return: long
     **/
    long cardinality(String metricName, long precisionThreshold, Class<T> clazz, String indexes);


}
