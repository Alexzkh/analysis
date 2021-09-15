/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import com.zqykj.repository.core.support.RepositoryFactorySupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.log.LogMessage;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>  Delegate for configuration integration to reuse the general way of detecting repositories. Customization is done by
 * providing a configuration format specific {@link RepositoryConfigurationSource} (currently either XML or annotations
 * are supported). The actual registration can then be triggered for different {@link RepositoryConfigurationExtension}
 * </h1>
 */
@Slf4j
public class RepositoryConfigurationDelegate {

    private static final String REPOSITORY_REGISTRATION = "Data %s - Registering repository: %s - Interface: %s - Factory: %s";
    private static final String MULTIPLE_MODULES = "Multiple Spring Data modules found, entering strict repository configuration mode!";
    private static final String NON_DEFAULT_AUTOWIRE_CANDIDATE_RESOLVER = "Non-default AutowireCandidateResolver (%s) detected. Skipping the registration of LazyRepositoryInjectionPointResolver. Lazy repository injection will not be working!";

    static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";

    private final RepositoryConfigurationSource configurationSource;
    private final ResourceLoader resourceLoader;
    private final Environment environment;
    private final boolean inMultiStoreMode;


    /**
     * Creates a new {@link RepositoryConfigurationDelegate} for the given {@link RepositoryConfigurationSource} and
     * {@link ResourceLoader} and {@link Environment}.
     *
     * @param configurationSource must not be {@literal null}.
     * @param resourceLoader      must not be {@literal null}.
     * @param environment         must not be {@literal null}.
     */
    public RepositoryConfigurationDelegate(RepositoryConfigurationSource configurationSource,
                                           ResourceLoader resourceLoader, Environment environment) {

        Assert.notNull(resourceLoader, "ResourceLoader must not be null!");

        this.configurationSource = configurationSource;
        this.resourceLoader = resourceLoader;
        this.environment = defaultEnvironment(environment, resourceLoader);
        this.inMultiStoreMode = multipleStoresDetected();
    }

    /**
     * <h2> 如果给定的Environment 为空,则使用此默认的 </h2>
     *
     * @param environment    can be {@literal null}.
     * @param resourceLoader can be {@literal null}.
     */
    private static Environment defaultEnvironment(@Nullable Environment environment,
                                                  @Nullable ResourceLoader resourceLoader) {
        if (environment != null) {
            return environment;
        }
        return resourceLoader instanceof EnvironmentCapable ? ((EnvironmentCapable) resourceLoader).getEnvironment()
                : new StandardEnvironment();
    }

    /**
     * Registers the found repositories in the given {@link BeanDefinitionRegistry}.
     *
     * @param registry  bean 注册定义
     * @param extension 用于后置处理BeanDefinitionBuilder的类
     * @return {@link BeanComponentDefinition}s for all repository bean definitions found.
     */
    public List<BeanComponentDefinition> registerRepositoriesIn(BeanDefinitionRegistry registry,
                                                                RepositoryConfigurationExtension extension) {

        if (log.isInfoEnabled()) {
            log.info(String.valueOf(LogMessage.format("Bootstrapping Spring Data %s repositories in %s mode.", //
                    extension.getModuleName(), configurationSource.getBootstrapMode().name())));
        }

        extension.registerBeansForRoot(registry, configurationSource);

        RepositoryBeanDefinitionBuilder builder = new RepositoryBeanDefinitionBuilder(registry, resourceLoader, environment);
        List<BeanComponentDefinition> definitions = new ArrayList<>();

        StopWatch watch = new StopWatch();

        if (log.isDebugEnabled()) {
            log.debug(String.valueOf(LogMessage.format("Scanning for %s repositories in packages %s.", //
                    extension.getModuleName(), //
                    configurationSource.getBasePackages().stream().collect(Collectors.joining(", ")))));
        }

        watch.start();

        Collection<RepositoryConfiguration<RepositoryConfigurationSource>> configurations = extension
                .getRepositoryConfigurations(configurationSource, resourceLoader, inMultiStoreMode);

        Map<String, RepositoryConfiguration<?>> configurationsByRepositoryName = new HashMap<>(configurations.size());

        for (RepositoryConfiguration<? extends RepositoryConfigurationSource> configuration : configurations) {

            configurationsByRepositoryName.put(configuration.getRepositoryInterface(), configuration);

            BeanDefinitionBuilder definitionBuilder = builder.build(configuration);

            extension.postProcess(definitionBuilder, configurationSource);

            extension.postProcess(definitionBuilder, (AnnotationRepositoryConfigurationSource) configurationSource);

            AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();

            String beanName = configurationSource.generateBeanName(beanDefinition);

            if (log.isTraceEnabled()) {
                log.trace(String.valueOf(LogMessage.format(REPOSITORY_REGISTRATION, extension.getModuleName(), beanName, configuration.getRepositoryInterface(),
                        configuration.getRepositoryFactoryBeanClassName())));
            }

            beanDefinition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, configuration.getRepositoryInterface());

            registry.registerBeanDefinition(beanName, beanDefinition);
            definitions.add(new BeanComponentDefinition(beanDefinition, beanName));
        }

