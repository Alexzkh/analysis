/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;


import com.zqykj.repository.core.NamedQueries;
import com.zqykj.repository.core.RepositoryInformation;
import com.zqykj.repository.query.QueryLookupStrategy;
import com.zqykj.repository.query.RepositoryQuery;
import com.zqykj.repository.util.QueryExecutionConverters;
import com.zqykj.util.Pair;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;


/**
 * <h1> 主要是拦截 extends {@link com.zqykj.repository.Repository} 的子接口内部所有的查询方法  {@link MethodInterceptor}</h1>
 */
class QueryExecutorMethodInterceptor implements MethodInterceptor {

    /**
     * 存储Repository 内 查询方法 与 查询策略 映射
     */
    private final Map<Method, RepositoryQuery> queries;
    private final RepositoryInformation repositoryInformation;
    private final Map<Method, RepositoryMethodInvoker> invocationMetadataCache = new ConcurrentReferenceHashMap<>();
    private final NamedQueries namedQueries;
    private final QueryExecutionResultHandler resultHandler;

    public QueryExecutorMethodInterceptor(RepositoryInformation repositoryInformation,
                                          Optional<QueryLookupStrategy> queryLookupStrategy, NamedQueries namedQueries) {
        this.repositoryInformation = repositoryInformation;
        this.namedQueries = namedQueries;
        this.resultHandler = new QueryExecutionResultHandler(QueryExecutionConverters.CONVERSION_SERVICE);

        // 判断查询方法的查询策略是否存在
        if (!queryLookupStrategy.isPresent()) {

            throw new IllegalStateException("You have defined query methods in the repository"
                    + " but do not have any query lookup strategy defined."
                    + " The infrastructure apparently does not support query methods!");
        }
        //
        this.queries = queryLookupStrategy //
                .map(it -> mapMethodsToQuery(repositoryInformation, it)) //
                .orElse(Collections.emptyMap());
    }

    private Map<Method, RepositoryQuery> mapMethodsToQuery(RepositoryInformation repositoryInformation,
                                                           QueryLookupStrategy lookupStrategy) {

        return repositoryInformation.getQueryMethods().stream() //
                .map(method -> lookupQuery(method, repositoryInformation, lookupStrategy)) //
                .collect(Pair.toMap());
    }

    /**
     * <h2> 查看method 的查询策略 </h2>
     */
    private Pair<Method, RepositoryQuery> lookupQuery(Method method, RepositoryInformation information,
                                                      QueryLookupStrategy strategy) {
        return Pair.of(method, strategy.resolveQuery(method, information, namedQueries));
    }

    /**
     * <h2> 查询方法拦截后在这里进行处理 </h2>
     */
    @Nullable
    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();

        // 这里想通过 Java 8+ 函数式库Vavr功能 去 适配方法返回类型处理的,目前暂时不支持这个
        QueryExecutionConverters.ExecutionAdapter executionAdapter = QueryExecutionConverters //
                .getExecutionAdapter(method.getReturnType());

        // 如果没有引用 Vavr 这个函数库, 处理查询方法逻辑走这边
        if (executionAdapter == null) {

            // 对调用结果的一个后处理(主要处理 方法返回类型)
            return resultHandler.postProcessInvocationResult(doInvoke(invocation), method);
        }

        return executionAdapter //
                .apply(() -> resultHandler.postProcessInvocationResult(doInvoke(invocation), method));
    }

    /**
     * <h2>
     * 主要处理elasticsearch 返回的hits 映射成 方法返回类型(展开的类型) eg. Optional<索引类>
     * 这个方法只会将 elasticsearch 返回的数据包装成 内部类型, 外层类型Optional
     * 会在 resultHandler.postProcessInvocationResult() 这个方法进行后续处理
     * </h2>
     */
    @Nullable
    private Object doInvoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        if (hasQueryFor(method)) {

            RepositoryMethodInvoker invocationMetadata = invocationMetadataCache.get(method);

            if (invocationMetadata == null) {
                invocationMetadata = RepositoryMethodInvoker.forRepositoryQuery(method, queries.get(method));
                invocationMetadataCache.put(method, invocationMetadata);
            }

            return invocationMetadata.invoke(repositoryInformation.getRepositoryInterface(),
                    invocation.getArguments());
        }

        return invocation.proceed();
    }

    /**
     * <h2> 返回method 是否有对应的查询策略 RepositoryQuery</h2>
     *
     * @param method 查询方法
     * @return true / false
     */
    private boolean hasQueryFor(Method method) {
        return queries.containsKey(method);
    }
}
