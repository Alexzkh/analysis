package com.zqykj.tldw.aggregate.searching;

import com.zqykj.tldw.aggregate.searching.esclientrhl.enums.AggsType;
import com.zqykj.tldw.aggregate.searching.esclientrhl.enums.SqlFormat;
import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.*;
import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.response.ScrollResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.metrics.Stats;

import java.util.List;
import java.util.Map;

/**
 * Elasticsearch basic function components
 * @see {https://www.elastic.co/guide/en/elasticsearch/reference/7.9/getting-started.html}
 **/
@SuppressWarnings("unchecked")
public interface ElasticsearchTemplateOperations<T, M> extends BaseOperations<T,M>{
    /**
     * create index
     *
     * @param t index pojo
     */
    public boolean save(T t) throws Exception;

}
