package com.lxwise.elastic.utils;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lstar
 * @create 2024-12
 * @description: 线程工具类
 */
public class ThreadUtils {
    private final static ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 开始
     *
     * @param task 任务
     */
    public static <T> void start(Task<T> task) {
        new Thread(task).start();
    }

    /**
     * 虚拟
     *
     * @return {@link ExecutorService}
     */
    public static ExecutorService virtual() {
        return service;
    }

}
