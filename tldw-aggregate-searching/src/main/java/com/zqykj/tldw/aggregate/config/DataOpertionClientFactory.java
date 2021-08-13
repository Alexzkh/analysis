package com.zqykj.tldw.aggregate.config;

import com.zqykj.tldw.aggregate.properties.ElasticsearchOperationClientProperties;
import com.zqykj.tldw.aggregate.searching.BaseOperations;
import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;
//import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.ElasticsearchTemplateImpl;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.Constant;
import com.zqykj.tldw.aggregate.searching.impl.EsOperationsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: Data operate client factory .it used to generating {@link BaseOperations} the implementions.
 * <p> it was deprecated because loading implementions by configuration. you can check {@link AggregateDataConfiguration} in detail .
 * @Author zhangkehou
 * @Date 2021/8/6
 */
@Slf4j
@Deprecated
public class DataOpertionClientFactory {
    // k-->Data Source Type v--> data operate client 
//    private static final Map<BaseOperations.DatasoureType, Object> clientMap = new ConcurrentHashMap<>(4);
//
//    // k-->the interface of Es operations ,v -->the implemention
//    private static final Map<Class<?>, Class<?>> mapping = new ConcurrentHashMap<>(4);
//
//    static {
//        mapping.put(ElasticsearchTemplateOperations.class, EsOperationsTemplate.class);
//    }
//
//
//    /**
//     * @param elasticsearchOperationClientProperties: The client creates the required configuration.
//     * @return: com.zqykj.tldw.aggregate.searching.esclientrhl.repository.ElasticsearchTemplateImpl: Elasticsearhch data operation implemention.
//     * <p>
//     * Generate the {@link RestHighLevelClient} according to the configuration at first .
//     * And then building the {@link BaseOperations} implemention with {@link ElasticsearchTemplateImpl},
//     * finally return it .
//     * <p>
//     **/
//    public static ElasticsearchTemplateOperations open(ElasticsearchOperationClientProperties elasticsearchOperationClientProperties) {
//        RestHighLevelClient restHighLevelClient = createInstance(elasticsearchOperationClientProperties);
//
//        clientMap.put(BaseOperations.DatasoureType.Elasticsearch, restHighLevelClient);
//        return EsOperationsTemplate.open(restHighLevelClient);
//    }
//
//    /**
//     * Generate the {@link RestHighLevelClient} according to the configuration
//     *
//     * @param elasticsearchOperationClientProperties: The client creates the required configuration.
//     * @return: org.elasticsearch.client.RestHighLevelClient
//     **/
//    public static RestHighLevelClient createInstance(ElasticsearchOperationClientProperties elasticsearchOperationClientProperties) {
//        RestHighLevelClient restHighLevelClient;
//        String host = elasticsearchOperationClientProperties.getHost();
//        String username = elasticsearchOperationClientProperties.getUserName();
//        String password = elasticsearchOperationClientProperties.getPassword();
//        Integer maxConnectTotal = elasticsearchOperationClientProperties.getMaxConnectTotal();
//        Integer maxConnectPerRoute = elasticsearchOperationClientProperties.getMaxConnectPerRoute();
//        Integer connectionRequestTimeoutMillis = elasticsearchOperationClientProperties.getConnectionRequestTimeoutMillis();
//        Integer socketTimeoutMillis = elasticsearchOperationClientProperties.getSocketTimeoutMillis();
//        Integer connectTimeoutMillis = elasticsearchOperationClientProperties.getConnectTimeoutMillis();
//        try {
//            if (ObjectUtils.isEmpty(host)) {
//                host = Constant.DEFAULT_ES_HOST;
//            }
//            String[] hosts = host.split(",");
//            HttpHost[] httpHosts = new HttpHost[hosts.length];
//            for (int i = 0; i < httpHosts.length; i++) {
//                String h = hosts[i];
//                httpHosts[i] = new HttpHost(h.split(":")[0], Integer.parseInt(h.split(":")[1]), "http");
//            }
//
//            RestClientBuilder builder = RestClient.builder(httpHosts);
//            builder.setRequestConfigCallback(requestConfigBuilder -> {
//                requestConfigBuilder.setConnectTimeout(connectTimeoutMillis);
//                requestConfigBuilder.setSocketTimeout(socketTimeoutMillis);
//                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeoutMillis);
//                return requestConfigBuilder;
//            });
//
//            if (!ObjectUtils.isEmpty(username)) {
//                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//                credentialsProvider.setCredentials(AuthScope.ANY,
//                        new UsernamePasswordCredentials(username, password));  //es账号密码（默认用户名为elastic）
//
//                builder.setHttpClientConfigCallback(httpClientBuilder -> {
//                    httpClientBuilder.disableAuthCaching();
//                    httpClientBuilder.setMaxConnTotal(maxConnectTotal);
//                    httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
//                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
//                    return httpClientBuilder;
//                });
//            } else {
//                builder.setHttpClientConfigCallback(httpClientBuilder -> {
//                    httpClientBuilder.disableAuthCaching();
//                    httpClientBuilder.setMaxConnTotal(maxConnectTotal);
//                    httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
//                    return httpClientBuilder;
//                });
//            }
//
//            restHighLevelClient = new RestHighLevelClient(builder);
//        } catch (Exception e) {
//            log.error("create RestHighLevelClient error{}", e);
//            return null;
//        }
//        return restHighLevelClient;
//    }
}


