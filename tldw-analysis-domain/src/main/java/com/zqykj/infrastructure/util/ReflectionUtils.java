/**
 * @作者 Mcj
 */
package com.zqykj.infrastructure.util;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

/**
 * <h1> specific reflection utility methods and classes. </h1>
 */
public final class ReflectionUtils {

    /**
     * Finds a constructor on the given type that matches the given constructor arguments.
     *
     * @param type                 must not be {@literal null}.
     * @param constructorArguments must not be {@literal null}.
     * @return a {@link Constructor} that is compatible with the given arguments.
     */
    public static Optional<Constructor<?>> findConstructor(Class<?> type, Object... constructorArguments) {

        Assert.notNull(type, "Target type must not be null!");
        Assert.notNull(constructorArguments, "Constructor arguments must not be null!");

        return Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> argumentsMatch(constructor.getParameterTypes(), constructorArguments))
                .findFirst();
    }

    private static boolean argumentsMatch(Class<?>[] parameterTypes, Object[] arguments) {

        if (parameterTypes.length != arguments.length) {
            return false;
        }

        int index = 0;
        for (Class<?> argumentType : parameterTypes) {

            Object argument = arguments[index];
            // 这里排除原始类型(Java 9种) 与 参数值为空的情况下 argument
            if (argumentType.isPrimitive() && null == argument) {
                return false;
            }

            // Type check if argument is not null, 且类型是否匹配
            if (null != argument && ClassUtils.isAssignableValue(argumentType, argument)) {
                return false;
            }

            index++;
        }

        return true;
    }
}
