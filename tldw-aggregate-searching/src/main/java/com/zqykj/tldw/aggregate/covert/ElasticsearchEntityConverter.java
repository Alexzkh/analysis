/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.covert;

import com.zqykj.infrastructure.util.JacksonUtils;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentProperty;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import com.zqykj.tldw.aggregate.model.BeanWrapper;
import org.springframework.util.ClassUtils;

import java.util.Map;

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

        // 遍历该索引类 所有的property
        for (SimpleElasticSearchPersistentProperty property : entity) {

            String name = property.getName();

            // 如果不包含, 说明该字段上设置了 别名
            if (!source.containsKey(name)) {

                // 此时的fieldName 是 annotatedFieldName
                String fieldName = property.getFieldName();

                // 设置  property name value 到 source中 (key : property.getName() , value : source.ge(annotatedFieldName) )
                if (source.containsKey(fieldName)) {
                    // 确实是使用别名
                    source.put(name, source.get(fieldName));
                }
            }
        }
        // 上述source 填充完成( type property name 基本都有对应), 开始反序列化
        return JacksonUtils.toObj(JacksonUtils.toJson(source), type);
    }


    @Override
    public void write(Object source, Map<String, Object> sink) {

        Class<?> entityType = ClassUtils.getUserClass(source.getClass());

        SimpleElasticSearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(entityType);

        // 遍历该索引类所有的property
        for (SimpleElasticSearchPersistentProperty property : entity) {

            // property.getFieldName( 如果property 未设置 别名, 那获取的是 property.getName)
            sink.put(property.getFieldName(), BeanWrapper.getProperty(property, source));
        }
    }
}
