package com.lxwise.elastic.gui;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.utils.JsonUtil;
import com.lxwise.elastic.utils.MessageUtils;
import com.lxwise.elastic.utils.RefreshScheduler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * @author lstar
 * @create 2025-02
 * @description: 集群健康/信息
 */
public class ClusterHealthMonitorController implements Initializable {

    private static Logger logger = LoggerFactory.getLogger(ClusterHealthMonitorController.class);

    @FXML
    public Button handleNodeRefreshBtn;
    @FXML
    public TableView<Map.Entry<String, String>> tableNodeView;
    @FXML
    public ComboBox<String>  refreshNodeIntervalComboBox;
    @FXML
    private Button handleRefreshBtn;
    @FXML
    private TableView<Map.Entry<String, String>> tableView;
    @FXML
    private ComboBox<String> refreshIntervalComboBox;

    //刷新集群信息任务
    private final RefreshScheduler refreshScheduler = new RefreshScheduler();
    //刷新节点信息任务
    private final RefreshScheduler refreshNodeScheduler = new RefreshScheduler();
    // 初始化为 -1，表示未启动
    private int clusterRefreshTaskId = -1;
    // 初始化为 -1，表示未启动
    private int nodeRefreshTaskId = -1;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //集群信息
        configureTableView();
        loadData();

        //节点信息
        configureNodeTableView();
        loadNodeData();

