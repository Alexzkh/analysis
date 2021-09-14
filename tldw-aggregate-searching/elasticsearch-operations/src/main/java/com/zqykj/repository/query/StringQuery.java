/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.domain.Pageable;
import com.zqykj.domain.Sort;

/**
 * <h1> 处理@Query 的 value </h1>
 */
public class StringQuery extends AbstractQuery {

    private String source;

    public StringQuery(String source) {
        this.source = source;
    }

    public StringQuery(String source, Pageable pageable) {
        this.source = source;
        this.pageable = pageable;
    }

    public StringQuery(String source, Pageable pageable, Sort sort) {
        this.pageable = pageable;
        this.sort = sort;
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
