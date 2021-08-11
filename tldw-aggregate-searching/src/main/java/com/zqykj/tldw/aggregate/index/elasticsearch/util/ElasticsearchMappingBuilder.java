/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch.util;

import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.annotations.DynamicMapping;
import com.zqykj.annotations.Field;
import com.zqykj.annotations.FieldType;
import com.zqykj.annotations.Mapping;
import com.zqykj.tldw.aggregate.index.context.AbstractMappingContext;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Optional;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class ElasticsearchMappingBuilder {

    private final AbstractMappingContext<?, ?> mappingContext;

    public ElasticsearchMappingBuilder(AbstractMappingContext<?, ?> mappingContext) {
        this.mappingContext = mappingContext;
    }

    public void mapEntity(XContentBuilder builder, @Nullable SimpleElasticSearchPersistentEntity<?> entity,
                          boolean isRootObject, String nestedObjectFieldName, boolean nestedOrObjectField, FieldType fieldType,
                          @Nullable Field parentFieldAnnotation, @Nullable DynamicMapping dynamicMapping) throws IOException {

        boolean writeNestedProperties = !isRootObject && (isAnyPropertyAnnotatedWithField(entity) || nestedOrObjectField);
        if (writeNestedProperties) {

            String type = nestedOrObjectField ? fieldType.toString().toLowerCase()
                    : FieldType.Object.toString().toLowerCase();
            builder.startObject(nestedObjectFieldName).field(MappingParameters.FIELD_PARAM_TYPE, type);

            if (nestedOrObjectField && FieldType.Nested == fieldType && parentFieldAnnotation != null
                    && parentFieldAnnotation.includeInParent()) {
                builder.field("include_in_parent", true);
            }
        }
//        if (dynamicMapping != null) {
//            builder.field(TYPE_DYNAMIC, dynamicMapping.value().name().toLowerCase());
//        }
        builder.startObject("properties");
        if (entity != null) {
            entity.doWithProperties(property -> {
                try {
//                    if (property.isAnnotationPresent(Transient.class) || isInIgnoreFields(property, parentFieldAnnotation)) {
//                        return;
//                    }
//
//                    if (property.isSeqNoPrimaryTermProperty()) {
//                        if (property.isAnnotationPresent(Field.class)) {
//                            log.warn("Property {} of {} is annotated for inclusion in mapping, but its type is " + //
//                                            "SeqNoPrimaryTerm that is never mapped, so it is skipped", //
//                                    property.getFieldName(), entity.getType());
//                        }
//                        return;
//                    }
                    buildPropertyMapping(builder, isRootObject, property);
                } catch (IOException e) {
                    log.warn("error mapping property with name {}", property.getName(), e);
                }
            });
        }

        builder.endObject();

        if (writeNestedProperties) {
            builder.endObject();
        }

    }

    private void buildPropertyMapping(XContentBuilder builder, boolean isRootObject,
                                      SimpleElasticSearchPersistentProperty property) throws IOException {

        if (property.isAnnotationPresent(Mapping.class)) {
            Optional<Mapping> annotationOptional = Optional.ofNullable(property.findAnnotation(Mapping.class));
            String mappingPath = null;
            if (annotationOptional.isPresent()) {
                mappingPath = annotationOptional.get().mappingPath();
            }
            if (!StringUtils.isEmpty(mappingPath)) {
                ClassPathResource mappings = new ClassPathResource(mappingPath);
                if (mappings.exists()) {
                    builder.rawField(property.getFieldName(), mappings.getInputStream(), XContentType.JSON);
                    return;
                }
            }
        }

        //TODO 暂不支持,等待后续开发
//        if (property.isGeoPointProperty()) {
//            applyGeoPointFieldMapping(builder, property);
//            return;
//        }
//
//        if (property.isGeoShapeProperty()) {
//            applyGeoShapeMapping(builder, property);
//        }
//
//        if (property.isJoinFieldProperty()) {
//            addJoinFieldMapping(builder, property);
//        }

        Field fieldAnnotation = property.findAnnotation(Field.class);
//        boolean isCompletionProperty = property.isCompletionProperty();
        boolean isNestedOrObjectProperty = isNestedOrObjectProperty(property);

        if (property.isEntity() && hasRelevantAnnotation(property)) {

            if (fieldAnnotation == null) {
                return;
            }

            if (isNestedOrObjectProperty) {
                Iterator<? extends TypeInformation<?>> iterator = property.getPersistentEntityTypes().iterator();
                SimpleElasticSearchPersistentEntity<?> persistentEntity = iterator.hasNext()
                        ? (SimpleElasticSearchPersistentEntity<?>) mappingContext.getPersistentEntity(iterator.next()) : null;
                mapEntity(builder, persistentEntity, false, property.getFieldName(), true, fieldAnnotation.type(),
                        fieldAnnotation, property.findAnnotation(DynamicMapping.class));
                return;
            }
        }

//        MultiField multiField = property.findAnnotation(MultiField.class);

//        if (isCompletionProperty) {
//            CompletionField completionField = property.findAnnotation(CompletionField.class);
//            applyCompletionFieldMapping(builder, property, completionField);
//        }

        if (isRootObject && fieldAnnotation != null && property.isIdProperty()) {
            applyDefaultIdFieldMapping(builder, property);
        } else if (fieldAnnotation != null) {
            addSingleFieldMapping(builder, property, fieldAnnotation, isNestedOrObjectProperty);
        }
    }

    private void applyDefaultIdFieldMapping(XContentBuilder builder, SimpleElasticSearchPersistentProperty property)
            throws IOException {

        builder.startObject(property.getFieldName()).field(MappingParameters.FIELD_PARAM_TYPE,
                MappingParameters.TYPE_VALUE_KEYWORD).field(MappingParameters.FIELD_INDEX, true)
                .endObject();
    }

    private boolean isAnyPropertyAnnotatedWithField(@Nullable SimpleElasticSearchPersistentEntity<?> entity) {

        return entity != null && entity.getPersistentProperties(Field.class) != null;
    }

    private void addSingleFieldMapping(XContentBuilder builder, SimpleElasticSearchPersistentProperty property,
                                       Field annotation, boolean nestedOrObjectField) throws IOException {

        // build the property json, if empty skip it as this is no valid mapping
        XContentBuilder propertyBuilder = jsonBuilder().startObject();
        addFieldMappingParameters(propertyBuilder, annotation, nestedOrObjectField);
        propertyBuilder.endObject().close();

        if ("{}".equals(propertyBuilder.getOutputStream().toString())) {
            return;
        }

        builder.startObject(property.getFieldName());
        addFieldMappingParameters(builder, annotation, nestedOrObjectField);
        builder.endObject();
    }

    private void addFieldMappingParameters(XContentBuilder builder, Annotation annotation, boolean nestedOrObjectField)
            throws IOException {

        MappingParameters mappingParameters = MappingParameters.from(annotation);

        if (!nestedOrObjectField && mappingParameters.isStore()) {
            builder.field(MappingParameters.FIELD_PARAM_STORE, true);
        }
        mappingParameters.writeTypeAndParametersTo(builder);
    }


    private boolean isNestedOrObjectProperty(SimpleElasticSearchPersistentProperty property) {

        Field fieldAnnotation = property.findAnnotation(Field.class);
        return fieldAnnotation != null
                && (FieldType.Nested == fieldAnnotation.type() || FieldType.Object == fieldAnnotation.type());
    }

    private boolean hasRelevantAnnotation(SimpleElasticSearchPersistentProperty property) {

//        return property.findAnnotation(Field.class) != null
//                || property.findAnnotation(MultiField.class) != null
//                || property.findAnnotation(GeoPointField.class) != null
//                || property.findAnnotation(CompletionField.class) != null;
        return null != property.findAnnotation(Field.class);
    }

}
