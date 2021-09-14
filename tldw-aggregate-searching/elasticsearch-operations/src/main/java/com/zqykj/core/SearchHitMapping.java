/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.core.convert.ElasticsearchConverter;
import com.zqykj.core.document.Document;
import com.zqykj.core.document.NestedMetaData;
import com.zqykj.core.document.SearchDocument;
import com.zqykj.core.document.SearchDocumentResponse;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.mapping.context.MappingContext;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> elasticsearch 命中结果映射 </h1>
 */
@Slf4j
class SearchHitMapping<T> {

    private final Class<T> type;
    private final ElasticsearchConverter converter;
    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    private SearchHitMapping(Class<T> type, ElasticsearchConverter converter) {
        Assert.notNull(type, "type is null");
        Assert.notNull(converter, "converter is null");

        this.type = type;
        this.converter = converter;
        this.mappingContext = converter.getMappingContext();
    }

    static <T> SearchHitMapping<T> mappingFor(Class<T> entityClass, ElasticsearchConverter converter) {
        return new SearchHitMapping<>(entityClass, converter);
    }

    SearchHits<T> mapHits(SearchDocumentResponse searchDocumentResponse, List<T> contents) {
        return mapHitsFromResponse(searchDocumentResponse, contents);
    }

    SearchScrollHits<T> mapScrollHits(SearchDocumentResponse searchDocumentResponse, List<T> contents) {
        return mapHitsFromResponse(searchDocumentResponse, contents);
    }

    /**
     * <h2> 从 Elastic search  Response 对 命中的内容 hits 进行映射  </h2>
     */
    private SearchHitsImpl<T> mapHitsFromResponse(SearchDocumentResponse searchDocumentResponse, List<T> contents) {

        Assert.notNull(searchDocumentResponse, "searchDocumentResponse is null");
        Assert.notNull(contents, "contents is null");

        Assert.isTrue(searchDocumentResponse.getSearchDocuments().size() == contents.size(),
                "Count of documents must match the count of entities");

        long totalHits = searchDocumentResponse.getTotalHits();
        float maxScore = searchDocumentResponse.getMaxScore();
        String scrollId = searchDocumentResponse.getScrollId();

        List<SearchHit<T>> searchHits = new ArrayList<>();
        List<SearchDocument> searchDocuments = searchDocumentResponse.getSearchDocuments();
        for (int i = 0; i < searchDocuments.size(); i++) {
            SearchDocument document = searchDocuments.get(i);
            T content = contents.get(i);
            SearchHit<T> hit = mapHit(document, content);
            searchHits.add(hit);
        }
        Aggregations aggregations = searchDocumentResponse.getAggregations();
        TotalHitsRelation totalHitsRelation = TotalHitsRelation.valueOf(searchDocumentResponse.getTotalHitsRelation());

        return new SearchHitsImpl<>(totalHits, totalHitsRelation, maxScore, scrollId, searchHits, aggregations);
    }

    /**
     * <h2> 映射命中的Hit </h2>
     */
    SearchHit<T> mapHit(SearchDocument searchDocument, T content) {

        Assert.notNull(searchDocument, "searchDocument is null");
        Assert.notNull(content, "content is null");

        return new SearchHit<T>(searchDocument.getIndex(), //
                searchDocument.hasId() ? searchDocument.getId() : null, //
                searchDocument.getScore(), //
                searchDocument.getSortValues(), //
                getHighlightsAndRemapFieldNames(searchDocument), //
                mapInnerHits(searchDocument), //
                searchDocument.getNestedMetaData(), //
                content); //
    }

    /**
     * <h2> 获取高亮并且重新映射字段名称 </h2>
     */
    @Nullable
    private Map<String, List<String>> getHighlightsAndRemapFieldNames(SearchDocument searchDocument) {
        Map<String, List<String>> highlightFields = searchDocument.getHighlightFields();

        if (highlightFields == null) {
            return null;
        }

        ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(type);
        if (persistentEntity == null) {
            return highlightFields;
        }

        return highlightFields.entrySet().stream().collect(Collectors.toMap(entry -> {
            ElasticsearchPersistentProperty property = persistentEntity.getPersistentPropertyWithFieldName(entry.getKey());
            return property != null ? property.getName() : entry.getKey();
        }, Map.Entry::getValue));
    }

    /**
     * <h2> 映射文档对象字段 内部命中的hits </h2>
     */
    private Map<String, SearchHits<?>> mapInnerHits(SearchDocument searchDocument) {

        Map<String, SearchHits<?>> innerHits = new LinkedHashMap<>();
        Map<String, SearchDocumentResponse> documentInnerHits = searchDocument.getInnerHits();

        if (documentInnerHits != null && documentInnerHits.size() > 0) {

            SearchHitMapping<SearchDocument> searchDocumentSearchHitMapping = SearchHitMapping
                    .mappingFor(SearchDocument.class, converter);

            for (Map.Entry<String, SearchDocumentResponse> entry : documentInnerHits.entrySet()) {
                SearchDocumentResponse searchDocumentResponse = entry.getValue();

                SearchHits<SearchDocument> searchHits = searchDocumentSearchHitMapping
                        .mapHitsFromResponse(searchDocumentResponse, searchDocumentResponse.getSearchDocuments());

                // map Documents to real objects
                SearchHits<?> mappedSearchHits = mapInnerDocuments(searchHits, type);

                innerHits.put(entry.getKey(), mappedSearchHits);
            }

        }
        return innerHits;
    }

