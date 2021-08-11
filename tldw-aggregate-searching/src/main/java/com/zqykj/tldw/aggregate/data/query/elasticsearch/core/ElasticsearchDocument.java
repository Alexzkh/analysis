/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zqykj.tldw.aggregate.Exception.ConversionException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.*;

/**
 * @author Mcj
 */
public interface ElasticsearchDocument extends Map<String, Object> {

    /**
     * Create a new mutable {@link ElasticsearchDocument}.
     *
     * @return a new {@link  ElasticsearchDocument}.
     */
    static ElasticsearchDocument create() {
        return new ElasticsearchMapDocument();
    }

    /**
     * Create a {@link ElasticsearchDocument} from a {@link Map} containing key-value pairs and sub-documents.
     *
     * @param map source map containing key-value pairs and sub-documents. must not be {@literal null}.
     * @return a new {@link ElasticsearchDocument}.
     */
    static ElasticsearchDocument from(Map<String, Object> map) {

        Assert.notNull(map, "Map must not be null");

        if (map instanceof LinkedHashMap) {
            return new ElasticsearchMapDocument(map);
        }

        return new ElasticsearchMapDocument(new LinkedHashMap<>(map));
    }

    /**
     * Parse JSON to {@link ElasticsearchDocument}.
     *
     * @param json must not be {@literal null}.
     * @return the parsed {@link ElasticsearchDocument}.
     */
    static ElasticsearchDocument parse(String json) {

        Assert.notNull(json, "JSON must not be null");

        try {
            Map<String, Object> map = JSON.parseObject(json, new TypeReference<Map<String, Object>>() {
            });
            return new ElasticsearchMapDocument(map);
        } catch (Exception e) {
            throw new ConversionException("Cannot parse JSON", e);
        }
    }

    /**
     * {@link #put(Object, Object)} the {@code key}/{@code value} tuple and return {@code this} {@link ElasticsearchDocument}.
     *
     * @param key   key with which the specified value is to be associated. must not be {@literal null}.
     * @param value value to be associated with the specified key.
     * @return {@code this} {@link ElasticsearchDocument}.
     */
    default ElasticsearchDocument append(String key, Object value) {

        Assert.notNull(key, "Key must not be null");

        put(key, value);
        return this;
    }

    /**
     * Return {@literal true} if this {@link ElasticsearchDocument} is associated with an identifier.
     *
     * @return {@literal true} if this {@link ElasticsearchDocument} is associated with an identifier, {@literal false} otherwise.
     */
    default boolean hasId() {
        return false;
    }

    /**
     * @return the index if this document was retrieved from an index
     * @since 4.1
     */
    @Nullable
    default String getIndex() {
        return null;
    }

