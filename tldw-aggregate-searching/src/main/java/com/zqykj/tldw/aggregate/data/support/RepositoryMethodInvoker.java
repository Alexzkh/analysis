/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.support;

import com.zqykj.tldw.aggregate.data.query.AbstractAggregateRepositoryQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * <h1> @Query method 方法调用 </h1>
 */
@Slf4j
abstract class RepositoryMethodInvoker {

    private final Method method;
    private final Class<?> returnedType;
    private final Invokable invokable;

    protected RepositoryMethodInvoker(Method method, Invokable invokable) {

        this.method = method;
        this.invokable = invokable;
        this.returnedType = method.getReturnType();
    }

    /**
     * Implementation to invoke query methods.
     */
    private static class RepositoryQueryMethodInvoker extends RepositoryMethodInvoker {
        public RepositoryQueryMethodInvoker(Method method, AbstractAggregateRepositoryQuery repositoryQuery) {
            super(method, repositoryQuery::execute);
        }
    }

    static RepositoryQueryMethodInvoker forRepositoryQuery(Method declaredMethod, AbstractAggregateRepositoryQuery query) {
        return new RepositoryQueryMethodInvoker(declaredMethod, query);
    }

    /**
     * <h2>Invoke the repository method and return its value.</h2>
     *
     * @param args invocation arguments.
     */
    @Nullable
    public Object invoke(Class<?> repositoryInterface, Object[] args)
            throws Exception {
        return doInvoke(repositoryInterface, args);
    }

    @Nullable
    private Object doInvoke(Class<?> repositoryInterface, Object[] args)
            throws Exception {
        try {
            return invokable.invoke(args);
        } catch (Exception e) {
            log.error("handle @Query error , msg = {}", e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    interface Invokable {

        @Nullable
        Object invoke(Object[] args) throws ReflectiveOperationException;
    }
}
