package com.zqykj.tldw.aggregate.searching.esclientrhl.impl;

import com.zqykj.annotations.Id;
import com.zqykj.infrastructure.constant.Constants;
import com.zqykj.infrastructure.util.JsonUtils;
import com.zqykj.infrastructure.util.Tools;
import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import com.zqykj.tldw.aggregate.index.elasticsearch.associate.ElasticsearchIndexOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Description: Es operations commons api .
 * @Author zhangkehou
 * @Date 2021/8/9
 */
@Slf4j
public class SimpleElasticsearchOperations<T, M> implements ElasticsearchOperations<T, M> {


    private final ElasticsearchIndexOperations indexOperations;
    private final RepositoryInformation information;
    private final Class<T> entityClass;
    private final ElasticsearchRestTemplate restTemplate;


    private static Map<Class, String> classIDMap = new ConcurrentHashMap();

    @SuppressWarnings("unchecked")
    public SimpleElasticsearchOperations(RepositoryInformation information,
                                         ElasticsearchRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.information = information;
        this.entityClass = (Class<T>) information.getDomainType();
        // 获取一个索引操作类
        this.indexOperations = restTemplate.indexOps(entityClass);
        // 自动构建索引与mapping (每当注入一个 extends ElasticsearchOperations) 且开启自动创建与映射配置、索引名称在es中不存在 触发此操作
        if (restTemplate.getMappingContext().isAutoIndexCreation() && !indexOperations.exists(getIndexCoordinates())) {
            // 创建索引
            indexOperations.createIndex();
            // 创建mappings
            indexOperations.createMapping(entityClass);
        }
    }

