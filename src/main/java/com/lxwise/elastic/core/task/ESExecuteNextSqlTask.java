package com.lxwise.elastic.core.task;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.model.ESSqlScrollResultModel;
import javafx.concurrent.Task;

/**
 * @author lstar
 * @create 2025-03
 * @description: 下一条Sql执行Rest请求的Task
 */
public class ESExecuteNextSqlTask extends Task<ESSqlScrollResultModel>{
    private final String cursor;
    public ESExecuteNextSqlTask(String cursor) {
        this.cursor = cursor;
    }
    @Override
    protected ESSqlScrollResultModel call() throws Exception {
        ESSqlScrollResultModel result = new ESSqlScrollResultModel();
        String json = ElasticManage.executeNextSql(ElasticManage.get(),cursor);
        if(StrUtil.isBlank(json)){
            return result;
        }
        result = JSON.parseObject(json, ESSqlScrollResultModel.class);
        return result;
    }
}
