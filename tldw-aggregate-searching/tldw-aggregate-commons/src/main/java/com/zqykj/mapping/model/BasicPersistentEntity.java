/**
 * @作者 Mcj
 */
package com.zqykj.mapping.model;

import com.zqykj.mapping.*;
import com.zqykj.util.TypeInformation;
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

    private static final String TYPE_MISMATCH = "Target bean of type %s is not of type of the persistent entity (%s)!";

    private final TypeInformation<T> information;
    private final List<P> properties;
    private final List<P> persistentPropertiesCache;
    private final @Nullable
    Comparator<P> comparator;

    private final Map<String, P> propertyCache;
    private final Map<Class<? extends Annotation>, Optional<Annotation>> annotationCache;
    private final MultiValueMap<Class<? extends Annotation>, P> propertyAnnotationCache;

    private @Nullable
    P idProperty;
    private @Nullable
    P versionProperty;

    public BasicPersistentEntity(TypeInformation<T> information) {
        this(information, null);
    }

    public BasicPersistentEntity(TypeInformation<T> information, @Nullable Comparator<P> comparator) {

        Assert.notNull(information, "Information must not be null!");

        this.information = information;
        this.properties = new ArrayList<>();
        this.persistentPropertiesCache = new ArrayList<>();
        this.comparator = comparator;

        this.propertyCache = new HashMap<>(16, 1f);
        this.annotationCache = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);
        this.propertyAnnotationCache = CollectionUtils
                .toMultiValueMap(new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK));
    }

    @Override
    public boolean isIdProperty(PersistentProperty<?> property) {
        return idProperty != null && idProperty.equals(property);
    }

    public boolean isVersionProperty(PersistentProperty<?> property) {
        return versionProperty != null && versionProperty.equals(property);
    }

    @Override
    public String getName() {
        return getType().getName();
    }


    @Nullable
    @Override
    public P getIdProperty() {
        return idProperty;
    }

    @Nullable
    public P getVersionProperty() {
        return versionProperty;
    }

    /**
     * <h2> 索引类 ID 访问器,通过它可以获取bean 当中的ID值 </h2>
     */
    @Override
    public <B> Object getIdentifierAccessor(B bean) {

        return hasIdProperty() ? new BeanWrapper<>(bean).getProperty(idProperty) : null;
    }

    /**
     * <h2> 属性访问器,通过它可以访问 bean 的 property 值 </h2>
     */
    @Override
    public <B> PersistentPropertyAccessor<B> getPropertyAccessor(B bean) {

        return new BeanWrapper<>(bean);
    }

    @Override
    public boolean hasIdProperty() {
        return idProperty != null;
    }

    public boolean hasVersionProperty() {
        return versionProperty != null;
    }

    public void addPersistentProperty(P property) {

        Assert.notNull(property, "Property must not be null!");

        if (properties.contains(property)) {
            return;
        }

        properties.add(property);

        if (!property.isTransient()) {
            persistentPropertiesCache.add(property);
        }

        propertyCache.computeIfAbsent(property.getName(), key -> property);

        P candidate = returnPropertyIfBetterIdPropertyCandidateOrNull(property);

        if (candidate != null) {
            this.idProperty = candidate;
        }

        if (isVersionProperty(property)) {

            P versionProperty = this.versionProperty;

            if (versionProperty != null) {

                throw new MappingException(
                        String.format(
                                "Attempt to add version property %s but already have property %s registered "
                                        + "as version. Check your mapping configuration!",
                                property.getField(), versionProperty.getField()));
            }

            this.versionProperty = property;
        }
    }

    /**
     * Returns the given property if it is a better candidate for the id property than the current id property.
     *
     * @param property the new id property candidate, will never be {@literal null}.
     * @return the given id property or {@literal null} if the given property is not an id property.
     */
    @Nullable
    protected P returnPropertyIfBetterIdPropertyCandidateOrNull(P property) {

        if (!property.isIdProperty()) {
            return null;
        }

        P idProperty = this.idProperty;

        if (idProperty != null) {
            throw new MappingException(String.format("Attempt to add id property %s but already have property %s registered "
                    + "as id. Check your mapping configuration!", property.getField(), idProperty.getField()));
        }

        return property;
    }

    @Override
    @Nullable
    public P getPersistentProperty(String name) {
        return propertyCache.get(name);
    }

    @Override
    public boolean isPropertyPresent(String name) {
        return propertyCache.containsKey(name);
    }

    /**
     * <h2> 根据注解类型查找符合的Property </h2>
     */
    @Override
    public List<P> getPersistentProperties(Class<? extends Annotation> annotationType) {

        return properties.stream()
                .filter(it -> it.isAnnotationPresent(annotationType))
                .collect(Collectors.toList());
    }

    @Override
    public Class<T> getType() {
        return information.getType();
    }

    @Override
    public TypeInformation<T> getTypeInformation() {
        return information;
    }


    /**
     * <h2> 处理该索引类上的每一个property </h2>
     */
    public void doWithProperties(PropertyHandler<P> handler) {

        Assert.notNull(handler, "PropertyHandler must not be null!");

        for (P property : persistentPropertiesCache) {
            handler.doWithPersistentProperty(property);
        }
    }

    @Nullable
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

    public void verify() {

        if (comparator != null) {
            properties.sort(comparator);
            persistentPropertiesCache.sort(comparator);
        }
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
}
