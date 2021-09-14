/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.springframework.lang.Nullable;

/**
 * <h1> 数据Field 过滤 </h1>
 */
public interface SourceFilter {

    /**
     * <h2> 包含的字段 </h2>
     */
    @Nullable
    String[] getIncludes();

    /**
     * <h2> 排除的字段 </h2>
     */
    @Nullable
    String[] getExcludes();
}
