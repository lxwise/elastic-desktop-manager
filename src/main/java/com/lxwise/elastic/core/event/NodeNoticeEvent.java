package com.lxwise.elastic.core.event;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author lstar
 * @create 2025-02
 * @description: 组件通知事件，支持传递消息或对象，并区分来源
 */
public class NodeNoticeEvent extends Event {
    private final Object source;  // 事件来源（可以是 Controller、Class 或其他对象）
    private final Object payload; // 事件携带的数据
    private final String targetKey; // 目标键

    public NodeNoticeEvent() {

        this.source = null;
        this.payload = null;
        this.targetKey = null;
    }

    public NodeNoticeEvent(Object payload, String targetKey) {
        this.source = null;
        this.payload = payload;
        this.targetKey = targetKey;
    }

    public NodeNoticeEvent(Object source, Object payload, String targetKey) {
        this.source = source;
        this.payload = payload;
        this.targetKey = targetKey;
    }

    public Object getSource() {
        return source;
    }

    public Object getPayload() {
        return payload;
    }


    public String getTargetKey() {
        return targetKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NodeNoticeEvent that = (NodeNoticeEvent) o;

        if (!Objects.equals(source, that.source)) return false;
        if (!Objects.equals(payload, that.payload)) return false;
        return Objects.equals(targetKey, that.targetKey);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (targetKey != null ? targetKey.hashCode() : 0);
        return result;
    }

    public static void subscribeByKey(String key, Consumer<NodeNoticeEvent> consumer) {
        EventBus.getInstance().subscribe(NodeNoticeEvent.class, event -> {
            if (Objects.equals(event.getTargetKey(), key)) {
                consumer.accept(event);
            }
        });
    }

    public static void subscribeByKeys(Map<String, Consumer<NodeNoticeEvent>> handlers) {
        EventBus.getInstance().subscribe(NodeNoticeEvent.class, event -> {
            String key = event.getTargetKey();
            if (key != null && handlers.containsKey(key)) {
                handlers.get(key).accept(event);
            }
        });
    }


}
