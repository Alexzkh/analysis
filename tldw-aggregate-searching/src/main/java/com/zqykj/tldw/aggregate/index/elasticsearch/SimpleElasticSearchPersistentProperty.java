/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch;

import com.zqykj.annotations.*;
import com.zqykj.tldw.aggregate.covert.ElasticsearchDateConverter;
import com.zqykj.tldw.aggregate.covert.ElasticsearchPersistentPropertyConverter;
import com.zqykj.tldw.aggregate.index.model.Property;
import com.zqykj.tldw.aggregate.index.model.SimpleTypeHolder;
import com.zqykj.tldw.aggregate.index.mapping.AbstractPersistentProperty;
import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <h1> Elasticsearch persistent entity property information describe</h1>
 */
public class SimpleElasticSearchPersistentProperty extends AbstractPersistentProperty<SimpleElasticSearchPersistentProperty> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleElasticSearchPersistentProperty.class);
    private final boolean isId;
    @Nullable
    private final String annotatedFieldName;
    @Nullable
    private ElasticsearchPersistentPropertyConverter propertyConverter;

    private static final List<String> SUPPORTED_ID_PROPERTY_NAMES = Arrays.asList("id", "document");

    public SimpleElasticSearchPersistentProperty(Property property, PersistentEntity<?, SimpleElasticSearchPersistentProperty> owner,
                                                 SimpleTypeHolder simpleTypeHolder) {

        super(property, owner, simpleTypeHolder);

        this.annotatedFieldName = getAnnotatedFieldName();

        this.isId = isAnnotationPresent(Id.class) || SUPPORTED_ID_PROPERTY_NAMES.contains(getFieldName());

        // deprecated since 4.1
        @Deprecated
        boolean isIdWithoutAnnotation = isId && !isAnnotationPresent(Id.class);
        if (isIdWithoutAnnotation) {
            LOGGER.warn("Using the property name of '{}' to identify the id property is deprecated."
                    + " Please annotate the id property with '@Id'", getName());
        }

        boolean isField = isAnnotationPresent(Field.class);

        if (isField && isAnnotationPresent(MultiField.class)) {
            throw new RuntimeException("@Field annotation must not be used on a @MultiField property.");
        }

        // 初始化Date converter
        initDateConverter();
    }


    /**
     * <h2> Initializes an {@link ElasticsearchPersistentPropertyConverter} if this property is annotated as a Field with type
     * * {@link com.zqykj.annotations.FieldType#Date}, has a {@link com.zqykj.annotations.DateFormat} set and if the type of the property is one of the Java8 temporal
     * * classes or java.util.Date. </h2>
     */
    private void initDateConverter() {
        Field field = findAnnotation(Field.class);

        Class<?> actualType = getActualTypeOrNull();

        if (actualType == null) {
            return;
        }

        boolean isTemporalAccessor = TemporalAccessor.class.isAssignableFrom(actualType);

        boolean isDate = Date.class.isAssignableFrom(actualType);

        // 该字段设置了日期时间类型, 需要有单独的converter支持
        if (field != null && (field.type() == FieldType.Date || field.type() == FieldType.Date_Nanos)
                && (isTemporalAccessor || isDate)) {

            DateFormat format = field.format();

            String property = getOwner().getType().getSimpleName() + "." + getName();

            if (DateFormat.none == format) {
                LOGGER.warn(
                        String.format("No DateFormat defined for property %s. Make sure you have a Converter registered for %s",
                                property, actualType.getSimpleName()));
                return;
            }

            ElasticsearchDateConverter converter;

            if (DateFormat.custom == format) {
                String pattern = field.pattern();

                if (!StringUtils.hasLength(pattern)) {
                    throw new RuntimeException(
                            String.format("Property %s is annotated with FieldType.%s and a custom format but has no pattern defined",
                                    property, field.type().name()));
                }
                converter = ElasticsearchDateConverter.of(pattern);
            } else {
                // 使用 DateFormat 封装的其他类型
                converter = ElasticsearchDateConverter.of(format);
            }

            propertyConverter = new ElasticsearchPersistentPropertyConverter() {

                final ElasticsearchDateConverter dateConverter = converter;

                @Override
                public String write(Object property) {
                    if (isTemporalAccessor && TemporalAccessor.class.isAssignableFrom(property.getClass())) {
                        return dateConverter.format((TemporalAccessor) property);
                    } else if (isDate && Date.class.isAssignableFrom(property.getClass())) {
                        return dateConverter.format((Date) property);
                    } else {
                        return property.toString();
                    }
                }

                @Override
                public Object read(String s) {
                    if (isTemporalAccessor) {
                        return dateConverter.parse(s, (Class<? extends TemporalAccessor>) actualType);
                    } else {
                        // must be Date
                        return dateConverter.parse(s);
                    }
                }
            };
        }
    }


    public boolean hasPropertyConverter() {
        return propertyConverter != null;
    }

    @Nullable
    public ElasticsearchPersistentPropertyConverter getPropertyConverter() {
        return propertyConverter;
    }

    @Override
    public String getFieldName() {
        return annotatedFieldName == null ? getProperty().getName() : annotatedFieldName;
    }

    @Override
    public boolean isIdProperty() {
        return isId;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    @Nullable
    protected String getAnnotatedFieldName() {
        String name = null;
        if (isAnnotationPresent(Field.class)) {
            name = findAnnotation(Field.class).name();
        }
        return StringUtils.hasText(name) ? name : null;
    }


}
