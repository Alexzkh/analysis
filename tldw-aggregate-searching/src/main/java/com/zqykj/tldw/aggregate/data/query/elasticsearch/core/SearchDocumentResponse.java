/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch.core;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * <h1> Elasticsearch restHighLevelClient SearchHit wrapper </h1>
 */
public class SearchDocumentResponse {

    private final long totalHits;
    private final String totalHitsRelation;
    private final float maxScore;
    private final String scrollId;
    private final List<SearchDocument> searchDocuments;
    private final Aggregations aggregations;

    private SearchDocumentResponse(long totalHits, String totalHitsRelation, float maxScore, String scrollId,
                                   List<SearchDocument> searchDocuments, Aggregations aggregations) {
        this.totalHits = totalHits;
        this.totalHitsRelation = totalHitsRelation;
        this.maxScore = maxScore;
        this.scrollId = scrollId;
        this.searchDocuments = searchDocuments;
        this.aggregations = aggregations;
    }

    public static SearchDocumentResponse from(SearchResponse searchResponse) {

        Assert.notNull(searchResponse, "searchResponse must not be null");

        Aggregations aggregations = searchResponse.getAggregations();
        String scrollId = searchResponse.getScrollId();

        SearchHits searchHits = searchResponse.getHits();

        return from(searchHits, scrollId, aggregations);
    }

    public static SearchDocumentResponse from(SearchHits searchHits, @Nullable String scrollId,
                                              @Nullable Aggregations aggregations) {
        TotalHits responseTotalHits = searchHits.getTotalHits();

        long totalHits;
        String totalHitsRelation;

        if (responseTotalHits != null) {
            totalHits = responseTotalHits.value;
            totalHitsRelation = responseTotalHits.relation.name();
        } else {
            totalHits = searchHits.getHits().length;
            totalHitsRelation = "OFF";
        }

        float maxScore = searchHits.getMaxScore();

        List<SearchDocument> searchDocuments = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            if (searchHit != null) {
                searchDocuments.add(from(searchHit));
            }
        }

