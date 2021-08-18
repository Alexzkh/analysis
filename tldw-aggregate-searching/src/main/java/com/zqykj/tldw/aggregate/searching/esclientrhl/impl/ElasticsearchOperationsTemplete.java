package com.zqykj.tldw.aggregate.searching.esclientrhl.impl;

import com.zqykj.annotations.Id;
import com.zqykj.infrastructure.constant.Constants;
import com.zqykj.infrastructure.util.JsonUtils;
import com.zqykj.infrastructure.util.Tools;
import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import com.zqykj.tldw.aggregate.index.elasticsearch.associate.ElasticsearchIndexOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.ElasticsearchOperations;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
public class ElasticsearchOperationsTemplete<T, M> implements ElasticsearchOperations<T, M> {


    private final ElasticsearchIndexOperations indexOperations;
    private final SimpleElasticsearchMappingContext mappingContext;
    private final RestHighLevelClient client;
    private final RepositoryInformation information;
    private final Class<T> entityClass;
    private final SimpleElasticSearchPersistentEntity<T> persistentEntity;


    private static Map<Class, String> classIDMap = new ConcurrentHashMap();

    @SuppressWarnings("unchecked")
    public ElasticsearchOperationsTemplete(RepositoryInformation information,
                                           ElasticsearchIndexOperations indexOperations,
                                           SimpleElasticsearchMappingContext mappingContext) {
        this.information = information;
        this.indexOperations = indexOperations;
        this.mappingContext = mappingContext;
        this.client = indexOperations.getClient();
        this.persistentEntity = (SimpleElasticSearchPersistentEntity<T>) mappingContext.getPersistentEntity(information.getDomainType());
        this.entityClass = (Class<T>) information.getDomainType();
    }


    @Override
    public boolean save(T t) throws Exception {

        return save(t,null);
    }

    @Override
    public boolean save(T t,String routing) throws Exception {
        String indexName = persistentEntity.getIndexName();
        String id = Tools.getESId(entityClass);
        IndexRequest indexRequest = new IndexRequest(indexName);
        if (ObjectUtils.isEmpty(id)) {
            indexRequest.id(id);
        }

        String source = JsonUtils.obj2String(t);
        indexRequest.source(source, XContentType.JSON);
        if (!ObjectUtils.isEmpty(routing)) {
            indexRequest.routing(routing);
        }
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("Index create success !");
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            log.info("Index update success !");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder) throws Exception {
        String indexName = persistentEntity.getIndexName();
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

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
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
        String indexname = persistentEntity.getIndexName();
        if (ObjectUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        DeleteRequest deleteRequest = new DeleteRequest(indexname, id.toString());
        if (!ObjectUtils.isEmpty(routing)) {
            deleteRequest.routing(routing);
        }
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
            log.info("Index delete success !");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public BulkByScrollResponse deleteByCondition(QueryBuilder queryBuilder) throws Exception {
        String indexname = persistentEntity.getIndexName();
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexname);
        request.setQuery(queryBuilder);
        BulkByScrollResponse bulkResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
        return bulkResponse;
    }

    @Override
    public Optional<T> findById(M id) throws Exception {
        String indexname = persistentEntity.getIndexName();
        if (ObjectUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        GetRequest getRequest = new GetRequest(indexname, id.toString());
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        if (getResponse.isExists()) {
            return Optional.of(JsonUtils.string2Obj(getResponse.getSourceAsString(), entityClass));
        }
        return Optional.empty();
    }

    @Override
    public List<T> multiGetById(M[] ids) throws Exception {
        String indexname = persistentEntity.getIndexName();
        MultiGetRequest request = new MultiGetRequest();
        for (int i = 0; i < ids.length; i++) {
            request.add(new MultiGetRequest.Item(indexname, ids[i].toString()));
        }
        MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
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
    public BulkResponse save(List<T> list) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        String indexname = persistentEntity.getIndexName();
        return savePart(list,indexname);
    }

    /**
     * @param list: pojo list
     * @param indexname:  operate index
     * @return: org.elasticsearch.action.bulk.BulkResponse
     **/
    private BulkResponse savePart(List<T> list,String indexname) throws Exception {
        BulkRequest bulkRequest  = new BulkRequest();
        for (int i = 0; i < list.size(); i++) {
            T tt = list.get(i);
            String id = Tools.getESId(tt);
            String sourceJsonStr = JsonUtils.obj2String(tt);
            IndexRequest indexRequest = new IndexRequest(indexname);
            indexRequest.id(id);
            indexRequest.source(sourceJsonStr,XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        return bulkResponse;
    }

    @Override
    public boolean updateByID(M id, String name) throws Exception {

        String indexname = persistentEntity.getIndexName();

        if (ObjectUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }

        UpdateRequest updateRequest = new UpdateRequest(indexname, id.toString());

        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("Index update success !");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            log.info("Index update success !");
        } else {
            return false;
        }
        return true;
    }


}
