/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.properties;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "mongodb.data.source")
@Component
public class MongoDBOperationClientProperties implements Serializable {

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 是否自动创建索引
     */
    private Boolean autoIndexCreation = true;

    /**
     * client 协议
     */
    private String scheme = "http";

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
    private Integer maxConnectTotal;

    /**
     * The number of requests that a service can receive in parallel each time.
     */
    private Integer maxConnectPerRoute;

    /**
     * Get a connection timeout from the connection pool in http clilent.
     */
    private Integer connectionRequestTimeoutMillis;

    /**
     * Response timeout, after which the response will not be read.
     */
    private Integer socketTimeoutMillis;

    /**
     * Timeout for connection establishment.
     */
    private Integer connectTimeoutMillis;

    private String enableDatasourceType = "mongodb";
}
