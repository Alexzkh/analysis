/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

/**
 * <h1> 定义要应用于" indices_boost " 查询子句的 IndexBoost </h1>
 */
public class IndexBoost {

    private String indexName;
    private float boost;

    public IndexBoost(String indexName, float boost) {
        this.indexName = indexName;
        this.boost = boost;
    }

    public String getIndexName() {
        return indexName;
    }

    public float getBoost() {
        return boost;
    }
}
