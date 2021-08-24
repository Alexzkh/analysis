/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.elasticsearch.associate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zqykj.infrastructure.util.ResourceUtil;
import com.zqykj.annotations.DynamicMapping;
import com.zqykj.annotations.FieldType;
import com.zqykj.annotations.Mapping;
import com.zqykj.annotations.Setting;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.util.ElasticsearchMappingBuilder;
import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import com.zqykj.tldw.aggregate.index.operation.AbstractDefaultIndexOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.client.indices.rollover.RolloverRequest;
import org.elasticsearch.client.indices.rollover.RolloverResponse;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.springframework.util.StringUtils.hasText;

/**
 * <h2> ElasticSearch Index Operations</h2>
 */
@Slf4j
public class ElasticsearchIndexOperations extends AbstractDefaultIndexOperations
        implements ElasticsearchIndexOperate {

    private final ElasticsearchMappingBuilder elasticsearchMappingBuilder;
    private final ElasticsearchRestTemplate restTemplate;

    public ElasticsearchIndexOperations(ElasticsearchRestTemplate restTemplate,
                                        Class<?> boundClass) {
        super(boundClass);
        this.restTemplate = restTemplate;
        this.elasticsearchMappingBuilder = new ElasticsearchMappingBuilder(restTemplate.getMappingContext());
    }

    /**
     * <h2> Es暂不支持此索引创建操作</h2>
     */
    @Override
    public boolean createIndex(PersistentEntity<?, ?> entity) {
        if (entity instanceof SimpleElasticSearchPersistentEntity) {
            SimpleElasticSearchPersistentEntity<?> elasticPersistentEntity = (SimpleElasticSearchPersistentEntity<?>) entity;
            if (StringUtils.isBlank(elasticPersistentEntity.getIndexName())) {
                log.error("entity = {} , index name is empty!", entity.getType().getSimpleName());
                return false;
            }
            // 判断索引是否存在
            GetIndexRequest getIndexRequest = new GetIndexRequest(elasticPersistentEntity.getIndexName());
            // 如果设置了Mapping 映射结构,可以在Persistent Entity 上打上Settings 注解,指定映射文件路径
            Boolean exists = restTemplate.execute(client -> client.indices().exists(getIndexRequest, RequestOptions.DEFAULT));
            if (exists) {
                log.warn("Index name  = {} already exists!", elasticPersistentEntity.getIndexName());
                return false;
            }
            Boolean createIndex = false;
            if (elasticPersistentEntity.isRollover()) {
                if (elasticPersistentEntity.getRolloverMaxIndexAgeCondition() == 0
                        && elasticPersistentEntity.getRolloverMaxIndexDocsCondition() == 0
                        && elasticPersistentEntity.getRolloverMaxIndexSizeCondition() == 0) {
                    throw new RuntimeException("rolloverMaxIndexAgeCondition is zero OR rolloverMaxIndexDocsCondition is zero OR rolloverMaxIndexSizeCondition is zero");
                }
                CreateIndexRequest createIndexRequest = new CreateIndexRequest("<" + elasticPersistentEntity.getIndexName() + "-{now/d}-000001>");

                Alias alias = new Alias(elasticPersistentEntity.getIndexName());
                alias.writeIndex(true);
                createIndexRequest.alias(alias);
                createIndexRequest.settings(createSettings(elasticPersistentEntity));
                createIndex = restTemplate.execute(client -> client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged());
                elasticPersistentEntity.setIndexName("<" + elasticPersistentEntity.getIndexName() + "-{now/d}-000001>");
            } else {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest((elasticPersistentEntity.getIndexName()));
                createIndexRequest.settings(createSettings(elasticPersistentEntity));
                createIndex = restTemplate.execute(client -> client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged());
            }
            // 若存在映射文件,则创建索引的同时,也会根据Settings 映射创建mapping
//            createIndexRequest.settings(createSettings(elasticPersistentEntity));
//            Boolean createIndex = execute(client -> client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged());
            // index create success
            if (createIndex) {
                // 根据PersistentEntity 构建出mapping
                return createMapping(elasticPersistentEntity);
            }
        }
        log.warn("entity does not belong to SimpleElasticSearchPersistentEntity, can not auto create index!");
        return false;
    }

    @Override
    public boolean exists(String... indexNames) {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexNames);
        return restTemplate.execute(client -> client.indices().exists(getIndexRequest, RequestOptions.DEFAULT));
    }

    @Override
    public void refresh(String... indexNames) {

        Assert.notNull(indexNames, "No index defined for refresh()");

        RefreshRequest refreshRequest = new RefreshRequest(indexNames);
        restTemplate.execute(client -> client.indices().refresh(refreshRequest, RequestOptions.DEFAULT));
    }

    /**
     * <h2> 创建索引配置</h2>
     */
    private Map<String, ?> createSettings(SimpleElasticSearchPersistentEntity<?> entity) {
        Map<String, ?> mappingMap = null;
        if (entity.isAnnotationPresent(Setting.class)) {
            Setting settings = entity.findAnnotation(Setting.class);
            if (null != settings) {
                mappingMap = loadSettings(settings.settingPath());
            }
        }
        if (null == mappingMap) {
            mappingMap = entity.getDefaultSettings();
        }
        return mappingMap;
    }


    /**
     * <h2> 创建索引映射结构 </h2>
     */
    public boolean createMapping(SimpleElasticSearchPersistentEntity<?> clazz) {
        if (null == clazz) {
            throw new RuntimeException("PersistentEntity is null!");
        }
        Map<String, Object> mapping = buildMapping(clazz);

        PutMappingRequest request = new PutMappingRequest(clazz.getIndexName());
        request.source(mapping);
        return restTemplate.execute(client -> client.indices().putMapping(request, RequestOptions.DEFAULT).isAcknowledged());
    }


    /**
     * <h2> 构建索引映射 </h2>
     */
    protected Map<String, Object> buildMapping(SimpleElasticSearchPersistentEntity<?> clazz) {

        // load mapping specified in Mapping annotation if present
        if (clazz.isAnnotationPresent(Mapping.class)) {
            String mappingPath = null;
            Optional<Mapping> optionalMapping = Optional.ofNullable(clazz.findAnnotation(Mapping.class));
            if (optionalMapping.isPresent()) {
                mappingPath = optionalMapping.get().mappingPath();
            }
            if (!StringUtils.isEmpty(mappingPath)) {
                String mappings = ResourceUtil.readFileFromClasspath(mappingPath);
                if (!StringUtils.isEmpty(mappings)) {
                    return JSON.parseObject(mappings, new TypeReference<Map<String, Object>>() {
                    });
                }
            } else {
                log.info("mappingPath in @Mapping has to be defined. Building mappings using @Field");
            }
        }
        // build mapping from field annotations
        try {
            String mapping = this.buildPropertyMapping(clazz);
            return JSON.parseObject(mapping, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to build mapping for " + clazz.getName(), e);
        }
    }

    /**
     * <h2> 加载配置 </h2>
     */
    private Map<String, Object> loadSettings(String settingPath) {
        if (hasText(settingPath)) {
            String settingsFile = ResourceUtil.readFileFromClasspath(settingPath);

            if (hasText(settingsFile)) {
                return JSON.parseObject(settingsFile, new TypeReference<Map<String, Object>>() {
                });
            }
        } else {
            log.info("settingPath in @Setting has to be defined. Using default instead.");
        }
        return null;
    }

    /**
     * <h2> 构建属性映射配置 </h2>
     */
    protected String buildPropertyMapping(SimpleElasticSearchPersistentEntity<?> entity) throws IOException {

        XContentBuilder builder = jsonBuilder().startObject();
        // TODO Dynamic templates (暂不支持,等待后续开发)
//        addDynamicTemplatesMapping(builder, entity);
        // TODO Parent (@Parent) (暂不支持,等待后续开发)
//        String parentType = entity.getParentType();
        elasticsearchMappingBuilder.mapEntity(builder, entity, true,
                "", false, FieldType.Auto,
                null, entity.findAnnotation(DynamicMapping.class));
        builder.endObject() // root object
                .close();
        return builder.getOutputStream().toString();
    }

    @Override
    public void rollover(PersistentEntity<?, ?> persistentEntity, boolean isAsyn) throws Exception {
        if (persistentEntity instanceof SimpleElasticSearchPersistentEntity) {
            SimpleElasticSearchPersistentEntity<?> elasticPersistentEntity = (SimpleElasticSearchPersistentEntity<?>) persistentEntity;


            if (!elasticPersistentEntity.isRollover()) {
                return;
            }

            if (elasticPersistentEntity.isAutoRollover()) {
                rollover(elasticPersistentEntity);
                return;
            } else {
                if (isAsyn) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(1024);
                            rollover(elasticPersistentEntity);
                        } catch (Exception e) {
                            log.error("rollover error {}", e);
                        }

                    }).start();
                } else {
                    rollover(elasticPersistentEntity);
                }
            }
        }
    }


    /**
     * <h2> 滚动索引执行 </h2>
     */
    public void rollover(SimpleElasticSearchPersistentEntity<?> persistentEntity) {

        RolloverRequest rolloverRequest = new RolloverRequest(persistentEntity.getIndexName(), null);
        if (persistentEntity.getRolloverMaxIndexAgeCondition() != 0) {
            rolloverRequest.addMaxIndexAgeCondition(new TimeValue(persistentEntity.getRolloverMaxIndexAgeCondition(),
                    persistentEntity.getRolloverMaxIndexAgeTimeUnit()));

        }

        if (persistentEntity.getRolloverMaxIndexDocsCondition() != 0) {
            rolloverRequest.addMaxIndexDocsCondition(persistentEntity.getRolloverMaxIndexDocsCondition());
        }

        if (persistentEntity.getRolloverMaxIndexSizeCondition() != 0) {
            rolloverRequest.addMaxIndexSizeCondition(new ByteSizeValue(persistentEntity.getRolloverMaxIndexSizeCondition(),
                    persistentEntity.getRolloverMaxIndexSizeByteSizeUnit()));
        }

        try {
            RolloverResponse rolloverResponse = restTemplate.execute(client -> client.indices().rollover(rolloverRequest, RequestOptions.DEFAULT));
            log.info("rollover alias[" + persistentEntity.getIndexName() + "]结果：" + rolloverResponse.isAcknowledged());
        } catch (Exception e) {
            log.error("rollover error {}", e);
        }
    }
}

