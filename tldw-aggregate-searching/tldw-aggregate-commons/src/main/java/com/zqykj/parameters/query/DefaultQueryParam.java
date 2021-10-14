/**
 * @作者 Mcj
 */
package com.zqykj.parameters.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 默认查询参数 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultQueryParam {

    // 当must(and) 与 should(or) , filter(where) 同一级的时候 , 这个参数可以指定至少需要满足 should(or) 的 几个条件
    private int minimumShouldMatch = 1;
}
