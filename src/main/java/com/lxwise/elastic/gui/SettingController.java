package com.lxwise.elastic.gui;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Tile;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.Styles;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.entity.SettingProperty;
import com.lxwise.elastic.enums.Language;
import com.lxwise.elastic.enums.Themes;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.store.ThemeManager;
import com.podigua.path.Paths;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsFilled;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import java.io.File;

/**
 * @author lstar
 * @create 2024-12
 * @description: 设置控制器
 */
public class SettingController  {

    private static Logger logger = LoggerFactory.getLogger(SettingController.class);


    private final SettingProperty settingProperty = SettingClient.get();
    @FXML
    public AnchorPane center;
    private final static double WIDTH = 400;
    private static final ThemeManager TM = ThemeManager.getInstance();

    private static final String DEFAULT_FONT_ID = "Default";

    @FXML
    public void initialize() {
        VBox box = new VBox();
        box.setSpacing(15);
        Tile language = language();
        Tile theme = theme();
        Tile timeout = timeout();
        Tile folder = folder();
        Tile fontFamily = fontFamily();
        Tile fontSize = fontSize();
        Tile autoUpdate = autoUpdate();
        Tile exitAsk = exitAsk();
        box.getChildren().addAll(language, theme, timeout, folder,fontFamily,fontSize, autoUpdate, exitAsk);
        this.center.getChildren().add(box);

        AnchorPane.setTopAnchor(box, 0.0);
        AnchorPane.setRightAnchor(box, 0.0);
        AnchorPane.setBottomAnchor(box, 0.0);
        AnchorPane.setLeftAnchor(box, 0.0);
    }

