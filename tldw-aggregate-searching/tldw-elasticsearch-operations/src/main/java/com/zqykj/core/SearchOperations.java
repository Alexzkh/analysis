/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.repository.query.Query;
import org.elasticsearch.action.search.SearchRequest;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * <h1> 对elasticsearch 进行 DSL query 基础封装 </h1>
 */
public interface SearchOperations {

    /**
     * Execute the criteria query against elasticsearch and return result as {@link SearchHits}
     *
     * @param <T>   element return type
     * @param query the query to execute
     * @param clazz the entity clazz used for property mapping and index name extraction
     * @return SearchHits containing the list of found objects
     */
    <T> SearchHits<T> search(Query query, Class<T> clazz);

    /**
     * Execute the criteria query against elasticsearch and return result as {@link SearchHits}
     *
     * @param <T>   element return type
     * @param query the query to execute
     * @param clazz the entity clazz used for property mapping
     * @param index the index to run the query against
     * @return SearchHits containing the list of found objects
     */
    <T> SearchHits<T> search(Query query, Class<T> clazz, String index);

    /**
     * Execute the criteria query against elasticsearch and return result as {@link SearchHits}
     *
     * @param <T>   element return type
     * @param query the query to execute
     * @param clazz the entity clazz used for property mapping
     * @param index the index to run the query against
     * @return SearchHits containing the list of found objects
     */
    <T> SearchHits<T> search(SearchRequest query, Class<T> clazz, String index);

    @Nullable
    default <T> SearchHit<T> searchOne(Query query, Class<T> clazz, String index) {
        List<SearchHit<T>> content = search(query, clazz, index).getSearchHits();
        return content.isEmpty() ? null : content.get(0);
    }

    <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz);

    <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz, String index);
}
