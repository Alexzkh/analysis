/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data;

import com.zqykj.tldw.aggregate.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <h1>扫描当前启动类所在的包以及子包下的指定接口,获取相关的BeanDefinition并包装成FactoryBean的子类</h1>
 *
 * @author Mcj
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
        for (BeanDefinition candidate : getCandidates()) {
            Objects.requireNonNull(candidate.getBeanClassName());
            String beanClassName = candidate.getBeanClassName();
            try {
                // Class<?> repositoryInterface = ClassUtils.forName(beanClassName, ClassUtils.getDefaultClassLoader());
                // 注册Bean定义信息
                BeanDefinitionBuilder definitionBuilder = generateBeanDefinitionBuilder(candidate);
                AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
                beanDefinition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClassName);
                // 向容器注册beanDefinition
                registry.registerBeanDefinition(this.generateBeanName(beanDefinition, beanNameGenerator, registry), beanDefinition);
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
    protected Iterator<String> getPackages() {
        return AutoConfigurationPackages.get(this.beanFactory).iterator();
    }

    /**
     * <h2>创建扫描包的定义类</h2>
     */
    protected ClassPathScanningCandidateComponentProvider createClassPathScanningCandidateComponentProvider() {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.setEnvironment(environment);
        scanner.setResourceLoader(resourceLoader);
        return scanner;
    }

    /**
     * <h2> 获取扫描指定Repository的 beanDefinition </h2>
     */
    protected List<BeanDefinition> getCandidates() {
        ClassPathScanningCandidateComponentProvider scanner =
                this.createClassPathScanningCandidateComponentProvider();
        scanner.addIncludeFilter(new InterfaceTypeFilter(TestRepository.class));
        // 由于迭代器没有 spliterator() 方法,需要特殊处理
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(getPackages(),
                        Spliterator.ORDERED), false)
                .flatMap(it -> scanner.findCandidateComponents(it).stream())
                .collect(Collectors.toList());
    }


    /**
     * <h2> 指定类型过滤器(此处需要的是interface) </h2>
     */
    private static class InterfaceTypeFilter extends AssignableTypeFilter {
        /**
         * Creates a new {@link InterfaceTypeFilter}.
         *
         * @param targetType 指定需要扫描的类
         */
        public InterfaceTypeFilter(Class<?> targetType) {
            super(targetType);
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                throws IOException {
            return metadataReader.getClassMetadata().isInterface() && super.match(metadataReader, metadataReaderFactory);
        }
    }


    public BeanDefinitionBuilder generateBeanDefinitionBuilder(BeanDefinition beanDefinition) {

        Assert.notNull(beanDefinition, "BeanDefinitionRegistry must not be null!");
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .rootBeanDefinition(AggregateRepositoryFactoryBean.class);
        builder.addConstructorArgValue(beanDefinition.getBeanClassName());
        return builder;
    }

    public String generateBeanName(BeanDefinition definition,
                                   BeanNameGenerator beanNameGenerator,
                                   BeanDefinitionRegistry registry) {

        AnnotatedBeanDefinition beanDefinition = definition instanceof AnnotatedBeanDefinition
                ? (AnnotatedBeanDefinition) definition
                : new AnnotatedGenericBeanDefinition(getRepositoryInterfaceFrom(definition));
        return beanNameGenerator.generateBeanName(beanDefinition, registry);
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
