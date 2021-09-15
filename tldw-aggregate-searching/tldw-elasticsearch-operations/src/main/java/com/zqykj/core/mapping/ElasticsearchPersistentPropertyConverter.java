/**
 * @作者 Mcj
 */
package com.zqykj.core.mapping;

/**
 * <h2> Elasticsearch property converter </h2>
 */
public interface ElasticsearchPersistentPropertyConverter {


    /**
     * <h2> converts the property value to a String. </h2>
     */
    String write(Object property);

    /**
     * <h2> converts a property value from a String. </h2>
     */
    Object read(String s);
}
