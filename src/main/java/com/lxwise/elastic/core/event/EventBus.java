package com.lxwise.elastic.core.event;

import javafx.application.Platform;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * @author lstar
 * @create 2025-02
 * @description: 事件总线
 */
public class EventBus {

    private final static EventBus INSTANCE = new EventBus();

    private EventBus() {
    }

    /**
     * 用户
     */
    private final Map<Class<?>, Set<Consumer>> subscribers = new ConcurrentHashMap<>();

    /**
     * 订阅
     *
     * @param eventType  事件类型
     * @param subscriber 订户
     */
    public <E extends Event> void subscribe(Class<? extends E> eventType, Consumer<E> subscriber) {
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(subscriber);
        Set<Consumer> eventSubscribers = getOrCreateSubscribers(eventType);
        eventSubscribers.add(subscriber);
    }

    /**
     * 退订
     *
     * @param eventType  事件类型
     * @param subscriber 订户
     */
    public <E extends Event> void unsubscribe(Class<? extends E> eventType, Consumer<E> subscriber) {
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(subscriber);

        subscribers.keySet().stream()
                .filter(eventType::isAssignableFrom)
                .map(subscribers::get)
                .forEach(eventSubscribers -> eventSubscribers.remove(subscriber));
    }

    /**
     * 发布
     * @param event
     * @param <E>
     */
    public <E extends Event> void publish(E event) {
        Objects.requireNonNull(event);
        Class<?> eventType = event.getClass();
        subscribers.keySet().stream()
                .filter(type -> type.isAssignableFrom(eventType))
                .flatMap(type -> subscribers.get(type).stream())
                .forEach(subscriber -> publish(event, subscriber));
    }

    /**
     * 发布
     *
     * @param event      事件
     * @param subscriber 订户
     */
    private <E extends Event> void publish(E event, Consumer<E> subscriber) {
        try {
            subscriber.accept(event);
        } catch (Exception e) {
            Platform.runLater(() -> {
                throw e;
            });
        }
    }


    /**
     * 获取订阅用户
     * @param eventType
     * @return
     * @param <E>
     */

    private <E> Set<Consumer> getOrCreateSubscribers(Class<E> eventType) {
        Set<Consumer> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers == null) {
            eventSubscribers = new CopyOnWriteArraySet<>();
            subscribers.put(eventType, eventSubscribers);
        }
        return eventSubscribers;
    }

    public static EventBus getInstance() {
        return EventBus.INSTANCE;
    }
}
