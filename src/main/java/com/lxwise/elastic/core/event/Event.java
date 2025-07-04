package com.lxwise.elastic.core.event;

import com.lxwise.elastic.utils.ThreadUtils;

import java.util.UUID;

/**
 * @author lstar
 * @create 2025-02
 * @description: 事件父类
 */
public class Event {
    /**
     * UUID
     */
    private final String uuid = UUID.randomUUID().toString().replace("-", "");

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event event)) {
            return false;
        }
        return uuid.equals(event.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    /**
     * 发布
     *
     * @param event 事件
     */
    public static <E extends Event> void publish(E event) {
        EventBus.getInstance().publish(event);
    }

    /**
     * 发布
     */
    public void publish() {
        EventBus.getInstance().publish(this);
    }

    /**
     * 发布
     */
    public void publishAsync() {
        ThreadUtils.virtual().execute(() -> {
            EventBus.getInstance().publish(this);
        });
    }
}
