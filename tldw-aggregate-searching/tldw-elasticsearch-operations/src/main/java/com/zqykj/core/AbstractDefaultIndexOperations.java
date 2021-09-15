/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.annotations.Mapping;
import com.zqykj.annotations.Setting;
import com.zqykj.core.convert.ElasticsearchConverter;
import com.zqykj.core.document.Document;
import com.zqykj.core.index.MappingBuilder;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.exception.UncategorizedElasticsearchException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static org.springframework.util.StringUtils.hasText;


/**
 * <h2>  Elasticsearch 抽象数据源索引操作 </h2>
 */
@Slf4j
public abstract class AbstractDefaultIndexOperations implements IndexOperations {

    protected final ElasticsearchConverter elasticsearchConverter;
    protected final RequestFactory requestFactory;

    /**
     * Elasticsearch Repository interface 泛型参数绑定的索引类
     */
    @Nullable
    protected final Class<?> boundClass;

    /**
     * Elasticsearch Repository interface 泛型参数绑定的索引类 绑定的索引名称
     */
    @Nullable
    protected final String boundIndex;

    public AbstractDefaultIndexOperations(ElasticsearchConverter elasticsearchConverter,
                                          @Nullable Class<?> boundClass) {

        Assert.notNull(boundClass, "boundClass may not be null");
        this.elasticsearchConverter = elasticsearchConverter;
        requestFactory = new RequestFactory(elasticsearchConverter);
        this.boundClass = boundClass;
        this.boundIndex = null;
    }

    public AbstractDefaultIndexOperations(ElasticsearchConverter elasticsearchConverter,
                                          @Nullable String boundIndex) {

        Assert.notNull(boundIndex, "boundIndex may not be null");

        this.elasticsearchConverter = elasticsearchConverter;
        requestFactory = new RequestFactory(elasticsearchConverter);
        this.boundClass = null;
        this.boundIndex = boundIndex;
    }

    /**
     * <h2> 检查绑定的索引类 </h2>
     */
    protected Class<?> checkForBoundClass() {
        if (boundClass == null) {
            throw new InvalidDataAccessApiUsageException("IndexOperations are not bound");
        }
        return boundClass;
    }

    @Override
    public boolean create() {

        Document settings = null;

        if (boundClass != null) {
            settings = createSettings(boundClass);
        }

        return doCreate(getIndexCoordinates(), settings);
    }

    @Override
    public boolean createIndexRollover() {

        Document settings = null;

        if (boundClass != null) {
            settings = createSettings(boundClass);
        }
        boolean isSuccess = doCreateRollover(getRolloverIndexCoordinates(), settings, getAlias());
        if (isSuccess) {
            setIndexName("<" + getIndexCoordinates() + "-{now/d}-000001>");
        }
        return isSuccess;
    }

    @Override
    public boolean createOrRollover() {
        if (isCreateIndexByRollover()) {

            return this.createIndexRollover();
        } else {

            return this.create();
        }
    }

    @Override
    public Document createSettings(Class<?> clazz) {

        Assert.notNull(clazz, "clazz must not be null");

        Document settings = null;

        if (clazz.isAnnotationPresent(Setting.class)) {
            String settingPath = clazz.getAnnotation(Setting.class).settingPath();
            settings = loadSettings(settingPath);
        }

        if (settings == null) {
            settings = getRequiredPersistentEntity(clazz).getDefaultSettings();
        }

        return settings;
    }

    @Override
    public boolean create(Document settings) {
        return doCreate(getIndexCoordinates(), settings);
    }

    protected abstract boolean doCreate(String index, @Nullable Document settings);

    protected abstract boolean doCreateRollover(String index, @Nullable Document settings, Alias alias);

    @Override
    public boolean delete() {
        return doDelete(getIndexCoordinates());
    }

    protected abstract boolean doDelete(String index);

    @Override
    public boolean exists() {
        return doExists(getIndexCoordinates());
    }

    protected abstract boolean doExists(String index);

