/**
 * @author Mcj
 */
package com.autoconfigure.data;


import com.zqykj.repository.ElasticsearchRepository;
import com.zqykj.repository.support.ElasticsearchRepositoryFactoryBean;
import org.elasticsearch.client.Client;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <h1> Elasticsearch Repository Auto configuration </h1>
 * <p>
 * {@link EnableAutoConfiguration Auto-configuration} for Elasticsearch
 * * Repositories.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Client.class, ElasticsearchRepository.class})
@ConditionalOnMissingBean(ElasticsearchRepositoryFactoryBean.class)
@Import(ElasticsearchRepositoriesRegistrar.class)
public class ElasticsearchRepositoriesAutoConfiguration {

}
