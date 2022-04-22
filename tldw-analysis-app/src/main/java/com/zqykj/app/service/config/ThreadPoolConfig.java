/**
 * @作者 Mcj
 */
package com.zqykj.app.service.config;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <h1> 自定义异步任务线程池 </h1>
 */
@Component
public class ThreadPoolConfig {

    private static ThreadPoolTaskExecutor executor;

    public ThreadPoolConfig(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        executor = threadPoolTaskExecutor;
    }

    public ThreadPoolConfig() {
    }

    @PostConstruct
    public ThreadPoolTaskExecutor commonThreadPool() {
        executor = new ThreadPoolTaskExecutor();
        // 核心线程数量
        executor.setCorePoolSize(12);
        // 最大线程数量
        executor.setMaxPoolSize(20);
        // 队列中最大任务数
        executor.setQueueCapacity(1000);
        // 线程名称前缀
        executor.setThreadNamePrefix("ThreadPool-Tactics-");
        // 当达到最大线程数时如何处理新任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程空闲后最大存活时间
        executor.setKeepAliveSeconds(60);
        // 初始化线程池
        executor.initialize();
        return executor;
    }

    public static ThreadPoolTaskExecutor getExecutor() {
        return executor;
    }
}
