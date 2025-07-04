package com.lxwise.elastic.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;

/**
 * @author lstar
 * @create 2025-02
 * @description: 可搜索ComboBox组件,基于controlsfx 的SearchableComboBox重写
 *
 * 它 ComboBox 的简单扩展，在弹出窗口显示时显示搜索字段。用户可以在此搜索字段中键入任何文本以筛选弹出列表。
 * 用户可以键入多个单词。对弹出列表进行筛选，检查项目的字符串表示是否包含所有筛选词（忽略大小写）。
 * 筛选列表后，用户可以通过以下方式选择条目
 * 按 ENTER：应用所选项目，弹出窗口关闭。如果未选择任何项目，则应用第一个项目。要选择其他项目，可以在按 ENTER 之前使用光标键。
 * 按 Tab：与 ENTER 相同，但焦点将转移到下一个控件。
 * 使用鼠标选择项目将关闭弹出窗口。
 * 在弹出窗口显示时按 ESCAPE 键时，将重新选择弹出窗口打开时选择的项（即使用户确实使用光标键选择了其他项）。
 * 除 之外 ComboBox，SearchableComboBox 在使用光标键时会打开 Popup（ ComboBox 只会更改所选项目而不打开弹出窗口）。这与 ESCAPE 键的行为相结合，确实允许使用光标键浏览项目列表，然后按 ESCAPE 来恢复更改。
 * 例
 * 让我们看一个例子来澄清这一点。组合框提供项 [“Berlin”， “Bern”， “Munich”， “Paris”， “New York”， “Alberta”]。用户现在在搜索字段中键入 “ber”。组合框弹出窗口将仅显示 [“Berlin”， “Bern”， “Alberta”]。
 * 要选择第一项（“Berlin”），用户现在只需按 ENTER 或 TAB，或者先使用光标向下键选择此项，然后按 ENTER 或 TAB，或者使用鼠标选择此项。
 * 若要选择第二项或第三项，用户必须首先使用光标键、使用鼠标或键入更多文本，直到搜索的项是列表中的第一项（或唯一项）。
 * 如果要修改现有的 ComboBox 皮肤，可以将皮肤设置为 SearchableComboBoxSkin （例如，using ComboBox.setSkin(Skin) 或 in CSS.
 * 另请参见：
 * SearchableComboBoxSkin
 */
public class SearchableComboBox<T> extends ComboBox<T> {

    public SearchableComboBox() {
        this(FXCollections.observableArrayList());
    }

    public SearchableComboBox(ObservableList<T> items) {
        super(items);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SearchableComboBoxSkin<>(this);
    }

}