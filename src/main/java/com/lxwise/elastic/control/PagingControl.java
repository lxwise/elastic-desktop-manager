package com.lxwise.elastic.control;

import cn.hutool.core.util.NumberUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.List;

/**
 * @author lstar
 * @create 2025-02
 * @description: 分页控件
 */
public class PagingControl extends HBox {

    private final ObservableList<Integer> pageItemList = FXCollections.observableArrayList(10, 20, 30, 50,100);
    private Pagination pagination;
    private Label totalLabel;
    private Label totalPageLabel;

    private ComboBox<Integer> pageComboBox;

    private TextField toPageText;

    private IntegerProperty total = new SimpleIntegerProperty(0);
    private IntegerProperty pageSize = new SimpleIntegerProperty(10);
    private IntegerProperty pageNum = new SimpleIntegerProperty(1);

    public PagingControl() {

        initView();
        initListeners();
    }

    private void initView() {
        pagination = new Pagination(1);
        pagination.setStyle("-fx-page-information-visible:flase;");
        pagination.setMinHeight(50);
        pagination.setMaxHeight(50);
        totalLabel = new Label();
        pageComboBox = new ComboBox();
        toPageText = new TextField();
        toPageText.setPrefWidth(80);
        setMaxHeight(40);
        setMinHeight(40);
        setAlignment(Pos.CENTER_RIGHT);
        setSpacing(10);
        getChildren().addAll(new Label("共"), totalLabel, new Label("条"), pageComboBox, pagination, new Label("前往"), toPageText, totalPageLabel = new Label());

        pageComboBox.setCellFactory(
                new Callback<ListView<Integer>, ListCell<Integer>>() {
                    @Override
                    public ListCell<Integer> call(ListView<Integer> param) {
                        final ListCell<Integer> cell = new ListCell<Integer>() {

                            @Override
                            public void updateItem(Integer item,
                                                   boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    setText(item + "条/页");

                                } else {
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                });

        totalLabel.textProperty().bind(Bindings.createStringBinding(
                () -> getTotal() + "", totalProperty())
        );
        totalPageLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "页 共" + pagination.getPageCount() + "页", pagination.pageCountProperty())
        );
        pageComboBox.setItems(pageItemList);

        pageComboBox.getSelectionModel().select(0);

    }

    private void initListeners() {
//        pageNum.bind(pagination.currentPageIndexProperty());
        // 用下面两个 Listener 做双向绑定
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            pageNum.set(newVal.intValue() + 1); // pageNum 是从1开始
        });

        pageNum.addListener((obs, oldVal, newVal) -> {
            int newIndex = newVal.intValue() - 1;
            if (pagination.getCurrentPageIndex() != newIndex) {
                pagination.setCurrentPageIndex(newIndex); // 保证程序设置 pageNum 也能更新 pagination
            }
        });
        pageSize.bind(pageComboBox.getSelectionModel().selectedItemProperty());
        toPageText.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {

                Integer index = NumberUtil.parseInt(toPageText.getText());
                if (index <= 0) {
                    index = 1;
                } else if (index > pagination.getPageCount()) {
                    index = pagination.getPageCount();
                }
                pagination.setCurrentPageIndex(index - 1);
            }
        });

        total.addListener((observable, oldValue, newValue) -> {
            int totalCount = newValue.intValue(); // 总条数
            int pageSize = (getPageSize() == 0 ? 1 : getPageSize()); // 每页条数
            int totalPages = totalCount / pageSize; // 计算总页数
            if (totalCount % pageSize != 0) {
                totalPages++; // 如果有余数，则总页数加1
            }
            if (totalCount == 0) {
                totalPages++; // 如果有余数，则总页数加1
            }

            pagination.setPageCount(totalPages);
        });
        pageComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int totalCount = getTotal(); // 总条数
            int pageSize = (newValue.intValue() == 0 ? 1 : newValue.intValue()); // 每页条数
            int totalPages = totalCount / pageSize; // 计算总页数
            if (totalCount % pageSize != 0) {
                totalPages++; // 如果有余数，则总页数加1
            }
            if (totalCount == 0) {
                totalPages++;
            }

            pagination.setPageCount(totalPages);
            pagination.setCurrentPageIndex(0); // 分页大小变化后跳到第一页
        });


    }

    public void setPageSize(Integer pageSize) {
        pageComboBox.getSelectionModel().select(pageSize);
    }

    public ObservableList<Integer> getPageItemList() {
        return pageItemList;
    }


    public int getTotal() {
        return total.get();
    }

    public IntegerProperty totalProperty() {
        return total;
    }

    public void setTotal(int total) {
        this.total.set(total);
    }

    public int getPageSize() {
        return pageSize.get();
    }

    public IntegerProperty pageSizeProperty() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize.set(pageSize);
    }

    public int getPageNum() {
        return pageNum.get();
    }

    public IntegerProperty pageNumProperty() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum.set(pageNum);
    }

    public void setPageItemList(List<Integer> items) {
        pageItemList.setAll(items);
        pageComboBox.setItems(pageItemList);
        if (!items.isEmpty()) {
            pageComboBox.getSelectionModel().select(0);
        }
    }

}
