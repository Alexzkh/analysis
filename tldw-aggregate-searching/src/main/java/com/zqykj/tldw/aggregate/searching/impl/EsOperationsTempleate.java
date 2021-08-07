package com.zqykj.tldw.aggregate.searching.impl;

import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESID;
import com.zqykj.tldw.aggregate.searching.esclientrhl.index.ElasticsearchIndex;
import com.zqykj.tldw.aggregate.searching.esclientrhl.index.ElasticsearchIndexImpl;
import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.ElasticsearchTemplateImpl;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.Constant;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.JsonUtils;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.MetaData;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.Tools;
import org.elasticsearch.action.DocWriteResponse;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/8/5
 */
//@Component
public class EsOperationsTempleate<T, M> implements ElasticsearchTemplateOperations<T, M> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //    @Autowired
    RestHighLevelClient client;

    //    @Autowired
    ElasticsearchIndex elasticsearchIndex;

    public EsOperationsTempleate( RestHighLevelClient client){
        this.client = client;
        this.elasticsearchIndex = new ElasticsearchIndexImpl(client);
    }

    public static EsOperationsTempleate open(final RestHighLevelClient client)  {
        return new EsOperationsTempleate(client);
    }



    @Override
    public <T1> void create(Class<T1> clazz) {

    }

    @Override
    public long deleteByID(M id, String name) {
        return 0;
    }

    @Override
    public long batchDelteByID(Collection<M> ids, String name) {
        return 0;
    }

    @Override
    public Optional<T> findById(M id, String name) {
        return Optional.empty();
    }

    @Override
    public List<T> findAllByIDs(Collection<M> ids, String name) {
        return null;
    }

    @Override
    public boolean updateByID(M id, T t, String name) throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(name, (String) id);
        updateRequest.doc(Tools.getFieldValue(t));
        UpdateResponse updateResponse = null;
        updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            logger.info("INDEX CREATE SUCCESS");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            logger.info("INDEX UPDATE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean update(T t, String name) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String id = Tools.getESId(t);
        if (ObjectUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        if (Tools.checkNested(t)) {
            throw new Exception("please using cover index update for nest object !");
        }
        UpdateRequest updateRequest = new UpdateRequest(indexname, id);
        updateRequest.doc(Tools.getFieldValue(t));
        UpdateResponse updateResponse = null;
        updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            logger.info("INDEX CREATE SUCCESS");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            logger.info("INDEX UPDATE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean save(T t) throws Exception {
        return this.save(t, null);
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return search(queryBuilder, clazz, indexname);
    }

    @Override
    public List<T> search(QueryBuilder queryBuilder, Class<T> clazz, String... indexs) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(clazz);
        List<T> list = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(indexs);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(Constant.DEFALT_PAGE_SIZE);
        searchRequest.source(searchSourceBuilder);
        if (metaData.isPrintLog()) {
            logger.info(searchSourceBuilder.toString());
        }
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(), clazz);
            // The _id field is reassigned to the field of the @ ESID annotation primary key
            correctID(clazz, t, (M) hit.getId());
            list.add(t);
        }
        return list;
    }

    private static Map<Class, String> classIDMap = new ConcurrentHashMap();

    /**
     * The _id field is reassigned to the field of the @ ESID annotation primary key
     *
     * @param clazz
     * @param t
     * @param _id
     */
    private void correctID(Class clazz, T t, M _id) {
        try {
            if (ObjectUtils.isEmpty(_id)) {
                return;
            }
            if (classIDMap.containsKey(clazz)) {
                Field field = clazz.getDeclaredField(classIDMap.get(clazz));
                field.setAccessible(true);
                //这里不支持非String类型的赋值，如果用默认的id，则id的类型一定是String类型的
                if (field.get(t) == null) {
                    field.set(t, _id);
                }
                return;
            }
            for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
                Field field = clazz.getDeclaredFields()[i];
                field.setAccessible(true);
                if (field.getAnnotation(ESID.class) != null) {
                    classIDMap.put(clazz, field.getName());
                    //这里不支持非String类型的赋值，如果用默认的id，则id的类型一定是String类型的
                    if (field.get(t) == null) {
                        field.set(t, _id);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("correctID error!", e);
        }
    }


    public boolean save(T t, String routing) throws Exception {
        MetaData metaData = elasticsearchIndex.getMetaData(t.getClass());
        String indexname = metaData.getIndexname();
        String id = Tools.getESId(t);
        IndexRequest indexRequest = null;
        if (ObjectUtils.isEmpty(id)) {
            indexRequest = new IndexRequest(indexname);
        } else {
            indexRequest = new IndexRequest(indexname);
            indexRequest.id(id);
        }
        String source = JsonUtils.obj2String(t);
        indexRequest.source(source, XContentType.JSON);
        if (!ObjectUtils.isEmpty(routing)) {
            indexRequest.routing(routing);
        }
        IndexResponse indexResponse = null;
        indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            logger.info("INDEX CREATE SUCCESS");
            // asyc execute rollover
            elasticsearchIndex.rollover(t.getClass(), true);
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            logger.info("INDEX UPDATE SUCCESS");
        } else {
            return false;
        }
        return true;
    }
}
