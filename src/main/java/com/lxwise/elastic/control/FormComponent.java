package com.lxwise.elastic.control;

import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lstar
 * @create 2025-05
 * @description:
 *  通用表单构建组件，可动态添加字段、校验、取值。
 *  支持常见控件：TextField, PasswordField, TextArea, CheckBox, ComboBox。
 *  使用:
 *  FormFieldWrapper username = new FormFieldWrapper("username", "用户名", new TextField(), true, 20);
 *  FormFieldWrapper password = new FormFieldWrapper("password", "密码", new PasswordField(), true, 20);
 *  FormFieldWrapper role = new FormFieldWrapper("role", "角色", new ComboBox<>(FXCollections.observableArrayList("Admin", "User")), true, 20);
 *  FormFieldWrapper remember = new FormFieldWrapper("remember", "记住我", new CheckBox("是否记住"), false, 20);
 *
 *  FormComponent form = new FormComponent(List.of(username, password, role, remember));
 *
 *  contentPane.getChildren().add(form);
 */
public class FormComponent extends VBox {

    private final Map<String, Node> controlMap = new HashMap<>();
    private final Map<String, Tile> tileMap = new HashMap<>();
    private final Map<String, Boolean> requiredMap = new HashMap<>();

    public FormComponent(List<FormFieldWrapper> fields) {
        this.setSpacing(15);
        this.setPadding(new Insets(10));
        for (FormFieldWrapper field : fields) {
            addField(field);
        }
    }

    /**
     * 添加字段控件到表单中。
     */
    private void addField(FormFieldWrapper wrapper) {
        Tile tile = new Tile();
        tile.setTitle(wrapper.getLabel());

        Node control = wrapper.getControl();

        // 设置控件偏移位置，实现 label 与控件间距
        control.setStyle("-fx-translate-x: " + wrapper.getLabelControlSpacing() + "px;");

        tile.setAction(control);
        tile.setActionHandler(() -> control.requestFocus());

        controlMap.put(wrapper.getKey(), control);
        tileMap.put(wrapper.getKey(), tile);
        requiredMap.put(wrapper.getKey(), wrapper.isRequired());

        // 清除错误提示描述：任意控件聚焦后
        control.focusedProperty().addListener((e, o, n) -> tile.setDescription(""));

        // 监听输入变更：清除红色错误边框
        if (control instanceof TextInputControl) {
            ((TextInputControl) control).textProperty().addListener((obs, oldVal, newVal) -> {
                if (StringUtils.hasText(newVal)) {
                    control.pseudoClassStateChanged(Styles.STATE_DANGER, false);
                }
            });
        } else if (control instanceof ComboBox<?>) {
            ((ComboBox<?>) control).valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    control.pseudoClassStateChanged(Styles.STATE_DANGER, false);
                }
            });
        } else if (control instanceof CheckBox) {
            ((CheckBox) control).selectedProperty().addListener((obs, oldVal, newVal) -> {
                control.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            });
        }

        this.getChildren().add(tile);
    }

    /**
     * 表单校验，返回是否全部字段通过校验。
     */
    public boolean validate() {
        boolean success = true;

        for (Map.Entry<String, Node> entry : controlMap.entrySet()) {
            String key = entry.getKey();
            Node control = entry.getValue();
            boolean required = requiredMap.getOrDefault(key, false);

            if (!required) continue;

            boolean isValid = true;

            if (control instanceof TextInputControl) {
                TextInputControl input = (TextInputControl) control;
                if (!StringUtils.hasText(input.getText())) {
                    isValid = false;
                }
            } else if (control instanceof ComboBox<?>) {
                ComboBox<?> comboBox = (ComboBox<?>) control;
                if (comboBox.getValue() == null) {
                    isValid = false;
                }
            } else if (control instanceof CheckBox) {
                // 这里按需校验 CheckBox（一般不用于必填）
                CheckBox checkBox = (CheckBox) control;
                if (!checkBox.isSelected()) {
                    isValid = false;
                }
            }

            if (!isValid) {
                tileMap.get(key).setDescription("[color=red]字段不能为空[/color]");
                control.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                success = false;
            } else {
                control.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }
        }

        return success;
    }

    /**
     * 获取指定控件对象。
     */
    public <T extends Node> T getControl(String key, Class<T> clazz) {
        Node node = controlMap.get(key);
        if (clazz.isInstance(node)) {
            return clazz.cast(node);
        }
        return null;
    }

    /**
     * 获取所有表单值（key -> value）。
     * TextField/PasswordField/TextArea -> String
     * ComboBox -> Object
     * CheckBox -> Boolean
     */
    public Map<String, Object> getFormValues() {
        Map<String, Object> values = new HashMap<>();

        for (Map.Entry<String, Node> entry : controlMap.entrySet()) {
            String key = entry.getKey();
            Node control = entry.getValue();

            Object value = null;

            if (control instanceof TextInputControl) {
                value = ((TextInputControl) control).getText();
            } else if (control instanceof CheckBox) {
                value = ((CheckBox) control).isSelected();
            } else if (control instanceof ComboBox<?>) {
                value = ((ComboBox<?>) control).getValue();
            } else {
                // 可扩展支持其他控件，如 DatePicker、Spinner 等

            }

            values.put(key, value);
        }

        return values;
    }
}
