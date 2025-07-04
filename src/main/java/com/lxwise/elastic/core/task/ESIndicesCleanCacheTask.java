package com.lxwise.elastic.core.task;

import com.lxwise.elastic.core.es.ElasticManage;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: 清除 ES指定索引缓存任务
 */
public class ESIndicesCleanCacheTask extends Task<String>{

    private final String indexName;

    public ESIndicesCleanCacheTask(String indexName) {
        this.indexName = indexName;
    }
    @Override
    protected String call() throws Exception {
        String json = ElasticManage.indicesCleanCache(ElasticManage.get(),indexName);
        return json;
    }
}
