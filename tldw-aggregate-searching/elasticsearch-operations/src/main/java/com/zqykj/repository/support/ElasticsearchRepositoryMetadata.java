/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.core.SearchHit;
import com.zqykj.repository.core.support.DefaultRepositoryMetadata;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * <h1> Elasticsearch Repository Metadata </h1>
 */
public class ElasticsearchRepositoryMetadata extends DefaultRepositoryMetadata {

    public ElasticsearchRepositoryMetadata(Class<?> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    public Class<?> getReturnedDomainClass(Method method) {

        Class<?> returnedDomainClass = super.getReturnedDomainClass(method);
        if (SearchHit.class.isAssignableFrom(returnedDomainClass)) {
            try {
                // dealing with Collection<SearchHit<T>> or Flux<SearchHit<T>>, getting to T
                ParameterizedType methodGenericReturnType = ((ParameterizedType) method.getGenericReturnType());
                if (isAllowedGenericType(methodGenericReturnType)) {

                    // 获取参数化类型 (eg. List<String> 中的String)
                    ParameterizedType collectionTypeArgument = (ParameterizedType) methodGenericReturnType
                            .getActualTypeArguments()[0];
                    if (SearchHit.class.isAssignableFrom((Class<?>) collectionTypeArgument.getRawType())) {
                        returnedDomainClass = (Class<?>) collectionTypeArgument.getActualTypeArguments()[0];
                    }
                }
            } catch (Exception ignored) {
                // 极少数情况下会出现参数化类型转换异常(此处可忽略)
            }
        }
        return returnedDomainClass;
    }

    /**
     * <h2> 是否是允许的通用类型 </h2>
     * <p> rawType : 原始类型 </p>
     */
    protected boolean isAllowedGenericType(ParameterizedType methodGenericReturnType) {
        return Collection.class.isAssignableFrom((Class<?>) methodGenericReturnType.getRawType())
                || Stream.class.isAssignableFrom((Class<?>) methodGenericReturnType.getRawType());
    }
}
