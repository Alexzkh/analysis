package com.zqykj.tldw.aggregate.searching.esclientrhl.index;

import com.zqykj.tldw.aggregate.searching.esclientrhl.util.IndexTools;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.MappingData;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.MetaData;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.Tools;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.rollover.RolloverRequest;
import org.elasticsearch.client.indices.rollover.RolloverResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Index structure basic method {@link com.zqykj.tldw.aggregate.searching.esclientrhl.index.ElasticsearchIndex} implementation class
 **/
//@Component
public class ElasticsearchIndexImpl<T> implements ElasticsearchIndex<T> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

//    @Autowired
    RestHighLevelClient client;

    private static final String NESTED = "nested";

//    @Autowired
    private IndexTools indexTools;

    public ElasticsearchIndexImpl(RestHighLevelClient client){
        this.client =client;
        this.indexTools = new IndexTools();
    }


    @Override
    public void createIndex(Class<T> clazz) throws Exception {
        MetaData metaData = indexTools.getMetaData(clazz);
        MappingSetting mappingSource = getMappingSource(clazz, metaData);
        CreateIndexRequest request = null;
        // If rollover is configured, replace the index name with the rollover name and create the corresponding alias
        if (metaData.isRollover()) {
            if (metaData.getRolloverMaxIndexAgeCondition() == 0
                    && metaData.getRolloverMaxIndexDocsCondition() == 0
                    && metaData.getRolloverMaxIndexSizeCondition() == 0) {
                throw new RuntimeException("rolloverMaxIndexAgeCondition is zero OR rolloverMaxIndexDocsCondition is zero OR rolloverMaxIndexSizeCondition is zero");
            }
            request = new CreateIndexRequest("<" + metaData.getIndexname() + "-{now/d}-000001>");
            Alias alias = new Alias(metaData.getIndexname());
            alias.writeIndex(true);
            request.alias(alias);
        } else {
            request = new CreateIndexRequest(metaData.getIndexname());
        }
        try {
            request.settings(mappingSource.builder);
            request.mapping(
                    mappingSource.mappingSource,
                    XContentType.JSON);
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            // The returned create index response allows you to retrieve information about the operation performed
            boolean acknowledged = createIndexResponse.isAcknowledged();
            logger.info("创建索引[" + metaData.getIndexname() + "]结果：" + acknowledged);
        } catch (IOException e) {
            logger.error("createIndex error", e);
        }
    }

    private static class MappingSetting {
        protected Settings.Builder builder;
        protected String mappingSource;
    }


    /**
     *
     * @param clazz: The document class that annotation by {@link com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData}
     * @param metaData: {@link MetaData} annotation are used to record index settings.
     * @return: {@link com.zqykj.tldw.aggregate.searching.esclientrhl.index.ElasticsearchIndexImpl.MappingSetting}
     **/
    private MappingSetting getMappingSource(Class clazz, MetaData metaData) throws Exception {
        StringBuffer source = new StringBuffer();
        source.append("  {\n" +

//                "    \"" + metaData.getIndextype() + "\": {\n" +
                "      \"properties\": {\n");
        MappingData[] mappingDataList = indexTools.getMappingData(clazz);
        boolean isNgram = false;
        for (int i = 0; i < mappingDataList.length; i++) {
            MappingData mappingData = mappingDataList[i];
            if (mappingData == null || mappingData.getField_name() == null) {
                continue;
            }
            source.append(" \"" + mappingData.getField_name() + "\": {\n");
            source.append(" \"type\": \"" + mappingData.getDatatype() + "\"\n");

            if (!mappingData.getDatatype().equals(NESTED)) {
                if (mappingData.isNgram() &&
                        (mappingData.getDatatype().equals("text") || mappingData.getDatatype().equals("keyword"))) {
                    isNgram = true;
                }
                source.append(oneField(mappingData));
            } else {
                source.append(" ,\"properties\": { ");
                if (mappingData.getNested_class() != null && mappingData.getNested_class() != Object.class) {
                    MappingData[] submappingDataList = indexTools.getMappingData(mappingData.getNested_class());
                    for (int j = 0; j < submappingDataList.length; j++) {
                        MappingData submappingData = submappingDataList[j];
                        if (submappingData == null || submappingData.getField_name() == null) {
                            continue;
                        }
                        source.append(" \"" + submappingData.getField_name() + "\": {\n");
                        source.append(" \"type\": \"" + submappingData.getDatatype() + "\"\n");

                        if (j == submappingDataList.length - 1) {
                            source.append(" }\n");
                        } else {
                            source.append(" },\n");
                        }
                    }
                } else {
                    throw new Exception("无法识别的Nested_class");
                }
                source.append(" }");
            }
            if (i == mappingDataList.length - 1) {
                source.append(" }\n");
            } else {
                source.append(" },\n");
            }
        }
        source.append(" }\n");
//        source.append(" }\n");
        source.append(" }\n");
        logger.info(source.toString());
        Settings.Builder builder = null;
        if (isNgram) {
            builder = Settings.builder()
                    .put("index.number_of_shards", metaData.getNumber_of_shards())
                    .put("index.number_of_replicas", metaData.getNumber_of_replicas())
                    .put("index.max_result_window", metaData.getMaxResultWindow())
                    .put("analysis.filter.autocomplete_filter.type", "edge_ngram")
                    .put("analysis.filter.autocomplete_filter.min_gram", 1)
                    .put("analysis.filter.autocomplete_filter.max_gram", 20)
                    .put("analysis.analyzer.autocomplete.type", "custom")
                    .put("analysis.analyzer.autocomplete.tokenizer", "standard")
                    .putList("analysis.analyzer.autocomplete.filter", new String[]{"lowercase", "autocomplete_filter"});
        } else {
            builder = Settings.builder()
                    .put("index.number_of_shards", metaData.getNumber_of_shards())
                    .put("index.number_of_replicas", metaData.getNumber_of_replicas())
                    .put("index.max_result_window", metaData.getMaxResultWindow());
        }


        MappingSetting mappingSetting = new MappingSetting();
        mappingSetting.mappingSource = source.toString();
        mappingSetting.builder = builder;
        return mappingSetting;
    }

    @Override
    public void switchAliasWriteIndex(Class<T> clazz, String writeIndex) throws Exception {
        MetaData metaData = indexTools.getMetaData(clazz);

        // When the alias is configured, the automatic index creation function will be disabled
        if (metaData.isAlias()) {
            if (Tools.arrayISNULL(metaData.getAliasIndex())) {
                throw new RuntimeException("aliasIndex must not be null");
            }
            if (ObjectUtils.isEmpty(writeIndex)) {
                // If write index is empty, the last alias index is write index by default
                metaData.setWriteIndex(metaData.getAliasIndex()[metaData.getAliasIndex().length - 1]);
            } else if (!Stream.of(metaData.getAliasIndex()).collect(Collectors.toList()).contains(metaData.getWriteIndex())) {
                throw new RuntimeException("aliasIndex must contains writeIndex");
            }
            //create Alias
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            Stream.of(metaData.getAliasIndex()).forEach(s -> {
                IndicesAliasesRequest.AliasActions aliasAction =
                        new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                                .index(s)
                                .alias(metaData.getIndexname());
                if (s.equals(writeIndex)) {
                    aliasAction.writeIndex(true);
                }
                request.addAliasAction(aliasAction);
            });
            AcknowledgedResponse indicesAliasesResponse = client.indices().updateAliases(request, RequestOptions.DEFAULT);
            logger.info("Update Alias[" + metaData.getIndexname() + "] result：" + indicesAliasesResponse.isAcknowledged());
        }
    }

    @Override
    public void createAlias(Class<T> clazz) throws Exception {
        MetaData metaData = indexTools.getMetaData(clazz);
        // When the alias is configured, the automatic index creation function will be disabled
        if (metaData.isAlias()) {
            if (Tools.arrayISNULL(metaData.getAliasIndex())) {
                throw new RuntimeException("aliasIndex must not be null");
            }
            if (ObjectUtils.isEmpty(metaData.getWriteIndex())) {
                // If write index is empty, the last alias index is write index by default
                metaData.setWriteIndex(metaData.getAliasIndex()[metaData.getAliasIndex().length - 1]);
            } else if (!Stream.of(metaData.getAliasIndex()).collect(Collectors.toList()).contains(metaData.getWriteIndex())) {
                throw new RuntimeException("aliasIndex must contains writeIndex");
            }
            // Judge whether alias exists, and jump out directly if it exists
            GetAliasesRequest requestWithAlias = new GetAliasesRequest(metaData.getIndexname());
            boolean exists = client.indices().existsAlias(requestWithAlias, RequestOptions.DEFAULT);
            if (exists) {
                logger.info("Alias[" + metaData.getIndexname() + "]已经存在");
            } else {
                // Create Alias
                IndicesAliasesRequest request = new IndicesAliasesRequest();
                Stream.of(metaData.getAliasIndex()).forEach(s -> {
                    IndicesAliasesRequest.AliasActions aliasAction =
                            new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                                    .index(s)
                                    .alias(metaData.getIndexname());
                    if (s.equals(metaData.getWriteIndex())) {
                        aliasAction.writeIndex(true);
                    }
                    request.addAliasAction(aliasAction);
                });
                AcknowledgedResponse indicesAliasesResponse = client.indices().updateAliases(request, RequestOptions.DEFAULT);
                logger.info("创建Alias[" + metaData.getIndexname() + "]结果：" + indicesAliasesResponse.isAcknowledged());
            }
        }
    }

    @Override
    public void createIndex(Map<String, String> settings, Map<String, String[]> settingsList, String mappingJson, String indexName) throws Exception {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        Settings.Builder build = Settings.builder();
        if (settings != null) {
            settings.forEach((k, v) -> build.put(k, v));
        }
        if (settingsList != null) {
            settings.forEach((k, v) -> build.putList(k, v));
        }
        request.mapping(
                mappingJson,
                XContentType.JSON);
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            boolean acknowledged = createIndexResponse.isAcknowledged();
            logger.info("创建索引[" + indexName + "]结果：" + acknowledged);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * not nested mapping
     *
     * @param mappingData
     * @return
     */
    private String oneField(MappingData mappingData) {
        StringBuilder source = new StringBuilder();
        if (!ObjectUtils.isEmpty(mappingData.getCopy_to())) {
            source.append(" ,\"copy_to\": \"" + mappingData.getCopy_to() + "\"\n");
        }
        if (!ObjectUtils.isEmpty(mappingData.getNull_value())) {
            source.append(" ,\"null_value\": \"" + mappingData.getNull_value() + "\"\n");
        }
        if (!mappingData.isAllow_search()) {
            source.append(" ,\"index\": false\n");
        }
        if (mappingData.isNgram() && (mappingData.getDatatype().equals("text") || mappingData.getDatatype().equals("keyword"))) {
            source.append(" ,\"analyzer\": \"autocomplete\"\n");
            source.append(" ,\"search_analyzer\": \"standard\"\n");

        } else if (mappingData.getDatatype().equals("text")) {
            source.append(" ,\"analyzer\": \"" + mappingData.getAnalyzer() + "\"\n");
            source.append(" ,\"search_analyzer\": \"" + mappingData.getSearch_analyzer() + "\"\n");
        }

        if (mappingData.isKeyword() && !mappingData.getDatatype().equals("keyword") && mappingData.isSuggest()) {
            source.append(" \n");
            source.append(" ,\"fields\": {\n");

            source.append(" \"keyword\": {\n");
            source.append(" \"type\": \"keyword\",\n");
            source.append(" \"ignore_above\": " + mappingData.getIgnore_above());
            source.append(" },\n");

            source.append(" \"suggest\": {\n");
            source.append(" \"type\": \"completion\",\n");
            source.append(" \"analyzer\": \"" + mappingData.getAnalyzer() + "\"\n");
            source.append(" }\n");

            source.append(" }\n");
        } else if (mappingData.isKeyword() && !mappingData.getDatatype().equals("keyword") && !mappingData.isSuggest()) {
            source.append(" \n");
            source.append(" ,\"fields\": {\n");
            source.append(" \"keyword\": {\n");
            source.append(" \"type\": \"keyword\",\n");
            source.append(" \"ignore_above\": " + mappingData.getIgnore_above());
            source.append(" }\n");
            source.append(" }\n");
        } else if (!mappingData.isKeyword() && mappingData.isSuggest()) {
            source.append(" \n");
            source.append(" ,\"fields\": {\n");
            source.append(" \"suggest\": {\n");
            source.append(" \"type\": \"completion\",\n");
            source.append(" \"analyzer\": \"" + mappingData.getAnalyzer() + "\"\n");
            source.append(" }\n");
            source.append(" }\n");
        }
        return source.toString();
    }

    @Override
    public void dropIndex(Class<T> clazz) throws Exception {
        MetaData metaData = indexTools.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        DeleteIndexRequest request = new DeleteIndexRequest(indexname);
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Override
    public boolean exists(Class<T> clazz) throws Exception {
        MetaData metaData = indexTools.getMetaData(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        GetIndexRequest request = new GetIndexRequest();
        request.indices(indexname);
        request.types(indextype);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        return exists;
    }

    @Override
    public void rollover(Class<T> clazz, boolean isAsyn) throws Exception {
        if (clazz == null) return;
        MetaData metaData = indexTools.getMetaData(clazz);
        if (!metaData.isRollover()) return;
        if (metaData.isAutoRollover()) {
            rollover(metaData);
            return;
        } else {
            if (isAsyn) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1024);
                        rollover(metaData);
                    } catch (Exception e) {
                        logger.error("rollover error", e);
                    }
                }).start();
            } else {
                rollover(metaData);
            }
        }
    }

    @Override
    public String getIndexName(Class<T> clazz) {
        return getMetaData(clazz).getIndexname();
    }

    @Override
    public MetaData getMetaData(Class<T> clazz) {
        return indexTools.getMetaData(clazz);
    }

    @Override
    public MappingData[] getMappingData(Class<T> clazz) {
        return indexTools.getMappingData(clazz);
    }


    /**
     *  Rollover
     * @param metaData: 元数据.
     * @return: void
     **/
    private void rollover(MetaData metaData) throws Exception {
        RolloverRequest request = new RolloverRequest(metaData.getIndexname(), null);
        if (metaData.getRolloverMaxIndexAgeCondition() != 0) {
            request.addMaxIndexAgeCondition(new TimeValue(metaData.getRolloverMaxIndexAgeCondition(), metaData.getRolloverMaxIndexAgeTimeUnit()));
        }
        if (metaData.getRolloverMaxIndexDocsCondition() != 0) {
            request.addMaxIndexDocsCondition(metaData.getRolloverMaxIndexDocsCondition());
        }
        if (metaData.getRolloverMaxIndexSizeCondition() != 0) {
            request.addMaxIndexSizeCondition(new ByteSizeValue(metaData.getRolloverMaxIndexSizeCondition(), metaData.getRolloverMaxIndexSizeByteSizeUnit()));
        }
        RolloverResponse rolloverResponse = client.indices().rollover(request, RequestOptions.DEFAULT);
        logger.info("rollover alias[" + metaData.getIndexname() + "] result：" + rolloverResponse.isAcknowledged());
    }
}
