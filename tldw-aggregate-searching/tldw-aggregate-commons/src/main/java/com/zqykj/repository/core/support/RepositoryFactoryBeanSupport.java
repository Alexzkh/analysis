/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import com.zqykj.mapping.PersistentEntity;
import com.zqykj.mapping.context.MappingContext;
import com.zqykj.repository.Repository;
import com.zqykj.repository.core.NamedQueries;
import com.zqykj.repository.query.QueryLookupStrategy;
import com.zqykj.util.Lazy;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * <h1> Adapter for Springs {@link FactoryBean} interface to allow easy setup of repository factories via Spring
 * configuration </h1>
 */
public abstract class RepositoryFactoryBeanSupport<T extends Repository>
        implements InitializingBean, FactoryBean<T>, BeanClassLoaderAware {

    private final Class<? extends T> repositoryInterface;

    private RepositoryFactorySupport factory;
    private QueryLookupStrategy.Key queryLookupStrategyKey;
    private Optional<Class<?>> repositoryBaseClass = Optional.empty();
    private NamedQueries namedQueries;
    private Optional<MappingContext<?, ?>> mappingContext = Optional.empty();
    private ClassLoader classLoader;
    private boolean lazyInit = false;

    private Lazy<T> repository;

    /**
     * <h2> Creates a new {@link RepositoryFactoryBeanSupport} for the given repository interface. </h2>
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    protected RepositoryFactoryBeanSupport(Class<? extends T> repositoryInterface) {

        Assert.notNull(repositoryInterface, "Repository interface must not be null!");
        this.repositoryInterface = repositoryInterface;
    }

    /**
     * <h2> 配置Repository 的基类(即实现类) eg. ElasticsearchRepository 对应 SimpleElasticsearchRepository </h2>
     *
     * @param repositoryBaseClass the repositoryBaseClass to set, can be {@literal null}.
     * @since 1.11
     */
    public void setRepositoryBaseClass(Class<?> repositoryBaseClass) {
        this.repositoryBaseClass = Optional.ofNullable(repositoryBaseClass);
    }

    /**
     * Set the {@link QueryLookupStrategy.Key} to be used.
     */
    public void setQueryLookupStrategyKey(QueryLookupStrategy.Key queryLookupStrategyKey) {
        this.queryLookupStrategyKey = queryLookupStrategyKey;
    }

    /**
     * Setter to inject a {@link NamedQueries} instance.
     *
     * @param namedQueries the namedQueries to set
     */
    public void setNamedQueries(NamedQueries namedQueries) {
        this.namedQueries = namedQueries;
    }

    /**
     * Configures the {@link MappingContext} to be used to lookup {@link PersistentEntity} instances for
     */
    protected void setMappingContext(MappingContext<?, ?> mappingContext) {
        this.mappingContext = Optional.of(mappingContext);
    }

    /**
     * <h2> 配置是否懒加载 去调用 getRepository() </h2>
     *
     * @param lazy whether to initialize the repository proxy lazily. This defaults to {@literal false}.
     */
    public void setLazyInit(boolean lazy) {
        this.lazyInit = lazy;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    @NonNull
    public T getObject() {
        return this.repository.get();
    }

    @Override
    @NonNull
    public Class<? extends T> getObjectType() {
        return repositoryInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() {

        this.factory = createRepositoryFactory();
        this.factory.setQueryLookupStrategyKey(queryLookupStrategyKey);
        this.factory.setNamedQueries(namedQueries);
        this.factory.setBeanClassLoader(classLoader);
        repositoryBaseClass.ifPresent(this.factory::setRepositoryBaseClass);

        this.repository = Lazy.of(() -> this.factory.getRepository(repositoryInterface));

        if (!lazyInit) {
            this.repository.get();
        }
    }

    /**
     * Create the actual { RepositoryFactorySupport} instance.
     */
    protected abstract RepositoryFactorySupport createRepositoryFactory();
}
