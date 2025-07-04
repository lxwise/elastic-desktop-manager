package com.lxwise.elastic.core.thread;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lstar
 * @create 2024-12
 * @description: 防抖函数
 */
public class Debounce {
    /**
     * 核心池大小
     */
    private static final Integer CORE_POOL_SIZE = 1;
    /**
     * 线程前缀
     */
    private static final String THREAD_PREFIX = "debounce";
    /**
     * 调度
     */
    private ScheduledExecutorService executorService;
    /**
     * 调度返回值
     */
    private ScheduledFuture<?> scheduledFuture;
    /**
     * 持续时间
     */
    private final Duration duration;

    /**
     * 防反跳
     *
     * @param duration 持续时间
     */
    public Debounce(Duration duration) {
        this.executorService = newScheduledThreadPoolExecutor();
        this.duration = duration;
    }

    /**
     * 执行
     *
     * @param runnable 可运行
     */
    public void execute(Runnable runnable) {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = newScheduledThreadPoolExecutor();
            scheduledFuture = null;
        }
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(Boolean.FALSE);
        }
        scheduledFuture = executorService.schedule(runnable, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 创建
     *
     * @return {@link ScheduledThreadPoolExecutor}
     */
    private ScheduledThreadPoolExecutor newScheduledThreadPoolExecutor() {
        return new ScheduledThreadPoolExecutor(CORE_POOL_SIZE, new CustomThreadFactory(THREAD_PREFIX));
    }

    /**
     * 取消
     */
    public void cancel() {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}

