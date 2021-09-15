/**
 * @作者 Mcj
 */
package com.zqykj.core;

import org.springframework.lang.Nullable;

/**
 * <h1> 返回的索引对象信息 </h1>
 */
public class IndexedObjectInformation {

    private final String id;
    @Nullable
    private final Long seqNo;
    @Nullable
    private final Long primaryTerm;
    @Nullable
    private final Long version;

    private IndexedObjectInformation(String id, @Nullable Long seqNo, @Nullable Long primaryTerm,
                                     @Nullable Long version) {
        this.id = id;
        this.seqNo = seqNo;
        this.primaryTerm = primaryTerm;
        this.version = version;
    }

    public static IndexedObjectInformation of(String id, @Nullable Long seqNo, @Nullable Long primaryTerm,
                                              @Nullable Long version) {
        return new IndexedObjectInformation(id, seqNo, primaryTerm, version);
    }

    public String getId() {
        return id;
    }

    @Nullable
    public Long getSeqNo() {
        return seqNo;
    }

    @Nullable
    public Long getPrimaryTerm() {
        return primaryTerm;
    }

    @Nullable
    public Long getVersion() {
        return version;
    }
}
