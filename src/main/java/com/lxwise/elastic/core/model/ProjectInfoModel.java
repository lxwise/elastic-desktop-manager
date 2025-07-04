package com.lxwise.elastic.core.model;

/**
 * @author lstar
 * @create 2025-02
 * @description: 项目信息
 */
public class ProjectInfoModel {
    String name;
    String url;
    String desc;

    public ProjectInfoModel(String name, String url, String desc) {
        this.name = name;
        this.url = url;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
