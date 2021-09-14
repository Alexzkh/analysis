package com.zqykj.infrastructure.util;

import com.zqykj.annotations.Id;
import com.zqykj.constant.Constants;
import org.springframework.util.ObjectUtils;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * description: 工具类
 **/
public class Tools {
    /**
     * Gets the field value of the ID according to the annotation in the object
     *
     * @param obj
     * @return
     */
    public static String getESId(Object obj) throws Exception {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            Id esid = f.getAnnotation(Id.class);
            if (esid != null) {
                Object value = f.get(obj);
                if (value == null) {
                    return null;
                } else {
                    return value.toString();
                }
            }
        }
        return null;
    }

    /**
     * Get the map combination with values for all fields in o
     */
    public static Map getFieldValue(Object o) throws IllegalAccessException {
        Map retMap = new HashMap();
        Field[] fs = o.getClass().getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            f.setAccessible(true);
            if (f.get(o) != null) {
                retMap.put(f.getName(), f.get(o));
            }
        }
        return retMap;
    }

    /**
     * Get the type of the generic parameter of the parent class declared when defining class by reflection
     *
     * @param clazz The class to introspect
     * @return the first generic declaration, or <code>Object.class</code> if cannot be determined
     */
    public static Class getSuperClassGenricType(Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * Get the type of the generic parameter of the parent class declared when defining class by reflection
     * for example public BookManager extends GenricManager<Book>
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     */
    public static Class getSuperClassGenricType(Class clazz, int index)
            throws IndexOutOfBoundsException {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }


    /**
     * judge the array is NULL or not
     *
     * @param objs: array
     * @return: boolean
     **/
    public static boolean arrayISNULL(Object[] objs) {
        if (objs == null || objs.length == 0) {
            return true;
        }
        boolean flag = false;
        for (int i = 0; i < objs.length; i++) {
            if (!ObjectUtils.isEmpty(objs[i])) {
                flag = true;
            }
        }
        if (flag) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * spilt list
     *
     * @param oriList:    original list
     * @param isParallel: parallel or not
     * @return: java.util.List<java.util.List < T>>
     **/
    public static <T> List<List<T>> splitList(List<T> oriList, boolean isParallel) {
        if (oriList.size() <= Constants.BULK_COUNT) {
            List<List<T>> splitList = new ArrayList<>();
            splitList.add(oriList);
            return splitList;
        }
        int limit = (oriList.size() + Constants.BULK_COUNT - 1) / Constants.BULK_COUNT;
        if (isParallel) {
            return Stream.iterate(0, n -> n + 1).limit(limit).parallel().map(a -> oriList.stream().skip(a * Constants.BULK_COUNT).limit(Constants.BULK_COUNT).parallel().collect(Collectors.toList())).collect(Collectors.toList());
        } else {
            final List<List<T>> splitList = new ArrayList<>();
            Stream.iterate(0, n -> n + 1).limit(limit).forEach(i -> {
                splitList.add(oriList.stream().skip(i * Constants.BULK_COUNT).limit(Constants.BULK_COUNT).collect(Collectors.toList()));
            });
            return splitList;
        }
    }

//    /**
//     * Determine whether the current class contains the nested field
//     */
//    private static Map<Class, Boolean> checkNested = new HashMap<>();
//
//    /**
//     * check the obj nested
//     *
//     * @param list: check list
//     * @return: boolean
//     **/
//    public static boolean checkNested(List list) {
//        if (list == null || list.size() == 0) {
//            return false;
//        }
//        return checkNested(list.get(0));
//    }

//    /**
//     * check the obj nested
//     *
//     * @param obj: check obj
//     * @return: boolean
//     **/
//    public static boolean checkNested(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (checkNested.containsKey(obj.getClass())) {
//            return checkNested.get(obj.getClass());
//        } else {
//            for (int i = 0; i < obj.getClass().getDeclaredFields().length; i++) {
//                Field f = obj.getClass().getDeclaredFields()[i];
//                if (f.getAnnotation(ESMapping.class) != null
//                        && (f.getAnnotation(ESMapping.class).datatype() == DataType.nested_type
//                        || (f.getAnnotation(ESMapping.class).nested_class() != null && f.getAnnotation(ESMapping.class).nested_class() != Object.class))) {
//                    checkNested.put(obj.getClass(), true);
//                    return true;
//                }
//            }
//            checkNested.put(obj.getClass(), false);
//            return false;
//        }
//    }
}
