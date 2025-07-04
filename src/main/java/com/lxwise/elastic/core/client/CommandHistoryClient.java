package com.lxwise.elastic.core.client;

import cn.hutool.core.util.StrUtil;
import com.lxwise.elastic.entity.EsCommandHistoryProperty;
import com.lxwise.elastic.utils.DatasourceUtils;
import java.util.List;

/**
 * @author lstar
 * @create 2025-02
 * @description: 命令历史sql
 */
public class CommandHistoryClient {
    private final static String INSERT = "INSERT INTO es_command_history(id, method, command, commandValue, createTime) VALUES (?, ?, ?, ?, ?)";
    private final static String INSERT_NULL = "INSERT INTO es_command_history(id, method, command, createTime) VALUES (?, ?, ?, ?)";
    private static final String DELETE = "delete from es_command_history where id = '%s'";
    private static final String SELECT = "select * from es_command_history order by id desc";

    public static List<EsCommandHistoryProperty> query4List() {
        return DatasourceUtils.query4List(SELECT, EsCommandHistoryProperty.class);
    }
    public static void save(EsCommandHistoryProperty historyProperty) {
        String sql;
        Object[] args;

        if (StrUtil.isBlank(historyProperty.getCommandValue())) {
            sql = INSERT_NULL;
            args = new Object[]{
                    historyProperty.getId(),
                    historyProperty.getMethod(),
                    historyProperty.getCommand(),
                    historyProperty.getCreateTime()
            };
        } else {
            sql = INSERT;
            args = new Object[]{
                    historyProperty.getId(),
                    historyProperty.getMethod(),
                    historyProperty.getCommand(),
                    historyProperty.getCommandValue(),
                    historyProperty.getCreateTime()
            };
        }

        DatasourceUtils.execute(sql, args);
    }

    public static void deleteById(String id) {
        DatasourceUtils.execute(String.format(DELETE, id));
    }
}
