/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.datasourceconfig;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterConnectionMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> 聚合数据源Client 链接配置</h1>
 * <p> 目前提供了Mongodb 与 ElasticSearch Client,若需要切换其他数据源自行补充</p>
 */
@Configuration
@ConditionalOnBean(value = AggregationDataSourceProperties.class)
public class AggregationDataSourceClientConfiguration {

    private AggregationDataSourceProperties properties;

    public AggregationDataSourceClientConfiguration(AggregationDataSourceProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "close") // bean 销毁的同时,释放资源
    @ConditionalOnExpression("'${enable.datasource.type}'.equals('mongodb')")
    public MongoClient mongoClient() {
        MongoCredential credential = MongoCredential.createCredential(properties.getUserName(),
                properties.getDatabase(), properties.getPassword().toCharArray());

        String hosts = properties.getHost();
        hosts = hosts.contains("http://") ? hosts.replace("http://", "") : hosts;
        // 指定多个主机地址
        List<ServerAddress> serverAddressList = Arrays.stream(hosts.split(",")).map(host -> {
            int index = host.indexOf(":");
            return new ServerAddress(host.substring(0, index), Integer.parseInt(host.substring(index + 1)));
        }).collect(Collectors.toList());
        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .codecRegistry(getCodecRegistry())
                .applyToClusterSettings(builder -> {
                    builder.hosts(serverAddressList);
                    if (serverAddressList.size() > 1) {
                        builder.mode(ClusterConnectionMode.MULTIPLE);
                    } else {
                        builder.mode(ClusterConnectionMode.SINGLE);
                    }
                })
                .applyToConnectionPoolSettings(builder -> {
                    // TODO 未设置连接池其他参数,目前都是默认
                    builder.maxSize(100);
                })
                .build();
        return MongoClients.create(settings);
    }

    @Bean(destroyMethod = "close") // bean 销毁的同时,释放资源
    @ConditionalOnExpression("'${enable.datasource.type}'.equals('elasticsearch')")
    public RestHighLevelClient restHighLevelClient() {

        String hosts = properties.getHost();
        hosts = hosts.contains("http://") ? hosts.replace("http://", "") : hosts;
        // 拆分地址
        HttpHost[] httpHosts = Arrays.stream(hosts.split(",")).map(host -> {
            int index = host.indexOf(":");
            return new HttpHost(host.substring(0, index), Integer.parseInt(host.substring(index + 1)), properties.getScheme());
        }).toArray(HttpHost[]::new);
        RestClientBuilder builder = RestClient.builder(httpHosts);

        // 若账号密码存在,使用账号密码链接
        if (StringUtils.isNotBlank(properties.getPassword())) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(properties.getUserName(), properties.getPassword()));
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        // 异步连接延时配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(properties.getConnectTimeout());
            requestConfigBuilder.setSocketTimeout(properties.getSocketTimeout());
            requestConfigBuilder.setConnectionRequestTimeout(properties.getConnectionRequestTimeout());
            return requestConfigBuilder;
        });

        // 异步连接数配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(properties.getMaxConnectTotal());
            httpClientBuilder.setMaxConnPerRoute(properties.getMaxConnectPerRoute());
            return httpClientBuilder;
        });
        return new RestHighLevelClient(builder);
    }


    private CodecRegistry getCodecRegistry() {
        return CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    }
}
