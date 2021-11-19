/**
 * @作者 Mcj
 */
package com.zqykj.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 分页参数 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Pagination {

    private int from = 0;
    private Integer size;
}
