/**
 * @作者 Mcj
 */
package com.zqykj.domain.page;

import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * <h1> 分页信息 数据包装 </h1>
 *
 * @author Mcj
 */
public interface Page<T> extends Iterable<T> {

    /**
     * <h2> 总页数 </h2>
     */
    int getTotalPages();

    /**
     * <h2> 总数据量 </h2>
     */
    long getTotalElements();


    <U> Page<U> map(Function<? super T, ? extends U> converter);

    /**
     * @return the aggregations.
     */
    @Nullable
    Aggregations getAggregations();

    /**
     * @return the maximum score
     */
    float getMaxScore();

    String getScrollId();

    List<T> getContent();

    default Pageable getPageable() {
        return PageRequest.of(getNumber(), getSize(), getSort());
    }

    Sort getSort();

    int getNumber();

    int getSize();
}
