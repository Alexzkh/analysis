/**
 * @author Mcj
 */
package com.zqykj.boot;

import com.zqykj.client.ElasticsearchRestClientProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestHighLevelClient.class)
@ConditionalOnMissingBean(RestClient.class)
@EnableConfigurationProperties(value = ElasticsearchRestClientProperties.class)
public class ElasticsearchRestClientAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RestClientBuilder.class)
    static class RestClientBuilderConfiguration {

        @Bean
        RestClientBuilder restClientBuilder(ElasticsearchRestClientProperties properties) {

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
                requestConfigBuilder.setConnectTimeout(properties.getConnectTimeoutMillis());
                requestConfigBuilder.setSocketTimeout(properties.getSocketTimeoutMillis());
                requestConfigBuilder.setConnectionRequestTimeout(properties.getConnectionRequestTimeoutMillis());
                return requestConfigBuilder;
            });

            // 异步连接数配置
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setMaxConnTotal(properties.getMaxConnectTotal());
                httpClientBuilder.setMaxConnPerRoute(properties.getMaxConnectPerRoute());
                return httpClientBuilder;
            });
            return builder;
        }
    }


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RestHighLevelClient.class)
    static class RestHighLevelClientConfiguration {

        @Bean
        RestHighLevelClient restHighLevelClient(RestClientBuilder restClientBuilder) {
            return new RestHighLevelClient(restClientBuilder);
        }

    }
}
