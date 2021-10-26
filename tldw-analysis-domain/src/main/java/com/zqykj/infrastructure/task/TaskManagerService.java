package com.zqykj.infrastructure.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description: 任务管理实现类
 * @Author zhangkehou
 * @Date 2021/10/14
 */
@Service(value = "managerService")
public class TaskManagerService implements TaskManagerInterface {
    private static Logger logger = LoggerFactory.getLogger(TaskManagerService.class);

    /**
     * 任务超时时间
     */
    private static int TASK_RESULT_TIMEOUT = 300 * 1000;

    /**
     * 任务管理服务实例
     */
    private static TaskManagerService INSTANCE = new TaskManagerService();

    /**
     * 任务字典表,{<key:taskId,value:Task>}
     */
    private static Map<String, Task<?>> TASKS = new ConcurrentHashMap<>();

    /**
     * 线程池资源
     */
    private ExecutorService taskThreadPool = Executors.newCachedThreadPool();

    private TaskManagerService() {
    }

    public static TaskManagerService getInstance() {
        return INSTANCE;
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    public void timerClearTask() {
        try {
            this.clearTask();
        } catch (Exception e) {
            logger.error("Timer clear task result error ", e);
        }
    }

    @Override
    public void taskHandle(Task<?> task) throws Exception {
        if (!TASKS.containsKey(task.getTaskId())) {
            this.addTask(task);
        }
        TaskRunable taskRunable = new TaskRunable();
        taskRunable.setTask(task);
        task.setStatus(TaskStatus.Waiting);
        taskThreadPool.execute(taskRunable);
    }

    /**
     * 执行已经创建的任务.
     *
     * @param executorService: 线程池
     * @param task:            创建的任务
     * @return: void
     **/
    public void taskHandle(final ExecutorService executorService, Task<?> task) throws Exception {
        if (!TASKS.containsKey(task.getTaskId())) {
            this.addTask(task);
        }
        TaskRunable taskRunable = new TaskRunable();
        taskRunable.setTask(task);
        task.setStatus(TaskStatus.Waiting);
        if (executorService == null) {
            taskThreadPool.execute(taskRunable);
        } else {
            executorService.execute(taskRunable);
        }
    }

    @Override
    public Task<?> getTask(String taskId) throws Exception {
        return TASKS.get(taskId);
    }

    @Override
    public void clearTask() throws Exception {
        if (TASKS != null && TASKS.size() > 0) {
            for (String taskId : TASKS.keySet()) {
                @SuppressWarnings("rawtypes")
                Task t = TASKS.get(taskId);
                if (t.getStatus() == TaskStatus.Finish || t.getStatus() == TaskStatus.Error) {
                    if ((System.currentTimeMillis() - TASKS.get(taskId).getFinishTime()) > TASK_RESULT_TIMEOUT) {
                        logger.info("Removing task {} due to expiration after completion.", taskId);
                        TASKS.remove(taskId);
                    }
                }
            }
        }
    }

    @Override
    public void addTask(Task<?> task) throws Exception {
        TASKS.put(task.getTaskId(), task);
    }

    /**
     * @param taskId: 根据任务id删除任务
     * @return: void
     **/
    public void removeTask(String taskId) throws Exception {
        TASKS.remove(taskId);
    }

    @Override
    public void cancelTask(String taskId) throws Exception {
        Task<?> task = TASKS.get(taskId);
        if (task == null) {
            throw new Exception("The task of " + taskId + " does not exist");
        }

        if (task instanceof CancelableTask<?>) {
            ((CancelableTask<?>) task).cancel();
        }

        throw new Exception("The task of " + taskId + " is not cancelable task");
    }

    /**
     * 任务执行.
     */
    private class TaskRunable implements Runnable {

        private Task<?> task;

        @Override
        public void run() {
            if (task == null) {
                return;
            }
            try {
                task.setStatus(TaskStatus.Executing);
                task.run();
                task.setStatus(TaskStatus.Finish);
                task.setFinishTime(System.currentTimeMillis());
            } catch (Throwable e) {
                logger.error("Error when executing task: " + task.getTaskId(), e);
                task.setStatus(TaskStatus.Error);
                task.setFinishTime(System.currentTimeMillis());
                return;
            }
        }

        public void setTask(Task<?> task) {
            this.task = task;
        }

    }
}

