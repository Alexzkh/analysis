/**
 * @作者 Mcj
 */
package com.zqykj.repository.core.support;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * <h1> Repository 查询方法调用监听器 </h1>
 */
public interface RepositoryMethodInvocationListener {

    /**
     * <h2> 处理调用事件, 该方法在执行完成后被调用 </h2>
     *
     * @param repositoryMethodInvocation the invocation to respond to.
     */
    void afterInvocation(RepositoryMethodInvocation repositoryMethodInvocation);

    /**
     * <h2> 捕获实际调用的值对象 </h2>
     */
    class RepositoryMethodInvocation {

        private final long durationNs;
        private final Class<?> repositoryInterface;
        private final Method method;
        private final RepositoryMethodInvocationResult result;

        /**
         * @param repositoryInterface the repository interface that was used to call {@link Method}.
         * @param method              the actual method that was called.
         * @param result              the outcome of the invocation. Must not be {@literal null}.
         * @param durationNs          the duration in {@link TimeUnit#NANOSECONDS}.
         */
        public RepositoryMethodInvocation(Class<?> repositoryInterface, Method method,
                                          RepositoryMethodInvocationResult result, long durationNs) {

            this.durationNs = durationNs;
            this.repositoryInterface = repositoryInterface;
            this.method = method;
            this.result = result;
        }

        public long getDuration(TimeUnit timeUnit) {

            Assert.notNull(timeUnit, "TimeUnit must not be null");

            return timeUnit.convert(durationNs, TimeUnit.NANOSECONDS);
        }

        public Class<?> getRepositoryInterface() {
            return repositoryInterface;
        }

        public Method getMethod() {
            return method;
        }

        @Nullable
        public RepositoryMethodInvocationResult getResult() {
            return result;
        }

        @Override
        public String toString() {

            return String.format("Invocation %s.%s(%s): %s ms - %s", repositoryInterface.getSimpleName(), method.getName(),
                    StringUtils.arrayToCommaDelimitedString(
                            Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).toArray()),
                    getDuration(TimeUnit.MILLISECONDS), result.getState());
        }
    }

    interface RepositoryMethodInvocationResult {

        State getState();

        @Nullable
        Throwable getError();

        public enum State {
            SUCCESS, ERROR, CANCELED, RUNNING
        }
    }
}
