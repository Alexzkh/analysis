/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterConnectionMode;
import com.zqykj.tldw.aggregate.properties.ElasticsearchOperationClientProperties;
import com.zqykj.tldw.aggregate.properties.MongoDBOperationClientProperties;
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
public class AggregateDataSourceClientConfiguration {

    private ElasticsearchOperationClientProperties elasticsearchOperationClientProperties;

    private MongoDBOperationClientProperties mongoDBOperationClientProperties;

    public AggregateDataSourceClientConfiguration(ElasticsearchOperationClientProperties properties,
                                                  MongoDBOperationClientProperties mongoDBOperationClientProperties) {
        this.elasticsearchOperationClientProperties = properties;
        this.mongoDBOperationClientProperties = mongoDBOperationClientProperties;
    }

    @Bean(destroyMethod = "close") // bean 销毁的同时,释放资源
    @ConditionalOnBean(value = MongoDBOperationClientProperties.class)
    @ConditionalOnExpression("'${enable.datasource.type}'.equals('mongodb')")
    public MongoClient mongoClient() {
        MongoCredential credential = MongoCredential.createCredential(mongoDBOperationClientProperties.getUserName(),
                mongoDBOperationClientProperties.getDatabase(), mongoDBOperationClientProperties.getPassword().toCharArray());

        String hosts = mongoDBOperationClientProperties.getHost();
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
    @ConditionalOnBean(value = ElasticsearchOperationClientProperties.class)
    @ConditionalOnExpression("'${enable.datasource.type}'.equals('elasticsearch')")
    public RestHighLevelClient restHighLevelClient() {

        String hosts = elasticsearchOperationClientProperties.getHost();
        hosts = hosts.contains("http://") ? hosts.replace("http://", "") : hosts;
        // 拆分地址
        HttpHost[] httpHosts = Arrays.stream(hosts.split(",")).map(host -> {
            int index = host.indexOf(":");
            return new HttpHost(host.substring(0, index), Integer.parseInt(host.substring(index + 1)), elasticsearchOperationClientProperties.getScheme());
        }).toArray(HttpHost[]::new);
        RestClientBuilder builder = RestClient.builder(httpHosts);

        // 若账号密码存在,使用账号密码链接
        if (StringUtils.isNotBlank(elasticsearchOperationClientProperties.getPassword())) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticsearchOperationClientProperties.getUserName(), elasticsearchOperationClientProperties.getPassword()));
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        // 异步连接延时配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(elasticsearchOperationClientProperties.getConnectTimeoutMillis());
            requestConfigBuilder.setSocketTimeout(elasticsearchOperationClientProperties.getSocketTimeoutMillis());
            requestConfigBuilder.setConnectionRequestTimeout(elasticsearchOperationClientProperties.getConnectionRequestTimeoutMillis());
            return requestConfigBuilder;
        });

        // 异步连接数配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(elasticsearchOperationClientProperties.getMaxConnectTotal());
            httpClientBuilder.setMaxConnPerRoute(elasticsearchOperationClientProperties.getMaxConnectPerRoute());
            return httpClientBuilder;
        });
        return new RestHighLevelClient(builder);
    }


    private CodecRegistry getCodecRegistry() {
        return CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    }
}
