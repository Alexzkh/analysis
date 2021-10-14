package com.zqykj.infrastructure.task;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Description: 任务抽象类.
 * @Author zhangkehou
 * @Date 2021/10/14
 */
@Data
public abstract class Task<T> implements StepListable {
    /**
     * 任务编号.
     */
    protected String taskId;

    /**
     * 任务执行当前步骤.
     */
    protected int nowStep;

    /**
     * 任务执行流程集合.
     */
    protected List<String> stepList = new ArrayList<>();

    /**
     * 任务执行完成时任务返回的数据体.
     */
    protected T result;

    /**
     * 任务完成时间.
     */
    protected long finishTime;

    /**
     * 任务状态.
     */
    protected TaskStatus status;

    /**
     * 任务信息记录，如出错时的任务信息.
     */
    protected String message;

    /**
     * 随机生成任务信息编号.
     */
    public Task() {
        setTaskId(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    /**
     * 行为交由下游实现.
     */
    public abstract void run() throws Exception;

}

