/**
 * @作者 Mcj
 */
package com.zqykj.builder;

import com.zqykj.enums.AggsType;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.aggregate.FetchSource;
import com.zqykj.parameters.aggregate.pipeline.PipelineAggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1> AggregationParams 构建器 </h1>
 */
public class AggregationParamsBuilders {

    private AggregationParamsBuilders() {

    }

    /**
     * <h2> 分组聚合 </h2>
     *
     * @param aggName 聚合名称
     * @param field   聚合字段
     * @param include 分组的时候 聚焦某一块数据集
     * @param exclude 分组的时候 排除某一块数据集
     * @param script  脚本
     */
    public static AggregationParams terms(String aggName, String field, @Nullable String script,
                                          @Nullable String[] include, @Nullable String[] exclude) {

        AggregationParams aggregationParams = new AggregationParams(aggName, AggsType.terms.name(), field);

        // 设置include 、exclude、script 等
        Map<String, String[]> includeExclude = new HashMap<>();
        if (null != include && include.length > 0) {
            includeExclude.put("includeValues", include);
        }
        if (null != exclude && exclude.length > 0) {
            includeExclude.put("excludeValues", exclude);
        }
        if (!CollectionUtils.isEmpty(includeExclude)) {
            aggregationParams.setIncludeExclude(includeExclude);
        }
        if (null != script) {

            aggregationParams.setScript(script);
        }
        return aggregationParams;
    }

    public static AggregationParams terms(String aggName, String field) {

        return terms(aggName, field, "");
    }

    public static AggregationParams terms(String aggName, String field, String script) {

        return terms(aggName, field, script, null, null);
    }

    public static AggregationParams terms(String aggName, String field, @Nullable String... include) {

        return terms(aggName, field, null, include, null);
    }

    /**
     * <h2> count 求总量(没有去重) </h2>
     *
     * @param aggName 聚合名称
     * @param field   聚合字段
     * @param script  脚本
     */
    public static AggregationParams count(String aggName, String field, @Nullable String script) {

        return defaultAggregationParams(aggName, AggsType.count.name(), field, script);
    }

    public static AggregationParams count(String aggName, String field) {

        return count(aggName, field, null);
    }

    /**
     * <h2> 去重 </h2>
     *
     * @param aggName 聚合名称
     * @param field   聚合字段
     * @param script  脚本
     */
    public static AggregationParams cardinality(String aggName, String field, @Nullable String script) {

        return defaultAggregationParams(aggName, AggsType.cardinality.name(), field, script);
    }

    /**
     * <h2> 去重 </h2>
     *
     * @param aggName 聚合名称
     * @param field   聚合字段
     */
    public static AggregationParams cardinality(String aggName, String field) {

        return cardinality(aggName, field, null);
    }

    /**
     * <h2> sum 求和 </h2>
     *
     * @param aggName 聚合名称
     * @param field   聚合字段
     * @param script  脚本
     */
    public static AggregationParams sum(String aggName, String field, @Nullable String script) {

        return defaultAggregationParams(aggName, AggsType.sum.name(), field, script);
    }

    /**
     * <h2> sum 求和 </h2>
     *
     * @param aggName 聚合名称
     * @param field   聚合字段
     */
    public static AggregationParams sum(String aggName, String field) {

        return sum(aggName, field, null);
    }

    /**
     * <h2> filter 过滤 </h2>
     *
     * @param aggName 聚合名称
     * @param query   查询参数 {@link QuerySpecialParams}
     * @param script  脚本
     */
    public static AggregationParams filter(String aggName, QuerySpecialParams query, @Nullable String script) {

        AggregationParams aggregationParams = new AggregationParams(aggName, AggsType.filter.name(), query);
        if (null != script) {
            aggregationParams.setScript(script);
        }
        return aggregationParams;
    }

    public static AggregationParams filter(String aggName, QuerySpecialParams query) {

        return filter(aggName, query, null);
    }

    /**
     * <h2> min 求最小值 </h2>
     *
     * @param aggName 聚合名称
     * @param field   聚合字段
     * @param script  脚本
     */
    public static AggregationParams min(String aggName, String field, @Nullable String script) {

        return defaultAggregationParams(aggName, AggsType.min.name(), field, script);
    }

