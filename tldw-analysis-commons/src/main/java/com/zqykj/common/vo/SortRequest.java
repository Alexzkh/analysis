/**
 * @作者 Mcj
 */
package com.zqykj.common.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 排序请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest {

    // 需要排序的字段
    private String property;

    // 排序方向  ASC,DESC
    private Direction order = Direction.DESC;
}
