package com.lxwise.elastic.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lstar
 * @create 2024-12
 * @description: 自定义线程工厂
 */
public class CustomThreadFactory implements ThreadFactory {

    /**
     * 命名前缀
     */
    private final String prefix;
    /**
     * 线程组
     */
    private final ThreadGroup group;
    /**
     * 线程组
     */
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    /**
     * 是否守护线程
     */
    private final boolean isDaemon;
    /**
     * 无法捕获的异常统一处理
     */
    private final Thread.UncaughtExceptionHandler handler;

    /**
     * 构造
     *
     * @param prefix 线程名前缀
     */
    public CustomThreadFactory(String prefix) {
        this(prefix, null, Boolean.FALSE);
    }

    /**
     * 构造
     *
     * @param prefix   线程名前缀
     * @param isDaemon 是否守护线程
     */
    public CustomThreadFactory(String prefix, boolean isDaemon) {
        this(prefix, null, isDaemon);
    }

    /**
     * 构造
     *
     * @param prefix      线程名前缀
     * @param threadGroup 线程组，可以为null
     * @param isDaemon    是否守护线程
     */
    public CustomThreadFactory(String prefix, ThreadGroup threadGroup, boolean isDaemon) {
        this(prefix, threadGroup, isDaemon, null);
    }

    /**
     * 构造
     *
     * @param prefix   线程名前缀
     * @param group    线程组，可以为null
     * @param isDaemon 是否守护线程
     * @param handler  未捕获异常处理
     */
    public CustomThreadFactory(String prefix, ThreadGroup group, boolean isDaemon, Thread.UncaughtExceptionHandler handler) {
        this.prefix = prefix;
        if (null == group) {
            group = Thread.currentThread().getThreadGroup();
        }
        this.group = group;
        this.isDaemon = isDaemon;
        this.handler = handler;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        final Thread thread = new Thread(this.group, runnable, prefix + threadNumber.getAndIncrement());

        //守护线程
        if (!thread.isDaemon()) {
            if (isDaemon) {
                thread.setDaemon(true);
            }
        } else if (!isDaemon) {
            thread.setDaemon(false);
        }
        if (null != this.handler) {
            thread.setUncaughtExceptionHandler(handler);
        }
        if (Thread.NORM_PRIORITY != thread.getPriority()) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }

}
