package com.lxwise.elastic.core.model.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * @author lstar
 * @create 2025-03
 * @description: 历史记录分页模型
 */
public class ClusterRestHistoryView {

    public SimpleIntegerProperty total = new SimpleIntegerProperty(0);
    public IntegerProperty pageNum = new SimpleIntegerProperty(0);
    public IntegerProperty pageSize = new SimpleIntegerProperty(10);

    public int getTotal() {
        return total.get();
    }

    public SimpleIntegerProperty totalProperty() {
        return total;
    }

    public int getPageNum() {
        return pageNum.get();
    }

    public IntegerProperty pageNumProperty() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize.get();
    }

    public IntegerProperty pageSizeProperty() {
        return pageSize;
    }
}
