/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data;

import com.zqykj.annotations.NoRepositoryBean;
import com.zqykj.tldw.aggregate.BaseOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <h1>扫描当前启动类所在的包以及子包下的指定接口 {@link BaseOperations} 子接口 </h1>
 * <p>
 * 标记 {@link NoRepositoryBean} 此注解的interface 不会生成beanDefinition
 */
@Slf4j
public class AggregateRepositoriesRegister implements ImportBeanDefinitionRegistrar,
        BeanFactoryAware, ResourceLoaderAware, EnvironmentAware {


    static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";

    private ResourceLoader resourceLoader;

    private BeanFactory beanFactory;

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry,
                                        BeanNameGenerator beanNameGenerator) {

        // 获取指定接口的BeanDefinition
        StopWatch watch = new StopWatch();
        watch.start();
        List<BeanComponentDefinition> definitions = new ArrayList<>();
        for (BeanDefinition candidate : getCandidates(registry)) {
            Objects.requireNonNull(candidate.getBeanClassName());
            String beanClassName = candidate.getBeanClassName();
            try {
                // Class<?> repositoryInterface = ClassUtils.forName(beanClassName, ClassUtils.getDefaultClassLoader());
                // 注册Bean定义信息
                BeanDefinitionBuilder definitionBuilder = generateBeanDefinitionBuilder(candidate);
                AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
                beanDefinition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClassName);
                // 向容器注册beanDefinition
                String beanName = this.generateBeanName(beanDefinition, beanNameGenerator, registry);
                registry.registerBeanDefinition(beanName, beanDefinition);
                // 如果 beanDefinition 有propertyReference bean 依赖,必须用  BeanComponentDefinition 包装
                // 否则依赖的bean 无法注入
                definitions.add(new BeanComponentDefinition(beanDefinition, beanName));
            } catch (Exception | LinkageError e) {
                log.warn(String.format(" Could not load type %s using class loader %s.",
                        beanClassName, ClassUtils.getDefaultClassLoader()), e);
            }
        }
        watch.stop();
        log.info("register aggregate repositories bean definitions cost time = {} ms ", watch.getLastTaskTimeMillis());
    }

    /**
     * @param importingClassMetadata 当前类的注解元信息
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinitions(importingClassMetadata, registry, null);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {

        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {

        this.resourceLoader = resourceLoader;
    }

    /**
     * <h2> 获取当前启动类下的所在包名称 </h2>
     */
    private Iterator<String> getPackages() {
        return AutoConfigurationPackages.get(beanFactory).iterator();
    }

    static class RepositoryComponentProvider extends ClassPathScanningCandidateComponentProvider {

        private boolean considerNestedRepositoryInterfaces;

        public RepositoryComponentProvider(Iterable<? extends TypeFilter> includeFilters,
                                           BeanDefinitionRegistry registry) {

            super(false);

            Assert.notNull(includeFilters, "Include filters must not be null!");
            Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

            if (includeFilters.iterator().hasNext()) {
                for (TypeFilter filter : includeFilters) {
                    addIncludeFilter(filter);
                }
            } else {
                super.addIncludeFilter(new InterfaceTypeFilter(BaseOperations.class));
            }
            addExcludeFilter(new AnnotationTypeFilter(NoRepositoryBean.class));
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {

            boolean isNonRepositoryInterface = !BaseOperations.class.getName().equals(beanDefinition.getBeanClassName());
            boolean isTopLevelType = !beanDefinition.getMetadata().hasEnclosingClass();
            boolean isConsiderNestedRepositories = isConsiderNestedRepositoryInterfaces();

            return isNonRepositoryInterface && (isTopLevelType || isConsiderNestedRepositories);
        }

        boolean isConsiderNestedRepositoryInterfaces() {
            return considerNestedRepositoryInterfaces;
        }

        public void setConsiderNestedRepositoryInterfaces(boolean considerNestedRepositoryInterfaces) {
            this.considerNestedRepositoryInterfaces = considerNestedRepositoryInterfaces;
        }

        private static class InterfaceTypeFilter extends AssignableTypeFilter {

            InterfaceTypeFilter(Class<?> targetType) {
                super(targetType);
            }

            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                    throws IOException {

                return metadataReader.getClassMetadata().isInterface() && super.match(metadataReader, metadataReaderFactory);
            }
        }

        @Override
        public Set<BeanDefinition> findCandidateComponents(String basePackage) {

            Set<BeanDefinition> candidates = super.findCandidateComponents(basePackage);

            for (BeanDefinition candidate : candidates) {
                if (candidate instanceof AnnotatedBeanDefinition) {
                    AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
                }
            }

            return candidates;
        }
    }

    /**
     * <h2> 获取扫描指定Repository的 beanDefinition </h2>
     */
    private List<BeanDefinition> getCandidates(BeanDefinitionRegistry registry) {
        RepositoryComponentProvider scanner = new RepositoryComponentProvider(Collections.emptySet(), registry);
        scanner.setConsiderNestedRepositoryInterfaces(false);
        scanner.setEnvironment(environment);
        scanner.setResourceLoader(resourceLoader);
        // 由于迭代器没有 spliterator() 方法,需要特殊处理
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(getPackages(),
                        Spliterator.ORDERED), false)
                .flatMap(it -> scanner.findCandidateComponents(it).stream())
                .collect(Collectors.toList());
    }

    private BeanDefinitionBuilder generateBeanDefinitionBuilder(BeanDefinition beanDefinition) throws ClassNotFoundException {

        Assert.notNull(beanDefinition, "BeanDefinitionRegistry must not be null!");
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .rootBeanDefinition(AggregateRepositoryFactoryBean.class.getName());
        builder.addConstructorArgValue(beanDefinition.getBeanClassName());
        builder.setLazyInit(false);
        /** ElasticsearchOperationsTemplete(Elasticsearch 数据源顶级接口实现类需要注入的bean依赖 {@link EsOperationsTemplate} */
        if (ElasticsearchOperations.class.isAssignableFrom(Class.forName(beanDefinition.getBeanClassName()))) {
            builder.addPropertyReference("elasticsearchIndexOperations", "elasticsearchIndexOperations");
        }
        // 其他数据源接口 实现类需要注入的bean 依赖自行补充
        // else if (....){}
        return builder;
    }

    private String generateBeanName(BeanDefinition definition,
                                    BeanNameGenerator beanNameGenerator,
                                    BeanDefinitionRegistry registry) {

        AnnotatedBeanDefinition beanDefinition = definition instanceof AnnotatedBeanDefinition
                ? (AnnotatedBeanDefinition) definition
                : new AnnotatedGenericBeanDefinition(getRepositoryInterfaceFrom(definition));
        return getDefaultBeanNameGenerator(beanNameGenerator).generateBeanName(beanDefinition, registry);
    }

    private BeanNameGenerator getDefaultBeanNameGenerator(BeanNameGenerator generator) {
        return generator == null || ConfigurationClassPostProcessor.IMPORT_BEAN_NAME_GENERATOR.equals(generator) //
                ? new AnnotationBeanNameGenerator() //
                : generator;
    }


    private Class<?> getRepositoryInterfaceFrom(BeanDefinition beanDefinition) {

        ConstructorArgumentValues.ValueHolder argumentValue = beanDefinition.getConstructorArgumentValues().getArgumentValue(0, Class.class);

        if (argumentValue == null) {
            throw new IllegalStateException(
                    String.format("Failed to obtain first constructor parameter value of BeanDefinition %s!", beanDefinition));
        }

        Object value = argumentValue.getValue();

        if (value == null) {

            throw new IllegalStateException(
                    String.format("Value of first constructor parameter value of BeanDefinition %s is null!", beanDefinition));

        } else if (value instanceof Class<?>) {

            return (Class<?>) value;

        } else {

            try {
                return ClassUtils.forName(value.toString(), ClassUtils.getDefaultClassLoader());
            } catch (Exception o_O) {
                throw new RuntimeException(o_O);
            }
        }
    }
}