        initRefreshNode();
    }

    /**
     * 初始化刷新相关组件
     */
    private void initRefreshNode() {
        //刷新集群信息
        handleRefreshBtn.setGraphic(new FontIcon(Material2MZ.REFRESH));
        handleRefreshBtn.getStyleClass().addAll(Styles.FLAT);

        refreshIntervalComboBox.setItems(FXCollections.observableArrayList("None", "5", "10", "15", "30", "60"));
        refreshIntervalComboBox.setValue("None");
        refreshIntervalComboBox.getStyleClass().add("custom-combo-box");
        refreshIntervalComboBox.setOnAction(e -> updateRefreshInterval());

        //刷新节点信息
        handleNodeRefreshBtn.setGraphic(new FontIcon(Material2MZ.REFRESH));
        handleNodeRefreshBtn.getStyleClass().addAll(Styles.FLAT);

        refreshNodeIntervalComboBox.setItems(FXCollections.observableArrayList("None", "5", "10", "15", "30", "60"));
        refreshNodeIntervalComboBox.setValue("None");
        refreshNodeIntervalComboBox.getStyleClass().add("custom-combo-box");
        refreshNodeIntervalComboBox.setOnAction(e -> updateNodeRefreshInterval());
    }


    /**
     * 集群信息表格
     */
    private void configureTableView() {
        TableColumn<Map.Entry<String, String>, String> keyColumn = new TableColumn<>();
        keyColumn.setCellValueFactory(param -> javafx.beans.binding.Bindings.createStringBinding(() -> param.getValue().getKey()));

        TableColumn<Map.Entry<String, String>, String> valueColumn = new TableColumn<>();
        valueColumn.setCellValueFactory(param -> javafx.beans.binding.Bindings.createStringBinding(() -> param.getValue().getValue()));

        // 确保 keyColumn 贴近左边
        keyColumn.setMinWidth(0); // 允许缩小到 0
        keyColumn.setPrefWidth(300); // 设定一个合适的宽度
        keyColumn.setMaxWidth(Double.MAX_VALUE); // 允许扩展
        keyColumn.setStyle("-fx-alignment: CENTER-LEFT;"); // 左对齐

        // 确保 valueColumn 靠右
        valueColumn.setMinWidth(0);
        valueColumn.setPrefWidth(200);
        valueColumn.setMaxWidth(Double.MAX_VALUE);
        valueColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        // 自定义 CellFactory 以确保对齐
        keyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font(14));
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 5px;");
                }
            }
        });

        // 自定义 status 显示方式（加上小圆球）
        valueColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Map.Entry<String, String> rowData = (Map.Entry<String, String>) getTableRow().getItem();
                    String key = rowData.getKey();
                    String value = rowData.getValue();
                    setText(item);
                    setFont(Font.font(14));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 5px;");
                    if ("status".equals(key)) {
                        Label statusLabel = new Label(value);
                        statusLabel.setStyle("-fx-font-size: 14px;");

                        // 创建小圆球（Circle）
                        Circle circle = new Circle(5); // 半径 5px
                        if ("green".equalsIgnoreCase(value)) {
                            circle.setFill(javafx.scene.paint.Color.GREEN);
                        } else {
                            circle.setFill(javafx.scene.paint.Color.RED);
                        }

                        HBox hbox = new HBox(6, circle, statusLabel);
                        hbox.setAlignment(Pos.CENTER_RIGHT);
                        setGraphic(hbox);
                        setText(null);
                    } else {
                        setText(value);
                        setGraphic(null);
                    }
                }
            }
        });

        tableView.getColumns().addAll(keyColumn, valueColumn);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableView.setEditable(false);
        tableView.getStyleClass().addAll(Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE);

        // 隐藏表头/滚动条
        tableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                tableView.lookup("TableHeaderRow").setVisible(false);
                tableView.lookupAll(".scroll-bar").forEach(node -> {
                    node.setStyle("-fx-opacity: 0;"); // 让滚动条完全透明
                    node.setMouseTransparent(true);  // 禁用交互
                });
            }
        });

    }

    /**
     * 节点信息表格
     */
    private void configureNodeTableView() {
        TableColumn<Map.Entry<String, String>, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setCellValueFactory(param -> javafx.beans.binding.Bindings.createStringBinding(() -> param.getValue().getKey()));

        TableColumn<Map.Entry<String, String>, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> javafx.beans.binding.Bindings.createStringBinding(() -> param.getValue().getValue()));

        // 确保 keyColumn 贴近左边
        keyColumn.setMinWidth(0); // 允许缩小到 0
        keyColumn.setPrefWidth(400); // 设定一个合适的宽度
        keyColumn.setMaxWidth(Double.MAX_VALUE); // 允许扩展
        keyColumn.setStyle("-fx-alignment: CENTER-LEFT;"); // 左对齐

        // 确保 valueColumn 靠右
        valueColumn.setMinWidth(0);
        valueColumn.setPrefWidth(500);
        valueColumn.setMaxWidth(Double.MAX_VALUE);
        valueColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        // 自定义 CellFactory 以确保对齐
        keyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font(14));
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 5px;");
                }
            }
        });

        valueColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font(14));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 5px;");
                }
            }
        });

        tableNodeView.getColumns().addAll(keyColumn, valueColumn);

        // 设定固定的列宽策略
        tableNodeView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        tableNodeView.setEditable(false);
        tableNodeView.getStyleClass().addAll(Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE);


        // 隐藏表头/滚动条
        tableNodeView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                tableNodeView.lookup("TableHeaderRow").setVisible(false);
                tableNodeView.lookupAll(".scroll-bar").forEach(node -> {
                    node.setStyle("-fx-opacity: 0;"); // 让滚动条完全透明
                    node.setMouseTransparent(true);  // 禁用交互
                });
            }
        });
    }


    @FXML
    private void handleRefresh(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String id = sourceButton.getId();
        if (!"handleRefreshBtn".equals(id)) {
            loadNodeData();
        }else {
            loadData();
        }
        MessageUtils.success("刷新成功");
    }

    private void loadData() {
        Map<String, String> clusterData = getClusterHealthData();
        ObservableList<Map.Entry<String, String>> data = FXCollections.observableArrayList(clusterData.entrySet());
        tableView.setItems(data);
        tableView.refresh();
    }
    private void loadNodeData() {
        Map<String, String> clusterData = getClusterNodeHealthData();
        ObservableList<Map.Entry<String, String>> data = FXCollections.observableArrayList(clusterData.entrySet());
        tableNodeView.setItems(data);
        tableNodeView.refresh();
    }

    private Map<String, String> getClusterHealthData(){
        String health = ElasticManage.health(ElasticManage.get());
        return JsonUtil.flattenToMap(health);
    }
    private Map<String, String> getClusterNodeHealthData() {

        String health = ElasticManage.esInfo(ElasticManage.get());
        return JsonUtil.flattenToMap(health);
    }

    // 更新定时刷新间隔
    private void updateRefreshInterval() {
        String intervalStr = refreshIntervalComboBox.getValue();
        if ("None".equals(intervalStr)) {
            if (clusterRefreshTaskId != -1) { // 如果任务已启动，则停止
                refreshScheduler.stop(clusterRefreshTaskId);
                clusterRefreshTaskId = -1; // 重置任务 ID
            }
        } else {
            int interval = Integer.parseInt(intervalStr);
            if (clusterRefreshTaskId == -1) { // 如果任务未启动，则启动
                clusterRefreshTaskId = refreshScheduler.schedule(this::loadData, interval);
            } else { // 如果任务已启动，则更新间隔
                refreshScheduler.updateInterval(clusterRefreshTaskId, interval);
            }
        }
    }
    // 更新定时刷新间隔
    private void updateNodeRefreshInterval() {
        String intervalStr = refreshNodeIntervalComboBox.getValue();
        if ("None".equals(intervalStr)) {
            if (nodeRefreshTaskId != -1) { // 如果任务已启动，则停止
                refreshNodeScheduler.stop(nodeRefreshTaskId);
                nodeRefreshTaskId = -1; // 重置任务 ID
            }
        } else {
            int interval = Integer.parseInt(intervalStr);
            if (nodeRefreshTaskId == -1) { // 如果任务未启动，则启动
                nodeRefreshTaskId = refreshNodeScheduler.schedule(this::loadNodeData, interval);
            } else { // 如果任务已启动，则更新间隔
                refreshNodeScheduler.updateInterval(nodeRefreshTaskId, interval);
            }
        }
    }
}
