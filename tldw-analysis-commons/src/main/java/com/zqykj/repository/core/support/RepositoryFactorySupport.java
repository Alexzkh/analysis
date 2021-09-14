/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import com.zqykj.repository.Repository;
import com.zqykj.repository.core.EntityInformation;
import com.zqykj.repository.core.NamedQueries;
import com.zqykj.repository.core.RepositoryInformation;
import com.zqykj.repository.core.RepositoryMetadata;
import com.zqykj.repository.query.QueryLookupStrategy;
import com.zqykj.repository.query.QueryMethod;
import com.zqykj.repository.query.RepositoryQuery;
import com.zqykj.repository.util.QueryExecutionConverters;
import com.zqykj.util.ReflectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.log.LogMessage;
import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.TransactionalProxy;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> Factory bean to create instances of a given repository interface. Creates a proxy implementing the configured
 * repository interface and apply an advice handing the control to the {@code QueryExecutorMethodInterceptor}. </h1>
 */
public abstract class RepositoryFactorySupport implements BeanClassLoaderAware {

    final static GenericConversionService CONVERSION_SERVICE = new DefaultConversionService();
    private static final Log logger = LogFactory.getLog(RepositoryFactorySupport.class);


    static {
        //  以当前的 convert service 为基础, 继续添加一些converter
        QueryExecutionConverters.registerConvertersIn(CONVERSION_SERVICE);
        // 移除Object
        CONVERSION_SERVICE.removeConvertible(Object.class, Object.class);
    }

    /**
     * Additional repository specific information cache
     * key: RepositoryInterface
     * value : RepositoryInformation
     */
    private final Map<Class<?>, RepositoryInformation> repositoryInformationCache;

    // repository interface
    private Optional<Class<?>> repositoryBaseClass;
    // method 对应的 查询策略
    @Nullable
    private QueryLookupStrategy.Key queryLookupStrategyKey;
    // 系统默认类加载器
    private ClassLoader classLoader;
    private NamedQueries namedQueries;