    @Override
    public boolean putMapping(Document mapping) {
        return doPutMapping(getIndexCoordinates(), mapping);
    }

    protected abstract boolean doPutMapping(String index, Document mapping);

    @Override
    public void refresh() {
        doRefresh(getIndexCoordinates());
    }

    protected abstract void doRefresh(String index);


    @Override
    public Document createMapping(Class<?> clazz) {
        return buildMapping(clazz);
    }

    @Override
    public Document createMapping() {
        return createMapping(checkForBoundClass());
    }

    /**
     * <h2> 构建映射 </h2>
     */
    protected Document buildMapping(Class<?> clazz) {

        // load mapping specified in Mapping annotation if present
        if (clazz.isAnnotationPresent(Mapping.class)) {
            String mappingPath = clazz.getAnnotation(Mapping.class).mappingPath();

            if (!StringUtils.isEmpty(mappingPath)) {
                String mappings = ResourceUtil.readFileFromClasspath(mappingPath);

                if (!StringUtils.isEmpty(mappings)) {
                    return Document.parse(mappings);
                }
            } else {
                log.info("mappingPath in @Mapping has to be defined. Building mappings using @Field");
            }
        }

        // build mapping from field annotations
        try {
            String mapping = new MappingBuilder(elasticsearchConverter.getMappingContext()).buildPropertyMapping(clazz);
            return Document.parse(mapping);
        } catch (Exception e) {
            throw new UncategorizedElasticsearchException("Failed to build mapping for " + clazz.getSimpleName(), e);
        }
    }

    @Override
    public Document createSettings() {
        return createSettings(checkForBoundClass());
    }

    @Nullable
    private Document loadSettings(String settingPath) {
        if (hasText(settingPath)) {
            String settingsFile = ResourceUtil.readFileFromClasspath(settingPath);

            if (hasText(settingsFile)) {
                return Document.parse(settingsFile);
            }
        } else {
            log.info("settingPath in @Setting has to be defined. Using default instead.");
        }
        return null;
    }

    ElasticsearchPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
        return elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
    }

    @Override
    public String getIndexCoordinates() {
        return (boundClass != null) ? getIndexCoordinatesFor(boundClass) : Objects.requireNonNull(boundIndex);
    }

    public String getRolloverIndexCoordinates() {
        return "<" + getIndexCoordinates() + "-{now/d}-000001>";
    }

    public Alias getAlias() {
        Alias alias = new Alias(getIndexCoordinates());
        alias.writeIndex(true);
        return alias;
    }

    @Override
    public void rollover(boolean isAsync) {

        ElasticsearchPersistentEntity<?> entity = getRequiredPersistentEntity(boundClass);

        if (!entity.isRollover()) {
            return;
        }

        if (entity.isAutoRollover()) {
            rollover(entity);
        } else {
            if (isAsync) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1024);
                        rollover(entity);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("rollover error {}", e.toString());
                    }

                }).start();
            } else {
                rollover(entity);
            }
        }
    }

    protected abstract void rollover(ElasticsearchPersistentEntity<?> entity);

    public boolean isCreateIndexByRollover() {
        ElasticsearchPersistentEntity<?> entity = getRequiredPersistentEntity(boundClass);
        if (entity.isRollover()) {
            if (entity.getRolloverMaxIndexAgeCondition() == 0
                    && entity.getRolloverMaxIndexDocsCondition() == 0
                    && entity.getRolloverMaxIndexSizeCondition() == 0) {
                throw new RuntimeException("rolloverMaxIndexAgeCondition is zero OR rolloverMaxIndexDocsCondition is zero OR rolloverMaxIndexSizeCondition is zero");
            }
            return true;
        }
        return false;
    }

    public String getIndexCoordinatesFor(Class<?> clazz) {
        return getRequiredPersistentEntity(clazz).getIndexName();
    }

    protected void setIndexName(String indexName) {
        getRequiredPersistentEntity(boundClass).setIndexName(indexName);
    }
}


