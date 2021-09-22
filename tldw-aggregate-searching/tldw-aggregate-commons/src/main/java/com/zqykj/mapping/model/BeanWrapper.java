/**
 * @作者 Mcj
 */
package com.zqykj.mapping.model;

import com.zqykj.mapping.PersistentProperty;
import com.zqykj.mapping.PersistentPropertyAccessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <h1> Domain service to allow accessing the values of {@link PersistentProperty}s on a given bean. </h1>
 */
public class BeanWrapper<T> implements PersistentPropertyAccessor<T> {

    private T bean;
    private ConversionService conversionService;

    /**
     * Creates a new {@link BeanWrapper} for the given bean.
     *
     * @param bean must not be {@literal null}.
     */
    protected BeanWrapper(T bean) {

        Assert.notNull(bean, "Bean must not be null!");
        this.bean = bean;
    }

    /**
     * Creates a new {@link BeanWrapper} for the given bean.
     *
     * @param bean              must not be {@literal null}.
     * @param conversionService must not be {@literal null}.
     */
    protected BeanWrapper(T bean, ConversionService conversionService) {

        Assert.notNull(bean, "Bean must not be null!");
        this.bean = bean;
        this.conversionService = conversionService;
    }

    @Nullable
    public Object getProperty(PersistentProperty<?> property) {
        return getProperty(property, property.getType());
    }

    @Nullable
    public <S> Object getProperty(PersistentProperty<?> property, Class<? extends S> type) {

        Assert.notNull(property, "PersistentProperty must not be null!");

        try {

            if (!property.usePropertyAccess()) {

                Field field = property.getRequiredField();

                ReflectionUtils.makeAccessible(field);
                return ReflectionUtils.getField(field, bean);
            }

            Method getter = property.getRequiredGetter();

            ReflectionUtils.makeAccessible(getter);
            return ReflectionUtils.invokeMethod(getter, bean);

        } catch (IllegalStateException e) {
            throw new RuntimeException(
                    String.format("Could not read property %s of %s!", property.toString(), bean.toString()), e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <S> S convertIfNecessary(@Nullable Object source, Class<S> type) {

        return (S) (source == null //
                ? null //
                : type.isAssignableFrom(source.getClass()) //
                ? source //
                : conversionService.convert(source, type));
    }

    public T getBean() {
        return bean;
    }
}