    /**
     * <h2> min 求最大值 </h2>
     *
     * @param aggName 聚合名称
     * @param field   聚合字段
     * @param script  脚本
     */
    public static AggregationParams max(String aggName, String field, @Nullable String script) {

        return defaultAggregationParams(aggName, AggsType.max.name(), field, script);
    }

    /**
     * <h2> 聚合中需要带出的字段 </h2>
     *
     * @param aggName     聚合名称
     * @param fetchSource 需要带出的字段
     */
    public static AggregationParams fieldSource(String aggName, FetchSource fetchSource) {

        return new AggregationParams(aggName, AggsType.top_hits.name(), fetchSource);
    }

    /**
     * <h2> 管道脚本聚合 </h2>
     * <p>
     * 对已经存在定义的聚合结果度量值 进行二次的计算生成一个新的聚合结果度量值
     *
     * @param aggName    聚合名称
     * @param bucketPath 聚合路径
     * @param script     聚合脚本
     */
    public static PipelineAggregationParams pipelineBucketScript(String aggName, Map<String, String> bucketPath, String script) {

        return pipelineBucketScript(aggName, bucketPath, script, null);
    }

    public static PipelineAggregationParams pipelineBucketScript(String aggName, Map<String, String> bucketPath, String script, @Nullable String format) {

        PipelineAggregationParams pipelineBucketScript = new PipelineAggregationParams(aggName, AggsType.bucket_script.name(), bucketPath, script);
        if (StringUtils.isNotBlank(format)) {
            pipelineBucketScript.setFormat(format);
        }
        return pipelineBucketScript;
    }

    /**
     * <h2> 管道聚合筛选 </h2>
     * <p>
     * 对已经存在定义的聚合结果度量值 进行过滤筛选(留下符合条件的统计结果)
     *
     * @param aggName    聚合名称
     * @param bucketPath 聚合路径
     * @param script     聚合脚本
     */
    public static PipelineAggregationParams pipelineSelector(String aggName, Map<String, String> bucketPath, String script) {

        return pipelineSelector(aggName, bucketPath, script, null);
    }

    public static PipelineAggregationParams pipelineSelector(String aggName, Map<String, String> bucketPath, String script, @Nullable String format) {

        PipelineAggregationParams selector = new PipelineAggregationParams(aggName, AggsType.bucket_selector.name(), bucketPath, script);
        if (StringUtils.isNotBlank(format)) {
            selector.setFormat(format);
        }
        return selector;
    }

    /**
     * <h2> 管道聚合再次求和 </h2>
     * <p>
     * 对第一次的统计结果再次求和 (eg. 对卡号分组然后对金额求和, 使用此方法可以求和所用卡的交易总金额)
     */
    public static PipelineAggregationParams pipelineBucketSum(String aggName, String path) {

        return pipelineBucketSum(aggName, path, null);
    }

    public static PipelineAggregationParams pipelineBucketSum(String aggName, String path, @Nullable String format) {

        return new PipelineAggregationParams(aggName, AggsType.sum_bucket.name(), path, null, format);
    }

    /**
     * <h2> 聚合排序 </h2>
     *
     * @param aggName       聚合名称
     * @param fieldSortList 需要排序的field 集合
     * @param from          起始值
     * @param size          偏移量
     */
    public static PipelineAggregationParams sort(String aggName, List<FieldSort> fieldSortList, int from, int size) {

        return new PipelineAggregationParams(aggName, AggsType.bucket_sort.name(), fieldSortList, new Pagination(from, size));
    }

    /**
     * <h2> 聚合排序 </h2>
     *
     * @param aggName 聚合名称
     * @param from    起始值
     * @param size    偏移量
     */
    public static PipelineAggregationParams sort(String aggName, int from, int size) {

        return sort(aggName, Collections.emptyList(), from, size);
    }


    /**
     * <h2> 默认 AggregationParams</h2>
     *
     * @param aggName 聚合名称
     * @param aggType 聚合类型
     * @param field   聚合字段
     * @param script  脚本
     */
    private static AggregationParams defaultAggregationParams(String aggName, String aggType, String field, @Nullable String script) {

        AggregationParams aggregationParams = new AggregationParams(aggName, aggType, field);
        if (null != script) {
            aggregationParams.setScript(script);
        }
        return aggregationParams;
    }
}
