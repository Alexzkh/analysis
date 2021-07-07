package com.zqykj.tldw.aggregate.searching.esclientrhl.auto.autoindex;

import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * <p>
 * Scan the annotated bean of {@link com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData}
 * (that is, the entity class of Elasticsearch data structure) for spring management
 * </p>
 **/
public class ESEntityScanner extends ClassPathBeanDefinitionScanner {
    public ESEntityScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public void registerDefaultFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(ESMetaData.class));
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        return beanDefinitions;
    }

    @Override
    public boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return super.isCandidateComponent(beanDefinition) && beanDefinition.getMetadata()
                .hasAnnotation(ESMetaData.class.getName());
    }
}
