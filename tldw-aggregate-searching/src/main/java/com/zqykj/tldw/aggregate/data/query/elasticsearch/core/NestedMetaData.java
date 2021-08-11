/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch.core;

import org.springframework.lang.Nullable;

/**
 * <h1> Elasticsearch 内嵌套数据 </h1>
 *
 * @author Mcj
 */
public class NestedMetaData {

    private final String field;
    private final int offset;
    @Nullable
    private NestedMetaData child;

    public static NestedMetaData of(String field, int offset, @Nullable NestedMetaData nested) {
        return new NestedMetaData(field, offset, nested);
    }

    private NestedMetaData(String field, int offset, @Nullable NestedMetaData child) {
        this.field = field;
        this.offset = offset;
        this.child = child;
    }

    public String getField() {
        return field;
    }

    public int getOffset() {
        return offset;
    }

    @Nullable
    public NestedMetaData getChild() {
        return child;
    }
}
