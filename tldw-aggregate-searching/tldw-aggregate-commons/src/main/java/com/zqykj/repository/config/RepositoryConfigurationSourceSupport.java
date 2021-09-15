/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import com.zqykj.util.Streamable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * <h1> RepositoryConfigurationSourceSupport </h1>
 * <p>
 * Base class to implement {@link RepositoryConfigurationSource}s.
 * </p>
 */
public abstract class RepositoryConfigurationSourceSupport implements RepositoryConfigurationSource {

    protected static final String DEFAULT_REPOSITORY_IMPL_POSTFIX = "Impl";

    private final Environment environment;
    private final RepositoryBeanNameGenerator beanNameGenerator;
    private final BeanDefinitionRegistry registry;

    /**
     * Creates a new {@link RepositoryConfigurationSourceSupport} with the given environment.
     *
     * @param environment must not be {@literal null}.
     * @param classLoader must not be {@literal null}.
     * @param registry    must not be {@literal null}.
     */
    public RepositoryConfigurationSourceSupport(Environment environment, ClassLoader classLoader,
                                                BeanDefinitionRegistry registry, BeanNameGenerator generator) {

        Assert.notNull(environment, "Environment must not be null!");
        Assert.notNull(classLoader, "ClassLoader must not be null!");
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

        this.environment = environment;
        this.beanNameGenerator = new RepositoryBeanNameGenerator(classLoader, generator, registry);
        this.registry = registry;
    }

    /**
     * <h2> 获取扫描到的所有符合Repository interfaces type's Candidates </h2>
     */
    @Override
    public Streamable<BeanDefinition> getCandidates(ResourceLoader loader) {

        RepositoryComponentProvider scanner = new RepositoryComponentProvider(getIncludeFilters(), registry);
        scanner.setConsiderNestedRepositoryInterfaces(shouldConsiderNestedRepositories());
        scanner.setEnvironment(environment);
        scanner.setResourceLoader(loader);

        getExcludeFilters().forEach(it -> scanner.addExcludeFilter(it));

        return Streamable.of(() -> getBasePackages().stream()//
                .flatMap(it -> scanner.findCandidateComponents(it).stream()));
    }

    /**
     * <h2> 过滤器类型 </h2>
     */
    protected Iterable<TypeFilter> getIncludeFilters() {
        return Collections.emptySet();
    }

    /**
     * <h2> 返回我们是否应该考虑嵌套存储库 </h2>
     */
    public boolean shouldConsiderNestedRepositories() {
        return false;
    }

    @Override
    public String generateBeanName(BeanDefinition beanDefinition) {
        return beanNameGenerator.generateBeanName(beanDefinition);
    }

    @Override
    public Streamable<TypeFilter> getExcludeFilters() {
        return Streamable.empty();
    }
}
