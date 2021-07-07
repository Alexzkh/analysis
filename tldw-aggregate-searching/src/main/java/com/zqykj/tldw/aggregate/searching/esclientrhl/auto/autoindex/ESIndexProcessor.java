package com.zqykj.tldw.aggregate.searching.esclientrhl.auto.autoindex;

import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.EnableESTools;
import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.util.GetBasePackage;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.AnnotationMetadata;


/**
 * <p>
 *After the initialization of spring, read the path of entity on the annotation of the startup class
 * {@link com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.EnableESTools}  (or do not configure, take the package of the startup class),
 * get the path, and then entrust the {@link com.zqykj.tldw.aggregate.searching.esclientrhl.auto.autoindex.ESEntityScanner} to scan the relevant path.
 * finally call {@link com.zqykj.tldw.aggregate.searching.esclientrhl.auto.ESCRegistrar}
 * </p>
 **/
public class ESIndexProcessor {


    /**
     *The class entity path or root path entity of scanning {@link com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData}annotation is managed to spring.
     *@param beanFactory
     *@throws BeansException
     */
    public void scan(AnnotationMetadata annotationMetadata, BeanFactory beanFactory, ApplicationContext applicationContext){
        GetBasePackage getBasePackage = new GetBasePackage(EnableESTools.class);
        ESEntityScanner scanner = new ESEntityScanner((BeanDefinitionRegistry) beanFactory);
        scanner.setResourceLoader(applicationContext);
        scanner.scan(getBasePackage.getEntityPackage(annotationMetadata).toArray(String[]::new));
    }
}
