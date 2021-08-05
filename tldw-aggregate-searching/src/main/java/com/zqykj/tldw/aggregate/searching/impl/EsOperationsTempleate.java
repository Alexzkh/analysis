package com.zqykj.tldw.aggregate.searching.impl;

import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.index.ElasticsearchIndex;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.JsonUtils;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.MetaData;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.Tools;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/8/5
 */
@Component
public class EsOperationsTempleate<T,M> implements ElasticsearchTemplateOperations<T,M> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ElasticsearchIndex elasticsearchIndex;


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
    public boolean updateByID(M id, T t, String name) throws Exception{
        UpdateRequest updateRequest = new UpdateRequest(name,(String) id);
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
    public boolean update(T t, String name) throws Exception{
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
        return this.save(t,null);
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
