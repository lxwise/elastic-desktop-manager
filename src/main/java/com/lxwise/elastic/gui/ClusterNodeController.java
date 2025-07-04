package com.lxwise.elastic.gui;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.event.LoadingEvent;
import com.lxwise.elastic.core.model.ESNodeInfoModel;
import com.lxwise.elastic.core.task.ESNodeInfoTask;
import com.lxwise.elastic.utils.AlertUtils;
import com.lxwise.elastic.utils.StageUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * @author lstar
 * @create 2025-03
 * @description: 集群节点信息
 */
public class ClusterNodeController {

    private static Logger logger = LoggerFactory.getLogger(ClusterNodeController.class);

    @FXML
    private TableView<ESNodeInfoModel> nodeTable;

    @FXML
    private Button handleNodeRefreshBtn;

    @FXML
    public BorderPane contentPane;

    private TableView<Map.Entry<String, String>> tableView = new TableView<>();

    @FXML
    public void initialize() {

        initTable();
        handleNodeRefreshBtn.setGraphic(new FontIcon(Material2MZ.REFRESH));
        handleNodeRefreshBtn.getStyleClass().addAll(Styles.FLAT);
        loadNodeData();

        initDetailTableStyle();
    }

    /**
     * 创建表格列
     */
    private void initTable() {
        ResourceBundle bundle = SettingClient.bundle();
        nodeTable.getColumns().addAll(
                createStringColumn(bundle.getString("cluster.node.table.name"), ESNodeInfoModel::getName),
                createStringColumn(bundle.getString("cluster.node.table.address"), ESNodeInfoModel::getHttpAddress),
                createStringColumn(bundle.getString("cluster.node.table.version"), ESNodeInfoModel::getVersion),
                createStringColumn(bundle.getString("cluster.node.table.master"), ESNodeInfoModel::getMaster),
                createStringColumn(bundle.getString("cluster.node.table.role"), ESNodeInfoModel::getRole),
                createStringColumn(bundle.getString("cluster.node.table.load"), n -> n.getLoad1m() + "/" + n.getLoad5m() + "/" + n.getLoad15m()),
                createStringColumn(bundle.getString("cluster.node.table.cpu"), n -> n.getCpu() + "%"),
                createUsageColumn(bundle.getString("cluster.node.table.memory"), ESNodeInfoModel::getRamCurrent, ESNodeInfoModel::getRamMax, ESNodeInfoModel::getRamPercent),
                createUsageColumn(bundle.getString("cluster.node.table.heapMemory"), ESNodeInfoModel::getHeapCurrent, ESNodeInfoModel::getHeapMax, ESNodeInfoModel::getHeapPercent),
                createUsageColumn(bundle.getString("cluster.node.table.disk"), ESNodeInfoModel::getDiskUsed, ESNodeInfoModel::getDiskTotal, ESNodeInfoModel::getDiskUsedPercent),
                createActionColumn()
        );

        nodeTable.setRowFactory(tv -> {
            TableRow<ESNodeInfoModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showDetailDialog(row.getItem());
                }
            });
            return row;
        });

        nodeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        nodeTable.setEditable(false);
        nodeTable.getStyleClass().addAll(Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE);
    }

    /**
     * 创建普通字符串列
     *
     * @param title
     * @param extractor
     * @return
     */
    private TableColumn<ESNodeInfoModel, String> createStringColumn(String title, Function<ESNodeInfoModel, String> extractor) {
        TableColumn<ESNodeInfoModel, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        return col;
    }

    /**
     * 创建带进度条的资源列
     *
     * @param title
     * @param currentFn
     * @param maxFn
     * @param percentFn
     * @return
     */
    private TableColumn<ESNodeInfoModel, ESNodeInfoModel> createUsageColumn(
            String title,
            Function<ESNodeInfoModel, String> currentFn,
            Function<ESNodeInfoModel, String> maxFn,
            Function<ESNodeInfoModel, String> percentFn
    ) {
        TableColumn<ESNodeInfoModel, ESNodeInfoModel> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        col.setCellFactory(column -> new TableCell<>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label label = new Label();
            private final Label labelRate = new Label();
            private final HBox infoBox = new HBox(5, progressBar, labelRate);
            private final VBox box = new VBox(5, label, infoBox);

            {
                progressBar.setPrefWidth(120);
            }

            @Override
            protected void updateItem(ESNodeInfoModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    double percent = parsePercent(percentFn.apply(item));
                    progressBar.setProgress(percent / 100.0);
                    label.setText(currentFn.apply(item) + " / " + maxFn.apply(item));
                    labelRate.setText(" (" + percentFn.apply(item) + "%)");
                    setGraphic(box);
                }
            }
        });
        return col;
    }


    /**
     * 创建操作列
     *
     * @return
     */
    private TableColumn<ESNodeInfoModel, Void> createActionColumn() {
        TableColumn<ESNodeInfoModel, Void> actionCol = new TableColumn<>(SettingClient.bundle().getString("cluster.node.table.action"));
        actionCol.setPrefWidth(50);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("", new FontIcon(Material2OutlinedAL.DESCRIPTION));

            {
                btn.setOnAction(e -> {
                    ESNodeInfoModel node = getTableView().getItems().get(getIndex());
                    showDetailDialog(node);
                });
                btn.getStyleClass().addAll(Styles.FLAT);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        return actionCol;
    }


    /**
     * 百分比解析
     *
     * @param percentStr
     * @return
     */
    private double parsePercent(String percentStr) {
        try {
            return Double.parseDouble(percentStr.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 显示详情对话框
     * @param node
     */
    public void showDetailDialog(ESNodeInfoModel node) {
        if (node == null || node.getRawData() == null || node.getRawData().isEmpty()) {
            return;
        }
        
        Map<String, String> clusterData = node.getRawData();
        ObservableList<Map.Entry<String, String>> data = FXCollections.observableArrayList(clusterData.entrySet());
        tableView.setItems(data);
        tableView.refresh();

        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefViewportHeight(800);
        scrollPane.setPrefViewportWidth(580);

        StageUtils.show(scrollPane,SettingClient.bundle().getString("cluster.node.table.detail"));
    }

    /**
     * 初始化详情表格样式
     */
    private void initDetailTableStyle() {
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

        valueColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Map.Entry<String, String> rowData = (Map.Entry<String, String>) getTableRow().getItem();
                    String value = rowData.getValue();
                    setText(item);
                    setFont(Font.font(14));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 5px;");
                    setText(value);
                }
            }
        });

        tableView.setPrefWidth(580);
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

    @FXML
    public void handleRefresh(ActionEvent event) {
        loadNodeData();
    }

    private void loadNodeData() {
        // 清空表格并设置加载中状态
        nodeTable.getItems().clear();

        ESNodeInfoTask task = new ESNodeInfoTask();
        new LoadingEvent(Boolean.TRUE, task).publish();

        task.setOnSucceeded(event -> {
            LoadingEvent.STOP.publish();
            nodeTable.setItems(task.getValue());
        });

        task.setOnFailed(e -> {
            logger.error("加载节点信息失败：{}", e.getSource().getException().getMessage());
            LoadingEvent.STOP.publish();
            Throwable translate = ElasticManage.translate(e.getSource().getException());
            AlertUtils.error(StateStore.stage, translate.getMessage());
        });

        new Thread(task).start();
    }

}
