package com.lxwise.elastic.core.event;

import atlantafx.base.controls.Notification;

/**
 * @author lstar
 * @create 2025-02
 * @description: 通知关闭事件
 */
public class NoticeCloseEvent extends Event {
    private final Notification notification;

    public NoticeCloseEvent(Notification notification) {
        this.notification = notification;
    }

    public Notification notification() {
        return notification;
    }
}
