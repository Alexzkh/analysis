/**
 * @作者 Mcj
 */
package com.zqykj.core.document;

import org.springframework.lang.Nullable;

/**
 * <h1> Elasticsearch 嵌套元数据 (field 类型是嵌套类型) </h1>
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
