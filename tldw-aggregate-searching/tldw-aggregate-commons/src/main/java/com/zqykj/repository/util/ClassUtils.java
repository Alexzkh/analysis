/**
 * @作者 Mcj
 */
package com.zqykj.repository.util;

import com.zqykj.repository.Repository;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ClassUtils {

    /**
     * <h2> 方法参数中是否 有给定的 类型 type </h2>
     */
    public static boolean hasParameterOfType(Method method, Class<?> type) {
        return Arrays.asList(method.getParameterTypes()).contains(type);
    }

    /**
     * <h2> Returns whether the given type name is a repository interface name. </h2>
     *
     * @param interfaceName 接口名称
     */
    public static boolean isGenericRepositoryInterface(@Nullable String interfaceName) {
        return Repository.class.getName().equals(interfaceName);
    }
}
