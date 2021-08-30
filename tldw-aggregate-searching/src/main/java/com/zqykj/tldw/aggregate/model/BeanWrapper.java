/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.model;

import com.zqykj.tldw.aggregate.index.mapping.PersistentProperty;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <h1> 对索引类对象处理 </h1>
 */
public class BeanWrapper {

    public static <T> Object getProperty(PersistentProperty<?> property, T bean) {

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
}
