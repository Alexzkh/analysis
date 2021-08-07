package com.zqykj.app.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch properties .
 **/
@Component
public class ElasticsearchProperties {
    @Value("${elasticsearch.host:127.0.0.1:9200}")
    private String host;
    @Value("${elasticsearch.username:}")
    private String username;
    @Value("${elasticsearch.password:}")
    private String password;

    /**
     * The maximum number of connections in the connection pool.
     */
    @Value("${elasticsearch.max_connect_total:30}")
    private Integer maxConnectTotal;

    /**
     * The number of requests that a service can receive in parallel each time.
     */
    @Value("${elasticsearch.max_connect_per_route:10}")
    private Integer maxConnectPerRoute;

    /**
     * Get a connection timeout from the connection pool in http clilent.
     */
    @Value("${elasticsearch.connection_request_timeout_millis:2000}")
    private Integer connectionRequestTimeoutMillis;

    /**
     * Response timeout, after which the response will not be read.
     */
    @Value("${elasticsearch.socket_timeout_millis:30000}")
    private Integer socketTimeoutMillis;

    /**
     * Timeout for connection establishment.
     */
    @Value("${elasticsearch.connect_timeout_millis:2000}")
    private Integer connectTimeoutMillis;

    /**
     * Postindex suffix configuration.
     */
    @Value("${elasticsearch.index.suffix:}")
    private String suffix;


    public Integer getMaxConnectTotal() {
        return maxConnectTotal;
    }

    public void setMaxConnectTotal(Integer maxConnectTotal) {
        this.maxConnectTotal = maxConnectTotal;
    }

    public Integer getMaxConnectPerRoute() {
        return maxConnectPerRoute;
    }

    public void setMaxConnectPerRoute(Integer maxConnectPerRoute) {
        this.maxConnectPerRoute = maxConnectPerRoute;
    }

    public Integer getConnectionRequestTimeoutMillis() {
        return connectionRequestTimeoutMillis;
    }

    public void setConnectionRequestTimeoutMillis(Integer connectionRequestTimeoutMillis) {
        this.connectionRequestTimeoutMillis = connectionRequestTimeoutMillis;
    }

    public Integer getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(Integer socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    public Integer getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(Integer connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
