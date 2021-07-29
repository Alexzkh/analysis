package com.zqykj.infrastructure.serialize;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName Serializer
 */
public interface Serializer {


    Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>(8);

    /**
     * Deserialize the data.
     *
     * @param data byte[]
     * @param <T>  class type
     * @return target object instance
     */
    <T> T deserialize(byte[] data);

    /**
     * Deserialize the data.
     *
     * @param data byte[]
     * @param cls  class
     * @param <T>  class type
     * @return target object instance
     */
    <T> T deserialize(byte[] data, Class<T> cls);

    /**
     * Deserialize the data.
     *
     * @param data byte[]
     * @param type data type
     * @param <T>  class type
     * @return target object instance
     */
    <T> T deserialize(byte[] data, Type type);

    /**
     * Deserialize the data.
     *
     * @param data          byte[]
     * @param classFullName class full name
     * @param <T>           class type
     * @return target object instance
     */
    default <T> T deserialize(byte[] data, String classFullName) {
        try {
            Class<?> cls;
            CLASS_CACHE.computeIfAbsent(classFullName, name -> {
                try {
                    return Class.forName(classFullName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            cls = CLASS_CACHE.get(classFullName);
            return (T) deserialize(data, cls);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Serialize the object.
     *
     * @param obj target obj
     * @return byte[]
     */
    <T> byte[] serialize(T obj);

    /**
     * The name of the serializer implementer.
     *
     * @return name
     */
    String name();


}
