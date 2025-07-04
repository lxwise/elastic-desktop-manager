package com.lxwise.elastic.utils;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lstar
 * @create 2025-02
 * @description: 定时任务工具类
 */
public class RefreshScheduler {

    private final Map<Integer, ScheduledService<Void>> scheduledServices = new HashMap<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);

    /**
     * 创建一个新的定时任务
     *
     * @param task             要执行的任务
     * @param intervalInSeconds 间隔时间（秒）
     * @return 任务 ID
     */
    public int schedule(Runnable task, int intervalInSeconds) {
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledService<Void> scheduledService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        Platform.runLater(task); // 确保在 JavaFX 线程中执行
                        return null;
                    }
                };
            }
        };

        scheduledService.setPeriod(Duration.seconds(intervalInSeconds));
        scheduledServices.put(taskId, scheduledService);
        scheduledService.start();
        return taskId;
    }

    /**
     * 停止指定的定时任务
     *
     * @param taskId 任务 ID
     */
    public void stop(int taskId) {
        ScheduledService<Void> scheduledService = scheduledServices.get(taskId);
        if (scheduledService != null) {
            scheduledService.cancel();
            scheduledServices.remove(taskId);
        }
    }

    /**
     * 更新指定定时任务的间隔时间
     *
     * @param taskId            任务 ID
     * @param intervalInSeconds 新的间隔时间（秒）
     */
    public void updateInterval(int taskId, int intervalInSeconds) {
        ScheduledService<Void> scheduledService = scheduledServices.get(taskId);
        if (scheduledService != null) {
            scheduledService.setPeriod(Duration.seconds(intervalInSeconds));
        }
    }

    /**
     * 停止所有定时任务
     */
    public void stopAll() {
        scheduledServices.values().forEach(ScheduledService::cancel);
        scheduledServices.clear();
    }
}