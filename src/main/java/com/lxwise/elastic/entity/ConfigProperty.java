package com.lxwise.elastic.entity;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author lstar
 * @create 2025-02
 * @description: 配置属性
 */
public class ConfigProperty {
    private String id;
    private final SimpleStringProperty name = new SimpleStringProperty("");
    private final SimpleStringProperty servers = new SimpleStringProperty("");
    private final SimpleStringProperty type = new SimpleStringProperty("cluster");
    private final SimpleStringProperty parentId = new SimpleStringProperty("");
    private final SimpleBooleanProperty security = new SimpleBooleanProperty(false);
    private final SimpleStringProperty username = new SimpleStringProperty("");
    private final SimpleStringProperty password = new SimpleStringProperty("");

    public ConfigProperty copy() {
        ConfigProperty result = new ConfigProperty();
        result.setId(this.getId());
        result.setName(this.getName());
        result.setServers(this.getServers());
        result.setType(this.getType());
        result.setParentId(this.getParentId());
        result.setSecurity(this.getSecurity());
        result.setUsername(this.getUsername());
        result.setPassword(this.getPassword());
        return result;
    }

    public void cluster() {
        this.type.set("cluster");
    }

    public SimpleStringProperty type() {
        return this.type;
    }

    public boolean _folder() {
        return "folder".equals(this.getType());
    }

    public void folder() {
        this.setType("folder");
    }

    public void setType(String type) {
        this.type.setValue(type);
    }

    public String getType() {
        return this.type.getValue();
    }

    public void setParentId(String parentId) {
        this.parentId.setValue(parentId);
    }

    public String getParentId() {
        return this.parentId.getValue();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setServers(String servers) {
        this.servers.set(servers);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty name() {
        return name;
    }

    public String getServers() {
        return servers.get();
    }

    public SimpleStringProperty servers() {
        return servers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setSecurity(Boolean security) {
        this.security.set(security);
    }

    public Boolean getSecurity() {
        return security.get();
    }

    public SimpleBooleanProperty security() {
        return security;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public SimpleStringProperty username() {
        return username;
    }

    public String getPassword() {
        return password.get();
    }

    public SimpleStringProperty password() {
        return password;
    }

}
