package com.lxwise.elastic.control;

import atlantafx.base.theme.Styles;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.utils.ClipboardUtils;
import com.lxwise.elastic.utils.JsonFileSaver;
import com.lxwise.elastic.utils.MessageUtils;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lstar
 * @create 2025-02
 * @description: 可复用的自带富文本区域版本,包含搜索文本面板，包含一个带高亮搜索、下一个匹配、复制和下载功能的文本区域
 *
 *需要从外部传富文本区域版本
 */

public class SearchToolbarPane extends HBox {

    private final TextField searchTextField;
    // 搜索图标
    private final FontIcon searchIcon;
    // 清空按钮
    private final FontIcon clearBtn;
    // 下一个匹配按钮
    private final FontIcon nextBtn;
    // 工具按钮：复制和下载
    private final Button copyBtn;
    private final Button downloadBtn;
    // 当前所有匹配位置
    private final List<int[]> matchRanges = new ArrayList<>();
    // 当前匹配索引
    private int currentMatchIndex = -1;
    // 防抖定时器：用于搜索输入防抖
    private final PauseTransition debounceTimer;
    // 富文本区域，用于展示和高亮文本
    private StyleClassedTextArea targetTextArea;

    public SearchToolbarPane(StyleClassedTextArea targetTextArea) {
        // 初始化所有 UI 控件

        this.targetTextArea = targetTextArea;

        searchTextField = new TextField();
        searchIcon = new FontIcon(Material2MZ.SEARCH);
        clearBtn = new FontIcon(Material2OutlinedAL.CLEAR);
        nextBtn = new FontIcon(Material2AL.ARROW_DOWNWARD);

        copyBtn = new Button();
        copyBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.COPY));

        downloadBtn = new Button();
        downloadBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.DOWNLOAD));

        debounceTimer = new PauseTransition(Duration.millis(150));

        initLayout();
        initBehavior();
    }

    /**
     * 布局结构初始化
     */
    private void initLayout() {
        // 将搜索框、搜索图标、清空图标组合在 StackPane 中
        StackPane searchFieldPane = new StackPane(searchTextField, searchIcon, clearBtn);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 5));
        StackPane.setAlignment(clearBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(clearBtn, new Insets(0, 5, 0, 0));
        searchTextField.setPadding(new Insets(3, 25, 3, 30)); // 给图标和按钮预留空间

        clearBtn.setCursor(Cursor.HAND);
        clearBtn.setVisible(false);// 默认不显示清空按钮
        nextBtn.setVisible(false);

        getChildren().addAll(searchFieldPane, nextBtn, copyBtn, downloadBtn);
        setAlignment(Pos.CENTER_RIGHT);
        setSpacing(5);
//        HBox.setHgrow(searchFieldPane, Priority.ALWAYS); // 搜索框自动填满剩余空间

        nextBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        copyBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        downloadBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
    }

    /**
     * 初始化组件交互逻辑
     */
    private void initBehavior() {
        searchTextField.setPromptText("Search");

        searchTextField.textProperty().addListener((obs, ov, nv) -> {
            clearBtn.setVisible(StrUtil.isNotBlank(nv));
        });

        clearBtn.setOnMouseClicked(e -> searchTextField.clear());

        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (matchRanges.isEmpty()) {
                    highlightSearch();
                } else {
                    currentMatchIndex = (currentMatchIndex + 1) % matchRanges.size();
                    moveCaretToCurrentMatch();
                }
            }
        });

        searchTextField.setOnKeyReleased(event -> {
            debounceTimer.setOnFinished(e -> highlightSearch());
            debounceTimer.playFromStart();
        });

        nextBtn.setOnMouseClicked(event -> {
            if (!matchRanges.isEmpty()) {
                currentMatchIndex = (currentMatchIndex + 1) % matchRanges.size();
                moveCaretToCurrentMatch();
            }
        });

        copyBtn.setOnAction(e -> {
            ClipboardUtils.copy(targetTextArea.getText());
            MessageUtils.success("已复制到剪切板");
        });

        downloadBtn.setOnAction(e -> {
            String content = targetTextArea.getText();
            if (StrUtil.isBlank(content)) return;
            JsonFileSaver.saveToJsonFile(StateStore.stage, content, "search-" + DateUtil.today());
        });


        // 创建右键菜单
        ContextMenu contextMenu = new ContextMenu();

        // 创建菜单项（带图标和快捷键）
        contextMenu.getItems().addAll(
                createMenuItem(SettingClient.bundle().getString("menu.item.undo"), new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), targetTextArea::undo),
                createMenuItem(SettingClient.bundle().getString("menu.item.redo"), new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN), targetTextArea::redo),
                new SeparatorMenuItem(),
                createMenuItem(SettingClient.bundle().getString("menu.item.cut"),new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), targetTextArea::cut),
                createMenuItem(SettingClient.bundle().getString("menu.item.copy"),new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN), targetTextArea::copy),
                createMenuItem(SettingClient.bundle().getString("menu.item.paste"),new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), targetTextArea::paste),
                new SeparatorMenuItem(),
                createMenuItem(SettingClient.bundle().getString("menu.item.selectAll"), new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN), targetTextArea::selectAll)
        );

        // 设置右键菜单
        targetTextArea.setContextMenu(contextMenu);

    }

    /**
     * 创建菜单项
     * @param text
     * @param shortcut
     * @param action
     * @return
     */
    private MenuItem createMenuItem(String text, KeyCombination shortcut, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setAccelerator(shortcut);
        item.setOnAction(e -> action.run());
        return item;
    }

    /**
     * 高亮搜索关键字并跳转到第一个匹配
     */
    private void highlightSearch() {
        if (targetTextArea == null) return;

        String query = searchTextField.getText().trim();
        String content = targetTextArea.getText();

        // 清除旧的样式
        targetTextArea.clearStyle(0, content.length());
        matchRanges.clear();
        currentMatchIndex = -1;
        nextBtn.setVisible(false);

        if (StrUtil.isBlank(query)) return;

        Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        // 匹配并记录所有出现的位置
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            targetTextArea.setStyleClass(start, end, "highlight");
            matchRanges.add(new int[]{start, end});
        }

        // 如果有结果，高亮第一个，并显示“下一个”按钮
        if (!matchRanges.isEmpty()) {
            currentMatchIndex = 0;
            moveCaretToCurrentMatch();
            nextBtn.setVisible(true);
        }
    }


    /**
     * 将光标跳转到当前匹配的位置，并选中高亮
     */
    private void moveCaretToCurrentMatch() {
        if (currentMatchIndex >= 0 && currentMatchIndex < matchRanges.size()) {
            int[] range = matchRanges.get(currentMatchIndex);
            targetTextArea.moveTo(range[0]);
            targetTextArea.selectRange(range[0], range[1]);
            targetTextArea.requestFollowCaret(); // 滚动到当前高亮
        }
    }

    public void setTargetTextArea(StyleClassedTextArea target) {
        this.targetTextArea = target;
    }
}