    /**
     * <h2> 尝试将 SearchDocument 实例转换为内部属性类的实例 </h2>
     *
     * @param searchHits {@link SearchHits} containing {@link Document} instances
     * @param type       the class of the containing class
     * @return a new {@link SearchHits} instance containing the mapped objects or the original inout if any error occurs
     */
    private SearchHits<?> mapInnerDocuments(SearchHits<SearchDocument> searchHits, Class<T> type) {

        if (searchHits.getTotalHits() == 0) {
            return searchHits;
        }

        try {
            NestedMetaData nestedMetaData = searchHits.getSearchHit(0).getContent().getNestedMetaData();
            ElasticsearchPersistentEntityWithNestedMetaData persistentEntityWithNestedMetaData = getPersistentEntity(
                    mappingContext.getPersistentEntity(type), nestedMetaData);

            if (persistentEntityWithNestedMetaData.entity != null) {
                List<SearchHit<Object>> convertedSearchHits = new ArrayList<>();
                Class<?> targetType = persistentEntityWithNestedMetaData.entity.getType();

                // convert the list of SearchHit<SearchDocument> to list of SearchHit<Object>
                searchHits.getSearchHits().forEach(searchHit -> {
                    SearchDocument searchDocument = searchHit.getContent();

                    Object targetObject = converter.read(targetType, searchDocument);
                    convertedSearchHits.add(new SearchHit<Object>(searchDocument.getIndex(), //
                            searchDocument.getId(), //
                            searchDocument.getScore(), //
                            searchDocument.getSortValues(), //
                            searchDocument.getHighlightFields(), //
                            searchHit.getInnerHits(), //
                            persistentEntityWithNestedMetaData.nestedMetaData, //
                            targetObject));
                });

                String scrollId = null;
                if (searchHits instanceof SearchHitsImpl) {
                    scrollId = ((SearchHitsImpl<?>) searchHits).getScrollId();
                }

                return new SearchHitsImpl<>(searchHits.getTotalHits(), //
                        searchHits.getTotalHitsRelation(), //
                        searchHits.getMaxScore(), //
                        scrollId, //
                        convertedSearchHits, //
                        searchHits.getAggregations());
            }
        } catch (Exception e) {
            log.warn("Could not map inner_hits", e);
        }

        return searchHits;
    }

    /**
     * find a {@link ElasticsearchPersistentEntity} following the property chain defined by the nested metadata
     *
     * @param persistentEntity base entity
     * @param nestedMetaData   nested metadata
     * @return A {@link ElasticsearchPersistentEntityWithNestedMetaData} containing the found entity or null together with
     * the {@link NestedMetaData} that has mapped field names.
     */
    private ElasticsearchPersistentEntityWithNestedMetaData getPersistentEntity(
            @Nullable ElasticsearchPersistentEntity<?> persistentEntity, @Nullable NestedMetaData nestedMetaData) {

        NestedMetaData currentMetaData = nestedMetaData;
        List<NestedMetaData> mappedNestedMetaDatas = new LinkedList<>();

        while (persistentEntity != null && currentMetaData != null) {
            ElasticsearchPersistentProperty persistentProperty = persistentEntity
                    .getPersistentPropertyWithFieldName(currentMetaData.getField());

            if (persistentProperty == null) {
                persistentEntity = null;
            } else {
                persistentEntity = mappingContext.getPersistentEntity(persistentProperty.getActualType());
                mappedNestedMetaDatas.add(0,
                        NestedMetaData.of(persistentProperty.getName(), currentMetaData.getOffset(), null));
                currentMetaData = currentMetaData.getChild();
            }
        }

        NestedMetaData mappedNestedMetaData = mappedNestedMetaDatas.stream().reduce(null,
                (result, nmd) -> NestedMetaData.of(nmd.getField(), nmd.getOffset(), result));

        return new ElasticsearchPersistentEntityWithNestedMetaData(persistentEntity, mappedNestedMetaData);
    }

    private static class ElasticsearchPersistentEntityWithNestedMetaData {
        @Nullable
        private ElasticsearchPersistentEntity<?> entity;
        private NestedMetaData nestedMetaData;

        public ElasticsearchPersistentEntityWithNestedMetaData(@Nullable ElasticsearchPersistentEntity<?> entity,
                                                               NestedMetaData nestedMetaData) {
            this.entity = entity;
            this.nestedMetaData = nestedMetaData;
        }
    }
}
