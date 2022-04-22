package com.zqykj.common.request;

import com.zqykj.common.vo.TrackingNode;
import lombok.*;


/**
 * @Description: 逐步追踪请求体
 * @Author zhangkehou
 * @Date 2021/11/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraduallyTrackingRequest {


    /**
     * 追踪的源节点
     */
    private TrackingNode start;

    /**
     * 追踪的下一个节点
     */
    private TrackingNode next;


    /**
     * 时间间隔
     */
    private Integer dateInterval;

    /**
     * 时间间隔单位
     */
    private String unit;

    /**
     * 金额偏差
     */
    private Integer amountDeviation;


}
