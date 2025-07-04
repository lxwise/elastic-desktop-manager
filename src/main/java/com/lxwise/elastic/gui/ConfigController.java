package com.lxwise.elastic.gui;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.lxwise.elastic.control.FilterableTreeItem;
import com.lxwise.elastic.control.LoadingPane;
import com.lxwise.elastic.core.client.ConfigClient;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.task.EsConnectTask;
import com.lxwise.elastic.entity.ConfigProperty;
import com.lxwise.elastic.utils.AlertUtils;
import com.lxwise.elastic.utils.MessageUtils;
import com.lxwise.elastic.utils.Resources;
import com.lxwise.elastic.utils.StageUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import com.lxwise.elastic.core.es.ElasticManage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author lstar
 * @create 2025-03
 * @description: 集群配置
 */
public class ConfigController {

    private final static Logger logger = LoggerFactory.getLogger(ConfigController.class);
    @FXML
    public Button addButton;
    @FXML
    public Button addFolderButton;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    @FXML
    public TreeTableView<ConfigProperty> tableView;
    @FXML
    public CheckBox openDialog;
    @FXML
    public Button connectButton;
    @FXML
    public CustomTextField filter;

    @FXML
    public Button testConnectButton;

    private FilterableTreeItem<ConfigProperty> root;

    private Stage parentStage;
    private HomeController homeController;

    @FXML
    public void initialize() {
        initButton();
        initFormFilter();
        initTable();
        this.openDialog.setSelected(SettingClient.get().getOpenDialog());
        SettingClient.get().openDialog().bind(this.openDialog.selectedProperty());
    }

    private void initButton() {
        addButton.setGraphic(new FontIcon(Material2AL.ADD));
        addButton.getStyleClass().addAll(Styles.FLAT, Styles.ACCENT);
        addFolderButton.setGraphic(new FontIcon(Material2AL.CREATE_NEW_FOLDER));
        addFolderButton.getStyleClass().addAll(Styles.FLAT, Styles.ACCENT);
        editButton.setGraphic(new FontIcon(Material2AL.EDIT));
        editButton.getStyleClass().addAll(Styles.FLAT, Styles.ACCENT);
        deleteButton.setGraphic(new FontIcon(Material2AL.DELETE));
        deleteButton.getStyleClass().addAll(Styles.FLAT, Styles.DANGER);
        connectButton.setGraphic(new FontIcon(Material2AL.LINK));
        connectButton.getStyleClass().addAll(Styles.FLAT, Styles.ACCENT);
        testConnectButton.setGraphic(new FontIcon(AntDesignIconsOutlined.API));
        testConnectButton.getStyleClass().addAll(Styles.FLAT, Styles.DANGER);
    }

    private void initFormFilter() {
        FontIcon clear = new FontIcon(Material2OutlinedAL.CLEAR);
        clear.setCursor(Cursor.DEFAULT);
        clear.setOnMouseClicked(event -> {
            filter.setText("");
        });
        filter.setLeft(new FontIcon(Material2MZ.SEARCH));
        filter.setRight(clear);

        filter.textProperty().addListener((observable, oldValue, newValue) -> {
            root.predicateProperty().set(node -> {
                if (node == null || !StringUtils.hasText(newValue)) {
                    return true;
                }
                boolean nameMatch = false;
                if (StringUtils.hasText(node.getName()) && node.getName().toLowerCase().contains(newValue.toLowerCase())) {
                    nameMatch = true;
                }
                boolean serverMatch = false;
                if (StringUtils.hasText(node.getServers()) && node.getServers().toLowerCase().contains(newValue.toLowerCase())) {
                    serverMatch = true;
                }
                return nameMatch || serverMatch;
            });
        });
    }

