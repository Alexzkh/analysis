/**
 * @作者 Mcj
 */
package com.zqykj.util;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            if (null != argument && !ClassUtils.isAssignableValue(argumentType, argument)) {
                return false;
            }
            index++;
        }

        return true;
    }

    public static Field findRequiredField(Class<?> type, String name) {

        Field result = org.springframework.util.ReflectionUtils.findField(type, name);

        if (result == null) {
            throw new IllegalArgumentException(String.format("Unable to find field %s on %s!", name, type));
        }

        return result;
    }

    public static void setField(Field field, Object target, @Nullable Object value) {

        org.springframework.util.ReflectionUtils.makeAccessible(field);
        org.springframework.util.ReflectionUtils.setField(field, target, value);
    }

    public static Method findRequiredMethod(Class<?> type, String name, Class<?>... parameterTypes) {

        Assert.notNull(type, "Class must not be null");
        Assert.notNull(name, "Method name must not be null");

        Method result = null;
        Class<?> searchType = type;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods()
                    : org.springframework.util.ReflectionUtils.getDeclaredMethods(searchType));
            for (Method method : methods) {
                if (name.equals(method.getName()) && hasSameParams(method, parameterTypes)) {
                    if (result == null || result.isSynthetic() || result.isBridge()) {
                        result = method;
                    }
                }
            }
            searchType = searchType.getSuperclass();
        }

        if (result == null) {

            String parameterTypeNames = Arrays.stream(parameterTypes) //
                    .map(Object::toString) //
                    .collect(Collectors.joining(", "));

            throw new IllegalArgumentException(
                    String.format("Unable to find method %s(%s)on %s!", name, parameterTypeNames, type));
        }

        return result;
    }

    private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
        return (paramTypes.length == method.getParameterCount() && Arrays.equals(paramTypes, method.getParameterTypes()));
    }

    public static Stream<Class<?>> returnTypeAndParameters(Method method) {

        Assert.notNull(method, "Method must not be null!");

        Stream<Class<?>> returnType = Stream.of(method.getReturnType());
        Stream<Class<?>> parameterTypes = Arrays.stream(method.getParameterTypes());

        return Stream.concat(returnType, parameterTypes);
    }


}
