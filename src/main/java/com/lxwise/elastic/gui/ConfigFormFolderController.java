package com.lxwise.elastic.gui;

import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import com.lxwise.elastic.core.client.ConfigClient;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.entity.ConfigProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author lstar
 * @create 2025-03
 * @description: 配置表单
 */
public class ConfigFormFolderController {

    private static Logger logger = LoggerFactory.getLogger(ConfigFormFolderController.class);


    @FXML
    public Button cancelButton;
    @FXML
    public Button saveButton;
    @FXML
    public Tile nameTile;
    private TextField nameField;

    private ConfigProperty clusterProperty;
    private ConfigController clusterController;
    private boolean isAdd;

    private Stage parent;

    @FXML
    public void initialize() {

        initButton();
        initNameField();
    }

    private void initNameField() {
        nameTile.setTitle(SettingClient.bundle().getString("config.table.name"));
        nameField = new TextField("");
        nameField.textProperty().addListener((e, o, n) -> {
            if (this.clusterProperty != null) {
                this.clusterProperty.setName(n);
            }
        });
        nameTile.setAction(nameField);
        nameTile.setActionHandler(nameField::requestFocus);
        nameField.focusedProperty().addListener((e, o, n) -> {
            nameTile.setDescription("");
        });
        nameField.setPrefWidth(300);
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.hasText(newValue)) {
                nameField.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }
        });
    }

    private void initButton() {
        cancelButton.setGraphic(new FontIcon(Material2AL.CANCEL));
        cancelButton.getStyleClass().addAll(
                Styles.BUTTON_OUTLINED, Styles.DANGER
        );
        saveButton.setGraphic(new FontIcon(Material2MZ.SAVE));
        saveButton.getStyleClass().addAll(
                Styles.BUTTON_OUTLINED, Styles.ACCENT
        );
    }

    public void set(ConfigController clusterController, Stage parent, ConfigProperty property, boolean isAdd) {
        this.clusterProperty = property.copy();
        this.nameField.setText(property.getName());
        this.clusterController = clusterController;
        this.parent=parent;
        this.isAdd=isAdd;
    }

    @FXML
    public void onCancel(ActionEvent event) {
        this.parent.close();
    }

    @FXML
    public void onSave(ActionEvent event) {
        boolean success = true;
        if (!StringUtils.hasText(nameField.getText())) {
            nameTile.setDescription("[color=red]"+ SettingClient.bundle().getString("config.table.name")+"[/color]");
            nameField.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            success = false;
        }
        if (!success) {
            return;
        }
        this.saveButton.setDisable(true);
        ConfigClient.save(this.clusterProperty);
        this.clusterController.success(this.clusterProperty,isAdd);
        this.parent.close();
        this.saveButton.setDisable(false);
    }
}
