/**
 * @author Mcj
 */
package com.zqykj.boot;

import com.zqykj.annotations.Document;
import com.zqykj.annotations.Persistent;
import com.zqykj.core.ElasticsearchRestTemplate;
import com.zqykj.core.convert.ElasticsearchConverter;
import com.zqykj.core.convert.MappingElasticsearchConverter;
import com.zqykj.core.mapping.SimpleElasticsearchMappingContext;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

abstract class ElasticsearchDataConfiguration {

    @Configuration(proxyBeanMethods = false)
    static class BaseConfiguration {

        @Bean
        public ElasticsearchConverter elasticsearchEntityMapper(
                SimpleElasticsearchMappingContext elasticsearchMappingContext) {

            MappingElasticsearchConverter elasticsearchConverter = new MappingElasticsearchConverter(
                    elasticsearchMappingContext);
            return elasticsearchConverter;
        }

        @Bean
        public SimpleElasticsearchMappingContext elasticsearchMappingContext(ApplicationContext applicationContext)
                throws ClassNotFoundException {

            SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
            mappingContext.setInitialEntitySet(new EntityScanner(applicationContext).scan(Document.class, Persistent.class));
            return mappingContext;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestHighLevelClient.class)
    static class RestClientConfiguration {

        @Bean
        ElasticsearchRestTemplate elasticsearchTemplate(RestHighLevelClient restHighLevelClient, ElasticsearchConverter elasticsearchConverter) {
            return new ElasticsearchRestTemplate(restHighLevelClient, elasticsearchConverter);
        }

    }
}
