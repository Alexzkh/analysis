/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mapping;

import com.zqykj.infrastructure.util.TypeInformation;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;

public interface PersistentEntity<T, P extends PersistentProperty<P>> extends Iterable<P> {

    String getName();

    /**
     * Returns whether the given {@link PersistentProperty} is the id property of the entity.
     *
     * @param property can be {@literal null}.
     * @return {@literal true} given property is the entities id.
     */
    boolean isIdProperty(PersistentProperty<?> property);


    /**
     * Returns the id property of the {@link PersistentEntity}. Can be {@literal null} in case this is an entity
     * completely handled by a custom conversion.
     *
     * @return the id property of the {@link PersistentEntity}.
     */
    @Nullable
    P getIdProperty();

    /**
     * Returns the id property of the {@link PersistentEntity}.
     *
     * @return the id property of the {@link PersistentEntity}.
     * @throws IllegalStateException if {@link PersistentEntity} does not define an {@literal id} property.
     * @since 2.0
     */
    default P getRequiredIdProperty() {

        P property = getIdProperty();

        if (property != null) {
            return property;
        }

        throw new IllegalStateException(String.format("Required identifier property not found for %s!", getType()));
    }

    /**
     * Obtains a {@link PersistentProperty} instance by name.
     *
     * @param name The name of the property. Can be {@literal null}.
     * @return the {@link PersistentProperty} or {@literal null} if it doesn't exist.
     */
    @Nullable
    P getPersistentProperty(String name);

    void doWithProperties(PropertyHandler<P> handler);

    /**
     * Returns the {@link PersistentProperty} with the given name.
     *
     * @param name the name of the property. Can be {@literal null} or empty.
     * @return the {@link PersistentProperty} with the given name.
     * @throws IllegalStateException in case no property with the given name exists.
     */
    default P getRequiredPersistentProperty(String name) {

        P property = getPersistentProperty(name);

        if (property != null) {
            return property;
        }

        throw new IllegalStateException(String.format("Required property %s not found for %s!", name, getType()));
    }


    /**
     * Returns whether the {@link PersistentEntity} has an id property. If this call returns {@literal true},
     * {@link #getIdProperty()} will return a non-{@literal null} value.
     *
     * @return {@literal true} if entity has an {@literal id} property.
     */
    boolean hasIdProperty();

    /**
     * Returns the resolved Java type of this entity.
     *
     * @return The underlying Java class for this entity. Never {@literal null}.
     */
    Class<T> getType();


    /**
     * Returns the {@link TypeInformation} backing this {@link PersistentEntity}.
     *
     * @return
     */
    TypeInformation<T> getTypeInformation();

    /**
     * Looks up the annotation of the given type on the {@link PersistentEntity}.
     *
     * @param annotationType must not be {@literal null}.
     * @return {@literal null} if not found.
     * @since 1.8
     */
    @Nullable
    <A extends Annotation> A findAnnotation(Class<A> annotationType);

    /**
     * Returns the required annotation of the given type on the {@link PersistentEntity}.
     *
     * @param annotationType must not be {@literal null}.
     * @return the annotation.
     * @throws IllegalStateException if the required {@code annotationType} is not found.
     * @since 2.0
     */
    default <A extends Annotation> A getRequiredAnnotation(Class<A> annotationType) throws IllegalStateException {

        A annotation = findAnnotation(annotationType);

        if (annotation != null) {
            return annotation;
        }

        throw new IllegalStateException(
                String.format("Required annotation %s not found for %s!", annotationType, getType()));
    }

    /**
     * Checks whether the annotation of the given type is present on the {@link PersistentEntity}.
     *
     * @param annotationType must not be {@literal null}.
     * @return {@literal true} if {@link Annotation} of given type is present.
     * @since 2.0
     */
    <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationType);
}
