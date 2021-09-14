/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.annotations.Highlight;
import com.zqykj.annotations.Query;
import com.zqykj.core.SearchHit;
import com.zqykj.core.SearchHits;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.mapping.context.MappingContext;
import com.zqykj.repository.core.RepositoryMetadata;
import com.zqykj.util.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * <h1> ElasticsearchQueryMethod </h1>
 */
public class ElasticsearchQueryMethod extends QueryMethod {

    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    @Nullable
    private ElasticsearchEntityMetadata<?> metadata;
    private final Method method; // private in base class, but needed here as well
    private final Query queryAnnotation;
    private final Highlight highlightAnnotation;
    private final Lazy<HighlightQuery> highlightQueryLazy = Lazy.of(this::createAnnotatedHighlightQuery);

    public ElasticsearchQueryMethod(Method method, RepositoryMetadata repositoryMetadata,
                                    MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {

        super(method, repositoryMetadata);

        Assert.notNull(mappingContext, "MappingContext must not be null!");

        this.method = method;
        this.mappingContext = mappingContext;
        this.queryAnnotation = method.getAnnotation(Query.class);
        this.highlightAnnotation = method.getAnnotation(Highlight.class);
    }


    public boolean hasAnnotatedQuery() {
        return this.queryAnnotation != null;
    }

    /**
     * <h2> 获取@Query 注解的value值 </h2>
     */
    public String getAnnotatedQuery() {
        return (String) AnnotationUtils.getValue(queryAnnotation, "value");
    }

    /**
     * <h2> 是否拥有高亮注解 </h2>
     */
    public boolean hasAnnotatedHighlight() {
        return highlightAnnotation != null;
    }


    public HighlightQuery getAnnotatedHighlightQuery() {

        Assert.isTrue(hasAnnotatedHighlight(), "no Highlight annotation present on " + getName());

        return highlightQueryLazy.get();
    }

    private HighlightQuery createAnnotatedHighlightQuery() {
        return new HighlightQueryBuilder(mappingContext).getHighlightQuery(highlightAnnotation, getDomainClass());
    }

    /**
     * <h2> 获取Elasticsearch Entity Metadata information </h2>
     */
    @Override
    public ElasticsearchEntityMetadata<?> getEntityInformation() {

        if (metadata == null) {

            Class<?> returnedObjectType = getReturnedObjectType();
            Class<?> domainClass = getDomainClass();

            if (ClassUtils.isPrimitiveOrWrapper(returnedObjectType)) {

                this.metadata = new SimpleElasticsearchEntityMetadata<>((Class<Object>) domainClass,
                        mappingContext.getRequiredPersistentEntity(domainClass));

            } else {

                ElasticsearchPersistentEntity<?> returnedEntity = mappingContext.getPersistentEntity(returnedObjectType);
                ElasticsearchPersistentEntity<?> managedEntity = mappingContext.getRequiredPersistentEntity(domainClass);
                returnedEntity = returnedEntity == null || returnedEntity.getType().isInterface() ? managedEntity
                        : returnedEntity;
                ElasticsearchPersistentEntity<?> collectionEntity = domainClass.isAssignableFrom(returnedObjectType)
                        ? returnedEntity
                        : managedEntity;

                this.metadata = new SimpleElasticsearchEntityMetadata<>((Class<Object>) returnedEntity.getType(),
                        collectionEntity);
            }
        }

        return this.metadata;
    }

    protected MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> getMappingContext() {
        return mappingContext;
    }

    /**
     * <h2> 方法返回类型是否是 SearchHit </h2>
     */
    public boolean isSearchHitMethod() {
        Class<?> methodReturnType = method.getReturnType();

        if (SearchHits.class.isAssignableFrom(methodReturnType)) {
            return true;
        }

        try {
            // dealing with Collection<SearchHit<T>>, getting to T
            ParameterizedType methodGenericReturnType = ((ParameterizedType) method.getGenericReturnType());
            if (isAllowedGenericType(methodGenericReturnType)) {
                ParameterizedType collectionTypeArgument = (ParameterizedType) methodGenericReturnType
                        .getActualTypeArguments()[0];
                if (SearchHit.class.isAssignableFrom((Class<?>) collectionTypeArgument.getRawType())) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    /**
     * <h2> 方法返回类型 </h2>
     */
    public Class<?> methodReturnType() {
        return method.getReturnType();
    }

    /**
     * <h2> 是否是通用类型 </h2>
     */
    protected boolean isAllowedGenericType(ParameterizedType methodGenericReturnType) {
        return Collection.class.isAssignableFrom((Class<?>) methodGenericReturnType.getRawType())
                || Stream.class.isAssignableFrom((Class<?>) methodGenericReturnType.getRawType());
    }

    public boolean isNotSearchHitMethod() {
        return !isSearchHitMethod();
    }
}
