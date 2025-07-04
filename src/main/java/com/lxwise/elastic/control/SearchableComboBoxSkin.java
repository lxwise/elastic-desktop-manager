package com.lxwise.elastic.control;

import atlantafx.base.controls.CustomTextField;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * @author lstar
 * @create 2025-02
 * @description: 可搜索ComboBox组件皮肤,基于controlsfx 的SearchableComboBoxSkin重写,
 * 去处理了原版的左右两侧图标
 */

public class SearchableComboBoxSkin<T> extends SkinBase<ComboBox<T>> {


    /**
     * 在内部用作代理的 “normal” 组合框，用于获取默认的组合框行为。
     * 此组合框包含筛选的项目并处理弹出窗口。
     */
    private final ComboBox<T> filteredComboBox;

    /**
     * 显示弹出窗口时显示的搜索字段。
     */
    private final CustomTextField searchField;

    /**
     * 按 ESC 时使用
     */
    private T previousValue;

    public SearchableComboBoxSkin(ComboBox<T> comboBox) {
        super(comboBox);

        // 首先创建筛选的组合框
        filteredComboBox = createFilteredComboBox();
        getChildren().add(filteredComboBox);

        // 搜索字段
        searchField = createSearchField();
        getChildren().add(searchField);

        bindSearchFieldAndFilteredComboBox();
        preventDefaultComboBoxKeyListener();

        // 在 Cursor Down 和 up 上打开弹出窗口
        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, this::checkOpenPopup);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        // 确保 filteredComboBox 和 searchField 的大小与字段的大小相同
        filteredComboBox.resizeRelocate(x, y, w, h);
        searchField.resizeRelocate(x, y, w, h);
    }

    private CustomTextField createSearchField() {
        var field = new atlantafx.base.controls.CustomTextField();
        field.setPromptText("搜索...");
        field.setId("search");
        field.getStyleClass().add("combo-box-search");

        // 不设置左右节点
        field.setLeft(null);
        field.setRight(null);

        // 若焦点时右边样式异常，可手动设置 padding
        field.setPadding(new Insets(5, 7, 5, 7));

        return field;
    }

    private ComboBox<T> createFilteredComboBox() {
        ComboBox<T> box = new ComboBox<>();
        box.setId("filtered");
        box.getStyleClass().add("combo-box-filtered");
        box.setFocusTraversable(false);

        // 单向绑定 -- 从 skinnable 复制值
        Bindings.bindContent(box.getStyleClass(), getSkinnable().getStyleClass());
        box.buttonCellProperty().bind(getSkinnable().buttonCellProperty());
        box.cellFactoryProperty().bind(getSkinnable().cellFactoryProperty());
        box.converterProperty().bind(getSkinnable().converterProperty());
        box.placeholderProperty().bind(getSkinnable().placeholderProperty());
        box.disableProperty().bind(getSkinnable().disableProperty());
        box.visibleRowCountProperty().bind(getSkinnable().visibleRowCountProperty());
        box.promptTextProperty().bind(getSkinnable().promptTextProperty());
        getSkinnable().showingProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal)
                box.show();
            else
                box.hide();
        });

        // 双向绑定
        box.valueProperty().bindBidirectional(getSkinnable().valueProperty());

        return box;
    }

    private void bindSearchFieldAndFilteredComboBox() {
        // 设置筛选组合框的项目
        filteredComboBox.setItems(createFilteredList());
        // 使其保持最新状态，即使原始列表发生更改
        getSkinnable().itemsProperty()
                .addListener((obs, oldVal, newVal) -> filteredComboBox.setItems(createFilteredList()));
        // 当搜索字段中的文本发生更改时，更新筛选器
        searchField.textProperty().addListener(o -> updateFilter());

        // 搜索字段必须仅在弹出窗口显示时可见
        searchField.visibleProperty().bind(filteredComboBox.showingProperty());

        filteredComboBox.showingProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal) {
                // 当 filtered 组合框弹出窗口显示时，我们还必须设置 showing 属性
                // 的原始组合框。在这里，我们必须记住
                // ESCAPE 行为。我们必须将焦点转移到搜索字段
                // 否则，搜索字段将不允许键入搜索文本。
                getSkinnable().show();
                previousValue = getSkinnable().getValue();
                searchField.requestFocus();
            } else {
                // 当 filtered 组合框弹出窗口被隐藏时，我们还必须设置 showing 属性
                // 设置为 false，请清除搜索字段。
                getSkinnable().hide();
                searchField.setText("");
            }
        });

        // 但是，当 Search 字段获得焦点时，弹出窗口仍必须显示
        searchField.focusedProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal)
                filteredComboBox.show();
            else
                filteredComboBox.hide();
        });
    }

    private FilteredList<T> createFilteredList() {
        return new FilteredList<T>(getSkinnable().getItems(), predicate());
    }

    /**
     * 每次筛选器文本更改时调用。
     */
    private void updateFilter() {

        filteredComboBox.setItems(createFilteredList());
    }

    /**
     * 返回 Predicate 以根据搜索字段筛选弹出项。
     */
    private Predicate<T> predicate() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            return null;
        }

        return predicate(searchText);
    }

    /**
     * 返回 Predicate 以根据给定的搜索文本筛选弹出项。
     */
    private Predicate<T> predicate(String searchText) {
        // 如果显示文本包含所有单词，则忽略大小写
        String[] lowerCaseSearchWords = searchText.toLowerCase().split(" ");
        return value ->
        {
            String lowerCaseDisplayText = getDisplayText(value).toLowerCase();
            return Arrays.stream(lowerCaseSearchWords).allMatch(word -> lowerCaseDisplayText.contains(word));
        };
    }

    /**
     * 为给定项目创建一个文本，该文本可用于与过滤器文本进行比较。
     */
    private String getDisplayText(T value) {
        StringConverter<T> converter = filteredComboBox.getConverter();
        return value == null ? "" : (converter != null ? converter.toString(value) : value.toString());
    }

    /**
     * ComboBoxListViewSkin 的默认行为是关闭
     * ENTER 和 SPACE，但我们需要覆盖此行为。
     */
    private void preventDefaultComboBoxKeyListener() {
        filteredComboBox.skinProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal instanceof ComboBoxListViewSkin) {
                ComboBoxListViewSkin cblwSkin = (ComboBoxListViewSkin)newVal;
                if(cblwSkin.getPopupContent() instanceof ListView) {
                    final ListView<T> listView = (ListView<T>) cblwSkin.getPopupContent();
                    if (listView != null) {
                        listView.setOnKeyPressed(this::checkApplyAndCancel);
                    }
                }
            }
        });
    }

    /**
     * 用于更改行为。在 Enter、Tab 和 ESC 上做出反应。
     */
    private void checkApplyAndCancel(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.ENTER || code == KeyCode.TAB) {
            // 如果未选择，则选择第一项
            if (filteredComboBox.getSelectionModel().isEmpty())
                filteredComboBox.getSelectionModel().selectFirst();
            getSkinnable().hide();
            if (code == KeyCode.ENTER) {
                // 否则，焦点将位于其他位置
                getSkinnable().requestFocus();
            }
        } else if (code == KeyCode.ESCAPE) {
            getSkinnable().setValue(previousValue);
            getSkinnable().hide();
            // 否则，焦点将位于其他位置
            getSkinnable().requestFocus();
        }
    }

    /**
     * 在 UP、DOWN 和开始键入单词时显示弹出窗口。
     */
    private void checkOpenPopup(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.UP || code == KeyCode.DOWN) {
            filteredComboBox.show();
            // 仅打开方框导航
            e.consume();
        } else if (code.isLetterKey() || code.isDigitKey() || code == KeyCode.SPACE) {
            // 显示 Box，让 Box 处理 KeyEvent
            filteredComboBox.show();
        }
    }

}
