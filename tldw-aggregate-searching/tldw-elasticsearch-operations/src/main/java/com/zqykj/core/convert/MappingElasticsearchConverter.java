/**
 * @作者 Mcj
 */
package com.zqykj.core.convert;

import com.zqykj.core.document.Document;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.core.mapping.ElasticsearchPersistentPropertyConverter;
import com.zqykj.coverter.EntityConverter;
import com.zqykj.mapping.PersistentPropertyAccessor;
import com.zqykj.mapping.context.MappingContext;
import com.zqykj.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> Elasticsearch specific {@link EntityConverter} implementation based on domain type
 * * {@link ElasticsearchPersistentEntity metadata} </h1>
 */
@Slf4j
public class MappingElasticsearchConverter implements ElasticsearchConverter, InitializingBean {

    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;
    private final GenericConversionService conversionService;

    public MappingElasticsearchConverter(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        this(mappingContext, null);
    }

    public MappingElasticsearchConverter(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext,
            @Nullable GenericConversionService conversionService) {

        Assert.notNull(mappingContext, "MappingContext must not be null!");

        this.mappingContext = mappingContext;
        this.conversionService = conversionService != null ? conversionService : new DefaultConversionService();
    }

    @Override
    public MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> getMappingContext() {
        return mappingContext;
    }

    @Override
    public ConversionService getConversionService() {
        return conversionService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DateFormatterRegistrar.addDateConverters(conversionService);
    }

    @Override
    public <R> R read(Class<R> type, Document source) {

        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(type);

        readProperties(entity, source);
        // 上述source 填充完成( type property name 基本都有对应), 开始反序列化
        return JacksonUtils.parse(JacksonUtils.toJson(source), type);
    }


    /**
     * <h2> 读取elasticsearch 返回的source data, 并 parse 为 entity 对应的索引类 </h2>
     */
    protected void readProperties(ElasticsearchPersistentEntity<?> entity, Document source) {
        // 遍历该索引类 所有的property
        for (ElasticsearchPersistentProperty property : entity) {

            // 索引类 字段原始 name
            String name = property.getName();

            // id 需要特殊处理
            if (property.isIdProperty() && source.hasId()) {
                Object id = source.get(property.getFieldName());
                if (null == id) {
                    source.put(property.getFieldName(), source.getId());
                }
            }
            // source 是map 结构
            // key 代表的索引类的字段name( 有可能不是原始name,可能通过注解@Field 的name 属性设置了自定义的name)
            // value 就是property 对应值
            // 那么再反序列化前,需要将 通过注解@Field 设置了name 属性的key 给还原成 原始的property name,这样方便下面进行反序列化
            // 否则反序列化到索引类的时候, property name 匹配不上
            if (!source.containsKey(name)) {

                // 此时的fieldName 是 annotatedFieldName (通过 @Field 设置了name)
                String fieldName = property.getFieldName();
                Object original = source.get(fieldName);
                if (null == original) {
                    // 继续处理下一个property
                    continue;
                }

                // 查看property 是否设置了自定义的converter
                // (eg. property 设置了注解 @Field( format = DateFormat.custom, patter = "yyyy-MM-dd HH:mm:ss")
                if (property.hasPropertyConverter()) {
                    source.put(name, propertyConverterRead(property, original));
                } else {
                    source.put(name, original);
                }
                // jackson 序列化我们已经设置了 忽略在JSON字符串中存在，而在Java中不存在的属性
//                source.put(fieldName, null);
            } else {
                // 查看property 是否设置了自定义的converter
                // (eg. property 设置了注解 @Field( format = DateFormat.custom, patter = "yyyy-MM-dd HH:mm:ss")
                if (property.hasPropertyConverter()) {
                    Object original = source.get(name);
                    if (null == original) {
                        continue;
                    }
                    source.put(name, propertyConverterRead(property, original));
                }
            }
        }
    }

