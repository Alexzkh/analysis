/**
 * @author Mcj
 */
package com.zqykj.boot;

import com.zqykj.repository.config.AnnotationRepositoryConfigurationSource;
import com.zqykj.repository.config.BootstrapMode;
import com.zqykj.repository.config.RepositoryConfigurationDelegate;
import com.zqykj.repository.config.RepositoryConfigurationExtension;
import com.zqykj.util.Streamable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

@Slf4j
public abstract class AbstractRepositoryConfigurationSourceSupport implements ImportBeanDefinitionRegistrar,
        BeanFactoryAware, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;

    private BeanFactory beanFactory;

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry,
                                        BeanNameGenerator importBeanNameGenerator) {
        RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(
                getConfigurationSource(registry, importBeanNameGenerator), this.resourceLoader, this.environment);
        delegate.registerRepositoriesIn(registry, getRepositoryConfigurationExtension());
    }

    /**
     * @param importingClassMetadata 当前类的注解元信息
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinitions(importingClassMetadata, registry, null);
    }

    private AnnotationRepositoryConfigurationSource getConfigurationSource(BeanDefinitionRegistry registry,
                                                                           BeanNameGenerator importBeanNameGenerator) {
        AnnotationMetadata metadata = AnnotationMetadata.introspect(getConfiguration());
        return new AutoConfiguredAnnotationRepositoryConfigurationSource(metadata, getAnnotation(), this.resourceLoader,
                this.environment, registry, importBeanNameGenerator) {
        };
    }

    protected Streamable<String> getBasePackages() {
        return Streamable.of(AutoConfigurationPackages.get(this.beanFactory));
    }

    /**
     * The Spring Data annotation used to enable the particular repository support.
     *
     * @return the annotation class
     */
    protected abstract Class<? extends Annotation> getAnnotation();

    /**
     * The configuration class that will be used by Spring Boot as a template.
     *
     * @return the configuration class
     */
    protected abstract Class<?> getConfiguration();

    /**
     * The {@link RepositoryConfigurationExtension} for the particular repository support.
     *
     * @return the repository configuration extension
     */
    protected abstract RepositoryConfigurationExtension getRepositoryConfigurationExtension();

    /**
     * The {@link BootstrapMode} for the particular repository support. Defaults to
     * {@link BootstrapMode#DEFAULT}.
     *
     * @return the bootstrap mode
     */
    protected BootstrapMode getBootstrapMode() {
        return BootstrapMode.DEFAULT;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * <h2> An auto-configured {@link AnnotationRepositoryConfigurationSource}. </h2>
     */
    private class AutoConfiguredAnnotationRepositoryConfigurationSource
            extends AnnotationRepositoryConfigurationSource {

        AutoConfiguredAnnotationRepositoryConfigurationSource(AnnotationMetadata metadata,
                                                              Class<? extends Annotation> annotation, ResourceLoader resourceLoader, Environment environment,
                                                              BeanDefinitionRegistry registry, BeanNameGenerator generator) {
            super(metadata, annotation, resourceLoader, environment, registry, generator);
        }

        @Override
        public Streamable<String> getBasePackages() {
            return AbstractRepositoryConfigurationSourceSupport.this.getBasePackages();
        }

        @Override
        public BootstrapMode getBootstrapMode() {
            return AbstractRepositoryConfigurationSourceSupport.this.getBootstrapMode();
        }

    }
}