    /**
     * Sets the index name for this document
     *
     * @param index index name
     *              <p>
     *              The default implementation throws {@link UnsupportedOperationException}.
     * @since 4.1
     */
    default void setIndex(@Nullable String index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve the identifier associated with this {@link ElasticsearchDocument}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}. It's recommended to check {@link #hasId()}
     * prior to calling this method.
     *
     * @return the identifier associated with this {@link ElasticsearchDocument}.
     * @throws IllegalStateException if the underlying implementation supports Id's but no Id was yet associated with the
     *                               document.
     */
    default String getId() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the identifier for this {@link ElasticsearchDocument}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}.
     */
    default void setId(String id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return {@literal true} if this {@link ElasticsearchDocument} is associated with a version.
     *
     * @return {@literal true} if this {@link ElasticsearchDocument} is associated with a version, {@literal false} otherwise.
     */
    default boolean hasVersion() {
        return false;
    }

    /**
     * Retrieve the version associated with this {@link ElasticsearchDocument}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}. It's recommended to check
     * {@link #hasVersion()} prior to calling this method.
     *
     * @return the version associated with this {@link ElasticsearchDocument}.
     * @throws IllegalStateException if the underlying implementation supports Id's but no Id was yet associated with the
     *                               document.
     */
    default long getVersion() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the version for this {@link ElasticsearchDocument}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}.
     */
    default void setVersion(long version) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return {@literal true} if this {@link ElasticsearchDocument} is associated with a seq_no.
     *
     * @return {@literal true} if this {@link ElasticsearchDocument} is associated with a seq_no, {@literal false} otherwise.
     */
    default boolean hasSeqNo() {
        return false;
    }

    /**
     * Retrieve the seq_no associated with this {@link ElasticsearchDocument}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}. It's recommended to check
     * {@link #hasSeqNo()} prior to calling this method.
     *
     * @return the seq_no associated with this {@link ElasticsearchDocument}.
     * @throws IllegalStateException if the underlying implementation supports seq_no's but no seq_no was yet associated
     *                               with the document.
     */
    default long getSeqNo() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the seq_no for this {@link ElasticsearchDocument}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}.
     */
    default void setSeqNo(long seqNo) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return {@literal true} if this {@link ElasticsearchDocument} is associated with a primary_term.
     *
     * @return {@literal true} if this {@link ElasticsearchDocument} is associated with a primary_term, {@literal false} otherwise.
     */
    default boolean hasPrimaryTerm() {
        return false;
    }

    /**
     * Retrieve the primary_term associated with this {@link ElasticsearchDocument}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}. It's recommended to check
     * {@link #hasPrimaryTerm()} prior to calling this method.
     *
     * @return the primary_term associated with this {@link ElasticsearchDocument}.
     * @throws IllegalStateException if the underlying implementation supports primary_term's but no primary_term was yet
     *                               associated with the document.
     */
    default long getPrimaryTerm() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the primary_term for this {@link ElasticsearchDocument}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}.
     */
    default void setPrimaryTerm(long primaryTerm) {
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

    /**
     * Returns the value to which the specified {@code key} is mapped, or {@literal null} if this document contains no
     * mapping for the key. If the value type is not a {@link Boolean}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@literal null} if this document contains no mapping for
     * the key.
     * @throws ClassCastException if the value of the given key is not a {@link Boolean}.
     */
    @Nullable
    default Boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }

    /**
     * Returns the value to which the specified {@code key} is mapped or {@code defaultValue} if this document contains no
     * mapping for the key. If the value type is not a {@link Boolean}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped or {@code defaultValue} if this document contains no mapping
     * for the key.
     * @throws ClassCastException if the value of the given key is not a {@link Boolean}.
     */
    default boolean getBooleanOrDefault(String key, boolean defaultValue) {
        return getBooleanOrDefault(key, () -> defaultValue);
    }

    /**
     * Returns the value to which the specified {@code key} is mapped or the value from {@code defaultValue} if this
     * document contains no mapping for the key. If the value type is not a {@link Boolean}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped or the value from {@code defaultValue} if this document
     * contains no mapping for the key.
     * @throws ClassCastException if the value of the given key is not a {@link Boolean}.
     * @see BooleanSupplier
     */
    default boolean getBooleanOrDefault(String key, BooleanSupplier defaultValue) {

        Boolean value = getBoolean(key);

        return value == null ? defaultValue.getAsBoolean() : value;
    }

    /**
     * Returns the value to which the specified {@code key} is mapped, or {@literal null} if this document contains no
     * mapping for the key. If the value type is not a {@link Integer}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@literal null} if this document contains no mapping for
     * the key.
     * @throws ClassCastException if the value of the given key is not a {@link Integer}.
     */
    @Nullable
    default Integer getInt(String key) {
        return get(key, Integer.class);
    }

    /**
     * Returns the value to which the specified {@code key} is mapped or {@code defaultValue} if this document contains no
     * mapping for the key. If the value type is not a {@link Integer}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped or {@code defaultValue} if this document contains no mapping
     * for the key.
     * @throws ClassCastException if the value of the given key is not a {@link Integer}.
     */
    default int getIntOrDefault(String key, int defaultValue) {
        return getIntOrDefault(key, () -> defaultValue);
    }

    /**
     * Returns the value to which the specified {@code key} is mapped or the value from {@code defaultValue} if this
     * document contains no mapping for the key. If the value type is not a {@link Integer}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped or the value from {@code defaultValue} if this document
     * contains no mapping for the key.
     * @throws ClassCastException if the value of the given key is not a {@link Integer}.
     * @see IntSupplier
     */
    default int getIntOrDefault(String key, IntSupplier defaultValue) {

        Integer value = getInt(key);

        return value == null ? defaultValue.getAsInt() : value;
    }

    /**
     * Returns the value to which the specified {@code key} is mapped, or {@literal null} if this document contains no
     * mapping for the key. If the value type is not a {@link Long}, then this method throws {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@literal null} if this document contains no mapping for
     * the key.
     * @throws ClassCastException if the value of the given key is not a {@link Long}.
     */
    @Nullable
    default Long getLong(String key) {
        return get(key, Long.class);
    }

    /**
     * Returns the value to which the specified {@code key} is mapped or {@code defaultValue} if this document contains no
     * mapping for the key. If the value type is not a {@link Long}, then this method throws {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped or {@code defaultValue} if this document contains no mapping
     * for the key.
     * @throws ClassCastException if the value of the given key is not a {@link Long}.
     */
    default long getLongOrDefault(String key, long defaultValue) {
        return getLongOrDefault(key, () -> defaultValue);
    }

    /**
     * Returns the value to which the specified {@code key} is mapped or the value from {@code defaultValue} if this
     * document contains no mapping for the key. If the value type is not a {@link Long}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped or the value from {@code defaultValue} if this document
     * contains no mapping for the key.
     * @throws ClassCastException if the value of the given key is not a {@link Long}.
     * @see LongSupplier
     */
    default long getLongOrDefault(String key, LongSupplier defaultValue) {

        Long value = getLong(key);

        return value == null ? defaultValue.getAsLong() : value;
    }

    /**
     * Returns the value to which the specified {@code key} is mapped, or {@literal null} if this document contains no
     * mapping for the key. If the value type is not a {@link String}, then this method throws {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@literal null} if this document contains no mapping for
     * the key.
     * @throws ClassCastException if the value of the given key is not a {@link String}.
     */
    @Nullable
    default String getString(String key) {
        return get(key, String.class);
    }

    /**
     * Returns the value to which the specified {@code key} is mapped or {@code defaultValue} if this document contains no
     * mapping for the key. If the value type is not a {@link String}, then this method throws {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped or {@code defaultValue} if this document contains no mapping
     * for the key.
     * @throws ClassCastException if the value of the given key is not a {@link String}.
     */
    default String getStringOrDefault(String key, String defaultValue) {
        return getStringOrDefault(key, () -> defaultValue);
    }

    /**
     * Returns the value to which the specified {@code key} is mapped or the value from {@code defaultValue} if this
     * document contains no mapping for the key. If the value type is not a {@link String}, then this method throws
     * {@link ClassCastException}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped or the value from {@code defaultValue} if this document
     * contains no mapping for the key.
     * @throws ClassCastException if the value of the given key is not a {@link String}.
     * @see Supplier
     */
    default String getStringOrDefault(String key, Supplier<String> defaultValue) {

        String value = getString(key);

        return value == null ? defaultValue.get() : value;
    }

    /**
     * This method allows the application of a function to {@code this} {@link ElasticsearchDocument}. The function should expect a
     * single {@link ElasticsearchDocument} argument and produce an {@code R} result.
     * <p>
     * Any exception thrown by the function will be propagated to the caller.
     *
     * @param transformer functional interface to a apply. must not be {@literal null}.
     * @param <R>         class of the result
     * @return the result of applying the function to this string
     * @see Function
     */
    default <R> R transform(Function<? super ElasticsearchDocument, ? extends R> transformer) {

        Assert.notNull(transformer, "transformer must not be null");

        return transformer.apply(this);
    }

    /**
     * Render this {@link ElasticsearchDocument} to JSON. Auxiliary values such as Id and version are not considered within the JSON
     * representation.
     *
     * @return a JSON representation of this document.
     */
    String toJson();
}
