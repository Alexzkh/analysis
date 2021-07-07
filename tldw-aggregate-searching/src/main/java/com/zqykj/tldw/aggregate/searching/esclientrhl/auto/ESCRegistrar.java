package com.zqykj.tldw.aggregate.searching.esclientrhl.auto;


import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.EnableESTools;
import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.autoindex.ESIndexProcessor;
import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.util.AbstractESCRegister;
import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.util.GetBasePackage;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 *<p>
 *Handle interface preparation in scope as spring bean (assisted by beanfactory);
 *Scan and host the entity class to spring management.
 * </p>
 **/
@Configuration
public class ESCRegistrar extends AbstractESCRegister implements BeanFactoryAware, ApplicationContextAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private @SuppressWarnings("null")
    @Nonnull
    ResourceLoader resourceLoader;
    private @SuppressWarnings("null")
    @Nonnull
    Environment environment;
    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    /**
     * template method
     * @param annotationMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        // scan entity
        new ESIndexProcessor().scan(annotationMetadata, beanFactory, applicationContext);
        // scan interface
        super.registerBeanDefinitions(beanFactory, environment, resourceLoader, annotationMetadata, registry);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Stream<String> getBasePackage(AnnotationMetadata annotationMetadata) {
        GetBasePackage getBasePackage = new GetBasePackage(EnableESTools.class);
        return getBasePackage.getBasePackage(annotationMetadata);
    }

}
