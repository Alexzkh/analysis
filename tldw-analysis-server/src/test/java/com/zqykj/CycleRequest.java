package com.zqykj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 图回路请求参数
 * @Author zhangkehou
 * @Date 2022/1/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleRequest {

    /**
     * 路径深度 必填 最大 10
     */
    private int depth = 1;

    /**
     * 源实体id 必填
     */
    private List<String> vfromVKeyId;

    /**
     * elp类型
     */
    private List<String> elpTypeKeys;

    /**
     * 属性（可选参数）
     */
    private List<Object> properties;
}
