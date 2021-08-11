/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.support;

import com.sun.istack.NotNull;
import com.zqykj.infrastructure.util.ReflectionUtils;
import com.zqykj.tldw.aggregate.repository.TestRepository;
import com.zqykj.tldw.aggregate.repository.impl.TestRepositoryImpl;
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

    public AggregateRepositoryFactorySupport() {
        this.classLoader = org.springframework.util.ClassUtils.getDefaultClassLoader();
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

        Object target = getTargetRepositoryViaReflection(getRepositoryBaseClass());

        // create proxy
        ProxyFactory result = new ProxyFactory();

        // set proxy class
        result.setTarget(target);
        // set proxy interface , TestRepository 是顶级接口(必须设置, repositoryInterface 很可能是用户继承TestRepository的自定义接口)
        result.setInterfaces(repositoryInterface, TestRepository.class);

        // ExposeInvocationInterceptor就是用来传递MethodInvocation的, 后续拦截器如果需要,通过 currentInvocation 获取
        result.addAdvice(ExposeInvocationInterceptor.INSTANCE);

        // 判断是默认方法,还是Query 方法,添加不同的advice(MethodInterceptor实现处理)
        AggregateRepositoryInformation repositoryInformation = new AggregateRepositoryInformation(repositoryInterface);

        // 添加查询方法拦截器(针对method 上带有@Query注解处理,如果是普通方法,直接走代理实现类方法
        result.addAdvice(new QueryExecutorMethodInterceptor(repositoryInterface, repositoryInformation));

        T repository = (T) result.getProxy(classLoader);

        if (log.isDebugEnabled()) {
            log.debug("Finished creation of repository instance for {}.", repositoryInterface.getName());
        }
        return repository;
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


    /**
     * <h2> 获取接口默认的实现类 </h2>
     */
    protected Class<?> getRepositoryBaseClass() {

        //TODO 后续修改 根据特定注解值(配置文件指定的值) 来获取指定的默认实现类
        // 根据FactorySupport 去动态获取 接口需要的实现类
        return TestRepositoryImpl.class;
    }


}
