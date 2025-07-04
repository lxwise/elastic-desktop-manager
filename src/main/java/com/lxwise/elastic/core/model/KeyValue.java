package com.lxwise.elastic.core.model;

/**
 * @author lstar
 * @create 2025-02
 * @description: 键值 对
 */
public class KeyValue {
    private final String key;
    private final Object value;

    public KeyValue(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public Object getValue() { return value; }

    @Override
    public String toString() {
        return (String) value;
    }
}