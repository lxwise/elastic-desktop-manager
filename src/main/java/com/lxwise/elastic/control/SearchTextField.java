package com.lxwise.elastic.control;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.util.function.Consumer;

/**
 * @author lstar
 * @create 2025-02
 * @description: 可复用的带 ICON和清除 的搜索框组件
 *
 */
public class SearchTextField extends StackPane {

    private final TextField textField;
    private final FontIcon icon;
    private FontIcon clearBtn;
    private boolean showClearButton;

    private Consumer<String> onTextChanged;
    private Runnable onClearClicked;

    public SearchTextField() {
        this(false);
    }

    public SearchTextField(boolean showClearButton) {
        this.textField = new TextField();
        this.icon = new FontIcon(Material2MZ.SEARCH);
        this.showClearButton = showClearButton;
        initialize();
    }

    private void initialize() {
        this.textField.setPromptText("Search");

        this.getChildren().addAll(textField, icon);
        StackPane.setAlignment(icon, Pos.CENTER_LEFT);
        StackPane.setMargin(icon, new Insets(0, 0, 0, 5));

        if (showClearButton) {
            clearBtn = new FontIcon(Material2OutlinedAL.CLEAR);
            clearBtn.setCursor(Cursor.DEFAULT);
            clearBtn.setOnMouseClicked(event -> {
                textField.clear();
            });

            this.getChildren().add(clearBtn);
            StackPane.setAlignment(clearBtn, Pos.CENTER_RIGHT);
        }

        textField.setPadding(new Insets(3, showClearButton ? 30 : 3, 3, 30));

        // 文本变化监听（用于外部处理）
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (onTextChanged != null) {
                onTextChanged.accept(newVal);
            }
        });
    }

    // ===== 对外方法 =====

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public void setPromptText(String promptText) {
        textField.setPromptText(promptText);
    }

    public String getPromptText() {
        return textField.getPromptText();
    }

    public void setOnTextChanged(Consumer<String> onTextChanged) {
        this.onTextChanged = onTextChanged;
    }

    public void setOnClearClicked(Runnable onClearClicked) {
        this.onClearClicked = onClearClicked;
    }

    public TextField getInnerTextField() {
        return textField;
    }
}
