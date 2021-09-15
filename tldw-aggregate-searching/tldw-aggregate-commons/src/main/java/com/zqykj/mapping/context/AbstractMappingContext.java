/**
 * @作者 Mcj
 */
package com.zqykj.mapping.context;

import com.zqykj.mapping.MappingException;
import com.zqykj.mapping.PersistentEntity;
import com.zqykj.mapping.PersistentProperty;
import com.zqykj.mapping.model.BasicPersistentEntity;
import com.zqykj.mapping.model.Property;
import com.zqykj.mapping.model.SimpleTypeHolder;
import com.zqykj.util.ClassTypeInformation;
import com.zqykj.util.Optionals;
import com.zqykj.util.Streamable;
import com.zqykj.util.TypeInformation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.KotlinDetector;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * <h1>  索引类扫描后保存的entity和property 抽象映射关系 </h1>
 */
public abstract class AbstractMappingContext<E extends BasicPersistentEntity<?, P>, P extends PersistentProperty<P>>
        implements MappingContext<E, P>, InitializingBean, ApplicationEventPublisherAware {

    private final Optional<E> NONE = Optional.empty();
    private Set<? extends Class<?>> initialEntitySet = new HashSet<>();
    private final Map<TypeInformation<?>, Optional<E>> persistentEntities = new HashMap<>();
    private SimpleTypeHolder simpleTypeHolder = SimpleTypeHolder.DEFAULT;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock read = lock.readLock();
    private final Lock write = lock.writeLock();
    private @Nullable
    ApplicationEventPublisher applicationEventPublisher;
    private boolean strict = false;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setInitialEntitySet(Set<? extends Class<?>> initialEntitySet) {
        this.initialEntitySet = initialEntitySet;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    /**
     * Configures the {@link SimpleTypeHolder} to be used by the {@link MappingContext}. Allows customization of what
     * types will be regarded as simple types and thus not recursively analyzed.
     *
     * @param simpleTypes must not be {@literal null}.
     */
    public void setSimpleTypeHolder(SimpleTypeHolder simpleTypes) {

        Assert.notNull(simpleTypes, "SimpleTypeHolder must not be null!");

        this.simpleTypeHolder = simpleTypes;
    }

    @Override
    public Collection<E> getPersistentEntities() {
        try {

            read.lock();
            return persistentEntities.values().stream()//
                    .flatMap(Optionals::toStream)//
                    .collect(Collectors.toSet());
        } finally {
            read.unlock();
        }
    }


    @Override
    @Nullable
    public E getPersistentEntity(Class<?> type) {
        return getPersistentEntity(ClassTypeInformation.from(type));
    }

    @Override
    public boolean hasPersistentEntityFor(Class<?> type) {

        Assert.notNull(type, "Type must not be null!");

        Optional<E> entity = persistentEntities.get(ClassTypeInformation.from(type));

        return entity != null && entity.isPresent();
    }

    @Nullable
    @Override
    public E getPersistentEntity(TypeInformation<?> type) {

        Assert.notNull(type, "Type must not be null!");

        try {

            read.lock();

            Optional<E> entity = persistentEntities.get(type);

            if (entity.isPresent()) {
                return entity.orElse(null);
            }

        } finally {
            read.unlock();
        }

        if (!shouldCreatePersistentEntityFor(type)) {

            try {
                write.lock();
                persistentEntities.put(type, NONE);
            } finally {
                write.unlock();
            }

            return null;
        }

        if (strict) {
            throw new MappingException("Unknown persistent entity " + type);
        }

        return addPersistentEntity(type).orElse(null);
    }

    @Nullable
    @Override
    public E getPersistentEntity(P persistentProperty) {

        Assert.notNull(persistentProperty, "PersistentProperty must not be null!");

        if (!persistentProperty.isEntity()) {
            return null;
        }

        TypeInformation<?> typeInfo = persistentProperty.getTypeInformation();
        return getPersistentEntity(typeInfo.getRequiredActualType());
    }

    protected Optional<E> addPersistentEntity(Class<?> type) {
        return addPersistentEntity(ClassTypeInformation.from(type));
    }

    /**
     * @param typeInformation must not be {@literal null}.
     */
    protected Optional<E> addPersistentEntity(TypeInformation<?> typeInformation) {

        Assert.notNull(typeInformation, "TypeInformation must not be null!");
        try {
            read.lock();
            Optional<E> persistentEntity = persistentEntities.getOrDefault(typeInformation, Optional.empty());
            if (persistentEntity.isPresent()) {
                return persistentEntity;
            }
        } finally {
            read.unlock();
        }
        Class<?> type = typeInformation.getType();
        E entity;
        try {
            write.lock();
            entity = createPersistentEntity(typeInformation);
            // Eagerly cache the entity as we might have to find it during recursive lookups.
            persistentEntities.put(typeInformation, Optional.of(entity));
            PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(type);
            final Map<String, PropertyDescriptor> descriptors = new HashMap<>();
            for (PropertyDescriptor descriptor : pds) {
                descriptors.put(descriptor.getName(), descriptor);
            }
            try {
                PersistentPropertyCreator persistentPropertyCreator = new PersistentPropertyCreator(entity, descriptors);
                ReflectionUtils.doWithFields(type, persistentPropertyCreator, PersistentPropertyFilter.INSTANCE);
                persistentPropertyCreator.addPropertiesForRemainingDescriptors();
            } catch (RuntimeException e) {
                persistentEntities.remove(typeInformation);
                throw e;
            }
        } catch (BeansException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            write.unlock();
        }
        // Inform listeners
        if (null != applicationEventPublisher) {
            applicationEventPublisher.publishEvent(new MappingContextEvent<>(this, entity));
        }
        return Optional.of(entity);
    }

    @Override
    public Collection<TypeInformation<?>> getManagedTypes() {

        try {

            read.lock();
            return Collections.unmodifiableSet(new HashSet<>(persistentEntities.keySet()));

        } finally {
            read.unlock();
        }
    }

    /**
     * Creates the concrete {@link PersistentEntity} instance.
     *
     * @param <T>
     * @param typeInformation
     * @return
     */
    protected abstract <T> E createPersistentEntity(TypeInformation<T> typeInformation);

    /**
     * Creates the concrete instance of {@link PersistentProperty}.
     *
     * @param property
     * @param owner
     * @param simpleTypeHolder
     * @return
     */
    protected abstract P createPersistentProperty(Property property, E owner, SimpleTypeHolder simpleTypeHolder);

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    /**
     * Initializes the mapping context. Will add the types configured through {@link #setInitialEntitySet(Set)} to the
     * context.
     */
    public void initialize() {
        initialEntitySet.forEach(this::addPersistentEntity);
    }

    protected boolean shouldCreatePersistentEntityFor(TypeInformation<?> type) {

        if (simpleTypeHolder.isSimpleType(type.getType())) {
            return false;
        }

        return !KotlinDetector.isKotlinType(type.getType());
    }

    /**
     * {@link ReflectionUtils.FieldCallback} to create {@link PersistentProperty} instances.
     */
    private final class PersistentPropertyCreator implements ReflectionUtils.FieldCallback {

        private final E entity;
        private final Map<String, PropertyDescriptor> descriptors;
        private final Map<String, PropertyDescriptor> remainingDescriptors;

        public PersistentPropertyCreator(E entity, Map<String, PropertyDescriptor> descriptors) {
            this(entity, descriptors, descriptors);
        }

        private PersistentPropertyCreator(E entity, Map<String, PropertyDescriptor> descriptors,
                                          Map<String, PropertyDescriptor> remainingDescriptors) {
            this.entity = entity;
            this.descriptors = descriptors;
            this.remainingDescriptors = remainingDescriptors;
        }


        @Override
        public void doWith(Field field) {

            String fieldName = field.getName();
            TypeInformation<?> type = entity.getTypeInformation();

            ReflectionUtils.makeAccessible(field);

            Property property = Optional.ofNullable(descriptors.get(fieldName))//
                    .map(it -> Property.of(type, field, it))//
                    .orElseGet(() -> Property.of(type, field));

            createAndRegisterProperty(property);

            this.remainingDescriptors.remove(fieldName);
        }

        /**
         * Adds {@link PersistentProperty} instances for all suitable {@link PropertyDescriptor}s without a backing
         * {@link Field}.
         */
        public void addPropertiesForRemainingDescriptors() {

            remainingDescriptors.values().stream() //
                    .filter(Property::supportsStandalone) //
                    .map(it -> Property.of(entity.getTypeInformation(), it)) //
                    .filter(PersistentPropertyFilter.INSTANCE::matches) //
                    .forEach(this::createAndRegisterProperty);
        }

        private void createAndRegisterProperty(Property input) {

            P property = createPersistentProperty(input, entity, simpleTypeHolder);

            entity.addPersistentProperty(property);

            property.getPersistentEntityTypes().forEach(AbstractMappingContext.this::addPersistentEntity);
        }
    }

    /**
     * Filter rejecting static fields as well as artificially introduced ones. See
     * {@link PersistentPropertyFilter#UNMAPPED_PROPERTIES} for details.
     */
    enum PersistentPropertyFilter implements ReflectionUtils.FieldFilter {

        INSTANCE;

        private static final Streamable<PropertyMatch> UNMAPPED_PROPERTIES;

        static {

            Set<PropertyMatch> matches = new HashSet<>();
            matches.add(new PropertyMatch("class", null));
            matches.add(new PropertyMatch("this\\$.*", null));
            matches.add(new PropertyMatch("metaClass", "groovy.lang.MetaClass"));

            UNMAPPED_PROPERTIES = Streamable.of(matches);
        }


        @Override
        public boolean matches(Field field) {

            if (Modifier.isStatic(field.getModifiers())) {
                return false;
            }
            return UNMAPPED_PROPERTIES.stream()
                    .noneMatch(it -> it.matches(field.getName(), field.getType()));
        }

        /**
         * Returns whether the given {@link PropertyDescriptor} is one to create a {@link PersistentProperty} for.
         *
         * @param property must not be {@literal null}.
         */
        public boolean matches(Property property) {

            Assert.notNull(property, "Property must not be null!");

            if (!property.hasAccessor()) {
                return false;
            }
            return UNMAPPED_PROPERTIES.stream()
                    .noneMatch(it -> it.matches(property.getName(), property.getType()));
        }

        /**
         * <h2> Value object to help defining property exclusion based on name patterns and types. </h2>
         */
        static class PropertyMatch {

            private final @Nullable
            String namePattern;
            private final @Nullable
            String typeName;

            /**
             * Creates a new {@link PropertyMatch} for the given name pattern and type name. At least one of the parameters
             * must not be {@literal null}.
             *
             * @param namePattern a regex pattern to match field names, can be {@literal null}.
             * @param typeName    the name of the type to exclude, can be {@literal null}.
             */
            public PropertyMatch(@Nullable String namePattern, @Nullable String typeName) {

                Assert.isTrue(!(namePattern == null && typeName == null), "Either name pattern or type name must be given!");

                this.namePattern = namePattern;
                this.typeName = typeName;
            }

            /**
             * Returns whether the given {@link Field} matches the defined {@link PropertyMatch}.
             *
             * @param name must not be {@literal null}.
             * @param type must not be {@literal null}.
             */
            public boolean matches(String name, Class<?> type) {

                Assert.notNull(name, "Name must not be null!");
                Assert.notNull(type, "Type must not be null!");

                if (namePattern != null && !name.matches(namePattern)) {
                    return false;
                }
                return typeName == null || type.getName().equals(typeName);
            }
        }
    }
}
