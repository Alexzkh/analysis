/**
 * @作者 Mcj
 */
package com.zqykj.util;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils.FieldFilter;

/**
 * <h1> specific reflection utility methods and classes. </h1>
 */
public final class ReflectionUtils {

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentReferenceHashMap<>(256);
    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

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

    /**
     * A {@link FieldFilter} that has a description.
     */
    public interface DescribedFieldFilter extends FieldFilter {

        /**
         * Returns the description of the field filter. Used in exceptions being thrown in case uniqueness shall be enforced
         * on the field filter.
         *
         * @return
         */
        String getDescription();
    }

    /**
     * A {@link FieldFilter} for a given annotation.
     */
    public static class AnnotationFieldFilter implements DescribedFieldFilter {

        private final Class<? extends Annotation> annotationType;

        public AnnotationFieldFilter(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        public boolean matches(Field field) {
            return AnnotationUtils.getAnnotation(field, annotationType) != null;
        }

        public String getDescription() {
            return String.format("Annotation filter for %s", annotationType.getName());
        }
    }

    @Nullable
    public static Field findField(Class<?> type, FieldFilter filter) {

        return findField(type, new DescribedFieldFilter() {

            public boolean matches(Field field) {
                return filter.matches(field);
            }

            public String getDescription() {
                return String.format("FieldFilter %s", filter.toString());
            }
        }, false);
    }

    public static void setField(Field field, Object target, @Nullable Object value) {

        org.springframework.util.ReflectionUtils.makeAccessible(field);
        org.springframework.util.ReflectionUtils.setField(field, target, value);
    }

    /**
     * Finds the field matching the given {@link DescribedFieldFilter}. Will make sure there's only one field matching the
     * filter in case {@code enforceUniqueness} is {@literal true}.
     *
     * @param type              must not be {@literal null}.
     * @param filter            must not be {@literal null}.
     * @param enforceUniqueness whether to enforce uniqueness of the field
     * @return the field matching the given {@link DescribedFieldFilter} or {@literal null} if none found.
     * @throws IllegalStateException if enforceUniqueness is true and more than one matching field is found
     */
    @Nullable
    public static Field findField(Class<?> type, DescribedFieldFilter filter, boolean enforceUniqueness) {

        Assert.notNull(type, "Type must not be null!");
        Assert.notNull(filter, "Filter must not be null!");

        Class<?> targetClass = type;
        Field foundField = null;

        while (targetClass != Object.class) {

            for (Field field : targetClass.getDeclaredFields()) {

                if (!filter.matches(field)) {
                    continue;
                }

                if (!enforceUniqueness) {
                    return field;
                }

                if (foundField != null && enforceUniqueness) {
                    throw new IllegalStateException(filter.getDescription());
                }

                foundField = field;
            }

            targetClass = targetClass.getSuperclass();
        }

        return foundField;
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

    @SuppressWarnings("unchecked")
    public static <R> R getTargetInstanceViaReflection(Optional<Constructor<?>> constructor, Class<?> baseClass, Object... constructorArguments) {
        return constructor.map(it -> (R) BeanUtils.instantiateClass(it, constructorArguments))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "No suitable constructor found on %s to match the given arguments: %s. Make sure you implement a constructor taking these",
                        baseClass, Arrays.stream(constructorArguments).map(Object::getClass).collect(Collectors.toList()))));
    }

    @SuppressWarnings("unchecked")
    public static <R> R getTargetInstanceViaReflection(Class<?> baseClass, Object... constructorArguments) {
        Optional<Constructor<?>> constructor = findConstructor(baseClass, constructorArguments);
        return constructor.map(it -> (R) BeanUtils.instantiateClass(it, constructorArguments))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "No suitable constructor found on %s to match the given arguments: %s. Make sure you implement a constructor taking these",
                        baseClass, Arrays.stream(constructorArguments).map(Object::getClass).collect(Collectors.toList()))));
    }

    public static Optional<Method> findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, EMPTY_CLASS_ARRAY);
    }

    public static Optional<Method> findMethod(Class<?> clazz, String name, @Nullable Class<?>... paramTypes) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(name, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() :
                    getDeclaredMethods(searchType, false));
            for (Method method : methods) {
                if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
                    return Optional.of(method);
                }
            }
            searchType = searchType.getSuperclass();
        }
        return Optional.empty();
    }

    private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
        Assert.notNull(clazz, "Class must not be null");
        Method[] result = declaredMethodsCache.get(clazz);
        if (result == null) {
            try {
                Method[] declaredMethods = clazz.getDeclaredMethods();
                List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
                if (defaultMethods != null) {
                    result = new Method[declaredMethods.length + defaultMethods.size()];
                    System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                    int index = declaredMethods.length;
                    for (Method defaultMethod : defaultMethods) {
                        result[index] = defaultMethod;
                        index++;
                    }
                } else {
                    result = declaredMethods;
                }
                declaredMethodsCache.put(clazz, (result.length == 0 ? EMPTY_METHOD_ARRAY : result));
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                        "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
            }
        }
        return (result.length == 0 || !defensive) ? result : result.clone();
    }

    @Nullable
    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }


    public static Optional<Class<?>> findParameterType(Class<?> clazz, String name) throws ClassNotFoundException {

        Field requiredField = ReflectionUtils.findRequiredField(clazz, name);

        Type type = requiredField.getAnnotatedType().getType();
        if (type instanceof ParameterizedType) {

            Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];

            return Optional.of(Class.forName(actualTypeArgument.getTypeName()));
        }

        return Optional.empty();
    }
}
