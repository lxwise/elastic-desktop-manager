package com.lxwise.elastic.control;

import atlantafx.base.util.IntegerStringConverter;
import cn.hutool.core.collection.CollUtil;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.model.KeyValue;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.ResourceBundle;

/**
 * @author lstar
 * @create 2025-02
 * @description: 条件属性动态组件
 */
public class ConditionAttributeComponent {
    /**
     * 创建简单条件动态组件
     * @return
     */
    public static VBox createConditionRow(List<String> fields) {
        HBox conditionRow = new HBox(5);
        conditionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label toggleLabel = new Label("▶");
        toggleLabel.setVisible(false);
        toggleLabel.setCursor(Cursor.HAND);
        //在不可见时同时从布局中移除。
        // 如果 visible = false，managed = false，该节点就不参与布局（不占位）。
        //如果 visible = true，managed = true，则参与布局。
        toggleLabel.managedProperty().bind(toggleLabel.visibleProperty());


        CheckBox enabledCheckBox = new CheckBox(SettingClient.bundle().getString("cluster.search.condition.enabled"));
        enabledCheckBox.setSelected(true);
        CheckBox groupCheckBox = new CheckBox(SettingClient.bundle().getString("cluster.search.condition.groupCheck"));

        ComboBox<String> splicerComboBox = new ComboBox<>();
        splicerComboBox.getItems().addAll("must", "must_not", "should");
        splicerComboBox.setValue("must");

        SearchableComboBox<String> fieldComboBox = new SearchableComboBox<>();
        fieldComboBox.setEditable( true);
        fieldComboBox.getItems().addAll(fields);
        if(CollUtil.isNotEmpty(fields)){
            fieldComboBox.setValue(fields.get(0));
        }

        ComboBox<String> operatorComboBox = new ComboBox<>();
        operatorComboBox.getItems().addAll("term", "terms", "wildcard", "match", "range", "exists");
        operatorComboBox.setValue("term");

        ComboBox<String> leftOperatorComboBox = new ComboBox<>();
        leftOperatorComboBox.getItems().addAll("gt", "gte");
        leftOperatorComboBox.setValue("gt");

        TextField value1TextField = new TextField();
        ComboBox<String> rightOperatorComboBox = new ComboBox<>();
        rightOperatorComboBox.getItems().addAll("lt", "lte");
        rightOperatorComboBox.setValue("lt");

        TextField value2TextField = new TextField();
        TextField value3TextField = new TextField();

        Button addChildButton = new Button("+");
        addChildButton.setVisible(false);
        addChildButton.managedProperty().bind(addChildButton.visibleProperty());
        Button removeButton = new Button("-");

        // 容器组合
        conditionRow.getChildren().addAll(
                toggleLabel, enabledCheckBox, groupCheckBox, splicerComboBox,
                fieldComboBox, operatorComboBox, value3TextField,
                addChildButton, removeButton
        );

        VBox childrenContainer = new VBox(5);
        childrenContainer.setPadding(new Insets(0, 0, 0, 20));
        childrenContainer.setVisible(false);
        childrenContainer.managedProperty().bind(childrenContainer.visibleProperty());

        // 动态切换操作符字段
        operatorComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            conditionRow.getChildren().removeAll(leftOperatorComboBox, value1TextField, rightOperatorComboBox, value2TextField, value3TextField);

            int index = conditionRow.getChildren().indexOf(operatorComboBox);
            if ("range".equals(newVal)) {
                conditionRow.getChildren().addAll(index + 1, List.of(leftOperatorComboBox, value1TextField, rightOperatorComboBox, value2TextField));
            } else if (!"exists".equals(newVal)) {
                conditionRow.getChildren().add(index + 1, value3TextField);
            }
        });

        // 子项添加逻辑
        addChildButton.setOnAction(e -> {
            VBox childRow = createConditionRow(fields);
            childrenContainer.getChildren().add(childRow);
            childrenContainer.setVisible(true);
            toggleLabel.setVisible(true);
            toggleLabel.setText("▼");
        });

        // 折叠/展开逻辑
        toggleLabel.setOnMouseClicked(e -> {
            boolean expanded = childrenContainer.isVisible();
            childrenContainer.setVisible(!expanded);
            toggleLabel.setText(expanded ? "▶" : "▼");
        });

        // 删除按钮逻辑
        removeButton.setOnAction(e -> {
            Parent wrapper = conditionRow.getParent();
            if (wrapper != null && wrapper.getParent() instanceof VBox parentVBox) {
                parentVBox.getChildren().remove(wrapper);
            }
        });


        // 控制组逻辑
        groupCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                addChildButton.setVisible(true);
                conditionRow.getChildren().removeAll(fieldComboBox, operatorComboBox, leftOperatorComboBox, rightOperatorComboBox,
                        value1TextField, value2TextField, value3TextField);
            } else {
                addChildButton.setVisible(false);
                childrenContainer.getChildren().clear();
                childrenContainer.setVisible(false);
                toggleLabel.setVisible(false);

                // 恢复条件输入组件
                int index = conditionRow.getChildren().indexOf(splicerComboBox);
                conditionRow.getChildren().addAll(index + 1, List.of(fieldComboBox, operatorComboBox, value3TextField));
            }
        });

        VBox wrapper = new VBox(5);
        wrapper.getChildren().addAll(conditionRow, childrenContainer);
        return wrapper;
    }


    /**
     * 创建聚合动态组件
     * @return
     */
    public static VBox createAttributeRow(List<String> fields) {
        HBox conditionRow = new HBox(5);
        conditionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label toggleLabel = new Label("▶");
        toggleLabel.setVisible(false);
        toggleLabel.setCursor(Cursor.HAND);
        //在不可见时同时从布局中移除。
        // 如果 visible = false，managed = false，该节点就不参与布局（不占位）。
        //如果 visible = true，managed = true，则参与布局。
        toggleLabel.managedProperty().bind(toggleLabel.visibleProperty());

        ComboBox<String> aggComboBox = new ComboBox<>();
        aggComboBox.getItems().addAll( "max","terms", "min","sum","value_count","avg","cardinality","date_histogram");
        aggComboBox.setValue("max");

        SearchableComboBox<String> fieldComboBox = new SearchableComboBox<>();
        fieldComboBox.setEditable( true);
        fieldComboBox.getItems().addAll(fields);
        if(CollUtil.isNotEmpty(fields)){
            fieldComboBox.setValue(fields.get(0));
        }

        TextField aliasTextField = new TextField();
        //数量
        Spinner<Integer> quantitySpinner = new Spinner<>(1, Integer.MAX_VALUE, 1);
        IntegerStringConverter.createFor(quantitySpinner);
        quantitySpinner.setEditable(true);
        //间隔
        ComboBox<KeyValue> intervalComboBox = new ComboBox<>();

        // 添加选项
//        intervalComboBox.getItems().addAll(
//                new KeyValue("s", "秒"),
//                new KeyValue("m", "分"),
//                new KeyValue("h", "时"),
//                new KeyValue("d", "天"),
//                new KeyValue("w", "周"),
//                new KeyValue("M", "月"),
//                new KeyValue("q", "季"),
//                new KeyValue("y", "年")
//        );
        intervalComboBox.getItems().addAll(getTimeUnitOptions(SettingClient.bundle()));


        // 设置显示文本为 label
        intervalComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(KeyValue item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.getValue());
            }
        });

        // 设置按钮区域显示为 label
        intervalComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(KeyValue item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.getValue());
            }
        });

        // 设置默认选中项
