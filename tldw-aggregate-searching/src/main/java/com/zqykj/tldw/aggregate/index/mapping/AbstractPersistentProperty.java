/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mapping;

import com.zqykj.infrastructure.util.*;
import com.zqykj.annotations.Id;
import com.zqykj.tldw.aggregate.index.model.Property;
import com.zqykj.tldw.aggregate.index.model.SimpleTypeHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * <h1> abstract persistent property describe  </h1>
 */
@Slf4j
public abstract class AbstractPersistentProperty<P extends PersistentProperty<P>> implements PersistentProperty<P> {

    private static final Field CAUSE_FIELD;

    static {
        CAUSE_FIELD = ReflectionUtils.findRequiredField(Throwable.class, "cause");
    }

    private final String name;
    private final TypeInformation<?> information;
    private final Class<?> rawType;
    private final PersistentEntity<?, P> owner;
    private final Map<Class<? extends Annotation>, Optional<? extends Annotation>> annotationCache = new ConcurrentHashMap<>();
    private final Lazy<Optional<? extends TypeInformation<?>>> entityTypeInformation;

    @SuppressWarnings("null") //
    private final Property property;
    private final Field field;
    private final Method getter;
    private final Method setter;
    private final Lazy<Boolean> usePropertyAccess;
    private final Lazy<Boolean> isId = Lazy.of(() -> isAnnotationPresent(Id.class));

    public AbstractPersistentProperty(Property property, PersistentEntity<?, P> owner,
                                      SimpleTypeHolder simpleTypeHolder) {

        Assert.notNull(simpleTypeHolder, "SimpleTypeHolder must not be null!");
        Assert.notNull(owner, "Owner entity must not be null!");
        populateAnnotationCache(property);

        this.name = property.getName();
        this.information = owner.getTypeInformation().getRequiredProperty(getName());
        this.rawType = this.information.getType();
        this.property = property;
        this.owner = owner;

        this.usePropertyAccess = Lazy.of(() -> owner.getType().isInterface() || CAUSE_FIELD.equals(getField()));
        this.entityTypeInformation = Lazy.of(() -> Optional.ofNullable(information.getActualType())
                .filter(it -> !simpleTypeHolder.isSimpleType(it.getType()))
                .filter(it -> !it.isCollectionLike())
                .filter(it -> !it.isMap()));
        this.getter = property.getGetter().orElse(null);
        this.setter = property.getSetter().orElse(null);
        this.field = property.getField().orElse(null);
    }

    @Override
    public PersistentEntity<?, P> getOwner() {
        return this.owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return information.getType();
    }

    @Override
    public Class<?> getRawType() {
        return this.rawType;
    }

    @Override
    public TypeInformation<?> getTypeInformation() {
        return information;
    }

    @Override
    @Nullable
    public Field getField() {
        return this.field;
    }

    @SuppressWarnings("null")
    protected Property getProperty() {
        return this.property;
    }

    public String getFieldName() {
        return getProperty().getName();
    }

    @Nullable
    protected Class<?> getActualTypeOrNull() {
        try {
            return getActualType();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Class<?> getActualType() {
        return information.getRequiredActualType().getType();
    }

    @Override
    @Nullable
    public <A extends Annotation> A findAnnotation(Class<A> annotationType) {

        Assert.notNull(annotationType, "Annotation type must not be null!");

        return doFindAnnotation(annotationType).orElse(null);
    }

    public boolean usePropertyAccess() {
        return usePropertyAccess.get();
    }

    @Nullable
    @Override
    public <A extends Annotation> A findPropertyOrOwnerAnnotation(Class<A> annotationType) {

        A annotation = findAnnotation(annotationType);

        return annotation != null ? annotation : getOwner().findAnnotation(annotationType);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return doFindAnnotation(annotationType).isPresent();
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> Optional<A> doFindAnnotation(Class<A> annotationType) {

        Optional<? extends Annotation> annotation = annotationCache.getOrDefault(annotationType, Optional.empty());

        if (annotation.isPresent()) {
            return (Optional<A>) annotation;
        }

        return (Optional<A>) annotationCache.computeIfAbsent(annotationType, type -> getAccessors()
                .map(it -> AnnotatedElementUtils.findMergedAnnotation(it, type))
                .flatMap(StreamUtils::fromNullable)
                .findFirst());
    }

    private void populateAnnotationCache(Property property) {

        Optionals.toStream(property.getGetter(), property.getSetter()).forEach(it -> {

            for (Annotation annotation : it.getAnnotations()) {

                Class<? extends Annotation> annotationType = annotation.annotationType();

                //TODO 需要校验property 上的注解是否 是我们自定义的注解

                annotationCache.put(annotationType,
                        Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(it, annotationType)));
            }
        });

        property.getField().ifPresent(it -> {

            for (Annotation annotation : it.getAnnotations()) {

                Class<? extends Annotation> annotationType = annotation.annotationType();

                //TODO 需要校验property 上的注解是否 是我们自定义的注解

                annotationCache.put(annotationType,
                        Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(it, annotationType)));
            }
        });
    }

    public Method getGetter() {
        return this.getter;
    }

    public Method getSetter() {
        return this.setter;
    }

    private Stream<? extends AnnotatedElement> getAccessors() {

        return Optionals.toStream(Optional.ofNullable(getGetter()), Optional.ofNullable(getSetter()),
                Optional.ofNullable(getField()));
    }

    @Nullable
    protected String getAnnotatedFieldName() {

        com.zqykj.annotations.Field annotation = findAnnotation(
                com.zqykj.annotations.Field.class);

        return annotation != null ? annotation.value() : null;
    }

    @Override
    public Iterable<? extends TypeInformation<?>> getPersistentEntityTypes() {

        if (!isEntity()) {
            return Collections.emptySet();
        }

        return entityTypeInformation.get()//
                .map(Collections::singleton)//
                .orElseGet(Collections::emptySet);
    }

    @Override
    public boolean isEntity() {
        return entityTypeInformation.get().isPresent();
    }

    @Override
    public boolean isIdProperty() {
        return isId.get();
    }
}
