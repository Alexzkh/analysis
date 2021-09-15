/**
 * @作者 Mcj
 */
package com.zqykj.core.index;

import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static org.springframework.util.StringUtils.hasText;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zqykj.annotations.*;
import com.zqykj.core.ResourceUtil;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.mapping.MappingException;
import com.zqykj.mapping.context.MappingContext;
import com.zqykj.util.TypeInformation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Optional;

@Slf4j
public class MappingBuilder {

    private static final String FIELD_DYNAMIC_TEMPLATES = "dynamic_templates";
    private static final String TYPE_DYNAMIC = "dynamic";
    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    public MappingBuilder(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        this.mappingContext = mappingContext;
    }

    /**
     * <h2> 为给定的 clazz 构建 Elasticsearch 映射 </h2>
     *
     * @return JSON string
     * @throws ElasticsearchException on errors while building the mapping
     */
    public String buildPropertyMapping(Class<?> clazz) throws ElasticsearchException {

        try {
            ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);

            XContentBuilder builder = jsonBuilder().startObject();

            addDynamicTemplatesMapping(builder, entity);

            // TODO Parent

            mapEntity(builder, entity, true, "", false, FieldType.Auto, null, entity.findAnnotation(DynamicMapping.class));

            builder.endObject() // root object
                    .close();

            return builder.getOutputStream().toString();
        } catch (MappingException | IOException e) {
            throw new ElasticsearchException("could not build mapping", e);
        }
    }

    public void mapEntity(XContentBuilder builder, @Nullable ElasticsearchPersistentEntity<?> entity,
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
        // dynamicMapping
        if (dynamicMapping != null) {
            builder.field(TYPE_DYNAMIC, dynamicMapping.value().name().toLowerCase());
        }

        builder.startObject("properties");
        if (entity != null) {
            entity.doWithProperties(property -> {
                try {
                    // TODO 注解Transient 与  isSeqNoPrimaryTermProperty 判断
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
                                      ElasticsearchPersistentProperty property) throws IOException {

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
                ElasticsearchPersistentEntity<?> persistentEntity = iterator.hasNext()
                        ? mappingContext.getPersistentEntity(iterator.next()) : null;
                mapEntity(builder, persistentEntity, false, property.getFieldName(), true, fieldAnnotation.type(),
                        fieldAnnotation, property.findAnnotation(DynamicMapping.class));
                return;
            }
        }

        // property 是否存在multi-fields
        MultiField multiField = property.findAnnotation(MultiField.class);

        // TODO
//        if (isCompletionProperty) {
//            CompletionField completionField = property.findAnnotation(CompletionField.class);
//            applyCompletionFieldMapping(builder, property, completionField);
//        }


        if (isRootObject && fieldAnnotation != null && property.isIdProperty()) {
            applyDefaultIdFieldMapping(builder, property);
        } else if (multiField != null) {
            addMultiFieldMapping(builder, property, multiField, isNestedOrObjectProperty);
        } else if (fieldAnnotation != null) {
            addSingleFieldMapping(builder, property, fieldAnnotation, isNestedOrObjectProperty);
        }
    }

    /**
     * <h2> 是否有相关注解 </h2>
     */
    private boolean hasRelevantAnnotation(ElasticsearchPersistentProperty property) {

//        return property.findAnnotation(Field.class) != null
//                || property.findAnnotation(MultiField.class) != null
//                || property.findAnnotation(GeoPointField.class) != null
//                || property.findAnnotation(CompletionField.class) != null;
        return null != property.findAnnotation(Field.class)
                || null != property.findAnnotation(MultiField.class);
    }

    /**
     * <h2> 默认ID 字段映射 </h2>
     */
    private void applyDefaultIdFieldMapping(XContentBuilder builder, ElasticsearchPersistentProperty property)
            throws IOException {

        builder.startObject(property.getFieldName()).field(MappingParameters.FIELD_PARAM_TYPE,
                MappingParameters.TYPE_VALUE_KEYWORD).field(MappingParameters.FIELD_INDEX, true)
                .endObject();
    }

    /**
     * <h2> 为@MultiField 添加映射   </h2>
     */
    private void addMultiFieldMapping(XContentBuilder builder, ElasticsearchPersistentProperty property,
                                      MultiField annotation, boolean nestedOrObjectField) throws IOException {

        // add main field
        builder.startObject(property.getFieldName());
        addFieldMappingParameters(builder, annotation.mainField(), nestedOrObjectField);

        // add InnerField
        builder.startObject("fields");
        for (InnerField innerField : annotation.otherFields()) {
            builder.startObject(innerField.suffix());
            addFieldMappingParameters(builder, innerField, false);
            builder.endObject();
        }

        builder.endObject();

        builder.endObject();
    }

    /**
     * <h2> 为 @Field注解 添加映射  </h2>
     */
    private void addSingleFieldMapping(XContentBuilder builder, ElasticsearchPersistentProperty property,
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

    /**
     * <h2> 添加字段映射参数 eg. 设置property type 、analyzer... </h2>
     */
    private void addFieldMappingParameters(XContentBuilder builder, Annotation annotation, boolean nestedOrObjectField)
            throws IOException {

        MappingParameters mappingParameters = MappingParameters.from(annotation);

        if (!nestedOrObjectField && mappingParameters.isStore()) {
            builder.field(MappingParameters.FIELD_PARAM_STORE, true);
        }
        mappingParameters.writeTypeAndParametersTo(builder);
    }

    /**
     * <h2> 为动态模板应用映射 </h2>
     */
    private void addDynamicTemplatesMapping(XContentBuilder builder, ElasticsearchPersistentEntity<?> entity)
            throws IOException {

        if (entity.isAnnotationPresent(DynamicTemplates.class)) {
            String mappingPath = entity.getRequiredAnnotation(DynamicTemplates.class).mappingPath();
            if (hasText(mappingPath)) {

                String jsonString = ResourceUtil.readFileFromClasspath(mappingPath);
                if (hasText(jsonString)) {

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonString).get("dynamic_templates");
                    if (jsonNode != null && jsonNode.isArray()) {
                        String json = objectMapper.writeValueAsString(jsonNode);
                        builder.rawField(FIELD_DYNAMIC_TEMPLATES, new ByteArrayInputStream(json.getBytes()), XContentType.JSON);
                    }
                }
            }
        }
    }

    /**
     * <h2> 判断entity 上 任意property 上是否有@Field注解 </h2>
     */
    private boolean isAnyPropertyAnnotatedWithField(@Nullable ElasticsearchPersistentEntity<?> entity) {

        return entity != null && entity.getPersistentProperties(Field.class) != null;
    }

    /**
     * <h2> 是否是嵌套 或者 对象属性 </h2>
     */
    private boolean isNestedOrObjectProperty(ElasticsearchPersistentProperty property) {

        Field fieldAnnotation = property.findAnnotation(Field.class);
        return fieldAnnotation != null
                && (FieldType.Nested == fieldAnnotation.type() || FieldType.Object == fieldAnnotation.type());
    }
}
