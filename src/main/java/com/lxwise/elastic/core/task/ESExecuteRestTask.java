package com.lxwise.elastic.core.task;

import com.lxwise.elastic.core.es.ElasticManage;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: ES执行Rest请求的Task
 */
public class ESExecuteRestTask extends Task<String>{
    private final String method;
    private final String url;
    private final String body;

    public ESExecuteRestTask(String method, String url, String body) {
        this.method = method;
        this.url = url;
        this.body = body;
    }
    @Override
    protected String call() throws Exception {
        String json = ElasticManage.executeRest(ElasticManage.get(),method, url, body);
        return json;
    }
}
