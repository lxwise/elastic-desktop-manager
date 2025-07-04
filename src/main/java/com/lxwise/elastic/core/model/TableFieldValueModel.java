package com.lxwise.elastic.core.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author lstar
 * @create 2025-03
 * @description: 自定义表单模型
 */
public class TableFieldValueModel {
    private final StringProperty field = new SimpleStringProperty();
    private final StringProperty value = new SimpleStringProperty();

    public TableFieldValueModel(String field, String value) {
        this.field.set(field);
        this.value.set(value);
    }

    public StringProperty fieldProperty() { return field; }
    public StringProperty valueProperty() { return value; }

    public String getField() { return field.get(); }
    public String getValue() { return value.get(); }

    public void setField(String field) {
        this.field.set(field);
    }

    public void setValue(String value) {
        this.value.set(value);
    }
}
