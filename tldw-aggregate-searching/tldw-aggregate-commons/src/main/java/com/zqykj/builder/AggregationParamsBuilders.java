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
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

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

        return new AggregationParams(aggName, AggsType.terms.name(), field);
    }

    public static AggregationParams terms(String aggName, String field, String script) {

        AggregationParams aggregationParams = new AggregationParams(aggName, AggsType.terms.name(), field);
        if (null != script) {

            aggregationParams.setScript(script);
        }
        return aggregationParams;
    }

    public static AggregationParams terms(String aggName, String field, @Nullable String[] include) {

        AggregationParams aggregationParams = new AggregationParams(aggName, AggsType.terms.name(), field);
        // 设置include 、exclude、script 等
        Map<String, String[]> includeExclude = new HashMap<>();
        if (null != include && include.length > 0) {
            includeExclude.put("includeValues", include);
        }
        if (!CollectionUtils.isEmpty(includeExclude)) {
            aggregationParams.setIncludeExclude(includeExclude);
        }
        return aggregationParams;
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

        return new AggregationParams(aggName, AggsType.cardinality.name(), field);
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

        return new AggregationParams(aggName, AggsType.sum.name(), field);
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
     *
     * @param aggName    聚合名称
     * @param bucketPath 聚合路径
     * @param script     聚合脚本
     */
    public static PipelineAggregationParams pipelineBucketScript(String aggName, Map<String, String> bucketPath, String script) {

        return new PipelineAggregationParams(aggName, AggsType.bucket_script.name(), bucketPath, script);
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

        return new PipelineAggregationParams(aggName, AggsType.bucket_sort.name(), new Pagination(from, size));
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
