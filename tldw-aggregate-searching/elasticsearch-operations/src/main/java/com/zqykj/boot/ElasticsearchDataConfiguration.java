/**
 * @author Mcj
 */
package com.zqykj.boot;

import com.zqykj.core.ElasticsearchRestTemplate;
import com.zqykj.core.convert.ElasticsearchConverter;
import com.zqykj.core.convert.MappingElasticsearchConverter;
import com.zqykj.core.mapping.SimpleElasticsearchMappingContext;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

abstract class ElasticsearchDataConfiguration {

    @Configuration(proxyBeanMethods = false)
    static class BaseConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext mappingContext) {
            return new MappingElasticsearchConverter(mappingContext);
        }

        @Bean
        @ConditionalOnMissingBean
        SimpleElasticsearchMappingContext mappingContext() {
            return new SimpleElasticsearchMappingContext();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestHighLevelClient.class)
    static class RestClientConfiguration {

        @Bean
        @ConditionalOnMissingBean(value = ElasticsearchRestTemplate.class, name = "elasticsearchTemplate")
        @ConditionalOnBean(RestHighLevelClient.class)
        ElasticsearchRestTemplate elasticsearchTemplate(RestHighLevelClient restHighLevelClient, ElasticsearchConverter elasticsearchConverter) {
            return new ElasticsearchRestTemplate(restHighLevelClient, elasticsearchConverter);
        }

    }
}
