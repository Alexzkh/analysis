/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mapping;

import com.zqykj.infrastructure.util.TypeInformation;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author Mcj
 */
public interface PersistentProperty<P extends PersistentProperty<P>> {

    PersistentEntity<?, P> getOwner();

    /**
     * The name of the property
     *
     * @return The property name
     */
    String getName();

    /**
     * The type of the property
     *
     * @return The property type
     */
    Class<?> getType();

    /**
     * Returns the {@link TypeInformation} of the property.
     *
     * @return
     */
    TypeInformation<?> getTypeInformation();

    @Nullable
    Field getField();

    Class<?> getRawType();

    Class<?> getActualType();

    @Nullable
    <A extends Annotation> A findAnnotation(Class<A> annotationType);

    @Nullable
    <A extends Annotation> A findPropertyOrOwnerAnnotation(Class<A> annotationType);

    boolean isAnnotationPresent(Class<? extends Annotation> annotationType);

    Iterable<? extends TypeInformation<?>> getPersistentEntityTypes();

    boolean isEntity();

    /**
     * Returns whether the property is transient.
     *
     * @return
     */
    default boolean isTransient() {
        return false;
    }

    /**
     * Returns whether the property is a <em>potential</em> identifier property of the owning {@link PersistentEntity}.
     * This method is mainly used by {@link PersistentEntity} implementation to discover id property candidates on
     * {@link PersistentEntity} creation you should rather call {@link PersistentEntity#(PersistentProperty)}
     * to determine whether the current property is the id property of that {@link PersistentEntity} under consideration.
     *
     * @return {@literal true} if the {@literal id} property.
     */
    boolean isIdProperty();
}
