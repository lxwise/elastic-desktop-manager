package com.lxwise.elastic.entity;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author lstar
 * @create 2025-03
 * @description: es历史命令属性
 */
public class EsCommandHistoryProperty {
    private String id;
    private final SimpleStringProperty method = new SimpleStringProperty("");
    private final SimpleStringProperty command = new SimpleStringProperty("");
    private final SimpleStringProperty commandValue = new SimpleStringProperty("");
    private final SimpleStringProperty createTime = new SimpleStringProperty("");

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method.get();
    }

    public SimpleStringProperty methodProperty() {
        return method;
    }

    public void setMethod(String method) {
        this.method.set(method);
    }

    public String getCommand() {
        return command.get();
    }

    public SimpleStringProperty commandProperty() {
        return command;
    }

    public void setCommand(String command) {
        this.command.set(command);
    }

    public String getCommandValue() {
        return commandValue.get();
    }

    public SimpleStringProperty commandValueProperty() {
        return commandValue;
    }

    public void setCommandValue(String commandValue) {
        this.commandValue.set(commandValue);
    }

    public String getCreateTime() {
        return createTime.get();
    }

    public SimpleStringProperty createTimeProperty() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime.set(createTime);
    }
}
