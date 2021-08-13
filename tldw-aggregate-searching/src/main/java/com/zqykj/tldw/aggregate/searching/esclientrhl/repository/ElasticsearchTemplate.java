//package com.zqykj.tldw.aggregate.searching.esclientrhl.repository;
//
//import com.zqykj.tldw.aggregate.searching.BaseOperations;
//import com.zqykj.tldw.aggregate.searching.esclientrhl.enums.AggsType;
//import com.zqykj.tldw.aggregate.searching.esclientrhl.enums.SqlFormat;
//import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.response.ScrollResponse;
//import org.elasticsearch.action.bulk.BulkResponse;
//import org.elasticsearch.action.search.ClearScrollResponse;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.Request;
//import org.elasticsearch.client.Response;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.reindex.BulkByScrollResponse;
//import org.elasticsearch.search.aggregations.AggregationBuilder;
//import org.elasticsearch.search.aggregations.Aggregations;
//import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
//import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
//import org.elasticsearch.search.aggregations.metrics.Stats;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * Elasticsearch basic function components
// * @see {https://www.elastic.co/guide/en/elasticsearch/reference/7.9/getting-started.html}
// **/
//@SuppressWarnings("unchecked")
//public interface ElasticsearchTemplate<T, M> {
//    /**
//     * Query through low level rest client
//     *
//     * @param request native query object
//     * @return
//     * @throws Exception
//     */
//    public Response request(Request request) throws Exception;
//
//    /**
//     * create index
//     *
//     * @param t index pojo
//     */
//    public boolean save(T t) throws Exception;
//
//    /**
//     * create index (routing)
//     *
//     * @param t       index pojo
//     * @param routing rouing infomation（the routing is _id from data by default）
//     * @return
//     * @throws Exception
//     */
//    public boolean save(T t, String routing) throws Exception;
//
//    /**
//     * create index collection
//     *
//     * @param list index pojo list
//     */
//    public BulkResponse save(List<T> list) throws Exception;
//
//
//    /**
//     * New index set (batch mode, improve performance, prevent es service memory overflow, default 5000 pieces of data per batch)
//     *
//     * @param list index pojo list
//     */
//    public BulkResponse[] saveBatch(List<T> list) throws Exception;
//
//    /**
//     * Update index list
//     *
//     * @param list index pojo list
//     * @return
//     * @throws Exception
//     */
//    public BulkResponse bulkUpdate(List<T> list) throws Exception;
//
//    /**
//     * Update index set (batch mode, improve performance, prevent es service memory overflow, default 5000 pieces of data per batch)
//     *
//     * @param list 索引pojo集合
//     * @return
//     * @throws Exception
//     */
//    public BulkResponse[] bulkUpdateBatch(List<T> list) throws Exception;
//
//
//    /**
//     * Update index by value field
//     *
//     * @param t index pojo list
//     */
//    public boolean update(T t) throws Exception;
//
//    /**
//     * Update index by value field  according to query builder query result .
//     *
//     * @param queryBuilder query condition
//     * @param t            index pojo
//     * @param clazz        index pojo type
//     * @param limitcount   update field can not over limit count
//     * @param asyn         asyc or not
//     * @return
//     * @throws Exception
//     */
//    public BulkResponse batchUpdate(QueryBuilder queryBuilder, T t, Class clazz, int limitcount, boolean asyn) throws Exception;
//
//    /**
//     * update cover index
//     *
//     * @param t index pojo.
//     */
//    public boolean updateCover(T t) throws Exception;
//
//    /**
//     * delete index
//     *
//     * @param t index pojo
//     */
//    public boolean delete(T t) throws Exception;
//
//
//    /**
//     * drop index by routing
//     *
//     * @param t       index pojo
//     * @param routing rouing infomation（the routing is _id from data by default）
//     * @return
//     * @throws Exception
//     */
//    public boolean delete(T t, String routing) throws Exception;
//
//
//    /**
//     * drop index according condition
//     *
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public BulkByScrollResponse deleteByCondition(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//    /**
//     * delete index
//     *
//     * @param id    index key
//     * @param clazz index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public boolean deleteById(M id, Class<T> clazz) throws Exception;
//
//
//    /**
//     * Native Query
//     *
//     * @param searchRequest Native Query request object
//     * @return
//     * @throws Exception
//     */
//    public SearchResponse search(SearchRequest searchRequest) throws Exception;
//
//    /**
//     * Non paged query
//     *
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//    /**
//     * Non paged query(Cross index)
//     *
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception;
//
//    /**
//     * Non paged query，Specifies the maximum number of returns
//     *
//     * @param queryBuilder query contion
//     * @param limitSize    the maximum number of returns
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public List<T> searchMore(QueryBuilder queryBuilder, int limitSize, Class<T> clazz) throws Exception;
//
//    /**
//     * Non paged query(Cross index)，Specifies the maximum number of returns
//     *
//     * @param queryBuilder query contion
//     * @param limitSize    the maximum number of returns
//     * @param clazz        index pojo class type
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public List<T> searchMore(QueryBuilder queryBuilder, int limitSize, Class<T> clazz, String... indexs) throws Exception;
//
//
//    /**
//     * Query by uri query string
//     *
//     * @param uri   uri query contion
//     * @param clazz index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public List<T> searchUri(String uri, Class<T> clazz) throws Exception;
//
//
//    /**
//     * query by sql
//     *
//     * @param sql       sql script (support MySQL syntax)
//     * @param sqlFormat sql request returns
//     * @return
//     * @throws Exception
//     */
//    public String queryBySQL(String sql, SqlFormat sqlFormat) throws Exception;
//
//    /**
//     * count
//     *
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public long count(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//
//    /**
//     * count(Cross index)
//     *
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public long count(QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception;
//
//    /**
//     * Support pagination, highlighting, sorting query
//     *
//     * @param queryBuilder      query contion
//     * @param pageSortHighLight Pagination + sorting + highlight object encapsulation
//     * @param clazz             index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight, Class<T> clazz) throws Exception;
//
//
//    /**
//     * Support pagination, highlighting, sorting query（Cross index）
//     *
//     * @param queryBuilder      query contion
//     * @param pageSortHighLight Pagination + sorting + highlight object encapsulation
//     * @param clazz             index pojo class type
//     * @param indexs            index name
//     * @return
//     * @throws Exception
//     */
//    public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight, Class<T> clazz, String... indexs) throws Exception;
//
//
//    /**
//     * It supports paging, highlighting, sorting, specifying return fields and routing queries
//     *
//     * @param queryBuilder query contion
//     * @param attach       Query enhanced objects (supports pagination, highlighting, sorting, specifying return fields, routing, and customization of searchafter information)
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public PageList<T> search(QueryBuilder queryBuilder, Attach attach, Class<T> clazz) throws Exception;
//
//
//    /**
//     * It supports paging, highlighting, sorting, specifying return fields and routing queries（Cross index）
//     *
//     * @param queryBuilder query contion
//     * @param attach       Query enhanced objects (supports pagination, highlighting, sorting, specifying return fields, routing, and customization of searchafter information)
//     * @param clazz        index pojo class type
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public PageList<T> search(QueryBuilder queryBuilder, Attach attach, Class<T> clazz, String... indexs) throws Exception;
//
//
//    /**
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    @Deprecated
//    public List<T> scroll(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//    /**
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    @Deprecated
//    public List<T> scroll(QueryBuilder queryBuilder, Class<T> clazz, Long time, String... indexs) throws Exception;
//
//
//    /**
//     * Query in scroll mode and create scroll
//     *
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param time         Scroll window time in hours
//     * @param size         Number of scroll queries per time
//     * @return
//     * @throws Exception
//     */
//    public ScrollResponse<T> createScroll(QueryBuilder queryBuilder, Class<T> clazz, Long time, Integer size) throws Exception;
//
//    /**
//     * Query in scroll mode and create scroll
//     *
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param time         Scroll window time in hours
//     * @param size         Number of scroll queries per time
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public ScrollResponse<T> createScroll(QueryBuilder queryBuilder, Class<T> clazz, Long time, Integer size, String... indexs) throws Exception;
//
//
//    /**
//     * Query in scroll
//     *
//     * @param clazz    index pojo class type
//     * @param time     Scroll window time in hours
//     * @param scrollId Scroll query ID, from scrollresponse
//     * @return
//     * @throws Exception
//     */
//    public ScrollResponse<T> queryScroll(Class<T> clazz, Long time, String scrollId) throws Exception;
//
//
//    /**
//     * clear scroll snopshot
//     *
//     * @param scrollId
//     * @return
//     * @throws Exception
//     */
//    public ClearScrollResponse clearScroll(String... scrollId) throws Exception;
//
//    /**
//     * Search by template. Template has been saved in script directory
//     *
//     * @param template_params Template parameters
//     * @param templateName    template name
//     * @param clazz           index pojo class type
//     * @return
//     */
//    public List<T> searchTemplate(Map<String, Object> template_params, String templateName, Class<T> clazz) throws Exception;
//
//    /**
//     * Search in template mode, and pass in template content in parameter mode
//     *
//     * @param template_params Template parameters
//     * @param templateSource  Template content
//     * @param clazz           index pojo class type
//     * @return
//     */
//    public List<T> searchTemplateBySource(Map<String, Object> template_params, String templateSource, Class<T> clazz) throws Exception;
//
//    /**
//     * Save Template
//     *
//     * @param templateName   template name
//     * @param templateSource Template content
//     * @return
//     */
//    public Response saveTemplate(String templateName, String templateSource) throws Exception;
//
//    /**
//     * Completion Suggester
//     *
//     * @param fieldName  Search suggestion corresponding query field
//     * @param fieldValue Completion Suggester query contion
//     * @param clazz      index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public List<String> completionSuggest(String fieldName, String fieldValue, Class<T> clazz) throws Exception;
//
//
//    /**
//     * Completion Suggester
//     *
//     * @param fieldName  Search suggestion corresponding query field
//     * @param fieldValue query contion
//     * @param clazz      index pojo class type
//     * @param indexs     index name
//     * @return
//     * @throws Exception
//     */
//    public List<String> completionSuggest(String fieldName, String fieldValue, Class<T> clazz, String... indexs) throws Exception;
//
//
//    /**
//     * phrase Suggester
//     *
//     * @param fieldName  Search suggestion corresponding query field
//     * @param fieldValue query contion
//     * @param param      Customize parameters of phrace suggester
//     * @param clazz      index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public List<String> phraseSuggest(String fieldName, String fieldValue, ElasticsearchTemplateImpl.PhraseSuggestParam param, Class<T> clazz) throws Exception;
//
//
//    /**
//     * phrase Suggester
//     *
//     * @param fieldName  Search suggestion corresponding query field
//     * @param fieldValue query contion
//     * @param param      Customize parameters of phrace suggester
//     * @param clazz      index pojo class type
//     * @param indexs     index name
//     * @return
//     * @throws Exception
//     */
//    public List<String> phraseSuggest(String fieldName, String fieldValue, ElasticsearchTemplateImpl.PhraseSuggestParam param, Class<T> clazz, String... indexs) throws Exception;
//
//
//    /**
//     * Query by ID
//     *
//     * @param id    index id
//     * @param clazz index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public T getById(M id, Class<T> clazz) throws Exception;
//
//    /**
//     * Batch query according to ID list
//     *
//     * @param ids   index id array
//     * @param clazz index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public List<T> mgetById(M[] ids, Class<T> clazz) throws Exception;
//
//    /**
//     * Does ID data exist
//     *
//     * @param id    Index data ID value
//     * @param clazz index pojo class type
//     * @return
//     */
//    public boolean exists(M id, Class<T> clazz) throws Exception;
//
//    /**
//     * General aggregate query
//     * Metric metric in aggtypes by bucket grouping
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketName   bucket field name
//     * @return
//     */
//    public Map aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName) throws Exception;
//
//
//    /**
//     * General aggregate query
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketName   bucket field name
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public Map aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, String... indexs) throws Exception;
//
//    /**
//     * Metric measurement in the way of aggstypes
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public double aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//    /**
//     * Metric measurement in the way of aggstypes
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public double aggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception;
//
//
//    /**
//     * Drill down aggregate query (no sort default policy)
//     * Metric metric in aggtypes by bucket grouping
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketNames  bucket field name
//     * @return
//     * @throws Exception
//     */
//    public List<Down> aggswith2level(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String[] bucketNames) throws Exception;
//
//
//    /**
//     * Drill down aggregate query (no sort default policy)
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketNames  bucket field name
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public List<Down> aggswith2level(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String[] bucketNames, String... indexs) throws Exception;
//
//
//    /**
//     * Statistical aggregation metric measure
//     *
//     * @param metricName   Measure field name
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public Stats statsAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//    /**
//     * Statistical aggregation metric measure
//     *
//     * @param metricName   Measure field name
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public Stats statsAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception;
//
//    /**
//     * Statistical aggregation metric measure by bucket
//     *
//     * @param metricName   Measure field name
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketName   bucket field name
//     * @return
//     * @throws Exception
//     */
//    public Map<String, Stats> statsAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, String bucketName) throws Exception;
//
//    /**
//     * Statistical aggregation metric measure by bucket
//     *
//     * @param metricName   Measure field name
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketName   bucket field name
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public Map<String, Stats> statsAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, String... indexs) throws Exception;
//
//
//    /**
//     * General (customized) aggregation foundation method
//     *
//     * @param aggregationBuilder Native aggregation builder
//     * @param queryBuilder       query contion
//     * @param clazz              index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public Aggregations aggs(AggregationBuilder aggregationBuilder, QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//    /**
//     * General (customized) aggregation foundation method
//     *
//     * @param aggregationBuilder Native aggregation builder
//     * @param queryBuilder       query contion
//     * @param clazz              index pojo class type
//     * @param indexs             index name
//     * @return
//     * @throws Exception
//     */
//    public Aggregations aggs(AggregationBuilder aggregationBuilder, QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception;
//
//
//    /**
//     * Cardinality query
//     *
//     * @param metricName   Measure field name
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public long cardinality(String metricName, QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//
//    /**
//     * Cardinality query
//     *
//     * @param metricName         Measure field name
//     * @param queryBuilder       query contion
//     * @param precisionThreshold Set the precisionthreshold, which is 3000 by default and 40000 by maximum
//     * @param clazz              index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public long cardinality(String metricName, QueryBuilder queryBuilder, long precisionThreshold, Class<T> clazz) throws Exception;
//
//    /**
//     * Cardinality query
//     *
//     * @param metricName   Measure field name
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public long cardinality(String metricName, QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception;
//
//    /**
//     * Cardinality query
//     *
//     * @param metricName         Measure field name
//     * @param queryBuilder       query contion
//     * @param precisionThreshold Set the precisionthreshold, which is 3000 by default and 40000 by maximum
//     * @param clazz              index pojo class type
//     * @param indexs             index name
//     * @return
//     * @throws Exception
//     */
//    public long cardinality(String metricName, QueryBuilder queryBuilder, long precisionThreshold, Class<T> clazz, String... indexs) throws Exception;
//
//    /**
//     * Percentage aggregation
//     *
//     * @param metricName   Measure field name
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @return
//     * @throws Exception
//     */
//    public Map percentilesAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz) throws Exception;
//
//    /**
//     * Percentage aggregation
//     *
//     * @param metricName    Measure field name
//     * @param queryBuilder  query contion
//     * @param clazz         index pojo class type
//     * @param customSegment Percentage segment
//     * @param indexs        index name
//     * @return
//     * @throws Exception
//     */
//    public Map percentilesAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, double[] customSegment, String... indexs) throws Exception;
//
//
//    /**
//     * Aggregate in percentile level (count the percentage within the numerical value)
//     *
//     * @param metricName    Measure field name
//     * @param queryBuilder  query contion
//     * @param clazz         index pojo class type
//     * @param customSegment Percentage segment
//     * @return
//     * @throws Exception
//     */
//    public Map percentileRanksAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, double[] customSegment) throws Exception;
//
//    /**
//     * Aggregate in percentile level (count the percentage within the numerical value)
//     *
//     * @param metricName    Measure field name
//     * @param queryBuilder  query contion
//     * @param clazz         index pojo class type
//     * @param customSegment Percentage segment
//     * @param indexs        index name
//     * @return
//     * @throws Exception
//     */
//    public Map percentileRanksAggs(String metricName, QueryBuilder queryBuilder, Class<T> clazz, double[] customSegment, String... indexs) throws Exception;
//
//
//    /**
//     * Filter aggregation
//     * new FiltersAggregator.KeyedFilter("men", QueryBuilders.termQuery("gender", "male"))
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param clazz        index pojo class type
//     * @param queryBuilder query contion
//     * @param filters      Bucket filter array
//     * @return
//     * @throws Exception
//     */
//    public Map filterAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, FiltersAggregator.KeyedFilter[] filters) throws Exception;
//
//    /**
//     * 过滤器聚合
//     * new FiltersAggregator.KeyedFilter("men", QueryBuilders.termQuery("gender", "male"))
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param clazz        index pojo class type
//     * @param queryBuilder query contion
//     * @param filters      Bucket filter array
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public Map filterAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, FiltersAggregator.KeyedFilter[] filters, String... indexs) throws Exception;
//
//
//    /**
//     * Histogram aggregation
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketName   bucket field name
//     * @param interval     Interval of bucket field values
//     * @return
//     * @throws Exception
//     */
//    public Map histogramAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, double interval) throws Exception;
//
//
//    /**
//     * Histogram aggregation
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketName   bucket field name
//     * @param interval     Interval of bucket field values
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public Map histogramAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, double interval, String... indexs) throws Exception;
//
//
//    /**
//     * Date histogram aggregation
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketName   bucket field name
//     * @param interval     分桶日期字段值的间隔
//     * @return
//     * @throws Exception
//     */
//    public Map dateHistogramAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, DateHistogramInterval interval) throws Exception;
//
//    /**
//     * Date histogram aggregation
//     *
//     * @param metricName   Measure field name
//     * @param aggsType     Measure type
//     * @param queryBuilder query contion
//     * @param clazz        index pojo class type
//     * @param bucketName   bucket field name
//     * @param interval     Interval of bucket field values
//     * @param indexs       index name
//     * @return
//     * @throws Exception
//     */
//    public Map dateHistogramAggs(String metricName, AggsType aggsType, QueryBuilder queryBuilder, Class<T> clazz, String bucketName, DateHistogramInterval interval, String... indexs) throws Exception;
//}