    @Override
    public <S extends T> S save(S entity) {

        Assert.notNull(entity, "Cannot save 'null' entity.");

        // 保存并刷新索引,否则新增的数据不会被立刻检索到
        return executeAndRefresh(restTemplate -> restTemplate.save(entity, getIndexCoordinates()));
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {

        Assert.notNull(entities, "Cannot insert 'null' as a List.");
        String indexCoordinates = getIndexCoordinates();
        executeAndRefresh(restTemplate -> restTemplate.save(entities, indexCoordinates));
        return entities;
    }

    @Override
    public Optional<T> findById(M id, @NonNull String routing) {
        return Optional.ofNullable(
                restTemplate.get(Objects.toString(id, null), entityClass, getIndexCoordinates(), routing));
    }

    /**
     * <h2> 获取当前entityClass 的 indexName </h2>
     */
    private String getIndexCoordinates() {
        return restTemplate.getIndexCoordinatesFor(entityClass);
    }

    @FunctionalInterface
    public interface OperationsCallback<R> {
        @Nullable
        R doWithOperations(ElasticsearchRestTemplate restTemplate);
    }

    @Nullable
    public <R> R execute(OperationsCallback<R> callback) {
        return callback.doWithOperations(restTemplate);
    }

    @Nullable
    public <R> R executeAndRefresh(OperationsCallback<R> callback) {
        R result = callback.doWithOperations(restTemplate);
        refresh();
        return result;
    }

    public void refresh(String... indexName) {
        indexOperations.refresh(indexName);
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder) throws Exception {
        String indexName = getIndexCoordinates();
        return search(queryBuilder, indexName);
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder, String... indexs) throws Exception {

        List<T> list = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(indexs);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(Constants.DEFALT_PAGE_SIZE);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restTemplate.execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), entityClass);
            // assign the value of the returned result(_id) to the attribute annotated with @Id
            correctID(t, (M) hit.getId());
            list.add(t);
        }
        return list;
    }


    /**
     * assign the value of the returned result(_id) to the attribute annotated with @Id
     *
     * @param t
     * @param _id
     */
    private void correctID(T t, M _id) {
        try {
            if (ObjectUtils.isEmpty(_id)) {
                return;
            }
            if (classIDMap.containsKey(entityClass)) {
                Field field = entityClass.getDeclaredField(classIDMap.get(entityClass));
                field.setAccessible(true);
                // Assignment of non string type is not supported here. If the default ID is used, the ID must be of string type.
                if (field.get(t) == null) {
                    field.set(t, _id);
                }
                return;
            }
            for (int i = 0; i < entityClass.getDeclaredFields().length; i++) {
                Field field = entityClass.getDeclaredFields()[i];
                field.setAccessible(true);
                if (field.getAnnotation(Id.class) != null) {
                    classIDMap.put(entityClass, field.getName());
                    //  Assignment of non string type is not supported here. If the default ID is used, the ID must be of string type.
                    if (field.get(t) == null) {
                        field.set(t, _id);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Assign the value of the returned result(_id) to the attribute annotated with @Id error! {}", e);
        }
    }

    @Override
    public boolean deleteById(M id) throws Exception {

        return delete(id, null);
    }

    @Override
    public boolean delete(M id, String routing) throws Exception {
        String indexname = getIndexCoordinates();
        if (ObjectUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        DeleteRequest deleteRequest = new DeleteRequest(indexname, id.toString());
        if (!ObjectUtils.isEmpty(routing)) {
            deleteRequest.routing(routing);
        }
        DeleteResponse deleteResponse = restTemplate.execute(client -> client.delete(deleteRequest, RequestOptions.DEFAULT));
        if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
            refresh(indexname);
            log.info("Index delete success !");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public BulkByScrollResponse deleteByCondition(QueryBuilder queryBuilder) throws Exception {
        String indexname = getIndexCoordinates();
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexname);
        request.setQuery(queryBuilder);
        BulkByScrollResponse bulkResponse = restTemplate.execute(client -> client.deleteByQuery(request, RequestOptions.DEFAULT));
        refresh(indexname);
        return bulkResponse;
    }

    @Override
    public List<T> multiGetById(M[] ids) throws Exception {
        String indexname = getIndexCoordinates();
        MultiGetRequest request = new MultiGetRequest();
        for (int i = 0; i < ids.length; i++) {
            request.add(new MultiGetRequest.Item(indexname, ids[i].toString()));
        }
        MultiGetResponse response = restTemplate.execute(client -> client.mget(request, RequestOptions.DEFAULT));
        List<T> list = new ArrayList<>();
        for (int i = 0; i < response.getResponses().length; i++) {
            MultiGetItemResponse item = response.getResponses()[i];
            GetResponse getResponse = item.getResponse();
            if (getResponse.isExists()) {
                list.add(JsonUtils.string2Obj(getResponse.getSourceAsString(), entityClass));
            }
        }
        return list;
    }

    @Override
    public boolean updateByID(M id, String name) throws Exception {

        String indexname = getIndexCoordinates();

        if (ObjectUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }

        UpdateRequest updateRequest = new UpdateRequest(indexname, id.toString());

        UpdateResponse updateResponse = restTemplate.execute(client -> client.update(updateRequest, RequestOptions.DEFAULT));
        refresh(indexname);
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("Index update success !");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            log.info("Index update success !");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public BulkResponse[] bulkUpdate(List<T> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        String indexName = getIndexCoordinates();
        List<List<T>> splitList = restTemplate.spiltList(list, true);
        BulkResponse[] bulkResponses = new BulkResponse[splitList.size()];
        for (int i = 0; i < splitList.size(); i++) {
            bulkResponses[i] = updatePart(splitList.get(i), indexName);

        }
        return bulkResponses;
    }

    /**
     * description : batch update pojo list method .
     *
     * @param list:      pojo list
     * @param indexName: index name
     * @return: org.elasticsearch.action.bulk.BulkResponse
     **/
    public BulkResponse updatePart(List<T> list, String indexName) {
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            String id = restTemplate.getEntityId(t);
            bulkRequest.add(new UpdateRequest(indexName, id).doc(JsonUtils.obj2String(t), XContentType.JSON));
        }
        BulkResponse bulkResponse = executeAndRefresh(restTemplate -> restTemplate.execute(client -> client.bulk(bulkRequest, RequestOptions.DEFAULT)));
        return bulkResponse;
    }


    //    @Override
//    public void rollover(boolean isAsyn) throws exception {
//        if (!persistentEntity.isRollover()) {
//            return;
//        }
//
//        if (persistentEntity.isAutoRollover()) {
//            rollover();
//            return;
//        }else {
//            if (isAsyn){
//                new Thread (() ->{
//                    try {
//                        Thread.sleep(1024);
//                        rollover();
//                    } catch (exception e) {
//                       log.error("rollover error {}",e);
//                    }
//
//                }).start();
//            } else {
//                 rollover();
//            }
//        }
//    }

    //    private void rollover() {
//
//        RolloverRequest rolloverRequest = new RolloverRequest(persistentEntity.getIndexName(), null);
//        if (persistentEntity.getRolloverMaxIndexAgeCondition() != 0) {
//            rolloverRequest.addMaxIndexAgeCondition(new TimeValue(persistentEntity.getRolloverMaxIndexAgeCondition(),
//                    persistentEntity.getRolloverMaxIndexAgeTimeUnit()));
//
//        }
//
//        if (persistentEntity.getRolloverMaxIndexDocsCondition() != 0) {
//            rolloverRequest.addMaxIndexDocsCondition(persistentEntity.getRolloverMaxIndexDocsCondition());
//        }
//
//        if (persistentEntity.getRolloverMaxIndexSizeCondition() != 0) {
//            rolloverRequest.addMaxIndexSizeCondition(new ByteSizeValue(persistentEntity.getRolloverMaxIndexSizeCondition(),
//                    persistentEntity.getRolloverMaxIndexSizeByteSizeUnit()));
//        }
//
//
//        RolloverResponse rolloverResponse = execute(client ->client.indices().rollover(rolloverRequest, RequestOptions.DEFAULT));
//        log.info("rollover alias[" + persistentEntity.getIndexName() + "]结果：" + rolloverResponse.isAcknowledged());
//
//
//    }

}
