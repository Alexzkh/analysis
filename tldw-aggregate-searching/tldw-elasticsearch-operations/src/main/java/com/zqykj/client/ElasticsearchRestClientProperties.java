/**
 * @作者 Mcj
 */
package com.zqykj.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: Configurations
 * @Author zhangkehou
 * @Date 2021/8/6
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "elasticsearch.data.source")
@ConditionalOnProperty(name = "enable.datasource.type", havingValue = "elasticsearch")
public class ElasticsearchRestClientProperties {

    /**
     * Host
     */
    private String host = "127.0.0.1:9200";

    /**
     * UserName.
     */
    private String userName;

    /**
     * Password
     */
    private String password;

    /**
     * The maximum number of connections in the connection pool.
     */
    private int maxConnectTotal = 1000;

    /**
     * The number of requests that a service can receive in parallel each time.
     */
    private int maxConnectPerRoute = 330;

    /**
     * Get a connection timeout from the connection pool in http client.
     */
    private int connectionRequestTimeoutMillis = 8000;

    /**
     * Response timeout, after which the response will not be read.
     */
    private int socketTimeoutMillis = 60000;

    /**
     * Timeout for connection establishment.
     */
    private int connectTimeoutMillis = 8000;

    /**
     * 是否自动创建索引与mapping
     */
    private boolean autoIndexCreation = true;

    /**
     * 协议 eg. http
     */
    private String scheme = "http";

}
