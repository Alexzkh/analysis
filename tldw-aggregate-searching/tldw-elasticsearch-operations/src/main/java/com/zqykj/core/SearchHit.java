/**
 * @作者 Mcj
 */
package com.zqykj.core;

import com.zqykj.core.document.NestedMetaData;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * the result of query when query or search successed the return object .
 */
public class SearchHit<T> {

    @Nullable
    private final String index;
    @Nullable
    private final String id;
    private final float score;
    private final List<Object> sortValues;
    private final T content;
    private final Map<String, List<String>> highlightFields = new LinkedHashMap<>();
    private final Map<String, SearchHits<?>> innerHits = new LinkedHashMap<>();
    @Nullable
    private final NestedMetaData nestedMetaData;

    public SearchHit(@Nullable String index, @Nullable String id, float score, @Nullable Object[] sortValues,
                     @Nullable Map<String, List<String>> highlightFields, T content) {
        this(index, id, score, sortValues, highlightFields, null, null, content);
    }

    public SearchHit(@Nullable String index, @Nullable String id, float score, @Nullable Object[] sortValues,
                     @Nullable Map<String, List<String>> highlightFields, @Nullable Map<String, SearchHits<?>> innerHits,
                     @Nullable NestedMetaData nestedMetaData, T content) {
        this.index = index;
        this.id = id;
        this.score = score;
        this.sortValues = (sortValues != null) ? Arrays.asList(sortValues) : new ArrayList<>();

        if (highlightFields != null) {
            this.highlightFields.putAll(highlightFields);
        }

        if (innerHits != null) {
            this.innerHits.putAll(innerHits);
        }

        this.nestedMetaData = nestedMetaData;

        this.content = content;
    }

    /**
     * @return the index name where the hit's document was found
     * @since 4.1
     */
    @Nullable
    public String getIndex() {
        return index;
    }

    @Nullable
    public String getId() {
        return id;
    }

    /**
     * @return the score for the hit.
     */
    public float getScore() {
        return score;
    }

    /**
     * @return the object data from the search.
     */
    public T getContent() {
        return content;
    }

    /**
     * @return the sort values if the query had a sort criterion.
     */
    public List<Object> getSortValues() {
        return Collections.unmodifiableList(sortValues);
    }

    /**
     * @return the map from field names to highlight values, never {@literal null}
     */
    public Map<String, List<String>> getHighlightFields() {
        return Collections.unmodifiableMap(highlightFields.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Collections.unmodifiableList(entry.getValue()))));
    }

    /**
     * gets the highlight values for a field.
     *
     * @param field must not be {@literal null}
     * @return possibly empty List, never null
     */
    public List<String> getHighlightField(String field) {

        Assert.notNull(field, "field must not be null");

        return Collections.unmodifiableList(highlightFields.getOrDefault(field, Collections.emptyList()));
    }


    @Nullable
    public SearchHits<?> getInnerHits(String name) {
        return innerHits.get(name);
    }

    /**
     * @return the map from inner_hits names to inner hits, in a {@link SearchHits} object, never {@literal null}
     * @since 4.1
     */
    public Map<String, SearchHits<?>> getInnerHits() {
        return innerHits;
    }


    @Nullable
    public NestedMetaData getNestedMetaData() {
        return nestedMetaData;
    }

    @Override
    public String toString() {
        return "SearchHit{" + "id='" + id + '\'' + ", score=" + score + ", sortValues=" + sortValues + ", content="
                + content + ", highlightFields=" + highlightFields + '}';
    }
}
