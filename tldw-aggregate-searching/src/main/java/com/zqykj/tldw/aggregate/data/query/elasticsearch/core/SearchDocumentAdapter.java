/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch.core;

import org.elasticsearch.common.document.DocumentField;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Adapter for a {@link SearchDocumentResponse.SearchDocument}.
 */
public class SearchDocumentAdapter implements SearchDocumentResponse.SearchDocument {

    private final float score;
    private final Object[] sortValues;
    private final Map<String, List<Object>> fields = new HashMap<>();
    private final ElasticsearchDocument delegate;
    private final Map<String, List<String>> highlightFields = new HashMap<>();
    private final Map<String, SearchDocumentResponse> innerHits = new HashMap<>();
    @Nullable
    private final NestedMetaData nestedMetaData;

    SearchDocumentAdapter(float score, Object[] sortValues, Map<String, DocumentField> fields,
                          Map<String, List<String>> highlightFields, ElasticsearchDocument delegate, Map<String, SearchDocumentResponse> innerHits,
                          @Nullable NestedMetaData nestedMetaData) {

        this.score = score;
        this.sortValues = sortValues;
        this.delegate = delegate;
        fields.forEach((name, documentField) -> this.fields.put(name, documentField.getValues()));
        this.highlightFields.putAll(highlightFields);
        this.innerHits.putAll(innerHits);
        this.nestedMetaData = nestedMetaData;
    }

    @Override
    public SearchDocumentResponse.SearchDocument append(String key, Object value) {
        delegate.append(key, value);

        return this;
    }

    @Override
    public float getScore() {
        return score;
    }

    @Override
    public Map<String, List<Object>> getFields() {
        return fields;
    }

    @Override
    public Object[] getSortValues() {
        return sortValues;
    }

    @Override
    public Map<String, List<String>> getHighlightFields() {
        return highlightFields;
    }

    @Override
    public String getIndex() {
        return delegate.getIndex();
    }

    @Override
    public boolean hasId() {
        return delegate.hasId();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String id) {
        delegate.setId(id);
    }

    @Override
    public boolean hasVersion() {
        return delegate.hasVersion();
    }

    @Override
    public long getVersion() {
        return delegate.getVersion();
    }

    @Override
    public void setVersion(long version) {
        delegate.setVersion(version);
    }

    @Override
    public boolean hasSeqNo() {
        return delegate.hasSeqNo();
    }

    @Override
    public long getSeqNo() {
        return delegate.getSeqNo();
    }

    @Override
    public void setSeqNo(long seqNo) {
        delegate.setSeqNo(seqNo);
    }

    @Override
    public boolean hasPrimaryTerm() {
        return delegate.hasPrimaryTerm();
    }

    @Override
    public long getPrimaryTerm() {
        return delegate.getPrimaryTerm();
    }

    @Override
    public void setPrimaryTerm(long primaryTerm) {
        delegate.setPrimaryTerm(primaryTerm);
    }

    @Override
    public Map<String, SearchDocumentResponse> getInnerHits() {
        return innerHits;
    }

    @Override
    @Nullable
    public NestedMetaData getNestedMetaData() {
        return nestedMetaData;
    }

    @Nullable
    @Override
    public <T> T get(Object key, Class<T> type) {
        return delegate.get(key, type);
    }

    @Override
    public String toJson() {
        return delegate.toJson();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<Object> values() {
        return delegate.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchDocumentAdapter)) {
            return false;
        }
        SearchDocumentAdapter that = (SearchDocumentAdapter) o;
        return Float.compare(that.score, score) == 0 && delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        delegate.forEach(action);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return delegate.remove(key, value);
    }

    @Override
    public String toString() {

        String id = hasId() ? getId() : "?";
        String version = hasVersion() ? Long.toString(getVersion()) : "?";

        return getClass().getSimpleName() + '@' + id + '#' + version + ' ' + toJson();
    }
}
