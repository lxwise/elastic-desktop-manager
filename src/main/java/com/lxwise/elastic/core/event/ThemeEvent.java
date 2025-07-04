package com.lxwise.elastic.core.event;

/**
 * @author lstar
 * @create 2025-02
 * @description: 主题事件
 */
public final class ThemeEvent extends Event {

    public enum EventType {
        // 主题可以更改基本字体大小和颜色
        THEME_CHANGE,
        // 字体大小或字体更改
        FONT_CHANGE,
        // 颜色更改
        COLOR_CHANGE,
        // 添加或删除的新主题
        THEME_ADD,
        THEME_REMOVE
    }

    private final EventType eventType;

    public ThemeEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "ThemeEvent{"
            + "eventType=" + eventType
            + "} " + super.toString();
    }
}
