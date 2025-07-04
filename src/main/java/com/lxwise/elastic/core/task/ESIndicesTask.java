package com.lxwise.elastic.core.task;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.model.ESIndicesModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lstar
 * @create 2025-03
 * @description: ES索引信息任务
 */
public class ESIndicesTask extends Task<ObservableList<ESIndicesModel>>{
    private final String format;

    public ESIndicesTask(String format) {
        this.format = format;
    }
    @Override
    protected ObservableList<ESIndicesModel> call() throws Exception {
        String json = ElasticManage.indicesInfo(ElasticManage.get(),format);
        List<ESIndicesModel> list = JSON.parseObject(json, new TypeReference<List<ESIndicesModel>>() {});
        if(CollUtil.isNotEmpty(list)){
            list = list.stream()
                    .sorted(Comparator.comparing(ESIndicesModel::getIndex))
                    .collect(Collectors.toList());
        }
        ObservableList<ESIndicesModel> indicesList = FXCollections.observableArrayList(list);
        return indicesList;
    }
}
