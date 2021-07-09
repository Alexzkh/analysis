package com.zqykj.tldw.aggregate.searching.esclientrhl.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Class object operation tool class
 **/
public class BeanTools {
    /**
     * It used map coventer Object.
     *
     * @param map:       the source
     * @param beanClass: the target
     * @return: java.lang.Object transform result d
     **/
    public static Object mapToObject(Map map, Class<?> beanClass) throws Exception {
        if (map == null)
            return null;

        Object obj = beanClass.newInstance();

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (map.get(field.getName()) == null || ObjectUtils.isEmpty(map.get(field.getName()))) {
                continue;
            }
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            field.set(obj, map.get(field.getName()));
        }
        return obj;
    }

    /**
     * It used object coventer map.
     *
     * @param obj: the source are object
     * @return: java.util.Map<java.lang.String, java.lang.Object> map that is the transfor result
     **/
    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }

        return map;
    }

    /**
     * it can use get no value property names .
     *
     * @param source: source
     * @return: java.lang.String[]
     **/
    public static String[] getNoValuePropertyNames(Object source) {
        Assert.notNull(source, "The parameter object passed cannot be empty");
        final BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();

        Set<String> noValuePropertySet = new HashSet<>();
        Arrays.stream(pds).forEach(pd -> {
            Object propertyValue = beanWrapper.getPropertyValue(pd.getName());
            if (ObjectUtils.isEmpty(propertyValue)) {
                noValuePropertySet.add(pd.getName());
            } else {
                if (Iterable.class.isAssignableFrom(propertyValue.getClass())) {
                    Iterable iterable = (Iterable) propertyValue;
                    Iterator iterator = iterable.iterator();
                    if (!iterator.hasNext()) noValuePropertySet.add(pd.getName());
                }
                if (Map.class.isAssignableFrom(propertyValue.getClass())) {
                    Map map = (Map) propertyValue;
                    if (map.isEmpty()) noValuePropertySet.add(pd.getName());
                }
            }
        });
        String[] result = new String[noValuePropertySet.size()];
        return noValuePropertySet.toArray(result);
    }
}
