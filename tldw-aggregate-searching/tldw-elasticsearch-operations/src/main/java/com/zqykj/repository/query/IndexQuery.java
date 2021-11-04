/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * <h1> IndexQuery </h1>
 */
public class IndexQuery {

    @Nullable
    private String id;
    @Nullable
    private Object object;
    @Nullable
    private Long version;
    @Nullable
    private String source;
    @Nullable
    private Long seqNo;
    @Nullable
    private Long primaryTerm;
    @Nullable
    private String routing;
    @Nullable
    private Map<String, ?> sourceMap;

    @Nullable
    public Map<String, ?> getSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(@Nullable Map<String, ?> sourceMap) {
        this.sourceMap = sourceMap;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Nullable
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Nullable
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Nullable
    public Long getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Long seqNo) {
        this.seqNo = seqNo;
    }

    @Nullable
    public Long getPrimaryTerm() {
        return primaryTerm;
    }

    public void setPrimaryTerm(Long primaryTerm) {
        this.primaryTerm = primaryTerm;
    }

    @Nullable
    public String getRouting() {
        return routing;
    }

    public void setRouting(@Nullable String routing) {
        this.routing = routing;
    }
}
