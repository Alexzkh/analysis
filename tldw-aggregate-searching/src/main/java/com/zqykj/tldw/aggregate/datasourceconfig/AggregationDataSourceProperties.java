/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.datasourceconfig;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aggregation.data.source")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class AggregationDataSourceProperties {

    /** 数据源链接地址 host, 如果有多个用,隔开 */
    private String host = "127.0.0.1:9200";

    /** 用户名 */
    private String userName;

    /** 密码 */
    private String password;

    /** 数据库名称 */
    private String database;

    /** 是否自动创建索引 */
    private Boolean autoIndexCreation = true;

    /** client 协议 */
    private String scheme = "http";

    /** 连接超时时间 */
    private int connectTimeout = 5000;

    /** Socket 连接超时时间 */
    private int socketTimeout = 5000;

    /** 获取连接的超时时间 */
    private int connectionRequestTimeout = 5000;

    /** 最大连接数 */
    private int maxConnectTotal = 100;

    /** 最大路由连接数 */
    private int maxConnectPerRoute = 100;
}
