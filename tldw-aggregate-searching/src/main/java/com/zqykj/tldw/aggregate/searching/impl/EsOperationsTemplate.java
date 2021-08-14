package com.zqykj.tldw.aggregate.searching.impl;

import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticSearchPersistentEntity;
import com.zqykj.tldw.aggregate.index.elasticsearch.SimpleElasticsearchMappingContext;
import com.zqykj.tldw.aggregate.index.elasticsearch.associate.ElasticsearchIndexOperations;
import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.Constant;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.*;

/**
 * @Description: Es operations commons api .
 * @Author zhangkehou
 * @Date 2021/8/9
 */
@Slf4j
public class EsOperationsTemplate<T, M> implements ElasticsearchTemplateOperations<T, M> {

    private final ElasticsearchIndexOperations indexOperations;
    private final SimpleElasticsearchMappingContext mappingContext;
    private final RestHighLevelClient client;
    private final RepositoryInformation information;
    private final Class<T> entityClass;
    private final SimpleElasticSearchPersistentEntity<T> persistentEntity;

    @SuppressWarnings("unchecked")
    public EsOperationsTemplate(RepositoryInformation information,
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
    public boolean save(T t) {
        return false;
    }

    @Override
    public <T1> void create(T1 clazz) throws Exception {

    }

    @Override
    public boolean deleteByID(M id, Class<T> clazz) throws Exception {
        return false;
    }

    @Override
    public Optional<T> findById(M id, Class<T> clazz) throws Exception {
        return Optional.empty();
    }

    @Override
    public boolean updateByID(M id, T entity, String name) throws Exception {
        return false;
    }

    @Override
    public boolean update(T entity, String name) throws Exception {
        return false;
    }

//    public static EsOperationsTemplate open(final RestHighLevelClient client) {
//        return new EsOperationsTemplate(client);
//    }


//    @Override
//    public <T> void create(T clazz) {
//    }
//
//    public boolean create(T t, String routing) throws Exception {
////        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
////        String indexname = metaData.getIndexname();
//        String indexname = persistentEntity.getIndexName();
//        String id = Tools.getESId(t);
//        IndexRequest indexRequest = null;
//        if (ObjectUtils.isEmpty(id)) {
//            indexRequest = new IndexRequest(indexname);
//        } else {
//            indexRequest = new IndexRequest(indexname);
//            indexRequest.id(id);
//        }
//        String source = JsonUtils.obj2String(t);
//        indexRequest.source(source, XContentType.JSON);
//        if (!ObjectUtils.isEmpty(routing)) {
//            indexRequest.routing(routing);
//        }
//        IndexResponse indexResponse = null;
//        indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
//        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
//            log.info("INDEX CREATE SUCCESS");
//            // asyc execute rollover
//            elasticsearchIndex.rollover(t.getClass(), true);
//        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
//            log.info("INDEX UPDATE SUCCESS");
//        } else {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean deleteByID(M id, Class<T> clazz) throws Exception {
//
//        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
//        String indexname = metaData.getIndexname();
//        if (ObjectUtils.isEmpty(id)) {
//            throw new Exception("ID cannot be empty");
//        }
//        DeleteRequest deleteRequest = new DeleteRequest(indexname, id.toString());
//        DeleteResponse deleteResponse = null;
//        try {
//            deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
//            log.info("INDEX DELETE SUCCESS");
//            return true;
//        }
//        return false;
//    }
//
//
//    @Override
//    public Optional<T> findById(M id, Class<T> clazz) throws Exception {
//        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
//        String indexname = metaData.getIndexname();
//        if (ObjectUtils.isEmpty(id)) {
//            throw new Exception("ID cannot be empty");
//        }
//        GetRequest getRequest = new GetRequest(indexname, id.toString());
//        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
//        if (getResponse.isExists()) {
//            return Optional.of(JsonUtils.string2Obj(getResponse.getSourceAsString(), clazz));
//        }
//        return null;
//    }
//
//
//    @Override
//    public boolean updateByID(M id, T t, String name) throws Exception {
//        UpdateRequest updateRequest = new UpdateRequest(name, (String) id);
//        updateRequest.doc(Tools.getFieldValue(t));
//        UpdateResponse updateResponse = null;
//        updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
//        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
//            log.info("INDEX CREATE SUCCESS");
//        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
//            log.info("INDEX UPDATE SUCCESS");
//        } else {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean update(T t, String name) throws Exception {
//        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
//        String indexname = metaData.getIndexname();
//        String id = Tools.getESId(t);
//        if (ObjectUtils.isEmpty(id)) {
//            throw new Exception("ID cannot be empty");
//        }
//        if (Tools.checkNested(t)) {
//            throw new Exception("please using cover index update for nest object !");
//        }
//        UpdateRequest updateRequest = new UpdateRequest(indexname, id);
//        updateRequest.doc(Tools.getFieldValue(t));
//        UpdateResponse updateResponse = null;
//        updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
//        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
//            log.info("INDEX CREATE SUCCESS");
//        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
//            log.info("INDEX UPDATE SUCCESS");
//        } else {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean save(T t) throws Exception {
//        return this.save(t, null);
//    }
//
    @Override
    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        return search(queryBuilder, clazz, persistentEntity.getIndexName());
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception {
        List<T> list = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(indexs);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(Constant.DEFALT_PAGE_SIZE);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), clazz);
            list.add(t);
        }
        return list;
    }
