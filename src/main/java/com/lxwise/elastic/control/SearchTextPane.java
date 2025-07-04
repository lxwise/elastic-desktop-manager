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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lstar
 * @create 2025-02
 * @description: 可复用的自带富文本区域版本,包含搜索文本面板，包含一个带高亮搜索、下一个匹配、复制和下载功能的文本区域
 */
public class SearchTextPane extends VBox {

    // 顶部搜索输入区域
    private final HBox searchInputGroup;

    // 富文本区域，用于展示和高亮文本
    private final StyleClassedTextArea outcomeTextArea;

    // 提供滚动条的包装器
    private final VirtualizedScrollPane<StyleClassedTextArea> scrollPane;

    // 搜索输入框和其附加按钮图标
    private final TextField searchTextField;
    private final FontIcon searchIcon; // 搜索图标
    private final FontIcon clearBtn;   // 清空按钮
    private final FontIcon nextBtn;    // 下一个匹配按钮

    // 工具按钮：复制和下载
    private final Button copyBtn;
    private final Button downloadBtn;

    // 防抖定时器：用于搜索输入防抖
    private final PauseTransition debounceTimer;

    // 外部按键处理器（可注入）
    private Consumer<KeyEvent> externalKeyHandler;

    // 当前所有匹配位置
    private final List<int[]> matchRanges = new ArrayList<>();
    // 当前匹配索引
    private int currentMatchIndex = -1;

