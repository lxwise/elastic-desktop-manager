package com.lxwise.elastic.core.model;

import java.util.List;

/**
 * @author lstar
 * @create 2025-03
 * @description: es sql搜索结果模型
 */
public class ESSqlScrollResultModel {
    private List<ESSqlScrollResultModel.ColumnMeta> columns;
    private List<List<Object>> rows;
    private String cursor;

    public List<ColumnMeta> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMeta> columns) {
        this.columns = columns;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public void setRows(List<List<Object>> rows) {
        this.rows = rows;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public static class ColumnMeta {
        private String name;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

}