//
//    private static Map<Class, String> classIDMap = new ConcurrentHashMap();
//
//    /**
//     * The _id field is reassigned to the field of the @ ESID annotation primary key
//     *
//     * @param clazz
//     * @param t
//     * @param _id
//     */
//    private void correctID(Class clazz, T t, M _id) {
//        try {
//            if (ObjectUtils.isEmpty(_id)) {
//                return;
//            }
//            if (classIDMap.containsKey(clazz)) {
//                Field field = clazz.getDeclaredField(classIDMap.get(clazz));
//                field.setAccessible(true);
//                //这里不支持非String类型的赋值，如果用默认的id，则id的类型一定是String类型的
//                if (field.get(t) == null) {
//                    field.set(t, _id);
//                }
//                return;
//            }
//            for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
//                Field field = clazz.getDeclaredFields()[i];
//                field.setAccessible(true);
////                if (field.getAnnotation(ESID.class) != null) {
////                    classIDMap.put(clazz, field.getName());
////                    //这里不支持非String类型的赋值，如果用默认的id，则id的类型一定是String类型的
////                    if (field.get(t) == null) {
////                        field.set(t, _id);
////                    }
////                }
//            }
//        } catch (Exception e) {
//            log.error("correctID error!", e);
//        }
//    }
//
//
//    public boolean save(T t, String routing) throws Exception {
//        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
//        String indexname = metaData.getIndexname();
//        String id = Tools.getESId(t);
//        IndexRequest indexRequest = null;
//        if (ObjectUtils.isEmpty(id)) {
//            indexRequest = new IndexRequest(indexname);
//        } else {
//            indexRequest = new IndexRequest(indexname);
//            indexRequest.id(id);
//        }
//        String source = JsonUtils.obj2String(t);
//        indexRequest.source(source, XContentType.JSON);
//        if (!ObjectUtils.isEmpty(routing)) {
//            indexRequest.routing(routing);
//        }
//        IndexResponse indexResponse = null;
//        indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
//        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
//            log.info("INDEX CREATE SUCCESS");
//            // asyc execute rollover
//            elasticsearchIndex.rollover(t.getClass(), true);
//        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
//            log.info("INDEX UPDATE SUCCESS");
//        } else {
//            return false;
//        }
//        return true;
//    }
//
//
    /**
     * @param ids:   the list of id .
     * @param clazz: the query entity .
     * @return: java.util.List<T>
     **/
    public List<T> mgetById(M[] ids, Class<T> clazz) throws Exception {
        String indexname =  persistentEntity.getIndexName();
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
                list.add(JsonUtils.string2Obj(getResponse.getSourceAsString(), clazz));
            }
        }
        return list;
    }
}
