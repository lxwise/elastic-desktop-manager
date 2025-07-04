package com.lxwise.elastic.core.task;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.model.ESSqlScrollResultModel;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: Sql执行Rest请求的Task
 */
public class ESExecuteSqlTask extends Task<ESSqlScrollResultModel>{
    private final String sql;
    private final Integer bathSize;

    public ESExecuteSqlTask(String sql, Integer  bathSize) {
        this.sql = sql;
        this.bathSize = bathSize;
    }
    @Override
    protected ESSqlScrollResultModel call() throws Exception {
        ESSqlScrollResultModel result = new ESSqlScrollResultModel();
        String json = ElasticManage.executeSql(ElasticManage.get(),sql, bathSize);
        if(StrUtil.isBlank(json)){
            return result;
        }
        result = JSON.parseObject(json, ESSqlScrollResultModel.class);
        return result;
    }
}