        potentiallyLazifyRepositories(configurationsByRepositoryName, registry, configurationSource.getBootstrapMode());

        watch.stop();

        if (log.isInfoEnabled()) {
            log.info(String.valueOf(LogMessage.format("Finished Spring Data repository scanning in %s ms. Found %s %s repository interfaces.", //
                    watch.getLastTaskTimeMillis(), configurations.size(), extension.getModuleName())));
        }

        return definitions;
    }

    /**
     * Scans {@code repository.support} packages for implementations of {@link RepositoryFactorySupport}. Finding more
     * than a single type is considered a multi-store configuration scenario which will trigger stricter repository
     * scanning.
     */
    private boolean multipleStoresDetected() {

        boolean multipleModulesFound = SpringFactoriesLoader
                .loadFactoryNames(RepositoryFactorySupport.class, resourceLoader.getClassLoader()).size() > 1;

        if (multipleModulesFound) {
            log.info(MULTIPLE_MODULES);
        }

        return multipleModulesFound;
    }

    /**
     * Registers a {@link LazyRepositoryInjectionPointResolver} over the default
     * {@link ContextAnnotationAutowireCandidateResolver} to make injection points of lazy repositories lazy, too. Will
     * augment the {@link LazyRepositoryInjectionPointResolver}'s configuration if there already is one configured.
     *
     * @param configurations must not be {@literal null}.
     * @param registry       must not be {@literal null}.
     */
    private static void potentiallyLazifyRepositories(Map<String, RepositoryConfiguration<?>> configurations,
                                                      BeanDefinitionRegistry registry, BootstrapMode mode) {

        if (!DefaultListableBeanFactory.class.isInstance(registry) || mode.equals(BootstrapMode.DEFAULT)) {
            return;
        }

        DefaultListableBeanFactory beanFactory = DefaultListableBeanFactory.class.cast(registry);

        AutowireCandidateResolver resolver = beanFactory.getAutowireCandidateResolver();

        if (!Arrays.asList(ContextAnnotationAutowireCandidateResolver.class, LazyRepositoryInjectionPointResolver.class)
                .contains(resolver.getClass())) {

            log.warn(String.valueOf(LogMessage.format(NON_DEFAULT_AUTOWIRE_CANDIDATE_RESOLVER, resolver.getClass().getName())));

            return;
        }

        AutowireCandidateResolver newResolver = LazyRepositoryInjectionPointResolver.class.isInstance(resolver) //
                ? LazyRepositoryInjectionPointResolver.class.cast(resolver).withAdditionalConfigurations(configurations) //
                : new LazyRepositoryInjectionPointResolver(configurations);

        beanFactory.setAutowireCandidateResolver(newResolver);

        if (mode.equals(BootstrapMode.DEFERRED)) {

            log.debug("Registering deferred repository initialization listener.");

            beanFactory.registerSingleton(DeferredRepositoryInitializationListener.class.getName(),
                    new DeferredRepositoryInitializationListener(beanFactory));
        }
    }

    /**
     * Customer {@link ContextAnnotationAutowireCandidateResolver} that also considers all injection points for lazy
     * repositories lazy.
     */
    static class LazyRepositoryInjectionPointResolver extends ContextAnnotationAutowireCandidateResolver {

        private static final Log logger = LogFactory.getLog(LazyRepositoryInjectionPointResolver.class);

        private final Map<String, RepositoryConfiguration<?>> configurations;

        public LazyRepositoryInjectionPointResolver(Map<String, RepositoryConfiguration<?>> configurations) {
            this.configurations = configurations;
        }

        /**
         * Returns a new {@link LazyRepositoryInjectionPointResolver} that will have its configurations augmented with the
         * given ones.
         *
         * @param configurations must not be {@literal null}.
         */
        LazyRepositoryInjectionPointResolver withAdditionalConfigurations(
                Map<String, RepositoryConfiguration<?>> configurations) {

            Map<String, RepositoryConfiguration<?>> map = new HashMap<>(this.configurations);
            map.putAll(configurations);

            return new LazyRepositoryInjectionPointResolver(map);
        }

        @Override
        protected boolean isLazy(DependencyDescriptor descriptor) {

            Class<?> type = descriptor.getDependencyType();

            RepositoryConfiguration<?> configuration = configurations.get(type.getName());

            if (configuration == null) {
                return super.isLazy(descriptor);
            }

            boolean lazyInit = configuration.isLazyInit();

            if (lazyInit) {
                logger.debug(LogMessage.format("Creating lazy injection proxy for %s…", configuration.getRepositoryInterface()));
            }

            return lazyInit;
        }
    }
}
