/**
 * @author Mcj
 */
package com.autoconfigure.data;

import com.zqykj.annotations.Document;
import com.zqykj.annotations.Persistent;
import com.zqykj.core.ElasticsearchRestTemplate;
import com.zqykj.core.convert.ElasticsearchConverter;
import com.zqykj.core.convert.MappingElasticsearchConverter;
import com.zqykj.core.mapping.SimpleElasticsearchMappingContext;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

abstract class ElasticsearchDataConfiguration {

    @Configuration(proxyBeanMethods = false)
    static class BaseConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ElasticsearchConverter elasticsearchEntityMapper(
                SimpleElasticsearchMappingContext elasticsearchMappingContext) {

            return new MappingElasticsearchConverter(elasticsearchMappingContext);
        }

        /**
         *  <h2> 默认扫描的启动类所在的包以及子包, 如果需要自定义扫描路径, 请使用注解 {@link EntityScan} 去指定包名 </h2>
         * */
        @Bean
        @ConditionalOnMissingBean
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
        @ConditionalOnMissingBean(value = ElasticsearchRestTemplate.class, name = "elasticsearchTemplate")
        @ConditionalOnBean(RestHighLevelClient.class)
        ElasticsearchRestTemplate elasticsearchTemplate(RestHighLevelClient restHighLevelClient, ElasticsearchConverter elasticsearchConverter) {
            return new ElasticsearchRestTemplate(restHighLevelClient, elasticsearchConverter);
        }

    }
}