    private void initTable() {
        this.root = new FilterableTreeItem<>(new ConfigProperty());
        this.tableView.setRoot(root);
        this.tableView.setShowRoot(false);
        tableView.getStyleClass().addAll(Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE);
        tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TreeTableColumn<ConfigProperty, String> name = new TreeTableColumn<>(SettingClient.bundle().getString("config.table.name"));
        name.setCellValueFactory(property -> property.getValue().getValue().name());
        TreeTableColumn<ConfigProperty, String> servers = new TreeTableColumn<>(SettingClient.bundle().getString("config.table.servers"));
        servers.setCellValueFactory(property -> property.getValue().getValue().servers());
        tableView.getColumns().addAll(name, servers);
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.isNull(newValue)) {
                editButton.setDisable(true);
                connectButton.setDisable(true);
                testConnectButton.setDisable(true);
                deleteButton.setDisable(true);
            } else {
                editButton.setDisable(false);
                deleteButton.setDisable(false);
                if (newValue.getValue() != null && "cluster".equals(newValue.getValue().getType())) {
                    connectButton.setDisable(false);
                    testConnectButton.setDisable(false);
                } else {
                    connectButton.setDisable(true);
                    testConnectButton.setDisable(true);
                }
            }
        });
        tableView.setOnMouseClicked(event -> {
            if (MouseButton.PRIMARY.equals(event.getButton()) && event.getClickCount() == 2) {
                getSelectValue().ifPresent(property -> {
                    if (!property._folder()) {
                        this.onConnect(null);
                    }
                });
            }
        });
        // 让 TreeTableView 失去焦点时清除选中状态
        tableView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getRoot().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                    if (!tableView.getBoundsInParent().contains(event.getSceneX(), event.getSceneY())) {
                        tableView.getSelectionModel().clearSelection();
                    }
                });
            }
        });

        reload();
    }

    private Optional<ConfigProperty> getSelectValue() {
        TreeItem<ConfigProperty> item = tableView.getSelectionModel().getSelectedItem();
        if (item != null) {
            return Optional.ofNullable(item.getValue());
        }
        return Optional.empty();
    }

    public void reload() {
        List<ConfigProperty> clusters = ConfigClient.query4List();
        this.root.getSourceChildren().clear();
        ObservableList<FilterableTreeItem<ConfigProperty>> list = buildTree(clusters, null);
        this.root.getSourceChildren().addAll(list);
        tableView.refresh();
    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    private ObservableList<FilterableTreeItem<ConfigProperty>> buildTree(List<ConfigProperty> clusters, String parent) {
        ObservableList<FilterableTreeItem<ConfigProperty>> result = FXCollections.observableArrayList();
        for (ConfigProperty cluster : clusters) {
            boolean add = false;
            if (StringUtils.hasText(parent) && parent.equals(cluster.getParentId())) {
                add = true;
            }
            if (!StringUtils.hasText(parent) && !StringUtils.hasText(cluster.getParentId())) {
                add = true;
            }
            if (add) {
                FilterableTreeItem<ConfigProperty> item = new FilterableTreeItem<>(cluster);
                item.setExpanded(true);
                ObservableList<FilterableTreeItem<ConfigProperty>> children = buildTree(clusters, cluster.getId());
                item.getSourceChildren().addAll(children);
                result.add(item);
            }
        }
        return result;
    }

    @FXML
    public void onAdd(ActionEvent event) {
        Optional<ConfigProperty> parent = getSelectValue();
        String parentId = "";
        if (parent.isPresent()) {
            parentId = parent.get().getId();
        }
        ConfigProperty property = new ConfigProperty();
        property.setParentId(parentId);
        property.cluster();
        openForm(property, true);
    }

    private void openForm(ConfigProperty property, boolean isAdd) {
        FXMLLoader loader = Resources.getLoader("/gui/es_config_form.fxml");
        ConfigFormController controller = loader.getController();
        String title = isAdd ? SettingClient.bundle().getString("config.form.new") : SettingClient.bundle().getString("config.form.edit");
        Stage formStage = StageUtils.show(loader.getRoot(), title, parentStage);
        controller.set(this, formStage, property, isAdd);
    }

    @FXML
    public void onAddFolder(ActionEvent event) {
        Optional<ConfigProperty> parent = getSelectValue();
        String parentId = "";
        if (parent.isPresent() && parent.get()._folder()) {
            parentId = parent.get().getId();
        }
        ConfigProperty property = new ConfigProperty();
        property.setParentId(parentId);
        property.folder();
        openFolderForm(property, true);
    }

    private void openFolderForm(ConfigProperty property, boolean isAdd) {
        FXMLLoader loader = Resources.getLoader("/gui/es_config_form_folder.fxml");
        ConfigFormFolderController controller = loader.getController();
        String title = isAdd ? SettingClient.bundle().getString("form.new") : SettingClient.bundle().getString("form.edit");
        Stage formStage = StageUtils.show(loader.getRoot(), title, parentStage);
        controller.set(this, formStage, property, isAdd);
    }
    @FXML
    public void onEdit(ActionEvent event) {
        getSelectValue().ifPresent(property -> {
            if (property._folder()) {
                openFolderForm(property, false);
            } else {
                openForm(property, false);
            }
        });
    }

    @FXML
    public void onDelete(ActionEvent event) {
        TreeItem<ConfigProperty> item = tableView.getSelectionModel().getSelectedItem();
        AlertUtils.confirm(SettingClient.bundle().getString("alert.delete.prompt")).ifPresent(type -> {
            getSelectValue().ifPresent(property -> {
                ConfigClient.deleteById(property.getId());
                Optional.ofNullable(item.getParent()).ifPresent(parent -> {
                    parent.getChildren().remove(item);
                });
                MessageUtils.success(SettingClient.bundle().getString("form.delete.success"));
                reload();
            });
        });
    }

    @FXML
    public void onConnect(ActionEvent event) {
        getSelectValue().ifPresent(property -> {
            if (ElasticManage.get(property.getId()) != null) {
                AlertUtils.error(parentStage, SettingClient.bundle().getString("alert.connect.tips"));
                return;
            }

            EsConnectTask task = new EsConnectTask(property);
            LoadingPane pane = new LoadingPane(e -> task.cancel());
            Stage stage = StageUtils.body(pane, parentStage);
            task.setOnSucceeded(e -> {
                logger.info("连接成功:{}", property.getServers());
                try {
                    ElasticManage.put(property.getId(), task.get());
                    MessageUtils.success(SettingClient.bundle().getString("alert.connect.success"));
                    homeController.setSelectorBtn(property.getName());
//                    new ClusterConnectEvent(property).publish();
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        throw new RuntimeException(ex);
                    });
                }
                stage.close();
                parentStage.close();
            });
            task.setOnFailed(e -> {
                logger.warn("连接失败:{}", property.getServers());
                stage.close();
                Throwable translate = ElasticManage.translate(e.getSource().getException());
                AlertUtils.error(parentStage, translate.getMessage());
            });
            task.setOnCancelled(e -> {
                logger.warn("取消连接:{}", property.getServers());
                stage.close();
            });
            new Thread(task).start();
        });
    }

    @FXML
    public void onTestConnect(ActionEvent event) {
        getSelectValue().ifPresent(property -> {
            EsConnectTask task = new EsConnectTask(property);
            LoadingPane pane = new LoadingPane(e -> task.cancel());
            Stage stage = StageUtils.body(pane, parentStage);
            task.setOnSucceeded(e -> {
                try {
                    MessageUtils.success(SettingClient.bundle().getString("alert.connect.success"));
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        throw new RuntimeException(ex);
                    });
                }
                stage.close();
            });
            task.setOnFailed(e -> {
                stage.close();
                Throwable translate = ElasticManage.translate(e.getSource().getException());
                AlertUtils.error(parentStage, translate.getMessage());
            });
            task.setOnCancelled(e -> {
                stage.close();
            });
            new Thread(task).start();
        });
    }

    public void success(ConfigProperty clusterProperty, boolean isAdd) {
        if (isAdd) {
            FilterableTreeItem<ConfigProperty> item = new FilterableTreeItem<>(clusterProperty);
            item.setExpanded(true);
            FilterableTreeItem<ConfigProperty> parent = (FilterableTreeItem<ConfigProperty>) tableView.getSelectionModel().getSelectedItem();
            if (parent != null && parent.getValue() != null) {
                parent.getSourceChildren().add(item);
            } else {
                this.root.getSourceChildren().add(item);
            }
        } else {
            TreeItem<ConfigProperty> item = tableView.getSelectionModel().getSelectedItem();
            if (item != null) {
                item.setValue(clusterProperty);
            }
        }
    }
}