    public SearchTextPane() {
        // 初始化所有 UI 控件
        searchInputGroup = new HBox();
        outcomeTextArea = new StyleClassedTextArea();
        scrollPane = new VirtualizedScrollPane<>(outcomeTextArea);

        searchTextField = new TextField();
        searchIcon = new FontIcon(Material2MZ.SEARCH);
        clearBtn = new FontIcon(Material2OutlinedAL.CLEAR);
        nextBtn = new FontIcon(Material2AL.ARROW_DOWNWARD); // "下一个"箭头图标

        copyBtn = new Button();
        copyBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.COPY));
        downloadBtn = new Button();
        downloadBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.DOWNLOAD));

        debounceTimer = new PauseTransition(Duration.millis(150));

        initializeLayout();     // 初始化布局结构
        initializeComponents(); // 初始化行为逻辑
    }

    /**
     * 布局结构初始化
     */
    private void initializeLayout() {
        setSpacing(10); // 垂直间距
        setPrefSize(800, 800); // 默认面板大小

        // 将搜索框、搜索图标、清空图标组合在 StackPane 中
        StackPane searchFieldPane = new StackPane(searchTextField, searchIcon, clearBtn);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 5));
        StackPane.setAlignment(clearBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(clearBtn, new Insets(0, 5, 0, 0));

        searchTextField.setPadding(new Insets(3, 25, 3, 30)); // 给图标和按钮预留空间
        clearBtn.setCursor(Cursor.HAND);
        clearBtn.setVisible(false); // 默认不显示清空按钮

        searchInputGroup.setAlignment(Pos.CENTER_RIGHT);
        searchInputGroup.setSpacing(5);
        searchInputGroup.getChildren().addAll(searchFieldPane, nextBtn, copyBtn, downloadBtn);
        HBox.setHgrow(searchFieldPane, Priority.ALWAYS); // 搜索框自动填满剩余空间

        VBox.setVgrow(scrollPane, Priority.ALWAYS); // 文本区垂直扩展填满
        getChildren().addAll(searchInputGroup, scrollPane);
    }

    /**
     * 初始化组件交互逻辑
     */
    private void initializeComponents() {
        // 设置搜索框提示文字
        searchTextField.setPromptText("Search");

        // 设置 next 按钮不可见（初始无结果）
        nextBtn.setVisible(false);
        nextBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);

        // 清除按钮逻辑
        clearBtn.setOnMouseClicked(e -> searchTextField.clear());

        // 输入监听：输入非空时显示清除按钮
        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearBtn.setVisible(StrUtil.isNotBlank(newVal));
        });

        // 键盘事件：按下回车执行搜索或跳转下一个
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (matchRanges.isEmpty()) {
                    highlightSearchResults(event); // 第一次回车，触发搜索
                } else {
                    // 多次回车，跳转到下一个匹配
                    currentMatchIndex = (currentMatchIndex + 1) % matchRanges.size();
                    moveCaretToCurrentMatch();
                }
            }
        });

        // 输入防抖处理
        searchTextField.setOnKeyReleased(event -> {
            debounceTimer.setOnFinished(e -> {
                if (externalKeyHandler != null) {
                    externalKeyHandler.accept(event);
                } else {
                    highlightSearchResults(event);
                }
            });
            debounceTimer.playFromStart();
        });

        // 点击 next 按钮：跳到下一个匹配位置
        nextBtn.setOnMouseClicked(event -> {
            if (!matchRanges.isEmpty()) {
                currentMatchIndex = (currentMatchIndex + 1) % matchRanges.size();
                moveCaretToCurrentMatch();
            }
        });

        // 添加示例文本
        outcomeTextArea.append("Hello, ", "");
        outcomeTextArea.append("RichTextFX", "highlight");
        outcomeTextArea.append(" example!\n", "");

        // 富文本样式类
        outcomeTextArea.getStyleClass().addAll("style-classed-text-area", "styled-text-area");

        // 复制按钮逻辑
        copyBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        copyBtn.setOnAction(e -> {
            ClipboardUtils.copy(outcomeTextArea.getText());
            MessageUtils.success(SettingClient.bundle().getString("action.alert.copy.success"));
        });

        // 下载按钮逻辑
        downloadBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        downloadBtn.setOnAction(e -> {
            if (StrUtil.isBlank(outcomeTextArea.getText())) return;
            JsonFileSaver.saveToJsonFile(StateStore.stage, outcomeTextArea.getText(), "search-" + DateUtil.today());
        });

        // 创建右键菜单
        ContextMenu contextMenu = new ContextMenu();

        // 创建菜单项（带图标和快捷键）
        contextMenu.getItems().addAll(
                createMenuItem(SettingClient.bundle().getString("menu.item.undo"), new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), outcomeTextArea::undo),
                createMenuItem(SettingClient.bundle().getString("menu.item.redo"), new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN), outcomeTextArea::redo),
                new SeparatorMenuItem(),
                createMenuItem(SettingClient.bundle().getString("menu.item.cut"),new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), outcomeTextArea::cut),
                createMenuItem(SettingClient.bundle().getString("menu.item.copy"),new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN), outcomeTextArea::copy),
                createMenuItem(SettingClient.bundle().getString("menu.item.paste"),new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), outcomeTextArea::paste),
                new SeparatorMenuItem(),
                createMenuItem(SettingClient.bundle().getString("menu.item.selectAll"), new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN), outcomeTextArea::selectAll)
        );

        // 设置右键菜单
        outcomeTextArea.setContextMenu(contextMenu);

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
    private void highlightSearchResults(KeyEvent event) {
        String query = searchTextField.getText().trim();
        String content = outcomeTextArea.getText();

        // 清除旧的样式
        outcomeTextArea.clearStyle(0, content.length());
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
            outcomeTextArea.setStyleClass(start, end, "highlight");
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
            outcomeTextArea.moveTo(range[0]);
            outcomeTextArea.selectRange(range[0], range[1]);
            outcomeTextArea.requestFollowCaret(); // 滚动到当前高亮
        }
    }

    // 外部设置文本内容
    public void setOutcomeTextArea(String text) {
        outcomeTextArea.clear();
        outcomeTextArea.appendText(text);
    }

    // 获取当前文本内容
    public String getOutcomeTextArea() {
        return outcomeTextArea.getText();
    }

    // 可设置外部搜索事件处理器
    public void setOnSearchKeyReleased(Consumer<KeyEvent> handler) {
        this.externalKeyHandler = handler;
    }
}
