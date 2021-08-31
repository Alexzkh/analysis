/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.covert;

import com.zqykj.infrastructure.util.JacksonUtils;
import com.zqykj.tldw.aggregate.index.elasticsearch.ElasticsearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentProperty;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import com.zqykj.tldw.aggregate.model.BeanWrapper;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> Elasticsearch 转换器, 处理es 返回数据 与 写入数据的转换处理 </h1>
 */
public class ElasticsearchEntityConverter implements EntityReader<Object, Map<String, Object>>,
        EntityWriter<Object, Map<String, Object>> {

    private final SimpleElasticsearchMappingContext mappingContext;

    public ElasticsearchEntityConverter(SimpleElasticsearchMappingContext mappingContext) {

        this.mappingContext = mappingContext;
    }

    @Override
    public <R> R read(Class<R> type, Map<String, Object> source) {

        SimpleElasticSearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(type);

        readProperties(entity, source);
        // 上述source 填充完成( type property name 基本都有对应), 开始反序列化
        return JacksonUtils.toObj(JacksonUtils.toJson(source), type);
    }

    /**
     * <h2> 读取 并处理 Elasticsearch 返回的数据 与 持久化实体Property 映射 </h2>
     */
    protected void readProperties(SimpleElasticSearchPersistentEntity<?> entity, Map<String, Object> source) {
        // 遍历该索引类 所有的property
        for (SimpleElasticSearchPersistentProperty property : entity) {

            String name = property.getName();

            // 如果不包含, 说明该字段上设置了 别名
            if (!source.containsKey(name)) {

                // 此时的fieldName 是 annotatedFieldName
                String fieldName = property.getFieldName();

                // 设置  property name value 到 source中 (key : property.getName() , value : source.ge(annotatedFieldName) )
                if (source.containsKey(fieldName)) {
                    Object original = source.get(fieldName);
                    // 确实是使用别名
                    // 如果是日期的话,需要特殊处理(重新更新值)
                    if (property.hasPropertyConverter()) {
                        source.put(name, propertyConverterRead(property, original));
                    } else {
                        source.put(name, original);
                    }
                }
            } else {
                // 如果是日期的话,需要特殊处理(重新更新值)
                if (property.hasPropertyConverter()) {
                    Object original = source.get(name);
                    Object change = propertyConverterRead(property, original);
                    source.put(name, change);
                }
            }
        }
    }


    @Override
    public void write(Object source, Map<String, Object> sink) {

        Class<?> entityType = ClassUtils.getUserClass(source.getClass());

        SimpleElasticSearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(entityType);

        writeProperties(entity, source, sink);
    }

    /**
     * <h2> 写入 并处理 保存到Elasticsearch 数据 </h2>
     */
    protected void writeProperties(ElasticsearchPersistentEntity<?> entity, Object source, Map<String, Object> sink) {

        // 遍历该索引类所有的property
        for (SimpleElasticSearchPersistentProperty property : entity) {

            // 如果是日期的话,需要特殊处理
            if (property.hasPropertyConverter()) {
                Object value = propertyConverterWrite(property, BeanWrapper.getProperty(property, source));
                sink.put(property.getFieldName(), value);
            } else {
                // property.getFieldName( 如果property 未设置 别名, 那获取的是 property.getName)
                sink.put(property.getFieldName(), BeanWrapper.getProperty(property, source));
            }
        }
    }

    /**
     * <h2> property write converter </h2>
     */
    private Object propertyConverterWrite(SimpleElasticSearchPersistentProperty property, Object value) {
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
    private Object propertyConverterRead(SimpleElasticSearchPersistentProperty property, Object source) {
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
