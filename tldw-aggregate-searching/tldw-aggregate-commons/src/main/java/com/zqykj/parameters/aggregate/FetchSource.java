/**
 * @作者 Mcj
 */
package com.zqykj.parameters.aggregate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;


/**
 * <h1> 聚合需要展示的字段 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FetchSource {

    // 包含的字段
    @Nullable
    private String[] includes;

    // 排序的字段
    @Nullable
    private String[] excludes;

    private int size = 1;

    private int from = 0;

    public FetchSource(@Nullable String[] includes, int from, int size) {

        this.includes = includes;
        this.from = from;
        this.size = size;
    }

    public FetchSource(@Nullable String[] includes) {

        this.includes = includes;
    }

    public FetchSource(@Nullable String[] includes, @Nullable String[] excludes) {

        this.includes = includes;
        this.excludes = excludes;
    }
}