    private Tile exitAsk() {
        Tile tile = new Tile(
                SettingClient.bundle().getString("config.table.closeAsk"),
                ""
        );
        ToggleSwitch toggle = new ToggleSwitch();
        toggle.setSelected(settingProperty.getCloseRemember());
        toggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingProperty.closeRememberProperty().set(newValue);
        });
        tile.setAction(toggle);
        tile.setActionHandler(toggle::requestFocus);
        return tile;
    }

    private Tile autoUpdate() {
        Tile tile = new Tile(
                SettingClient.bundle().getString("config.table.autoUpdate"),
                ""
        );
        ToggleSwitch toggle = new ToggleSwitch();
        toggle.setSelected(settingProperty.getAutoUpdater());
        toggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingProperty.autoUpdater().set(newValue);
        });
        tile.setAction(toggle);
        tile.setActionHandler(toggle::requestFocus);
        return tile;
    }

    private Tile fontSize() {
        Tile tile = new Tile(SettingClient.bundle().getString("setting.form.fontSize"),  ""
        );
        Spinner<Integer> fontSizeSpinner = createFontSizeSpinner();
        tile.setAction(fontSizeSpinner);
        tile.setActionHandler(fontSizeSpinner::requestFocus);
        return tile;
    }

    private Spinner<Integer> createFontSizeSpinner() {
        var spinner = new Spinner<Integer>(
                ThemeManager.SUPPORTED_FONT_SIZE.get(0),
                ThemeManager.SUPPORTED_FONT_SIZE.get(ThemeManager.SUPPORTED_FONT_SIZE.size() - 1),
                TM.getFontSize()
        );
        spinner.setPrefWidth(100);

        // Instead of this we should obtain font size from a rendered node.
        // But since it's not trivial (thanks to JavaFX doesn't expose relevant API)
        // we just keep current font size inside ThemeManager singleton.
        // It works fine if ThemeManager default font size value matches
        // default theme font size value.
        spinner.getValueFactory().setValue(TM.getFontSize());

        spinner.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                TM.setFontSize(val);
                settingProperty.setFontSize(val);
            }
        });

        return spinner;
    }

    private Tile fontFamily() {
        Tile tile = new Tile(SettingClient.bundle().getString("setting.form.font"),  ""
        );
        ComboBox<String> comboBox = createFontFamilyChooser();
        tile.setAction(comboBox);
        tile.setActionHandler(comboBox::requestFocus);
        return tile;
    }
    private ComboBox<String> createFontFamilyChooser() {
        var comboBox = new ComboBox<String>();
        comboBox.setPrefWidth(200);

        // keyword to reset font family to its default value
        comboBox.getItems().add(DEFAULT_FONT_ID);
        comboBox.getItems().addAll(FXCollections.observableArrayList(Font.getFamilies()));

        // select active font family value on page load
        comboBox.getSelectionModel().select(TM.getFontFamily());
        comboBox.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                String fontFamily = DEFAULT_FONT_ID.equals(val) ? ThemeManager.DEFAULT_FONT_FAMILY_NAME : val;
                TM.setFontFamily(fontFamily);
                settingProperty.setFontFamily(fontFamily);
            }
        });

        return comboBox;
    }

    private Tile folder() {
        Tile tile = new Tile(
                SettingClient.bundle().getString("setting.form.folder"), ""
        );
        Button select = new Button(
                "", new FontIcon(AntDesignIconsOutlined.FOLDER)
        );
        select.setCursor(Cursor.DEFAULT);
        CustomTextField folder = new CustomTextField();
        FontIcon clear = new FontIcon(AntDesignIconsFilled.CLOSE_CIRCLE);
        folder.setRight(clear);
        folder.setEditable(false);
        folder.setText(settingProperty.getDownloadFolder());

        select.getStyleClass().addAll(Styles.FONT_ICON);
        clear.setCursor(Cursor.DEFAULT);
        clear.getStyleClass().add(Styles.DANGER);
        clear.addEventHandler(MouseEvent.MOUSE_PRESSED, event->{
            folder.setText("");
            settingProperty.setDownloadFolder("");
        });
        select.setOnAction(event -> {
                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle(SettingClient.bundle().getString("setting.chooser.select"));
                    File directory = new File(Paths.downloads());
                    if (StringUtils.hasText(folder.getText())) {
                        File current = new File(folder.getText());
                        if (current.exists()) {
                            directory = current;
                        }
                    }
                    chooser.setInitialDirectory(directory);
                    File file = chooser.showDialog(StateStore.stage());
                    if (file != null) {
                        folder.setText(file.getAbsolutePath());
                        settingProperty.setDownloadFolder(file.getAbsolutePath());
                    }
                }

        );
        InputGroup group = new InputGroup(select,folder);
        group.setPrefWidth(WIDTH);
        folder.setPrefWidth(group.getPrefWidth() - select.getWidth());
        tile.setAction(group);
        tile.setActionHandler(group::requestFocus);
        return tile;
    }

    private Tile timeout() {
        Tile tile = new Tile(
                SettingClient.bundle().getString("setting.form.timeout"), ""
        );
        ComboBox<Integer> comboBox = new ComboBox<>(new SimpleListProperty<>(FXCollections.observableArrayList(5, 10, 15, 30, 60)));
        comboBox.setValue(settingProperty.getTimeout());
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            settingProperty.timeout().set(newValue);
        });
        comboBox.setPrefWidth(WIDTH);
        tile.setAction(comboBox);
        tile.setActionHandler(comboBox::requestFocus);
        return tile;
    }

    private Tile theme() {
        Tile tile = new Tile(
                SettingClient.bundle().getString("setting.form.theme"), ""
        );
        InputGroup group = new InputGroup();
        CheckBox check = new CheckBox();
        Label label = new Label("", check);
        label.setTooltip(new Tooltip(SettingClient.bundle().getString("setting.form.theme.auto")));
        check.setSelected(settingProperty.getAutoTheme());
        check.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingProperty.setAutoTheme(newValue);
        });
        ComboBox<Themes> comboBox = new ComboBox<>(new SimpleListProperty<>(SettingClient.THEMES));
        comboBox.setValue(settingProperty.getTheme());
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            settingProperty.theme().set(newValue);
        });
        comboBox.setDisable(check.isSelected());
        check.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                comboBox.setDisable(true);
            } else {
                comboBox.setDisable(false);
            }
        });
        group.setPrefWidth(WIDTH);
        comboBox.setPrefWidth(group.getPrefWidth() - label.getWidth());
        group.getChildren().addAll(label, comboBox);
        tile.setAction(group);
        tile.setActionHandler(group::requestFocus);
        return tile;
    }

    private Tile language() {
        Tile tile = new Tile(
                SettingClient.bundle().getString("setting.form.language"),
                SettingClient.bundle().getString("setting.form.language.prompt")
        );
        ComboBox<Language> comboBox = new ComboBox<>(new SimpleListProperty<>(SettingClient.LANGUAGES));
        comboBox.setValue(settingProperty.getLanguage());
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            settingProperty.language().set(newValue);
        });
        comboBox.setPrefWidth(WIDTH);
        tile.setAction(comboBox);
        tile.setActionHandler(comboBox::requestFocus);
        return tile;
    }
}
