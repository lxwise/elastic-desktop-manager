package com.lxwise.elastic.core.task;

import com.lxwise.elastic.core.es.ElasticManage;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: ES索引信息详情任务
 */
public class ESIndicesDetailsTask extends Task<String>{

    private final String indexName;

    public ESIndicesDetailsTask(String indexName) {
        this.indexName = indexName;
    }
    @Override
    protected String call() throws Exception {
        String json = ElasticManage.indicesDetails(ElasticManage.get(),indexName);
        return json;
    }
}
