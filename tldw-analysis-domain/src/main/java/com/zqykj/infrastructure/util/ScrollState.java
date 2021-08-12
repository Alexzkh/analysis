/**
 * @作者 Mcj
 */
package com.zqykj.infrastructure.util;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Mcj
 */
public class ScrollState {

    private final Object lock = new Object();

    private final Set<String> pastIds = new LinkedHashSet<>();
    private String scrollId;

    public ScrollState() {
    }

    public ScrollState(String scrollId) {
        updateScrollId(scrollId);
    }

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
