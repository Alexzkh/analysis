/**
 * @作者 Mcj
 */
package com.zqykj.common.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 分页请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    private Integer page = 0;
    private Integer pageSize = 25;

    // 计算总页数
    public int getTotalPages(int total, int pageSize) {
        return total == 0 ? 0 : (int) Math.ceil((double) total / (double) pageSize);
    }
}
