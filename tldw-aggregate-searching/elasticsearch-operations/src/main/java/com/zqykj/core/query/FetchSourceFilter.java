/**
 * @author Mcj
 */
package com.zqykj.core.query;

import com.zqykj.repository.query.SourceFilter;
import org.springframework.lang.Nullable;

public class FetchSourceFilter implements SourceFilter {

    @Nullable
    private final String[] includes;
    @Nullable
    private final String[] excludes;

    public FetchSourceFilter(@Nullable final String[] includes, @Nullable final String[] excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @Override
    public String[] getIncludes() {
        return includes;
    }

    @Override
    public String[] getExcludes() {
        return excludes;
    }
}
