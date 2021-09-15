/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation;

import com.zqykj.core.ScoredPage;
import com.zqykj.core.ScrolledPage;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;

/**
 * <h1> 聚合分页 </h1>
 */
public interface AggregatedPage<T> extends ScrolledPage<T>, ScoredPage<T> {

    boolean hasAggregations();

    @Nullable
    Aggregations getAggregations();

    @Nullable
    Aggregation getAggregation(String name);
}
