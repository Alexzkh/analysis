/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.query.parameters;

import com.zqykj.domain.Sort;
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

    // 对一个桶聚合中子聚合结果进行排序
    private String fieldName;

    // 排序方向  ASC,DESC
    private Sort.Direction direction;
}
