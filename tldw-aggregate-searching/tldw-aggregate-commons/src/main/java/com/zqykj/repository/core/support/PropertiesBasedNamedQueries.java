/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import com.zqykj.repository.core.NamedQueries;
import org.springframework.util.Assert;

import java.util.Properties;

/**
 * <h1> 基于属性的命名查询 </h1>
 */
public class PropertiesBasedNamedQueries implements NamedQueries {

    private static final String NO_QUERY_FOUND = "No query with name %s found! Make sure you call hasQuery(…) before calling this method!";

    public static final NamedQueries EMPTY = new PropertiesBasedNamedQueries(new Properties());

    private final Properties properties;

    public PropertiesBasedNamedQueries(Properties properties) {
        this.properties = properties;
    }

    public boolean hasQuery(String queryName) {

        Assert.hasText(queryName, "Query name must not be null or empty!");

        return properties.containsKey(queryName);
    }

    public String getQuery(String queryName) {

        Assert.hasText(queryName, "Query name must not be null or empty!");

        String query = properties.getProperty(queryName);

        if (query == null) {
            throw new IllegalArgumentException(String.format(NO_QUERY_FOUND, queryName));
        }

        return query;
    }
}
