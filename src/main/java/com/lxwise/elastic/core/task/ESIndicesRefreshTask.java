package com.lxwise.elastic.core.task;

import com.lxwise.elastic.core.es.ElasticManage;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: 刷新ES指定索引任务
 */
public class ESIndicesRefreshTask extends Task<String>{

    private final String indexName;

    public ESIndicesRefreshTask(String indexName) {
        this.indexName = indexName;
    }
    @Override
    protected String call() throws Exception {
        String json = ElasticManage.indicesRefresh(ElasticManage.get(),indexName);
        return json;
    }
}
