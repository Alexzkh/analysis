package com.zqykj.infrastructure.task;

/**
 * @Description: 任务执行状态.
 * @Author zhangkehou
 * @Date 2021/10/14
 */
public enum TaskStatus {

    /**
     * 任务已创建.
     */
    Created,

    /**
     * 任务等待中.
     */
    Waiting,

    /**
     * 任务执行中.
     */
    Executing,

    /**
     * 任务已完成.
     */
    Finish,

    /**
     * 任务出错了.
     */
    Error,

    /**
     * 任务被用户取消.
     */
    UserEsc;
}
