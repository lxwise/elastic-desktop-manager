package com.lxwise.elastic.core.task;

import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.entity.ConfigProperty;
import javafx.concurrent.Task;
import org.elasticsearch.client.RestHighLevelClient;
/**
 * @author lstar
 * @create 2025-03
 * @description: es连接任务
 */
public class EsConnectTask extends Task<RestHighLevelClient> {
    private final ConfigProperty property;

    public EsConnectTask(ConfigProperty property) {
        this.property = property;
    }

    @Override
    protected RestHighLevelClient call() throws Exception {
        return ElasticManage.connect(property);
    }
}