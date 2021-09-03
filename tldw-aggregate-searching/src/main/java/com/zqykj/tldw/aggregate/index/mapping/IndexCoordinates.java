package com.zqykj.tldw.aggregate.index.mapping;

import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * @Description: Immutable value object encapsulating index name(s) and index type(s)
 * @Author zhangkehou
 * @Date 2021/8/30
 */
public class IndexCoordinates {

    public static final String TYPE = "_doc";

    private final String[] indexNames;

    public static IndexCoordinates of(String... indexNames) {
        Assert.notNull(indexNames, "indexNames must not be null");
        return new IndexCoordinates(indexNames);
    }

    private IndexCoordinates(String[] indexNames) {
        Assert.notEmpty(indexNames, "indexNames may not be null or empty");
        this.indexNames = indexNames;
    }

    public String getIndexName() {
        return indexNames[0];
    }

    public String[] getIndexNames() {
        return Arrays.copyOf(indexNames, indexNames.length);
    }

    @Override
    public String toString() {
        return "IndexCoordinates{" + "indexNames=" + Arrays.toString(indexNames) + '}';
    }
}
