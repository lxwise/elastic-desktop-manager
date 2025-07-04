package com.lxwise.elastic.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/***
 * @author lstar
 * @create 2025-02
 * @description: 表格表头过滤组件
 *
 * SearchableCheckListView checkListView = new SearchableCheckListView();
 * checkListView.setItems(Arrays.asList(
 *     "transfer", "label1", "label1Name", "label2",
 *     "label2Name", "label3", "label3Name", "linkPhone", "linkQq"
 * ));
 *
 * // 监听选中的项
 * checkListView.setSelectionListener(selectedItems -> {
 *     System.out.println("当前选中：" + selectedItems);
 * });
 *
 * // 获取当前选中的值（可用于按钮点击事件）
 * List<String> selected = checkListView.getSelectedItems();
 * System.out.println("已选中的项: " + selected);
 */
public class SearchableTableHeardFilterPane extends VBox {

    private ListView<CheckBox> listView;
    private ObservableList<String> items;
    private ObservableList<CheckBox> checkBoxItems;
    private TextField searchField;
    private Label selectedCountLabel;
    private CheckBox selectAllCheckBox;

    private Consumer<List<String>> selectionListener;

    public SearchableTableHeardFilterPane() {
        this.items = FXCollections.observableArrayList();
        this.checkBoxItems = FXCollections.observableArrayList();
        this.listView = new ListView<>(checkBoxItems);
        this.listView.setPrefHeight(500);

        // 搜索框
        this.searchField = new TextField();
        this.searchField.setPromptText("过滤");
        this.searchField.textProperty().addListener((obs, oldVal, newVal) -> filterList(newVal));

        // 统计已选择数量的 Label
        this.selectedCountLabel = new Label("已选择 0/0");

        // 全选复选框
        this.selectAllCheckBox = new CheckBox("全选");
        this.selectAllCheckBox.setOnAction(e -> toggleSelectAll(selectAllCheckBox.isSelected()));

        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(selectAllCheckBox, selectedCountLabel);

        this.setPadding(new Insets(10));
        this.setSpacing(5);
        this.getChildren().addAll(hBox, searchField, listView);
    }

    // 设置数据
    public void setItems(List<String> newItems) {
        this.items.setAll(newItems);
        updateCheckBoxList(items);
    }

    // 过滤列表
    private void filterList(String filter) {
        ObservableList<String> filteredItems = items.stream()
                .filter(item -> item.toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        updateCheckBoxList(filteredItems);
    }

    // 更新列表
    private void updateCheckBoxList(ObservableList<String> updatedItems) {
        checkBoxItems.clear();
        for (String item : updatedItems) {
            CheckBox checkBox = new CheckBox(item);
            checkBox.setOnAction(e -> updateSelectedCount());
            checkBoxItems.add(checkBox);
        }
        updateSelectedCount();
    }

    // 计算已选择数量
    private void updateSelectedCount() {
        long count = checkBoxItems.stream().filter(CheckBox::isSelected).count();
        selectedCountLabel.setText("已选择 " + count + "/" + items.size());
        selectAllCheckBox.setSelected(count == items.size());

        // 触发监听器
        if (selectionListener != null) {
            selectionListener.accept(getSelectedItems());
        }
    }


    // 全选/取消全选
    private void toggleSelectAll(boolean isSelected) {
        checkBoxItems.forEach(checkBox -> checkBox.setSelected(isSelected));
        updateSelectedCount();
    }

    // 获取选中的项
    public List<String> getSelectedItems() {
        return checkBoxItems.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());
    }

    // 设置选中的项
    public void setCheckedItems(List<String> checkedItems) {
        if (checkedItems == null || checkedItems.isEmpty()) {
            // 如果传入的列表为空，则取消所有选中项
            checkBoxItems.forEach(checkBox -> checkBox.setSelected(false));
        } else {
            // 遍历所有 CheckBox，根据传入的列表设置选中状态
            checkBoxItems.forEach(checkBox -> {
                checkBox.setSelected(checkedItems.contains(checkBox.getText()));
            });
        }
        updateSelectedCount();
    }

    /**
     * 设置选中项监听器
     * @param listener
     */
    public void setSelectionListener(Consumer<List<String>> listener) {
        this.selectionListener = listener;
    }

}
