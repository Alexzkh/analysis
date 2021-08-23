/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.support;

import com.sun.istack.NotNull;
import com.zqykj.infrastructure.util.ReflectionUtils;
import com.zqykj.tldw.aggregate.data.repository.AbstractRepositoryMetadata;
import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import com.zqykj.tldw.aggregate.data.repository.RepositoryMetadata;
import com.zqykj.tldw.aggregate.data.repository.elasticsearch.ElasticsearchRepositoryInformation;
import com.zqykj.tldw.aggregate.index.elasticsearch.associate.ElasticsearchIndexOperations;
import com.zqykj.tldw.aggregate.BaseOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchRestTemplate;
import com.zqykj.tldw.aggregate.searching.esclientrhl.impl.SimpleElasticsearchOperations;
import com.zqykj.tldw.aggregate.searching.mongoclientrhl.MongoRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <h1> FactoryBean 实现一些通用方法调用放在这 </h1>
 */
@Slf4j
public abstract class AggregateRepositoryFactorySupport {

    private ClassLoader classLoader;
    private Optional<ElasticsearchRestTemplate> elasticsearchRestTemplate = Optional.empty();
    private Optional<MongoRestTemplate> mongoRestTemplate = Optional.empty();

    public AggregateRepositoryFactorySupport() {
        this.classLoader = org.springframework.util.ClassUtils.getDefaultClassLoader();
    }

    protected void setElasticsearchTemplate(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = Optional.of(elasticsearchRestTemplate);
    }

    protected void setMongoTemplate(MongoRestTemplate mongoTemplate) {
        this.mongoRestTemplate = Optional.of(mongoTemplate);
    }

    /**
     * <h2> 返回给定接口的实例 </h2>
     */
    @SuppressWarnings({"unchecked"})
    protected <T> T getRepository(Class<T> repositoryInterface) {

        if (log.isDebugEnabled()) {
            log.debug("Initializing repository instance for = {} …", repositoryInterface.getName());
        }

        Assert.notNull(repositoryInterface, "Repository Interface must not be null!");

        // 获取Repository metadata
        RepositoryMetadata metadata = getRepositoryMetadata(repositoryInterface);

        // 生成特定 Repository Interface information,并注入到实现类中
        RepositoryInformation repositoryInformation = getRepositoryInformation(metadata, repositoryInterface);

        Object target = getTargetRepository(repositoryInformation);

        // create proxy
        ProxyFactory result = new ProxyFactory();

        // set proxy class
        result.setTarget(target);
        // set proxy interface , TestRepository 是顶级接口(必须设置, repositoryInterface 很可能是用户继承TestRepository的自定义接口)
        result.setInterfaces(repositoryInterface, BaseOperations.class);

        // ExposeInvocationInterceptor就是用来传递MethodInvocation的, 后续拦截器如果需要,通过 currentInvocation 获取
        result.addAdvice(ExposeInvocationInterceptor.INSTANCE);

        // 判断是默认方法,还是Query 方法,添加不同的advice(MethodInterceptor实现处理)
//        AggregateRepositoryInformation repositoryInformation = new AggregateRepositoryInformation(repositoryInterface);

        // 添加查询方法拦截器(针对method 上带有@Query注解处理,如果是普通方法,直接走代理实现类方法
        result.addAdvice(new QueryExecutorMethodInterceptor(repositoryInformation));

        T repository = (T) result.getProxy(classLoader);

        if (log.isDebugEnabled()) {
            log.debug("Finished creation of repository instance for {}.", repositoryInterface.getName());
        }
        return repository;
    }

    /**
     * <h2> 获取接口默认的实现类 </h2>
     */
    protected Class<?> getRepositoryBaseClass(Class<?> repositoryInterface) {

        // 根据 repositoryInterface 拿到对应的 impl Class
        if (ElasticsearchOperations.class.isAssignableFrom(repositoryInterface)) {
            return SimpleElasticsearchOperations.class;
        }
        //TODO 其他数据源的顶级RepositoryInterface.class 判断
//        else if (....){
//
//        }
        // 默认使用Es
        return SimpleElasticsearchOperations.class;
    }

    /**
     * <h2> 获取ElasticsearchRepositoryMetadata </h2>
     */
    protected RepositoryMetadata getRepositoryMetadata(Class<?> repositoryInterface) {
        return AbstractRepositoryMetadata.getMetadata(repositoryInterface);
    }

    /**
     * <h2> Elasticsearch Repository information </h2>
     */
    protected RepositoryInformation getRepositoryInformation(RepositoryMetadata metadata, Class<?> repositoryInterface) {
        return new ElasticsearchRepositoryInformation(metadata, getRepositoryBaseClass(repositoryInterface));
    }

    /**
     * <h2> 根据目标Repository 生成 对应的Impl </h2>
     */
    protected Object getTargetRepository(RepositoryInformation metadata) {

        // 生成对应实现类想要的client
        if (ElasticsearchOperations.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            // 生成Es 实现类构造函数想要的Object
            return getTargetRepositoryViaReflection(metadata, metadata, elasticsearchRestTemplate.orElse(null));
        }
        // TODO 其他数据源实现类 构造函数需要注入的...
//        else if(...){
//
//        }
        else {
            // TODO
        }
        return getTargetRepositoryViaReflection(metadata);
    }

    private Object getTargetRepositoryViaReflection(RepositoryInformation information,
                                                    Object... constructorArguments) {
        Class<?> repositoryBaseClass = information.getRepositoryBaseClass();
        return getTargetRepositoryViaReflection(repositoryBaseClass, constructorArguments);
    }

    /**
     * <h2> 生成接口代理类instance(因为是多数据源, 因此baseClass可能是Mongo impl,Elasticsearch impl.....) </h2>
     */
    @SuppressWarnings("unchecked")
    protected <R> R getTargetRepositoryViaReflection(Class<?> baseClass, @NotNull Object... constructorArguments) {

        Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(baseClass, constructorArguments);

        // baseClass instantiate by construct
        return constructor.map(it -> (R) BeanUtils.instantiateClass(it, constructorArguments))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "No suitable constructor found on %s to match the given arguments: %s. Make sure you implement a constructor taking these",
                        baseClass, Arrays.stream(constructorArguments).map(Object::getClass).collect(Collectors.toList()))));
    }

}
