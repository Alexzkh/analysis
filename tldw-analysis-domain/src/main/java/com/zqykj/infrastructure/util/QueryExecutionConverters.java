/**
 * @作者 Mcj
 */
package com.zqykj.infrastructure.util;

import com.zqykj.domain.page.Page;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * <h1> 处理使用@Query 的method, 对method return type support handle </h1>
 */
public class QueryExecutionConverters {

    private static final Set<WrapperType> WRAPPER_TYPES = new HashSet<>();
    private static final Set<WrapperType> UNWRAPPER_TYPES = new HashSet<WrapperType>();
    private static final Set<Converter<Object, Object>> UNWRAPPERS = new HashSet<>();
    private static final Set<Class<?>> ALLOWED_PAGEABLE_TYPES = new HashSet<>();
    private static final Map<Class<?>, ExecutionAdapter> EXECUTION_ADAPTER = new HashMap<>();
    private static final Map<Class<?>, Boolean> supportsCache = new ConcurrentReferenceHashMap<>();
    public final static GenericConversionService CONVERSION_SERVICE = new DefaultConversionService();

    static {

        WRAPPER_TYPES.add(WrapperType.singleValue(Future.class));
        UNWRAPPER_TYPES.add(WrapperType.singleValue(Future.class));
        WRAPPER_TYPES.add(WrapperType.singleValue(ListenableFuture.class));
        UNWRAPPER_TYPES.add(WrapperType.singleValue(ListenableFuture.class));

        ALLOWED_PAGEABLE_TYPES.add(Page.class);
        ALLOWED_PAGEABLE_TYPES.add(List.class);

        WRAPPER_TYPES.add(WrapperType.singleValue(CompletableFuture.class));
        WRAPPER_TYPES.add(WrapperType.singleValue(java.util.Optional.class));
        UNWRAPPER_TYPES.add(WrapperType.singleValue(java.util.Optional.class));

    }

    static {
        QueryExecutionConverters.registerConvertersIn(CONVERSION_SERVICE);
        CONVERSION_SERVICE.removeConvertible(Object.class, Object.class);
    }

    private QueryExecutionConverters() {
    }

    /**
     * <h2> Returns whether the given type is a supported wrapper type. </h2>
     *
     * @param type must not be {@literal null}.
     */
    public static boolean supports(Class<?> type) {

        Assert.notNull(type, "Type must not be null!");

        return supportsCache.computeIfAbsent(type, key -> {

            for (WrapperType candidate : WRAPPER_TYPES) {
                if (candidate.getType().isAssignableFrom(key)) {
                    return true;
                }
            }
            return false;
        });
    }