    @Override
    public void write(Object source, Document sink) {

        if (source instanceof Map) {
            // noinspection unchecked
            sink.putAll((Map<String, Object>) source);
            return;
        }

        Class<?> entityType = ClassUtils.getUserClass(source.getClass());

        ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(entityType);

        writeEntity(entity, source, sink);
    }

    public void writeMap(Object source, String index) {

        if (source instanceof Map) {

            Map<String, Object> map = (Map<String, Object>) source;

            // 获取实体
            Optional<? extends ElasticsearchPersistentEntity<?>> optional = mappingContext.getPersistentEntities().stream().filter(e -> mappingContext.getRequiredPersistentEntity(e.getType()).getIndexName().equals(index))
                    .findFirst();
            optional.ifPresent(entity -> {
                for (ElasticsearchPersistentProperty property : entity) {

                    if (property.hasPropertyConverter()) {

                        Object firstValue = map.get(property.getFieldName());
                        if (null == firstValue) {
                            continue;
                        }
                        Object value = propertyConverterWrite(property, map.get(property.getFieldName()));
                        map.put(property.getFieldName(), value);
                    }
                }
            });
        }
    }

    /**
     * <h2> 处理entity 对象数据,写入elasticsearch </h2>
     */
    protected void writeEntity(ElasticsearchPersistentEntity<?> entity, Object source, Document sink) {

        PersistentPropertyAccessor<?> accessor = entity.getPropertyAccessor(source);

        writeProperties(entity, accessor, sink);
    }

    /**
     * <h2> 处理entity 的property 与 其 value 的映射关系 </h2>
     */
    protected void writeProperties(ElasticsearchPersistentEntity<?> entity,
                                   PersistentPropertyAccessor<?> accessor, Document sink) {

        // 遍历该索引类entity 所有的property
        for (ElasticsearchPersistentProperty property : entity) {

            Object value = accessor.getProperty(property);
            if (value == null) {

                if (property.storeNullValue()) {
                    sink.put(property.getFieldName(), null);
                }

                continue;
            }
            // 查看property 是否设置了自定义的converter
            // (eg. property 设置了注解 @Field( format = DateFormat.custom, patter = "yyyy-MM-dd HH:mm:ss")
            if (property.hasPropertyConverter()) {

                value = propertyConverterWrite(property, value);
            }
            // property 未设置 别名 (eg. property 未设置 @Field( name = "")
            sink.put(property.getFieldName(), value);
        }
    }

    /**
     * <h2> property 设置了 自定义的转换器 </h2>
     */
    private Object propertyConverterWrite(ElasticsearchPersistentProperty property, Object value) {
        ElasticsearchPersistentPropertyConverter propertyConverter = Objects
                .requireNonNull(property.getPropertyConverter());

        if (value instanceof List) {
            value = ((List<?>) value).stream().map(propertyConverter::write).collect(Collectors.toList());
        } else if (value instanceof Set) {
            value = ((Set<?>) value).stream().map(propertyConverter::write).collect(Collectors.toSet());
        } else {
            value = propertyConverter.write(value);
        }
        return value;
    }

    /**
     * <h2> property read converter </h2>
     */
    private Object propertyConverterRead(ElasticsearchPersistentProperty property, Object source) {
        ElasticsearchPersistentPropertyConverter propertyConverter = Objects
                .requireNonNull(property.getPropertyConverter());

        if (source instanceof String[]) {
            // convert to a List
            source = Arrays.asList((String[]) source);
        }

        if (source instanceof List) {
            source = ((List<?>) source).stream().map(it -> convertOnRead(propertyConverter, it)).collect(Collectors.toList());
        } else if (source instanceof Set) {
            source = ((Set<?>) source).stream().map(it -> convertOnRead(propertyConverter, it)).collect(Collectors.toSet());
        } else {
            source = convertOnRead(propertyConverter, source);
        }
        return source;
    }

    private Object convertOnRead(ElasticsearchPersistentPropertyConverter propertyConverter, Object source) {
        if (String.class.isAssignableFrom(source.getClass())) {
            source = propertyConverter.read((String) source);
        }
        return source;
    }
}
