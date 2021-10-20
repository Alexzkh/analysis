package com.zqykj.infrastructure.task;

/**
 * @Description: 取消任务抽象类
 * @Author zhangkehou
 * @Date 2021/10/14
 */
public abstract class CancelableTask<T> extends Task<T> {

    /**
     * 取消任务操作，交由子类实现
     *
     * @return: void
     **/
    public abstract void cancel() throws Exception;
}