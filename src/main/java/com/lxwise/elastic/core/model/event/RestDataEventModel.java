package com.lxwise.elastic.core.model.event;

/**
 * @author lstar
 * @create 2025-02
 * @description: rest数据
 */
public class RestDataEventModel {

    private String method;
    private String body;
    private String url;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
