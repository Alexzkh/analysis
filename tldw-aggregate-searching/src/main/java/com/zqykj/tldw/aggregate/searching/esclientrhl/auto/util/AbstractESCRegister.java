package com.zqykj.tldw.aggregate.searching.esclientrhl.auto.util;

import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.intfproxy.ESCRepository;
import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.intfproxy.RepositoryFactorySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;


import java.io.IOException;
import java.util.stream.Stream;

/**
 * Abstract implementation for annotation {@link com.zqykj.tldw.aggregate.searching.esclientrhl.auto.ESCRegistrar}
 **/
public abstract class AbstractESCRegister {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void registerBeanDefinitions(BeanFactory factory, Environment environment, ResourceLoader resourceLoader, AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        getCandidates(annotationMetadata, registry, environment, resourceLoader).forEach(beanDefinition -> {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RepositoryFactorySupport.class);
            String beanClassName = beanDefinition.getBeanClassName();
            // Pass in the interface to be instantiated.
            beanDefinitionBuilder.addConstructorArgValue(beanClassName);
            // Get instance of bean.
            BeanDefinition bd = beanDefinitionBuilder.getRawBeanDefinition();
            // Generate bean name .
            String beanName = beanClassName.substring(beanClassName.lastIndexOf(".") + 1);
            if (EnableESTools.isPrintregmsg()) {
                logger.info("generate ESCRegistrar beanClassName:" + beanClassName);
                logger.info("generate ESCRegistrar beanName:" + beanName);
            }
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) factory;
            // Register bean that beanName is proxy bean name .
            beanDefinitionRegistry.registerBeanDefinition(beanName, bd);
        });
    }

    /**
     * Scan the type of the escrepository interface and return as a candidate
     *
     * @param registry
     * @return
     */
    public Stream<BeanDefinition> getCandidates(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry, Environment environment, ResourceLoader resourceLoader) {
        ESCRepositoryComponentProvider scanner = new ESCRepositoryComponentProvider(registry);
        scanner.setEnvironment(environment);
        scanner.setResourceLoader(resourceLoader);
        // The input is basepackages and the output is stream of beandefinition.
        return getBasePackage(annotationMetadata).flatMap(it -> scanner.findCandidateComponents(it).stream());
    }

    /**
     * It must be implemented by subclass. Autoconfig is different.
     *
     * @param annotationMetadata
     * @return
     */
    public abstract Stream<String> getBasePackage(AnnotationMetadata annotationMetadata);

    /**
     * scanner interface ESCRepository
     */
    private static class ESCRepositoryComponentProvider extends ClassPathScanningCandidateComponentProvider {
        private BeanDefinitionRegistry registry;

        public ESCRepositoryComponentProvider(BeanDefinitionRegistry registry) {
            super(false);
            Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");
            super.addIncludeFilter(new InterfaceTypeFilter(ESCRepository.class));
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            boolean isNonRepositoryInterface = !isGenericRepositoryInterface(beanDefinition.getBeanClassName());
            boolean isTopLevelType = !beanDefinition.getMetadata().hasEnclosingClass();
            boolean isConsiderNestedRepositories = false;
            return isNonRepositoryInterface && (isTopLevelType || isConsiderNestedRepositories);
        }

        private static boolean isGenericRepositoryInterface(@Nullable String interfaceName) {
            return ESCRepository.class.getName().equals(interfaceName);
        }
    }

    private static class InterfaceTypeFilter extends AssignableTypeFilter {
        public InterfaceTypeFilter(Class<?> targetType) {
            super(targetType);
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                throws IOException {
            return metadataReader.getClassMetadata().isInterface() && super.match(metadataReader, metadataReaderFactory);
        }
    }
}
