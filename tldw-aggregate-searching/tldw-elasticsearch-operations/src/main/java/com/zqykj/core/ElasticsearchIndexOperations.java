/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.core.document.Document;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.client.indices.rollover.RolloverRequest;
import org.elasticsearch.client.indices.rollover.RolloverResponse;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * <h2> ElasticSearch Index Operations</h2>
 */
@Slf4j
public class ElasticsearchIndexOperations extends AbstractDefaultIndexOperations
        implements IndexOperations {

    private final ElasticsearchRestTemplate restTemplate;

    public ElasticsearchIndexOperations(ElasticsearchRestTemplate restTemplate, Class<?> boundClass) {
        super(restTemplate.getElasticsearchConverter(), boundClass);
        this.restTemplate = restTemplate;
    }

    public ElasticsearchIndexOperations(ElasticsearchRestTemplate restTemplate, String boundIndex) {
        super(restTemplate.getElasticsearchConverter(), boundIndex);
        this.restTemplate = restTemplate;
    }

    @Override
    protected boolean doCreate(String index, @Nullable Document settings) {
        CreateIndexRequest request = requestFactory.createIndexRequest(index, settings, null);
        return restTemplate.execute(client -> client.indices().create(request, RequestOptions.DEFAULT).isAcknowledged());
    }

    @Override
    protected boolean doCreateRollover(String index, @Nullable Document settings, Alias alias) {
        CreateIndexRequest request = requestFactory.createIndexRequest(index, settings, alias);
        return restTemplate.execute(client -> client.indices().create(request, RequestOptions.DEFAULT).isAcknowledged());
    }

    @Override
    protected boolean doDelete(String index) {

        Assert.notNull(index, "index must not be null");

        if (doExists(index)) {
            DeleteIndexRequest deleteIndexRequest = requestFactory.deleteIndexRequest(index);
            return restTemplate
                    .execute(client -> client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT).isAcknowledged());
        }
        return false;
    }

    @Override
    protected boolean doExists(String index) {

        GetIndexRequest getIndexRequest = requestFactory.getIndexRequest(index);
        return restTemplate.execute(client -> client.indices().exists(getIndexRequest, RequestOptions.DEFAULT));
    }

    @Override
    protected boolean doPutMapping(String index, Document mapping) {

        Assert.notNull(index, "No index defined for putMapping()");

        PutMappingRequest request = requestFactory.putMappingRequest(mapping, index);
        return restTemplate
                .execute(client -> client.indices().putMapping(request, RequestOptions.DEFAULT).isAcknowledged());
    }

    @Override
    protected void doRefresh(String index) {

        Assert.notNull(index, "No index defined for refresh()");

        RefreshRequest refreshRequest = requestFactory.refreshRequest(index);
        restTemplate.execute(client -> client.indices().refresh(refreshRequest, RequestOptions.DEFAULT));
    }

    /**
     * <h2> 滚动索引执行 </h2>
     */
    public void rollover(ElasticsearchPersistentEntity<?> entity) {

        RolloverRequest rolloverRequest = new RolloverRequest(entity.getIndexName(), null);
        if (entity.getRolloverMaxIndexAgeCondition() != 0) {
            rolloverRequest.addMaxIndexAgeCondition(new TimeValue(entity.getRolloverMaxIndexAgeCondition(),
                    entity.getRolloverMaxIndexAgeTimeUnit()));

        }

        if (entity.getRolloverMaxIndexDocsCondition() != 0) {
            rolloverRequest.addMaxIndexDocsCondition(entity.getRolloverMaxIndexDocsCondition());
        }

        if (entity.getRolloverMaxIndexSizeCondition() != 0) {
            rolloverRequest.addMaxIndexSizeCondition(new ByteSizeValue(entity.getRolloverMaxIndexSizeCondition(),
                    entity.getRolloverMaxIndexSizeByteSizeUnit()));
        }

        try {
            RolloverResponse rolloverResponse =
                    restTemplate.execute(client -> client.indices().rollover(rolloverRequest, RequestOptions.DEFAULT));
            log.info("rollover alias[" + entity.getIndexName() + "]结果：" + rolloverResponse.isAcknowledged());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("rollover error {}", e.toString());
        }
    }
}

