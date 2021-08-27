/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mapping;

import com.zqykj.infrastructure.util.TypeInformation;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Mcj
 */
public interface PersistentProperty<P extends PersistentProperty<P>> {

    PersistentEntity<?, P> getOwner();


    boolean usePropertyAccess();

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

    default Field getRequiredField() {

        Field field = getField();

        if (field == null) {
            throw new IllegalArgumentException(String.format("No field backing persistent property %s!", this));
        }

        return field;
    }

    default Method getRequiredGetter() {

        Method getter = getGetter();

        if (getter == null) {
            throw new IllegalArgumentException(String.format("No getter available for persistent property %s!", this));
        }

        return getter;
    }

    @Nullable
    Method getGetter();

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
