package com.zqykj.tldw.aggregate.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @Description: Configurations
 * @Author zhangkehou
 * @Date 2021/8/6
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "elasticsearch.data.source")
@Component
public class ElasticsearchOperationClientProperties implements Serializable {

    /**
     * Host
     */
    private String host;

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
     * Get a connection timeout from the connection pool in http clilent.
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
     * Postindex suffix configuration.
     */
    private String suffix;

    /**
     * 是否自动创建索引
     */
    private Boolean autoIndexCreation = true;


    private String scheme;

    private String enableDatasourceType = "elasticsearch";
}
