package com.zqykj.domain.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Description: 交易路径获取图路径结果请求体
 * @Author zhangkehou
 * @Date 2021/12/13
 */
@Data
@Builder
public class PathRequest {
    /**
     * 路径深度
     */
    private int depth = 1;

    /**
     * 路径起始节点id集合
     */
    private List<String> vfromVKeyId;

    /**
     * 路径终点节点id集合
     */
    private List<String> vtoVKeyId;

    /**
     * elp 类型集合（即可指定具体路径的elp类型）
     */
    private List<String> elpTypeKeys;

    /**
     * 查找路径的方向（both:无向; out:正向;in:反向）
     */
    private String direction;
}
