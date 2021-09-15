/**
 * @作者 Mcj
 */
package com.zqykj.core.mapping;


import com.zqykj.mapping.PersistentProperty;
import org.springframework.lang.Nullable;

/**
 * <h1> elasticsearch index entity property describe </h1>
 */
public interface ElasticsearchPersistentProperty extends PersistentProperty<ElasticsearchPersistentProperty> {

    /**
     * Returns the name to be used to store the property in the document.
     *
     * @return
     */
    String getFieldName();

    /**
     * @return true if an {@link ElasticsearchPersistentPropertyConverter} is available for this instance.
     * @since 4.0
     */
    boolean hasPropertyConverter();

    /**
     * @return the {@link ElasticsearchPersistentPropertyConverter} for this instance.
     * @since 4.0
     */
    @Nullable
    ElasticsearchPersistentPropertyConverter getPropertyConverter();

    /**
     * @return {@literal true} if null values should be stored in Elasticsearch
     * @since 4.1
     */
    boolean storeNullValue();

    /**
     * calls {@link #getActualType()} but returns null when an exception is thrown
     *
     * @since 4.1
     */
    @Nullable
    default Class<?> getActualTypeOrNull() {
        try {
            return getActualType();
        } catch (Exception e) {
            return null;
        }
    }
}
