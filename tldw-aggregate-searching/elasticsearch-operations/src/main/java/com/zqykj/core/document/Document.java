/**
 * @作者 Mcj
 */
package com.zqykj.core.document;


import com.fasterxml.jackson.core.type.TypeReference;
import com.zqykj.util.JacksonUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public interface Document extends Map<String, Object> {

    static Document create() {
        return new MapDocument();
    }

    static Document from(Map<String, ? extends Object> map) {

        Assert.notNull(map, "Map must not be null");

        if (map instanceof LinkedHashMap) {
            return new MapDocument(map);
        }

        return new MapDocument(new LinkedHashMap<>(map));
    }

    static Document parse(String json) {

        Assert.notNull(json, "JSON must not be null");
        return new MapDocument(JacksonUtils.parse(json, new TypeReference<Map<String, ?>>() {
        }));
    }

    default Document append(String key, Object value) {

        Assert.notNull(key, "Key must not be null");

        put(key, value);
        return this;
    }

    /**
     * @return the index if this document was retrieved from an index
     */
    @Nullable
    default String getIndex() {
        return null;
    }

    /**
     * <h2> Sets the index name for this document </h2>
     */
    default void setIndex(@Nullable String index) {
        throw new UnsupportedOperationException();
    }

    default boolean hasId() {
        return false;
    }

    default String getId() {
        throw new UnsupportedOperationException();
    }

    default void setId(String id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return {@literal true} if this {@link Document} is associated with a seq_no.
     *
     * @return {@literal true} if this {@link Document} is associated with a seq_no, {@literal false} otherwise.
     */
    default boolean hasSeqNo() {
        return false;
    }

    default long getSeqNo() {
        throw new UnsupportedOperationException();
    }

    default void setSeqNo(long seqNo) {
        throw new UnsupportedOperationException();
    }

    default boolean hasPrimaryTerm() {
        return false;
    }

    default long getPrimaryTerm() {
        throw new UnsupportedOperationException();
    }

    default void setPrimaryTerm(long primaryTerm) {
        throw new UnsupportedOperationException();
    }

    default boolean hasVersion() {
        return false;
    }

    default long getVersion() {
        throw new UnsupportedOperationException();
    }

    default void setVersion(long version) {
        throw new UnsupportedOperationException();
    }


    /**
     * Returns the value to which the specified {@code key} is mapped, or {@literal null} if this document contains no
     * mapping for the key. The value is casted within the method which makes it useful for calling code as it does not
     * require casting on the calling side. If the value type is not assignable to {@code type}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key  the key whose associated value is to be returned
     * @param type the expected return value type.
     * @param <T>  expected return type.
     * @return the value to which the specified key is mapped, or {@literal null} if this document contains no mapping for
     * the key.
     * @throws ClassCastException if the value of the given key is not of {@code type T}.
     */
    @Nullable
    default <T> T get(Object key, Class<T> type) {

        Assert.notNull(key, "Key must not be null");
        Assert.notNull(type, "Type must not be null");

        return type.cast(get(key));
    }

    default <R> R transform(Function<? super Document, ? extends R> transformer) {

        Assert.notNull(transformer, "transformer must not be null");

        return transformer.apply(this);
    }

    String toJson();
}
