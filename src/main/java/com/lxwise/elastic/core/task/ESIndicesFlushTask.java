package com.lxwise.elastic.core.task;

import com.lxwise.elastic.core.es.ElasticManage;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: Flush ES指定索引任务
 */
public class ESIndicesFlushTask extends Task<String>{

    private final String indexName;

    public ESIndicesFlushTask(String indexName) {
        this.indexName = indexName;
    }
    @Override
    protected String call() throws Exception {
        String json = ElasticManage.indicesFlush(ElasticManage.get(),indexName);
        return json;
    }
}
