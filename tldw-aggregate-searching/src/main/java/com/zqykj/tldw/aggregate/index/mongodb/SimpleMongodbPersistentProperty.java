/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb;

import com.zqykj.infrastructure.util.Lazy;
import com.zqykj.annotations.Id;
import com.zqykj.tldw.aggregate.index.model.Property;
import com.zqykj.tldw.aggregate.index.model.SimpleTypeHolder;
import com.zqykj.tldw.aggregate.index.mapping.AbstractPersistentProperty;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class SimpleMongodbPersistentProperty
        extends AbstractPersistentProperty<SimpleMongodbPersistentProperty> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleMongodbPersistentProperty.class);

    private static final String ID_FIELD_NAME = "_id";
    private static final String LANGUAGE_FIELD_NAME = "language";
    private static final Set<Class<?>> SUPPORTED_ID_TYPES = new HashSet<>();
    private static final Set<String> SUPPORTED_ID_PROPERTY_NAMES = new HashSet<>();

    private final Lazy<Boolean> isId = Lazy.of(() -> isAnnotationPresent(Id.class));

    static {
        SUPPORTED_ID_TYPES.add(ObjectId.class);
        SUPPORTED_ID_TYPES.add(String.class);
        SUPPORTED_ID_TYPES.add(BigInteger.class);
        SUPPORTED_ID_PROPERTY_NAMES.add("id");
        SUPPORTED_ID_PROPERTY_NAMES.add("_id");
    }


    public SimpleMongodbPersistentProperty(Property property, SimpleMongoPersistentEntity<?> owner,
                                           SimpleTypeHolder simpleTypeHolder) {

        super(property, owner, simpleTypeHolder);

        if (isIdProperty() && hasExplicitFieldName()) {

            String annotatedName = getAnnotatedFieldName();
            if (!ID_FIELD_NAME.equals(annotatedName)) {
                LOG.warn(
                        "Customizing field name for id property '{}.{}' is not allowed! Custom name ('{}') will not be considered!",
                        owner.getName(), getName(), annotatedName);
            }
        }
    }

    @Override
    public boolean isIdProperty() {
        return isId.get();
    }

    protected boolean hasExplicitFieldName() {
        return StringUtils.hasText(getAnnotatedFieldName());
    }
}
