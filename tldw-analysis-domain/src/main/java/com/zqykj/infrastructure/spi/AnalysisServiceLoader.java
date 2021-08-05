package com.zqykj.infrastructure.spi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI Service Loader.
 *
 */
public class AnalysisServiceLoader {
    
    private static final Map<Class<?>, Collection<Class<?>>> SERVICES = new ConcurrentHashMap<Class<?>, Collection<Class<?>>>();
    
    /**
     * Load service.
     *
     * <p>Load service by SPI and cache the classes for reducing cost when load second time.
     *
     * @param service service class
     * @param <T> type of service
     * @return service instances
     */
    public static <T> Collection<T> load(final Class<T> service) {
        if (SERVICES.containsKey(service)) {
            return newServiceInstances(service);
        }
        Collection<T> result = new LinkedHashSet<T>();
        for (T each : ServiceLoader.load(service)) {
            result.add(each);
            cacheServiceClass(service, each);
        }
        return result;
    }
    
    private static <T> void cacheServiceClass(final Class<T> service, final T instance) {
        if (!SERVICES.containsKey(service)) {
            SERVICES.put(service, new LinkedHashSet<Class<?>>());
        }
        SERVICES.get(service).add(instance.getClass());
    }
    
    /**
     * New service instances.
     *
     * @param service service class
     * @param <T> type of service
     * @return service instances
     */
    public static <T> Collection<T> newServiceInstances(final Class<T> service) {
        return SERVICES.containsKey(service) ? newServiceInstancesFromCache(service) : Collections.<T>emptyList();
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Collection<T> newServiceInstancesFromCache(Class<T> service) {
        Collection<T> result = new LinkedHashSet<T>();
        for (Class<?> each : SERVICES.get(service)) {
            result.add((T) newServiceInstance(each));
        }
        return result;
    }
    
    private static Object newServiceInstance(final Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new ServiceLoaderException(clazz, e);
        } catch (InstantiationException e) {
            throw new ServiceLoaderException(clazz, e);
        }
    }
}
