package com.lxwise.elastic.control;

import javafx.scene.Node;

/**
 * @author lstar
 * @create 2025-05
 * @description:
 * 动态表单字段包装类
 使用:
 FormFieldWrapper username = new FormFieldWrapper("username", "用户名", new TextField(), true, 20);
 FormFieldWrapper password = new FormFieldWrapper("password", "密码", new PasswordField(), true, 20);
 FormFieldWrapper role = new FormFieldWrapper("role", "角色", new ComboBox<>(FXCollections.observableArrayList("Admin", "User")), true, 20);
 FormFieldWrapper remember = new FormFieldWrapper("remember", "记住我", new CheckBox("是否记住"), false, 20);
 FormComponent form = new FormComponent(List.of(username, password, role, remember));
 contentPane.getChildren().add(form);
 */

public class FormFieldWrapper {

    private final String key;
    private final String label;
    private final Node control;
    private final boolean required;
    private final double labelControlSpacing;

    public FormFieldWrapper(String key, String label, Node control, boolean required) {
        this(key, label, control, required, 10); // 默认间距
    }

    public FormFieldWrapper(String key, String label, Node control, boolean required, double labelControlSpacing) {
        this.key = key;
        this.label = label;
        this.control = control;
        this.required = required;
        this.labelControlSpacing = labelControlSpacing;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public Node getControl() { return control; }
    public boolean isRequired() { return required; }
    public double getLabelControlSpacing() { return labelControlSpacing; }
}

