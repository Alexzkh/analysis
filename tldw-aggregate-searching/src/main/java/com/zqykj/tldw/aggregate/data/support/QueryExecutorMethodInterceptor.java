/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.support;


import com.zqykj.tldw.aggregate.data.query.AbstractAggregateRepositoryQuery;
import com.zqykj.tldw.aggregate.util.ApplicationUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <h1> 拦截自定义方法的调用 (如接口方法上添加了@Query 注解)</h1>
 */
public class QueryExecutorMethodInterceptor implements MethodInterceptor {


    private final Class<?> repositoryInterface;
    /**
     * 存储Query 注解的Method 和 Value
     */
    private final Map<Method, String> queries;

    private final AggregateRepositoryInformation repositoryInformation;

    public QueryExecutorMethodInterceptor(Class<?> repositoryInterface, AggregateRepositoryInformation repositoryInformation) {
        this.repositoryInterface = repositoryInterface;
        this.repositoryInformation = repositoryInformation;
        this.queries = mapMethodsToQuery(repositoryInformation.getQueryMethods());
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        return doInvoke(invocation);
    }

    private Map<Method, String> mapMethodsToQuery(List<Method> methods) {
        return methods.stream().collect(Collectors.toMap(method -> method, repositoryInformation::getAnnotatedQuery));
    }


    @Nullable
    private Object doInvoke(MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();

        // 如果method 上带有@Query 注解(需要特殊处理实现)
        if (hasQueryFor(method)) {

            return execute(method, invocation.getArguments(), queries.get(method));
        }
        // 否则走代理实现类方法
        return invocation.proceed();
    }

    private boolean hasQueryFor(Method method) {
        return queries.containsKey(method);
    }

    /**
     * <h2> 对@Query value 解析并构建Search </h2>
     */
    public Object execute(Method method, Object[] parameters, String query) {
        // 这里处理@Query 注解方法实现,由于不同数据源根据对应client 实现方式不一致,因此需要抽象多个实现
        // TODO 目前先实现Elasticsearch
        AbstractAggregateRepositoryQuery stringQuery = ApplicationUtils.getBean(AbstractAggregateRepositoryQuery.class);
        stringQuery.setMethod(method);
        stringQuery.setQuery(query);
        stringQuery.setRepositoryInformation(repositoryInformation);
        return stringQuery.execute(parameters);
    }
}
