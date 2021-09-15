/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import com.zqykj.config.ConfigurationUtils;
import com.zqykj.repository.query.QueryLookupStrategy;
import com.zqykj.util.Streamable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * <h1> DefaultRepositoryConfiguration </h1>
 */
public class DefaultRepositoryConfiguration<T extends RepositoryConfigurationSource>
        implements RepositoryConfiguration<T> {

    public static final String DEFAULT_REPOSITORY_IMPLEMENTATION_POSTFIX = "Impl";
    public static final QueryLookupStrategy.Key DEFAULT_QUERY_LOOKUP_STRATEGY = QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

    private final T configurationSource;
    private final BeanDefinition definition;
    private final RepositoryConfigurationExtension extension;

    public DefaultRepositoryConfiguration(T configurationSource, BeanDefinition definition,
                                          RepositoryConfigurationExtension extension) {

        this.configurationSource = configurationSource;
        this.definition = definition;
        this.extension = extension;
    }

    public String getBeanId() {
        return StringUtils.uncapitalize(ClassUtils.getShortName(getRepositoryBaseClassName().orElseThrow(
                () -> new IllegalStateException("Can't create bean identifier without a repository base class defined!"))));
    }

    public Object getQueryLookupStrategyKey() {
        return configurationSource.getQueryLookupStrategyKey().orElse(DEFAULT_QUERY_LOOKUP_STRATEGY);
    }

    public Streamable<String> getBasePackages() {
        return configurationSource.getBasePackages();
    }


    public String getRepositoryInterface() {
        return ConfigurationUtils.getRequiredBeanClassName(definition);
    }

    public RepositoryConfigurationSource getConfigSource() {
        return configurationSource;
    }

    public Optional<String> getNamedQueriesLocation() {
        return configurationSource.getNamedQueryLocation();
    }

    @Nullable
    @Override
    public Object getSource() {
        return configurationSource.getSource();
    }

    @Override
    public T getConfigurationSource() {
        return configurationSource;
    }

    @Override
    public Optional<String> getRepositoryBaseClassName() {
        return configurationSource.getRepositoryBaseClassName();
    }

    @Override
    public String getRepositoryFactoryBeanClassName() {

        return configurationSource.getRepositoryFactoryBeanClassName()
                .orElseGet(extension::getRepositoryFactoryBeanClassName);
    }

    @Override
    public boolean isLazyInit() {
        return definition.isLazyInit() || !configurationSource.getBootstrapMode().equals(BootstrapMode.DEFAULT);
    }

    @Override
    public boolean isPrimary() {
        return definition.isPrimary();
    }

    @Override
    public Streamable<TypeFilter> getExcludeFilters() {
        return configurationSource.getExcludeFilters();
    }

}
