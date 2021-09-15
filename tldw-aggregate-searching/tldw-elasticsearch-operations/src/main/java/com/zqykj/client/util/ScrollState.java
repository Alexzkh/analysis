/**
 * @作者 Mcj
 */
package com.zqykj.client.util;

import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.search.Scroll;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * <h1> Mutable state object holding scrollId to be used for {@link SearchScrollRequest#scroll(Scroll)} </h1>
 */
public class ScrollState {

    private final Object lock = new Object();

    private final Set<String> pastIds = new LinkedHashSet<>();
    @Nullable
    private String scrollId;

    public ScrollState() {
    }

    public ScrollState(String scrollId) {
        updateScrollId(scrollId);
    }

    @Nullable
    public String getScrollId() {
        return scrollId;
    }

    public List<String> getScrollIds() {

        synchronized (lock) {
            return Collections.unmodifiableList(new ArrayList<>(pastIds));
        }
    }

    public void updateScrollId(@Nullable String scrollId) {

        if (StringUtils.hasText(scrollId)) {

            synchronized (lock) {

                this.scrollId = scrollId;
                pastIds.add(scrollId);
            }
        }
    }
}
