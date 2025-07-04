package com.lxwise.elastic.gui;

import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import com.lxwise.elastic.core.client.ConfigClient;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.entity.ConfigProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
 * @description: 集群配置表单
 */
public class ConfigFormController {

    private static Logger logger = LoggerFactory.getLogger(ConfigFormController.class);


    @FXML
    public Button cancelButton;
    @FXML
    public Button saveButton;
    @FXML
    public Tile nameTile;
    @FXML
    public Tile serverTile;
    @FXML
    public Tile securityTile;
    @FXML
    public Tile usernameTile;
    @FXML
    public Tile passwordTile;

    private ConfigController clusterController;
    private Stage parent;
    private boolean isAdd;
    private ConfigProperty clusterProperty;
    private TextField nameField;
    private TextField serverField;
    private CheckBox securityField;
    private TextField usernameField;
    private TextField passwordField;

    @FXML
    public void initialize() {

        initButton();
        initNameField();
        initServerField();
        initSecurityField();
        initUsernameField();
        initPasswordField();
        this.nameField.requestFocus();
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

    private void initPasswordField() {
        passwordTile.setTitle(SettingClient.bundle().getString("config.table.password"));
        passwordField = new PasswordField();
        passwordField.textProperty().addListener((e, o, n) -> {
            if (this.clusterProperty != null) {
                this.clusterProperty.setPassword(n);
            }
        });
        passwordTile.setAction(passwordField);
        passwordTile.setActionHandler(passwordField::requestFocus);
        passwordField.focusedProperty().addListener((e, o, n) -> {
            passwordTile.setDescription("");
        });
        passwordField.setPrefWidth(300);

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.hasText(newValue)) {
                passwordField.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }
        });
    }

    private void initUsernameField() {
        usernameTile.setTitle(SettingClient.bundle().getString("config.table.username"));
        usernameField = new TextField("");
        usernameField.textProperty().addListener((e, o, n) -> {
            if (this.clusterProperty != null) {
                this.clusterProperty.setUsername(n);
            }
        });
        usernameTile.setAction(usernameField);
        usernameTile.setActionHandler(usernameField::requestFocus);
        usernameField.focusedProperty().addListener((e, o, n) -> {
            usernameTile.setDescription("");
        });
        usernameField.setPrefWidth(300);

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.hasText(newValue)) {
                usernameField.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }
        });
    }


    private void initSecurityField() {
        securityTile.setTitle("Security");
        securityField = new CheckBox();
        securityField.setSelected(false);
        securityField.selectedProperty().addListener((e, o, n) -> {
            if (this.clusterProperty != null) {
                this.clusterProperty.setSecurity(n);
            }
            if(n){

                usernameTile.setVisible(true);
                passwordTile.setVisible(true);
            }else{
                usernameTile.setVisible(false);
                passwordTile.setVisible(false);

            }
        });
        securityTile.setAction(securityField);
        securityTile.setActionHandler(securityField::requestFocus);
        securityField.focusedProperty().addListener((e, o, n) -> {
            securityTile.setDescription("");
        });
        securityTile.getStyleClass().add("validate");
    }


    private void initServerField() {
        serverTile.setTitle(SettingClient.bundle().getString("config.table.servers"));
        serverField = new TextField("");
        serverField.textProperty().addListener((e, o, n) -> {
            if (this.clusterProperty != null) {
                this.clusterProperty.setServers(n);
            }
        });
        serverField.setPromptText("localhost:9200,localhost:9300");
        serverTile.setAction(serverField);
        serverTile.setActionHandler(serverField::requestFocus);
        serverField.focusedProperty().addListener((e, o, n) -> {
            serverTile.setDescription("");
        });
        serverField.setPrefWidth(300);
        serverTile.getStyleClass().add("validate");

        serverField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.hasText(newValue)) {
                serverField.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }
        });
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

    public void set(ConfigController clusterController, Stage parent, ConfigProperty property, boolean isAdd) {
        this.clusterProperty = property.copy();
        this.nameField.setText(property.getName());
        this.serverField.setText(property.getServers());
        this.securityField.setSelected(property.getSecurity());
        this.usernameField.setText(property.getUsername());
        this.passwordField.setText(property.getPassword());
        this.clusterController = clusterController;
        this.parent = parent;
        this.isAdd = isAdd;
    }

    @FXML
    public void onCancel(ActionEvent event) {
        this.parent.close();
    }

    @FXML
    public void onSave(ActionEvent event) {
        boolean success = true;
        if (!StringUtils.hasText(nameField.getText())) {
            nameTile.setDescription("[color=red]"+SettingClient.bundle().getString("config.form.name.required")+"[/color]");
            nameField.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            success = false;
        }
        if (!StringUtils.hasText(serverField.getText())) {
            serverTile.setDescription("[color=red]"+SettingClient.bundle().getString("config.form.servers.required")+"[/color]");
            serverField.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            success = false;
        }
        if(securityField.isSelected()){
            if (!StringUtils.hasText(usernameField.getText())) {
                usernameTile.setDescription("[color=red]"+SettingClient.bundle().getString("config.form.username.required")+"[/color]");
                usernameField.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                success = false;
            }
            if (!StringUtils.hasText(passwordField.getText())) {
                passwordTile.setDescription("[color=red]"+SettingClient.bundle().getString("config.form.password.required")+"[/color]");
                passwordField.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                success = false;
            }
        }
        if (!success) {
            return;
        }
        this.saveButton.setDisable(true);
        ConfigClient.save(this.clusterProperty);
        this.clusterController.success(this.clusterProperty,this.isAdd);
        this.parent.close();
        this.saveButton.setDisable(false);
    }


}
