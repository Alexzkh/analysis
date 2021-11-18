/**
 * @作者 Mcj
 */
package com.zqykj.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 管道聚合排序参数 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FieldSort {

    // 排序字段
    private String field;

    // 排序方向  ASC,DESC
    private String direction;
}
