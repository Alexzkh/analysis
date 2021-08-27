/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mapping;

import com.zqykj.domain.routing.Routing;
import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentProperty;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.MultiValueMap;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> Universal Base persistent entity describe </h1>
 */
public class BasicPersistentEntity<T, P extends PersistentProperty<P>> implements PersistentEntity<T, P> {

    private final TypeInformation<T> information;
    private final List<P> properties;
    private final List<P> persistentPropertiesCache;
    private final Map<String, P> propertyCache;
    private final MultiValueMap<Class<? extends Annotation>, P> propertyAnnotationCache;
    @Nullable
    private P idProperty;
    @Nullable
    private P versionProperty;
    @Nullable
    private final Map<Class<? extends Annotation>, Optional<Annotation>> annotationCache;
    @Nullable
    private SimpleElasticSearchPersistentProperty routingFieldProperty;

    public BasicPersistentEntity(TypeInformation<T> information) {
        this(information, null);
    }

    public BasicPersistentEntity(TypeInformation<T> information, @Nullable Comparator<P> comparator) {

        Assert.notNull(information, "Information must not be null!");
        this.information = information;
        this.properties = new ArrayList<>();
        this.persistentPropertiesCache = new ArrayList<>();
        this.propertyCache = new HashMap<>(16, 1f);
        this.annotationCache = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);
        this.propertyAnnotationCache = CollectionUtils
                .toMultiValueMap(new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK));
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Nullable
    public SimpleElasticSearchPersistentProperty getRoutingFieldProperty() {
        return routingFieldProperty;
    }

    @Override
    public boolean isIdProperty(PersistentProperty<?> property) {
        return idProperty != null && idProperty.equals(property);
    }

    @Nullable
    @Override
    public P getIdProperty() {
        return idProperty;
    }

    @Override
    public P getPersistentProperty(String name) {
        return propertyCache.get(name);
    }

    @Override
    public boolean isPropertyPresent(String name) {
        return propertyCache.containsKey(name);
    }

    @Override
    public void doWithProperties(PropertyHandler<P> handler) {

        Assert.notNull(handler, "PropertyHandler must not be null!");

        for (P property : persistentPropertiesCache) {
            handler.doWithPersistentProperty(property);
        }
    }

    @Override
    public boolean hasIdProperty() {
        return idProperty != null;
    }

    @Override
    public Class<T> getType() {
        return information.getType();
    }

    @Override
    public TypeInformation<T> getTypeInformation() {
        return information;
    }


    @Override
    public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
        return doFindAnnotation(annotationType).orElse(null);
    }

    @Override
    public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationType) {
        return doFindAnnotation(annotationType).isPresent();
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> Optional<A> doFindAnnotation(Class<A> annotationType) {

        return (Optional<A>) annotationCache.computeIfAbsent(annotationType,
                it -> Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(getType(), it)));
    }

    @Override
    public Iterator<P> iterator() {
        Iterator<P> iterator = properties.iterator();

        return new Iterator<P>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public P next() {
                return iterator.next();
            }
        };
    }

    public void addPersistentProperty(P property) {

        Assert.notNull(property, "Property must not be null!");

        if (properties.contains(property)) {
            return;
        }

        properties.add(property);

        propertyCache.putIfAbsent(property.getName(), property);

        P candidate = returnPropertyIfBetterIdPropertyCandidateOrNull(property);

        if (candidate != null) {
            this.idProperty = candidate;
        }

        if (!property.isTransient()) {
            persistentPropertiesCache.add(property);
        }
//        if (property.isVersionProperty()) {
//            P versionProperty = this.versionProperty;
//            if (versionProperty != null) {
//                throw new RuntimeException(
//                        String.format(
//                                "Attempt to add version property %s but already have property %s registered "
//                                        + "as version. Check your mapping configuration!",
//                                property.getField(), versionProperty.getField()));
//            }
//            this.versionProperty = property;
//        }
        Class<?> actualType = this.getActualTypeOrNull(property);
        if (actualType == Routing.class) {
            SimpleElasticSearchPersistentProperty joinProperty = this.routingFieldProperty;

            if (joinProperty != null) {
                throw new RuntimeException(
                        String.format(
                                "Attempt to add Join property %s but already have property %s registered "
                                        + "as Join property. Check your entity configuration!",
                                property.getField(), joinProperty.getField()));
            }
            this.routingFieldProperty = (SimpleElasticSearchPersistentProperty) property;
        }
    }

    protected Class<?> getActualTypeOrNull(P property) {
        try {
            return property.getActualType();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <h2> 根据注解类型查找符合的Property </h2>
     */
    public List<P> getPersistentProperties(Class<? extends Annotation> annotationType) {

        return properties.stream()
                .filter(it -> it.isAnnotationPresent(annotationType))
                .collect(Collectors.toList());
    }

    @Nullable
    protected P returnPropertyIfBetterIdPropertyCandidateOrNull(P property) {

        if (!property.isIdProperty()) {
            return null;
        }

        P idProperty = this.idProperty;

        if (idProperty != null) {
            throw new RuntimeException(String.format("Attempt to add id property %s but already have property %s registered "
                    + "as id. Check your mapping configuration!", property.getField(), idProperty.getField()));
        }

        return property;
    }
}
