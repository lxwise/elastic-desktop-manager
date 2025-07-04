package com.lxwise.elastic.core.event;

import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-02
 * @description: 加载事件
 */
public class LoadingEvent extends Event {
    private static Task<?> work; // 事件携带的数据
    public static LoadingEvent LOADING=new LoadingEvent(true);
    public static LoadingEvent STOP=new LoadingEvent(false);
    private final boolean loading;

    public LoadingEvent(Boolean loading) {
        this.loading = loading;
    }
    public LoadingEvent(Boolean loading, Task<?> work) {
        this.loading = loading;
        this.work = work;
    }

    public boolean loading() {
        return loading;
    }

    public Task<?> getWork() {
        return work;
    }

    public void setWork(Task<?> work) {
        LoadingEvent.work = work;
    }
}
