package com.lxwise.elastic.core.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.model.ESShardsModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;

/**
 * @author lstar
 * @create 2025-03
 * @description: es分片信息任务
 */
public class ESShardsInfoTask extends Task<ObservableList<ESShardsModel>>{
    @Override
    protected ObservableList<ESShardsModel> call() throws Exception {
        String json = ElasticManage.shardsInfo(ElasticManage.get());
        List<ESShardsModel> list = JSON.parseObject(json, new TypeReference<List<ESShardsModel>>() {});
        ObservableList<ESShardsModel> shardsList = FXCollections.observableArrayList(list);
        return shardsList;
    }
}
