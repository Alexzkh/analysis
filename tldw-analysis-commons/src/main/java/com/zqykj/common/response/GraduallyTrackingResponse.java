package com.zqykj.common.response;

import com.zqykj.common.vo.TrackingNode;
import lombok.*;

import java.util.List;

/**
 * @Description: 逐步追踪响应体
 * @Author zhangkehou
 * @Date 2021/11/30
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraduallyTrackingResponse {

    /**
     * 源节点
     */
    private TrackingNode start;

    /**
     * 下一节点
     */
    private TrackingNode next;

    /**
     * 追踪的节点
     */
    private List<TrackingNode> trackingNodes;
}