    /**
     * @param type must not be {@literal null}.
     */
    public static boolean supportsUnwrapping(Class<?> type) {

        Assert.notNull(type, "Type must not be null!");

        for (WrapperType candidate : UNWRAPPER_TYPES) {
            if (candidate.getType().isAssignableFrom(type)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSingleValue(Class<?> type) {

        for (WrapperType candidate : WRAPPER_TYPES) {
            if (candidate.getType().isAssignableFrom(type)) {
                return candidate.isSingleValue();
            }
        }

        return false;
    }

    /**
     * Registers converters for wrapper types found on the classpath.
     *
     * @param conversionService must not be {@literal null}.
     */
    public static void registerConvertersIn(ConfigurableConversionService conversionService) {

        Assert.notNull(conversionService, "ConversionService must not be null!");

        conversionService.removeConvertible(Collection.class, Object.class);

        conversionService.addConverter(NullableWrapperToJdk8OptionalConverter.INSTANCE);
        conversionService.addConverter(new NullableWrapperToCompletableFutureConverter());
        conversionService.addConverter(new NullableWrapperToFutureConverter());
        conversionService.addConverter(new IterableToStreamableConverter());
    }

    /**
     * Returns the types that are supported on paginating query methods. Will include custom collection types of e.g.
     */
    public static Set<Class<?>> getAllowedPageableTypes() {
        return Collections.unmodifiableSet(ALLOWED_PAGEABLE_TYPES);
    }


    /**
     * Unwraps the given source value in case it's one of the currently supported wrapper types detected at runtime.
     *
     * @param source can be {@literal null}.
     * @return
     */
    @Nullable
    public static Object unwrap(@Nullable Object source) {

        if (source == null || !supports(source.getClass())) {
            return source;
        }

        for (Converter<Object, Object> converter : UNWRAPPERS) {

            Object result = converter.convert(source);

            if (result != source) {
                return result;
            }
        }

        return source;
    }

    /**
     * Recursively unwraps well known wrapper types from the given {@link TypeInformation}.
     *
     * @param type must not be {@literal null}.
     * @return will never be {@literal null}.
     */
    public static TypeInformation<?> unwrapWrapperTypes(TypeInformation<?> type) {

        Assert.notNull(type, "type must not be null");

        Class<?> rawType = type.getType();

        boolean needToUnwrap = type.isCollectionLike() //
                || rawType.isArray() //
                || supports(rawType) //
                || Stream.class.isAssignableFrom(rawType);
        return needToUnwrap ? unwrapWrapperTypes(type.getRequiredComponentType()) : type;
    }

    /**
     * A Spring {@link Converter} to support JDK 8's {@link java.util.Optional}.
     *
     * @author Oliver Gierke
     */
    private static class NullableWrapperToJdk8OptionalConverter extends WrapperType.AbstractWrapperTypeConverter {

        public static final NullableWrapperToJdk8OptionalConverter INSTANCE = new NullableWrapperToJdk8OptionalConverter();

        private NullableWrapperToJdk8OptionalConverter() {
            super(java.util.Optional.empty());
        }


        @Override
        protected Object wrap(Object source) {
            return java.util.Optional.of(source);
        }

        public static WrapperType getWrapperType() {
            return WrapperType.singleValue(java.util.Optional.class);
        }
    }

    /**
     * A Spring {@link Converter} to support returning {@link Future} instances from repository methods.
     *
     * @author Oliver Gierke
     */
    private static class NullableWrapperToFutureConverter extends WrapperType.AbstractWrapperTypeConverter {

        /**
         * Creates a new {@link NullableWrapperToFutureConverter} using the given {@link ConversionService}.
         */
        NullableWrapperToFutureConverter() {
            super(new AsyncResult<>(null), Arrays.asList(Future.class, ListenableFuture.class));
        }

        @Override
        protected Object wrap(Object source) {
            return new AsyncResult<>(source);
        }
    }

    /**
     * A Spring {@link Converter} to support returning {@link CompletableFuture} instances from repository methods.
     *
     * @author Oliver Gierke
     */
    private static class NullableWrapperToCompletableFutureConverter extends WrapperType.AbstractWrapperTypeConverter {

        /**
         * Creates a new {@link NullableWrapperToCompletableFutureConverter} using the given {@link ConversionService}.
         */
        NullableWrapperToCompletableFutureConverter() {
            super(CompletableFuture.completedFuture(null));
        }

        @Override
        protected Object wrap(Object source) {
            return source instanceof CompletableFuture ? source : CompletableFuture.completedFuture(source);
        }

        static WrapperType getWrapperType() {
            return WrapperType.singleValue(CompletableFuture.class);
        }
    }


    private static class IterableToStreamableConverter implements ConditionalGenericConverter {

        private static final TypeDescriptor STREAMABLE = TypeDescriptor.valueOf(Streamable.class);

        private final Map<TypeDescriptor, Boolean> targetTypeCache = new ConcurrentHashMap<>();
        private final ConversionService conversionService = DefaultConversionService.getSharedInstance();

        IterableToStreamableConverter() {
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(new ConvertiblePair(Iterable.class, Object.class));
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {

            if (sourceType.isAssignableTo(targetType)) {
                return false;
            }

            if (!Iterable.class.isAssignableFrom(sourceType.getType())) {
                return false;
            }

            if (Streamable.class.equals(targetType.getType())) {
                return true;
            }

            return targetTypeCache.computeIfAbsent(targetType, it -> {
                return conversionService.canConvert(STREAMABLE, targetType);
            });
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

            Streamable<Object> streamable = source == null //
                    ? Streamable.empty() //
                    : Streamable.of(Iterable.class.cast(source));

            return Streamable.class.equals(targetType.getType()) //
                    ? streamable //
                    : conversionService.convert(streamable, STREAMABLE, targetType);
        }
    }

    public interface ThrowingSupplier {
        Object get() throws Throwable;
    }

    public interface ExecutionAdapter {
        Object apply(ThrowingSupplier supplier) throws Throwable;
    }

    public static final class WrapperType {

        private WrapperType(Class<?> type, Cardinality cardinality) {
            this.type = type;
            this.cardinality = cardinality;
        }

        public Class<?> getType() {
            return this.type;
        }

        public Cardinality getCardinality() {
            return cardinality;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (!(o instanceof WrapperType)) {
                return false;
            }

            WrapperType that = (WrapperType) o;

            if (!ObjectUtils.nullSafeEquals(type, that.type)) {
                return false;
            }

            return cardinality == that.cardinality;
        }

        @Override
        public int hashCode() {
            int result = ObjectUtils.nullSafeHashCode(type);
            result = 31 * result + ObjectUtils.nullSafeHashCode(cardinality);
            return result;
        }


        @Override
        public String toString() {
            return "QueryExecutionConverters.WrapperType(type=" + this.getType() + ", cardinality=" + this.getCardinality()
                    + ")";
        }

        enum Cardinality {
            NONE, SINGLE, MULTI;
        }

        private final Class<?> type;
        private final Cardinality cardinality;

        public static WrapperType singleValue(Class<?> type) {
            return new WrapperType(type, Cardinality.SINGLE);
        }

        public static WrapperType multiValue(Class<?> type) {
            return new WrapperType(type, Cardinality.MULTI);
        }

        public static WrapperType noValue(Class<?> type) {
            return new WrapperType(type, Cardinality.NONE);
        }

        boolean isSingleValue() {
            return cardinality.equals(Cardinality.SINGLE);
        }

        /**
         * Base class for converters that create instances of wrapper types such as Google Guava's and JDK 8's
         * {@code Optional} types.
         *
         * @author Oliver Gierke
         */
        private static abstract class AbstractWrapperTypeConverter implements GenericConverter {

            private final Object nullValue;
            private final Iterable<Class<?>> wrapperTypes;

            /**
             * Creates a new {@link AbstractWrapperTypeConverter} using the given {@link ConversionService} and wrapper type.
             *
             * @param nullValue must not be {@literal null}.
             */
            AbstractWrapperTypeConverter(Object nullValue) {

                Assert.notNull(nullValue, "Null value must not be null!");

                this.nullValue = nullValue;
                this.wrapperTypes = Collections.singleton(nullValue.getClass());
            }

            AbstractWrapperTypeConverter(Object nullValue,
                                         Iterable<Class<?>> wrapperTypes) {
                this.nullValue = nullValue;
                this.wrapperTypes = wrapperTypes;
            }

            @Override
            public Set<ConvertiblePair> getConvertibleTypes() {

                return Streamable.of(wrapperTypes)//
                        .map(it -> new ConvertiblePair(NullableWrapper.class, it))//
                        .stream().collect(StreamUtils.toUnmodifiableSet());
            }


            @Nullable
            @Override
            public final Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

                if (source == null) {
                    return null;
                }

                NullableWrapper wrapper = (NullableWrapper) source;
                Object value = wrapper.getValue();

                // TODO: Add Recursive conversion once we move to Spring 4
                return value == null ? nullValue : wrap(value);
            }

            /**
             * Wrap the given, non-{@literal null} value into the wrapper type.
             *
             * @param source will never be {@literal null}.
             * @return must not be {@literal null}.
             */
            protected abstract Object wrap(Object source);
        }
    }
}
