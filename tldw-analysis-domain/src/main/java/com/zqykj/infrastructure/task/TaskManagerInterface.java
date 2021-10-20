package com.zqykj.infrastructure.task;

/**
 * @Description: 任务管理接口
 * @Author zhangkehou
 * @Date 2021/10/14
 */
public interface TaskManagerInterface {

    /**
     * 提交任务.
     *
     * @param task: 实现了{@link Task}的任务.
     * @return: void
     **/
    public void taskHandle(Task<?> task) throws Exception;

    /**
     * 根据任务编号获取任务.
     *
     * @param taskId: 任务编号.
     * @return: com.zqykj.infrastructure.task.Task<?>
     **/
    public Task<?> getTask(String taskId) throws Exception;

    /**
     * 根据任务编号取消正在执行的任务,此时任务还存在.
     *
     * @param taskId: 任务编号.
     * @return: void
     **/
    public void cancelTask(String taskId) throws Exception;

    /**
     * 清楚所有任务
     *
     * @return: void
     **/
    public void clearTask() throws Exception;

    /**
     * 向任务列表中添加任务.
     *
     * @param task: 实现了{@link Task}的任务.
     * @return: void
     **/
    public void addTask(Task<?> task) throws Exception;


    /**
     * 根据任务编号移除执任务,此时任务没有了.
     *
     * @param taskId: 任务编号.
     * @return: void
     **/
    public void removeTask(String taskId) throws Exception;
}
