package com.lxwise.elastic.core.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.model.ESNodeInfoModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lstar
 * @create 2025-03
 * @description: es节点信息任务
 */
public class ESNodeInfoTask extends Task<ObservableList<ESNodeInfoModel>>{
    @Override
    protected ObservableList<ESNodeInfoModel> call() throws Exception {
        ObservableList<ESNodeInfoModel> nodeList = FXCollections.observableArrayList();

        String json = ElasticManage.nodesDetails(ElasticManage.get());
        if(StrUtil.isBlank(json)){
            return nodeList;
        }
        List<Map<String, Object>> list = JSON.parseObject(json, new TypeReference<List<Map<String, Object>>>() {
        });

        if (CollUtil.isEmpty(list)) {
            return nodeList;
        }
        for (Map<String, Object> nodeMap : list) {
            ESNodeInfoModel model = new ESNodeInfoModel();
            model.name = getStr(nodeMap, "name");
            model.httpAddress = getStr(nodeMap, "http_address");
            model.version = getStr(nodeMap, "version");
            model.master = "*".equals(getStr(nodeMap, "master")) ? SettingClient.bundle().getString("cluster.node.table.master.true") : SettingClient.bundle().getString("cluster.node.table.master.false");
            model.role = getStr(nodeMap, "node.role");
            model.load1m = getStr(nodeMap, "load_1m");
            model.load5m = getStr(nodeMap, "load_5m");
            model.load15m = getStr(nodeMap, "load_15m");
            model.cpu = getStr(nodeMap, "cpu");
            model.ramCurrent = getStr(nodeMap, "ram.current");
            model.ramMax = getStr(nodeMap, "ram.max");
            model.ramPercent = getStr(nodeMap, "ram.percent");
            model.heapCurrent = getStr(nodeMap, "heap.current");
            model.heapMax = getStr(nodeMap, "heap.max");
            model.heapPercent = getStr(nodeMap, "heap.percent");
            model.diskUsed = getStr(nodeMap, "disk.used");
            model.diskTotal = getStr(nodeMap, "disk.total");
            model.diskUsedPercent = getStr(nodeMap, "disk.used_percent");

            nodeList.add(model);
            // 设置完整的原始 JSON 数据映射
            Map<String, String> detailMap = new LinkedHashMap<>();
            nodeMap.forEach((key, value) -> {
                detailMap.put(key, value.toString());
            });
            model.setRawData(detailMap);
        }

        return nodeList;
    }


    // 工具方法
    private String getStr(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}