    @SuppressWarnings("null")
    public RepositoryFactorySupport() {

        this.repositoryInformationCache = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);
        this.repositoryBaseClass = Optional.empty();
        this.namedQueries = PropertiesBasedNamedQueries.EMPTY;
        this.classLoader = org.springframework.util.ClassUtils.getDefaultClassLoader();
    }

    /**
     * <h2> 设置一个method 的查询查找策略 </h2>
     *
     * @param key {@link QueryLookupStrategy.Key}
     */
    public void setQueryLookupStrategyKey(QueryLookupStrategy.Key key) {
        this.queryLookupStrategyKey = key;
    }

    /**
     * <h2> Configures a {@link NamedQueries} instance to be handed to the {@link QueryLookupStrategy} for query creation. </h2>
     *
     * @param namedQueries the namedQueries to set
     */
    public void setNamedQueries(NamedQueries namedQueries) {
        this.namedQueries = namedQueries == null ? PropertiesBasedNamedQueries.EMPTY : namedQueries;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader == null ? org.springframework.util.ClassUtils.getDefaultClassLoader() : classLoader;
    }

    /**
     * <h2> 配置要在创建存储库代理时使用的存储库基类,如果没有将使用默认的返回类型 </h2>
     */
    public void setRepositoryBaseClass(Class<?> repositoryBaseClass) {
        this.repositoryBaseClass = Optional.ofNullable(repositoryBaseClass);
    }

    /**
     * <h2> Returns a repository instance for the given interface. </h2>
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    @SuppressWarnings({"unchecked"})
    public <T> T getRepository(Class<T> repositoryInterface) {

        if (logger.isDebugEnabled()) {
            logger.debug(LogMessage.format("Initializing repository instance for %s…", repositoryInterface.getName()));
        }

        Assert.notNull(repositoryInterface, "Repository interface must not be null!");

        // 获取给定 Repository metadata
        RepositoryMetadata metadata = getRepositoryMetadata(repositoryInterface);

        // 获取Repository additional information
        RepositoryInformation information = getRepositoryInformation(metadata, repositoryInterface);

        // TODO 这里可以校验 一些上面 获得的 Repository metadata 和 information

        // 获取target repository instance (基于 information 中 getRepositoryBaseClass 的Class)
        Object target = getTargetRepository(information);

        // create proxy factory
        ProxyFactory result = new ProxyFactory();

        // set proxy class
        result.setTarget(target);

        // set proxy interfaces
        result.setInterfaces(repositoryInterface, Repository.class, TransactionalProxy.class);

        // ExposeInvocationInterceptor就是用来传递MethodInvocation的, 后续拦截器如果需要,通过 currentInvocation 获取
        result.addAdvice(ExposeInvocationInterceptor.INSTANCE);

        // TODO 这里可以添加任意的切面 拦截器, 比如可以 拦截一些自定义的实现方法去处理(eg. findAll、findById、deleteById....)
        Optional<QueryLookupStrategy> queryLookupStrategy = getQueryLookupStrategy(queryLookupStrategyKey);
        // 我们这里只是添加 并 处理 方法上 携带 @Query 注解的特定方法
        result.addAdvice(new QueryExecutorMethodInterceptor(information, queryLookupStrategy, namedQueries));

        // 获取当前 Repository proxy(即对应顶级数据源Repository 完整的impl instance eg. ElasticsearchRepository 对应 SimpleElasticsearchRepository)
        T repository = (T) result.getProxy(classLoader);

        if (logger.isDebugEnabled()) {

            logger.debug(LogMessage.format("Finished creation of repository instance for {}.", repositoryInterface.getName()));
        }
        return repository;
    }

    /**
     * <h2> Returns the {@link RepositoryMetadata} for the given repository interface. </h2>
     *
     * @param repositoryInterface will never be {@literal null}.
     */
    protected RepositoryMetadata getRepositoryMetadata(Class<?> repositoryInterface) {
        return AbstractRepositoryMetadata.getMetadata(repositoryInterface);
    }


    /**
     * <h2> Returns the {@link RepositoryInformation} for the given repository interface. </h2>
     *
     * @param metadata Repository metadata information
     */
    private RepositoryInformation getRepositoryInformation(RepositoryMetadata metadata,
                                                           Class<?> repositoryInterface) {

        return repositoryInformationCache.computeIfAbsent(repositoryInterface, key -> {

            Class<?> baseClass = repositoryBaseClass.orElse(getRepositoryBaseClass(metadata));

            return new DefaultRepositoryInformation(metadata, baseClass);
        });
    }

    /**
     * <h2> Returns the {@link EntityInformation} for the given domain class. </h2>
     *
     * @param <T>         the entity type
     * @param <ID>        the id type
     * @param domainClass domainClass
     */
    public abstract <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass);

    /**
     * <h2> Create a repository instance as backing for the query proxy. </h2>
     *
     * @param information RepositoryInformation
     */
    protected abstract Object getTargetRepository(RepositoryInformation information);

    /**
     * <h2> Returns the base class backing the actual repository instance. Make sure </h2>
     * {@link #getTargetRepository(RepositoryInformation)} returns an instance of this class.
     *
     * @param metadata RepositoryMetadata
     */
    protected abstract Class<?> getRepositoryBaseClass(RepositoryMetadata metadata);

    /**
     * <h2> Returns the {@link QueryLookupStrategy} for the given {@link QueryLookupStrategy.Key} </h2>
     */
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key) {
        return Optional.empty();
    }


    /**
     * <h2> Creates a repository of the repository base class defined in the given {@link RepositoryInformation} using
     * * reflection. </h2>
     *
     * @param information          RepositoryInformation
     * @param constructorArguments constructorArguments
     */
    protected final <R> R getTargetRepositoryViaReflection(RepositoryInformation information,
                                                           Object... constructorArguments) {

        Class<?> baseClass = information.getRepositoryBaseClass();
        return getTargetRepositoryViaReflection(baseClass, constructorArguments);
    }

    /**
     * <h2> Creates a repository of the repository base class defined in the given {@link RepositoryInformation} using
     * * reflection. </h2>
     *
     * @param baseClass            Repository implementation class
     * @param constructorArguments constructorArguments
     */
    @SuppressWarnings("unchecked")
    protected final <R> R getTargetRepositoryViaReflection(Class<?> baseClass, Object... constructorArguments) {
        Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(baseClass, constructorArguments);

        return constructor.map(it -> (R) BeanUtils.instantiateClass(it, constructorArguments))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "No suitable constructor found on %s to match the given arguments: %s. Make sure you implement a constructor taking these",
                        baseClass, Arrays.stream(constructorArguments).map(Object::getClass).collect(Collectors.toList()))));
    }
}
