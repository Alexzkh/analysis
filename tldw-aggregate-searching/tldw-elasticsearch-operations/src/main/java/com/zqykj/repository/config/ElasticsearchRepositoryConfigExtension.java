/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import com.zqykj.annotations.Document;
import com.zqykj.repository.ElasticsearchRepository;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ElasticsearchRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

    @Override
    public String getRepositoryFactoryBeanClassName() {
        return ElasticsearchRepositoryFactoryBean.class.getName();
    }

    @Override
    protected String getModulePrefix() {
        return "elasticsearch";
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {

        AnnotationAttributes attributes = config.getAttributes();
        builder.addPropertyReference("elasticsearchTemplate", attributes.getString("elasticsearchTemplateRef"));
    }

    @Override
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Collections.singleton(Document.class);
    }

    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Arrays.asList(ElasticsearchRepository.class, EntranceRepository.class);
    }

}
