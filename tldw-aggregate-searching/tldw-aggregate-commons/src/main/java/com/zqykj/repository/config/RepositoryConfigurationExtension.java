/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.ResourceLoader;

import java.util.Collection;

/**
 * <h2> 对Repository BeanDefinitionBuilder 进行后置处理 </h2>
 */
public interface RepositoryConfigurationExtension {

    /**
     * <h2> 返回模块的名称 </h2>
     */
    String getModuleName();

    /**
     * Returns all {@link RepositoryConfiguration}s obtained through the given {@link RepositoryConfigurationSource}.
     *
     * @param configSource
     * @param loader
     * @param strictMatchesOnly whether to return strict repository matches only. Handing in {@literal true} will cause
     *                          the repository interfaces and domain types handled to be checked whether they are managed by the current
     *                          store.
     */
    <T extends RepositoryConfigurationSource> Collection<RepositoryConfiguration<T>> getRepositoryConfigurations(
            T configSource, ResourceLoader loader, boolean strictMatchesOnly);

    /**
     * Returns the name of the repository factory class to be used.
     */
    String getRepositoryFactoryBeanClassName();

    /**
     * Callback to register additional bean definitions for a {@literal repositories} root node. This usually includes
     * beans you have to set up once independently of the number of repositories to be created. Will be called before any
     * repositories bean definitions have been registered.
     */
    void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource configurationSource);

    /**
     * Callback to post process the {@link BeanDefinition} and tweak the configuration if necessary.
     *
     * @param builder will never be {@literal null}.
     * @param config  will never be {@literal null}.
     */
    void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource config);

    /**
     * Callback to post process the {@link BeanDefinition} built from annotations and tweak the configuration if
     * necessary.
     *
     * @param builder will never be {@literal null}.
     * @param config  will never be {@literal null}.
     */
    void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config);
}
