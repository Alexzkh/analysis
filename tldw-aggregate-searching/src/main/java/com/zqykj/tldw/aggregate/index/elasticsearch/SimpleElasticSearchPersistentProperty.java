/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch;

import com.zqykj.annotations.Field;
import com.zqykj.tldw.aggregate.index.model.Property;
import com.zqykj.tldw.aggregate.index.model.SimpleTypeHolder;
import com.zqykj.tldw.aggregate.index.mapping.AbstractPersistentProperty;
import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import com.zqykj.annotations.Id;

import java.util.Arrays;
import java.util.List;

/**
 * <h1> Elasticsearch persistent entity property information describe</h1>
 */
public class SimpleElasticSearchPersistentProperty extends AbstractPersistentProperty<SimpleElasticSearchPersistentProperty> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleElasticSearchPersistentProperty.class);
    private final boolean isId;
    private final @Nullable
    String annotatedFieldName;

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