//        intervalComboBox.setValue(new KeyValue("s", "秒"));

        // 监听选中事件
        intervalComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {

            }
        });


        Button addChildButton = new Button("+");
        addChildButton.setVisible(false);
        addChildButton.managedProperty().bind(addChildButton.visibleProperty());
        Button removeButton = new Button("-");
        // 容器组合
        conditionRow.getChildren().addAll(
                toggleLabel,aggComboBox, fieldComboBox, aliasTextField,
                addChildButton, removeButton
        );

        VBox childrenContainer = new VBox(5);
        childrenContainer.setPadding(new Insets(0, 0, 0, 20));
        childrenContainer.setVisible(false);
        childrenContainer.managedProperty().bind(childrenContainer.visibleProperty());

        // 动态切换操作符字段
        aggComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            conditionRow.getChildren().removeAll(quantitySpinner, intervalComboBox);
            addChildButton.setVisible(false);

            int index = conditionRow.getChildren().indexOf(aliasTextField);
            if ("terms".equals(newVal)) {
                conditionRow.getChildren().addAll(index + 1, List.of(quantitySpinner));
                addChildButton.setVisible(true);
            }
            if ("date_histogram".equals(newVal)) {
                conditionRow.getChildren().addAll(index + 1, List.of(quantitySpinner, intervalComboBox));
                addChildButton.setVisible(true);
            }
        });

        // 子项添加逻辑
        addChildButton.setOnAction(e -> {
            VBox childRow = createAttributeRow(fields);
            childrenContainer.getChildren().add(childRow);
            childrenContainer.setVisible(true);
            toggleLabel.setVisible(true);
            toggleLabel.setText("▼");
        });

        // 折叠/展开逻辑
        toggleLabel.setOnMouseClicked(e -> {
            boolean expanded = childrenContainer.isVisible();
            childrenContainer.setVisible(!expanded);
            toggleLabel.setText(expanded ? "▶" : "▼");
        });

        // 删除按钮逻辑
        removeButton.setOnAction(e -> {
            Parent wrapper = conditionRow.getParent();
            if (wrapper != null && wrapper.getParent() instanceof VBox parentVBox) {
                parentVBox.getChildren().remove(wrapper);
            }
        });


        // 控制组逻辑

        VBox wrapper = new VBox(5);
        wrapper.getChildren().addAll(conditionRow, childrenContainer);
        return wrapper;
    }

    public static List<KeyValue> getTimeUnitOptions(ResourceBundle bundle) {
        return List.of(
                new KeyValue("s", bundle.getString("cluster.search.time.unit.s")),
                new KeyValue("m", bundle.getString("cluster.search.time.unit.m")),
                new KeyValue("h", bundle.getString("cluster.search.time.unit.h")),
                new KeyValue("d", bundle.getString("cluster.search.time.unit.d")),
                new KeyValue("w", bundle.getString("cluster.search.time.unit.w")),
                new KeyValue("M", bundle.getString("cluster.search.time.unit.M")),
                new KeyValue("q", bundle.getString("cluster.search.time.unit.q")),
                new KeyValue("y", bundle.getString("cluster.search.time.unit.y"))
        );
    }

}
