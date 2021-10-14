/**
 * @作者 Mcj
 */
package com.zqykj.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: Configurations
 * @Author zhangkehou
 * @Date 2021/8/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private Integer maxConnectTotal = 100;

    /**
     * The number of requests that a service can receive in parallel each time.
     */
    private Integer maxConnectPerRoute = 100;

    /**
     * Get a connection timeout from the connection pool in http client.
     */
    private Integer connectionRequestTimeoutMillis = 5000;

    /**
     * Response timeout, after which the response will not be read.
     */
    private Integer socketTimeoutMillis = 5000;

    /**
     * Timeout for connection establishment.
     */
    private Integer connectTimeoutMillis = 5000;

    /**
     * 是否自动创建索引与mapping
     */
    private boolean autoIndexCreation = true;

    /**
     * 协议 eg. http
     */
    private String scheme = "http";

}
