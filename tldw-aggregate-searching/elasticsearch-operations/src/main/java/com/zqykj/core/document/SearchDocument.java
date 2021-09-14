/**
 * @作者 Mcj
 */
package com.zqykj.core.document;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * <h1> 扩展搜索相关相应数据 </h1>
 */
public interface SearchDocument extends Document {

    float getScore();

    Map<String, List<Object>> getFields();

    /**
     * <h2> the fields for the search result </h2>
     */
    @Nullable
    default <V> V getFieldValue(final String name) {
        List<Object> values = getFields().get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return (V) values.get(0);
    }

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
     */
    @Nullable
    default Map<String, SearchDocumentResponse> getInnerHits() {
        return null;
    }

    /**
     * @return the nested metadata in case this is a nested inner hit.
     */
    @Nullable
    default NestedMetaData getNestedMetaData() {
        return null;
    }
}