        return new SearchDocumentResponse(totalHits, totalHitsRelation, maxScore, scrollId, searchDocuments, aggregations);
    }

    public static SearchDocument from(SearchHit source) {

        Assert.notNull(source, "SearchHit must not be null");

        Map<String, List<String>> highlightFields = new HashMap<>(source.getHighlightFields().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> Arrays.stream(entry.getValue().getFragments()).map(Text::string).collect(Collectors.toList()))));

        Map<String, SearchDocumentResponse> innerHits = new LinkedHashMap<>();
        Map<String, SearchHits> sourceInnerHits = source.getInnerHits();

        if (sourceInnerHits != null) {
            sourceInnerHits.forEach((name, searchHits) -> {
                innerHits.put(name, SearchDocumentResponse.from(searchHits, null, null));
            });
        }

        NestedMetaData nestedMetaData = null;

        if (source.getNestedIdentity() != null) {
            nestedMetaData = from(source.getNestedIdentity());
        }

        BytesReference sourceRef = source.getSourceRef();

        if (sourceRef == null || sourceRef.length() == 0) {
            return new SearchDocumentAdapter(
                    source.getScore(), source.getSortValues(), source.getFields(), highlightFields, fromDocumentFields(source,
                    source.getIndex(), source.getId(), source.getVersion(), source.getSeqNo(), source.getPrimaryTerm()),
                    innerHits, nestedMetaData);
        }

        ElasticsearchDocument document = ElasticsearchDocument.from(source.getSourceAsMap());
        document.setIndex(source.getIndex());
        document.setId(source.getId());

        if (source.getVersion() >= 0) {
            document.setVersion(source.getVersion());
        }
        document.setSeqNo(source.getSeqNo());
        document.setPrimaryTerm(source.getPrimaryTerm());

        return new SearchDocumentAdapter(source.getScore(), source.getSortValues(), source.getFields(), highlightFields,
                document, innerHits, nestedMetaData);
    }

    public static ElasticsearchDocument fromDocumentFields(Iterable<DocumentField> documentFields, String index, String id,
                                                           long version, long seqNo, long primaryTerm) {

        if (documentFields instanceof Collection) {
            return new DocumentFieldAdapter((Collection<DocumentField>) documentFields, index, id, version, seqNo,
                    primaryTerm);
        }

        List<DocumentField> fields = new ArrayList<>();
        for (DocumentField documentField : documentFields) {
            fields.add(documentField);
        }

        return new DocumentFieldAdapter(fields, index, id, version, seqNo, primaryTerm);
    }

    private static NestedMetaData from(SearchHit.NestedIdentity nestedIdentity) {

        NestedMetaData child = null;

        if (nestedIdentity.getChild() != null) {
            child = from(nestedIdentity.getChild());
        }

        return NestedMetaData.of(nestedIdentity.getField().string(), nestedIdentity.getOffset(), child);
    }

    public long getTotalHits() {
        return totalHits;
    }

    public String getTotalHitsRelation() {
        return totalHitsRelation;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public String getScrollId() {
        return scrollId;
    }

    public List<SearchDocument> getSearchDocuments() {
        return searchDocuments;
    }

    public Aggregations getAggregations() {
        return aggregations;
    }

    // TODO: Performance regarding keys/values/entry-set
    static class DocumentFieldAdapter implements ElasticsearchDocument {

        private final Collection<DocumentField> documentFields;
        private final String index;
        private final String id;
        private final long version;
        private final long seqNo;
        private final long primaryTerm;

        DocumentFieldAdapter(Collection<DocumentField> documentFields, String index, String id, long version, long seqNo,
                             long primaryTerm) {
            this.documentFields = documentFields;
            this.index = index;
            this.id = id;
            this.version = version;
            this.seqNo = seqNo;
            this.primaryTerm = primaryTerm;
        }

        @Override
        public String getIndex() {
            return index;
        }

        @Override
        public boolean hasId() {
            return StringUtils.hasLength(id);
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public boolean hasVersion() {
            return this.version >= 0;
        }

        @Override
        public long getVersion() {

            if (!hasVersion()) {
                throw new IllegalStateException("No version associated with this Document");
            }

            return this.version;
        }

        @Override
        public boolean hasSeqNo() {
            return true;
        }

        @Override
        public long getSeqNo() {

            if (!hasSeqNo()) {
                throw new IllegalStateException("No seq_no associated with this Document");
            }

            return this.seqNo;
        }

        @Override
        public boolean hasPrimaryTerm() {
            return true;
        }

        @Override
        public long getPrimaryTerm() {

            if (!hasPrimaryTerm()) {
                throw new IllegalStateException("No primary_term associated with this Document");
            }

            return this.primaryTerm;
        }

        @Override
        public int size() {
            return documentFields.size();
        }

        @Override
        public boolean isEmpty() {
            return documentFields.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {

            for (DocumentField documentField : documentFields) {
                if (documentField.getName().equals(key)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean containsValue(Object value) {

            for (DocumentField documentField : documentFields) {

                Object fieldValue = getValue(documentField);
                if (fieldValue != null && fieldValue.equals(value) || value == fieldValue) {
                    return true;
                }
            }

            return false;
        }

        @Override
        @Nullable
        public Object get(Object key) {
            return documentFields.stream() //
                    .filter(documentField -> documentField.getName().equals(key)) //
                    .map(DocumentField::getValue).findFirst() //
                    .orElse(null); //

        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            return documentFields.stream().map(DocumentField::getName).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public Collection<Object> values() {
            return documentFields.stream().map(DocumentFieldAdapter::getValue).collect(Collectors.toList());
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return documentFields.stream().collect(Collectors.toMap(DocumentField::getName, DocumentFieldAdapter::getValue))
                    .entrySet();
        }

        @Override
        public void forEach(BiConsumer<? super String, ? super Object> action) {

            Objects.requireNonNull(action);

            documentFields.forEach(field -> action.accept(field.getName(), getValue(field)));
        }

        @Override
        public String toJson() {

            JsonFactory nodeFactory = new JsonFactory();
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
                generator.writeStartObject();
                for (DocumentField value : documentFields) {
                    if (value.getValues().size() > 1) {
                        generator.writeArrayFieldStart(value.getName());
                        for (Object val : value.getValues()) {
                            generator.writeObject(val);
                        }
                        generator.writeEndArray();
                    } else {
                        generator.writeObjectField(value.getName(), value.getValue());
                    }
                }
                generator.writeEndObject();
                generator.flush();
                return new String(stream.toByteArray(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Cannot render JSON", e);
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '@' + this.id + '#' + this.version + ' ' + toJson();
        }

        @Nullable
        private static Object getValue(DocumentField documentField) {

            if (documentField.getValues().isEmpty()) {
                return null;
            }

            if (documentField.getValues().size() == 1) {
                return documentField.getValue();
            }

            return documentField.getValues();
        }
    }

    public interface SearchDocument extends ElasticsearchDocument {

        /**
         * Return the search {@code score}.
         *
         * @return the search {@code score}.
         */
        float getScore();

        /**
         * @return the fields for the search result, not {@literal null}
         */
        Map<String, List<Object>> getFields();

        /**
         * The first value of the given field.
         *
         * @param name the field name
         */
        @Nullable
        default <V> V getFieldValue(final String name) {
            List<Object> values = getFields().get(name);
            if (values == null || values.isEmpty()) {
                return null;
            }
            return (V) values.get(0);
        }

        /**
         * @return the sort values for the search hit
         */
        @Nullable
        default Object[] getSortValues() {
            return null;
        }

        /**
         * @return the highlightFields for the search hit.
         */
        @Nullable
        default Map<String, List<String>> getHighlightFields() {
            return null;
        }

        /**
         * @return the innerHits for the SearchHit
         * @since 4.1
         */
        @Nullable
        default Map<String, SearchDocumentResponse> getInnerHits() {
            return null;
        }

        /**
         * @return the nested metadata in case this is a nested inner hit.
         * @since 4.1
         */
        @Nullable
        default NestedMetaData getNestedMetaData() {
            return null;
        }
    }
}
