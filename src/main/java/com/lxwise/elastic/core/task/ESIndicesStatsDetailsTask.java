package com.lxwise.elastic.core.task;

import com.lxwise.elastic.core.es.ElasticManage;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: ES索引状态详情任务
 */
public class ESIndicesStatsDetailsTask extends Task<String>{

    private final String indexName;

    public ESIndicesStatsDetailsTask(String indexName) {
        this.indexName = indexName;
    }
    @Override
    protected String call() throws Exception {
        String json = ElasticManage.indicesStatsDetails(ElasticManage.get(),indexName);
        return json;
    }
}
