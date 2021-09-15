/**
 * @author Mcj
 */
package com.autoconfigure.data;

import com.zqykj.boot.AbstractRepositoryConfigurationSourceSupport;
import com.zqykj.repository.config.ElasticsearchRepositoryConfigExtension;
import com.zqykj.repository.config.EnableElasticsearchRepositories;
import com.zqykj.repository.config.RepositoryConfigurationExtension;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;

import java.lang.annotation.Annotation;

/**
 * <h1> {@link ImportBeanDefinitionRegistrar} used to auto-configure Spring Data Elasticsearch
 * Repositories. </h1>
 */
public class ElasticsearchRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableElasticsearchRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableElasticsearchRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new ElasticsearchRepositoryConfigExtension();
    }

    @EnableElasticsearchRepositories
    private static class EnableElasticsearchRepositoriesConfiguration {

    }
}
