/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/**
 * <h1> Repository bean definition build </h1>
 */
public class RepositoryBeanDefinitionBuilder {

    private final BeanDefinitionRegistry registry;
    private final ResourceLoader resourceLoader;


    /**
     * Creates a new {@link RepositoryBeanDefinitionBuilder} from the given {@link BeanDefinitionRegistry},
     * {@link RepositoryConfigurationExtension} and {@link ResourceLoader}.
     *
     * @param registry       must not be {@literal null}.
     * @param resourceLoader must not be {@literal null}.
     * @param environment    must not be {@literal null}.
     */
    public RepositoryBeanDefinitionBuilder(BeanDefinitionRegistry registry, ResourceLoader resourceLoader, Environment environment) {

        Assert.notNull(resourceLoader, "ResourceLoader must not be null!");
        Assert.notNull(environment, "Environment must not be null!");

        this.registry = registry;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Builds a new {@link BeanDefinitionBuilder} from the given {@link BeanDefinitionRegistry} and {@link ResourceLoader}
     *
     * @param configuration must not be {@literal null}.
     */
    public BeanDefinitionBuilder build(RepositoryConfiguration<?> configuration) {

        Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");
        Assert.notNull(resourceLoader, "ResourceLoader must not be null!");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .rootBeanDefinition(configuration.getRepositoryFactoryBeanClassName());

        builder.getRawBeanDefinition().setSource(configuration.getSource());
        builder.addConstructorArgValue(configuration.getRepositoryInterface());
        builder.addPropertyValue("queryLookupStrategyKey", configuration.getQueryLookupStrategyKey());
        builder.addPropertyValue("lazyInit", configuration.isLazyInit());
        builder.setLazyInit(configuration.isLazyInit());
        builder.setPrimary(configuration.isPrimary());

        configuration.getRepositoryBaseClassName()//
                .ifPresent(it -> builder.addPropertyValue("repositoryBaseClass", it));
        return builder;
    }
}
