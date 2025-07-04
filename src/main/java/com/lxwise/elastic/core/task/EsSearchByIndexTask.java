package com.lxwise.elastic.core.task;

import com.lxwise.elastic.core.es.ElasticManage;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: 查询索引任务
 */
public class EsSearchByIndexTask extends Task<String> {

    private final String indexName;
    private final String body;
    private final Integer timeout;

    public EsSearchByIndexTask(String indexName, String body,Integer timeout) {
        this.indexName = indexName;
        this.body = body;
        this.timeout = timeout;
    }
    @Override
    protected String call() throws Exception {
        String json = ElasticManage.searchByIndex(ElasticManage.get(),indexName,body,timeout);
        return json;
    }
}
