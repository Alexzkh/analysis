/**
 * @作者 Mcj
 */
package com.zqykj.core.document;

import com.zqykj.util.JacksonUtils;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <h1> {@link Document} implementation backed by a {@link LinkedHashMap}. </h1>
 */
public class MapDocument implements Document {

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

    MapDocument() {
        this(new LinkedHashMap<>());
    }

    MapDocument(Map<String, ? extends Object> documentAsMap) {
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

    @Override
    public boolean hasId() {
        return this.id != null;
    }

    @Override
    public String getId() {

        if (!hasId()) {
            throw new IllegalStateException("No Id associated with this Document");
        }

        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean hasSeqNo() {
        return this.seqNo != null;
    }

    @Override
    public long getSeqNo() {

        if (!hasSeqNo()) {
            throw new IllegalStateException("No seq_no associated with this Document");
        }

        return this.seqNo;
    }

    public void setSeqNo(long seqNo) {
        this.seqNo = seqNo;
    }

    @Override
    public boolean hasPrimaryTerm() {
        return this.primaryTerm != null;
    }

    @Override
    public long getPrimaryTerm() {

        if (!hasPrimaryTerm()) {
            throw new IllegalStateException("No primary_term associated with this Document");
        }

        return this.primaryTerm;
    }

    public void setPrimaryTerm(long primaryTerm) {
        this.primaryTerm = primaryTerm;
    }

    @Override
    public boolean hasVersion() {
        return this.version != null;
    }

    @Override
    public long getVersion() {

        if (!hasVersion()) {
            throw new IllegalStateException("No version associated with this Document");
        }

        return this.version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int size() {
        return documentAsMap.size();
    }

    @Override
    public boolean isEmpty() {
        return documentAsMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return documentAsMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return documentAsMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return documentAsMap.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return documentAsMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return documentAsMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        documentAsMap.putAll(m);
    }

    @Override
    public void clear() {
        documentAsMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return documentAsMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return documentAsMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return documentAsMap.entrySet();
    }

    @Override
    public String toJson() {
        return JacksonUtils.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        return documentAsMap.equals(o);
    }

    @Override
    public int hashCode() {
        return documentAsMap.hashCode();
    }

    @Override
    public String toString() {

        String id = hasId() ? getId() : "?";
        String version = hasVersion() ? Long.toString(getVersion()) : "?";

        return getClass().getSimpleName() + '@' + id + '#' + version + ' ' + toJson();
    }
}
