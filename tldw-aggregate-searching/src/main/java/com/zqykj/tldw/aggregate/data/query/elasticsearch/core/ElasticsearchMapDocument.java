/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch.core;

import com.alibaba.fastjson.JSON;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author Mcj
 */
public class ElasticsearchMapDocument implements ElasticsearchDocument {

    private final LinkedHashMap<String, Object> documentAsMap;

    @Nullable
    private String index;
    @Nullable
    private String id;
    @Nullable
    private Long version;
    @Nullable
    private Long seqNo;
    @Nullable
    private Long primaryTerm;

    ElasticsearchMapDocument() {
        this(new LinkedHashMap<>());
    }

    ElasticsearchMapDocument(Map<String, Object> documentAsMap) {
        this.documentAsMap = new LinkedHashMap<>(documentAsMap);
    }

    @Override
    public void setIndex(@Nullable String index) {
        this.index = index;
    }

    @Nullable
    @Override
    public String getIndex() {
        return index;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#hasId()
     */
    @Override
    public boolean hasId() {
        return this.id != null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#getId()
     */
    @Override
    public String getId() {

        if (!hasId()) {
            throw new IllegalStateException("No Id associated with this Document");
        }

        return this.id;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#hasVersion()
     */
    @Override
    public boolean hasVersion() {
        return this.version != null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#getVersion()
     */
    @Override
    public long getVersion() {

        if (!hasVersion()) {
            throw new IllegalStateException("No version associated with this Document");
        }

        return this.version;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#setVersion(long)
     */
    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#hasSeqNo()
     */
    @Override
    public boolean hasSeqNo() {
        return this.seqNo != null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#getSeqNo()
     */
    @Override
    public long getSeqNo() {

        if (!hasSeqNo()) {
            throw new IllegalStateException("No seq_no associated with this Document");
        }

        return this.seqNo;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#setSeqNo()
     */
    @Override
    public void setSeqNo(long seqNo) {
        this.seqNo = seqNo;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#hasPrimaryTerm()
     */
    @Override
    public boolean hasPrimaryTerm() {
        return this.primaryTerm != null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#getPrimaryTerm()
     */
    @Override
    public long getPrimaryTerm() {

        if (!hasPrimaryTerm()) {
            throw new IllegalStateException("No primary_term associated with this Document");
        }

        return this.primaryTerm;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#setPrimaryTerm()
     */
    @Override
    public void setPrimaryTerm(long primaryTerm) {
        this.primaryTerm = primaryTerm;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return documentAsMap.size();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return documentAsMap.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return documentAsMap.containsKey(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return documentAsMap.containsValue(value);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        return documentAsMap.get(key);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#getOrDefault(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return documentAsMap.getOrDefault(key, defaultValue);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value) {
        return documentAsMap.put(key, value);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        return documentAsMap.remove(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#putAll(Map)
     */
    @Override
    public void putAll(Map<? extends String, ?> m) {
        documentAsMap.putAll(m);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        documentAsMap.clear();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        return documentAsMap.keySet();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Object> values() {
        return documentAsMap.values();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return documentAsMap.entrySet();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return documentAsMap.equals(o);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return documentAsMap.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#forEach(java.util.function.BiConsumer)
     */
    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        documentAsMap.forEach(action);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.document.Document#toJson()
     */
    @Override
    public String toJson() {
        try {
            return JSON.toJSONString(this);
        } catch (Exception e) {
            throw new RuntimeException("Cannot render document to JSON", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String id = hasId() ? getId() : "?";
        String version = hasVersion() ? Long.toString(getVersion()) : "?";

        return getClass().getSimpleName() + '@' + id + '#' + version + ' ' + toJson();
    }

}
