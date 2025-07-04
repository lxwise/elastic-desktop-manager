package com.lxwise.elastic.gui;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.control.PagingControl;
import com.lxwise.elastic.control.SearchTextField;
import com.lxwise.elastic.control.SearchTextPane;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.event.LoadingEvent;
import com.lxwise.elastic.core.event.NodeNoticeEvent;
import com.lxwise.elastic.core.model.ESIndicesModel;
import com.lxwise.elastic.core.model.view.ClusterIndicesView;
import com.lxwise.elastic.core.task.*;
import com.lxwise.elastic.enums.PayloadType;
import com.lxwise.elastic.enums.TabId;
import com.lxwise.elastic.utils.AlertUtils;
import com.lxwise.elastic.utils.ClipboardUtils;
import com.lxwise.elastic.utils.MessageUtils;
import com.lxwise.elastic.utils.StageUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lstar
 * @create 2025-03
 * @description: 索引管理
 */
public class ClusterIndexController {

    private static Logger logger = LoggerFactory.getLogger(ClusterIndexController.class);

    @FXML
    public Button handleRefreshBtn;

    @FXML
    public TableView<ESIndicesModel> tableView;

    @FXML
    public BorderPane rootPane;

    @FXML
    public HBox searchInputGroup;
    private PagingControl pagingControl;

    private ObservableList<ESIndicesModel> originalNodeData;
    private ObservableList<ESIndicesModel> filteredData;

    private ClusterIndicesView viewModel = new ClusterIndicesView();

    @FXML
    public void initialize() {

        SearchTextField searchText = new SearchTextField();
        searchText.setPromptText("Search");
        searchInputGroup.getChildren().add(0, searchText);
        searchText.setOnKeyReleased(this::filterData);

        initTable();

        pagingControl = new PagingControl();
        rootPane.setBottom(pagingControl);
        pagingControl.setPageItemList(Arrays.asList(20,30, 40,50,100));
        pagingControl.totalProperty().bind(viewModel.totalProperty());
        viewModel.pageNumProperty().bind(pagingControl.pageNumProperty());
        viewModel.pageSizeProperty().bind(pagingControl.pageSizeProperty());

        pagingControl.pageNumProperty().addListener((observable, oldValue, newValue) -> {
            updateTableData();
        });

        pagingControl.pageSizeProperty().addListener((observable, oldValue, newValue) -> {
            updateTableData();
        });

        // 异步加载数据
        loadNodeData();

        handleRefreshBtn.setGraphic(new FontIcon(Material2MZ.REFRESH));
        handleRefreshBtn.getStyleClass().addAll(Styles.FLAT);

    }

    /**
     * 创建表格列
     */
    private void initTable() {
        var indexCol = new TableColumn<ESIndicesModel, String>("ID");
        indexCol.setPrefWidth(20);
        indexCol.setCellFactory(col -> {
           var cell = new TableCell<ESIndicesModel, String>();
            StringBinding binding = Bindings.when(cell.emptyProperty())
                    .then("")
                    .otherwise(cell.indexProperty().add(1).asString());
            cell.textProperty().bind(binding);
            return cell;
        });
        ResourceBundle bundle = SettingClient.bundle();
        tableView.getColumns().addAll(
                indexCol,
                createStringColumn(bundle.getString("cluster.index.table.name"), ESIndicesModel::getIndex),
                createUsageColumn(bundle.getString("cluster.index.table.health"), ESIndicesModel::getHealth),
                createStringColumn(bundle.getString("cluster.index.table.status"),ESIndicesModel::getStatus),
                createStringColumn("UUID", ESIndicesModel::getUuid),
                createStringColumn(bundle.getString("cluster.index.table.shard"), n -> n.getPri() + "/" + n.getRep()),
                createStringColumn(bundle.getString("cluster.index.table.quantity"), ESIndicesModel::getDocsCount),
                createStringColumn(bundle.getString("cluster.index.table.storage"), ESIndicesModel::getStoreSize),
                createStringColumn(bundle.getString("cluster.index.table.memory"), ESIndicesModel::getMemoryTotal),
                createStringColumn(bundle.getString("cluster.index.table.time"), n -> DateUtil.date(Long.parseLong(n.getCreationDate())).toString("yyyy-MM-dd HH:mm:ss")),
                createActionColumn()
        );

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.setEditable(false);
        tableView.getStyleClass().addAll(Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE);
    }

    /**
     * 创建带进度条的资源列
     *
     * @param title
     * @param healthFn
     * @return
     */
    private TableColumn<ESIndicesModel, ESIndicesModel> createUsageColumn(
            String title,
            Function<ESIndicesModel, String> healthFn
    ) {
        TableColumn<ESIndicesModel, ESIndicesModel> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(ESIndicesModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    String health = healthFn.apply(item);
                    setFont(Font.font(14));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 5px;");

                    Label statusLabel = new Label(health);
                    statusLabel.setStyle("-fx-font-size: 14px;");
                    // 创建小圆球（Circle）
                    Circle circle = new Circle(5); // 半径 5px
                    switch (health) {
                        case "green" -> circle.setFill(Color.GREEN);
                        case "yellow" -> circle.setFill(Color.GOLD);
                        case "red" -> circle.setFill(Color.RED);
                        default -> circle.setFill(Color.GRAY);
                    }

                    HBox hbox = new HBox(6, circle, statusLabel);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);

                    // 添加Tooltip显示完整健康状态
                    Tooltip tooltip = new Tooltip("健康状态: " + health);
                    Tooltip.install(this, tooltip);
                }
            }
        });
        return col;
    }

    /**
     * 创建普通字符串列
     *
     * @param title
     * @param extractor
     * @return
     */
//    private TableColumn<ESIndicesModel, String> createStringColumn(String title, Function<ESIndicesModel, String> extractor) {
//        TableColumn<ESIndicesModel, String> col = new TableColumn<>(title);
//        col.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
//        return col;
//    }
    /**
     * 创建普通字符串列
     *
     * @param title
     * @param extractor
     * @return
     */
    private TableColumn<ESIndicesModel, String> createStringColumn(String title, Function<ESIndicesModel, String> extractor) {
        TableColumn<ESIndicesModel, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        col.setCellFactory(tc -> new TableCell<>() {
            private final TextField textField = new TextField();
            private final Tooltip tooltip = new Tooltip();
//            private final ContextMenu contextMenu = new ContextMenu();

            {
                textField.setEditable(false); // 禁止编辑
                textField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
                textField.setFocusTraversable(false);
                textField.setPrefHeight(USE_COMPUTED_SIZE);
                textField.setMouseTransparent(false); // 关键：允许鼠标选中
//                    textField.setOnMouseClicked(e -> textField.selectAll()); // 可选：点击全选

                tooltip.setWrapText(true);
                tooltip.setHideDelay(Duration.millis(2));

//                MenuItem copyItem = new MenuItem(SettingClient.bundle().getString("menu.item.copy"));
//                copyItem.setOnAction(e -> {
//                    ClipboardUtils.copy(textField.getText());
//                });
//                contextMenu.getItems().add(copyItem);
//
//                setContextMenu(contextMenu);
                setGraphic(textField);
                setStyle("-fx-padding: 3;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    textField.setText("");
                    setTooltip(null);
                } else {
                    textField.setText(item.length() > 100 ? item.substring(0, 100) + "..." : item);
                    tooltip.setText(item);
                    setTooltip(tooltip);
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
    private TableColumn<ESIndicesModel, Void> createActionColumn() {
        TableColumn<ESIndicesModel, Void> actionCol = new TableColumn<>(SettingClient.bundle().getString("cluster.index.table.action"));
        actionCol.setPrefWidth(40); // 增加宽度确保按钮完整显示
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final HBox hbox = new HBox(3);
            private Button searchBtn;
            private MenuButton setBtn;

            {
                hbox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                // 清除旧组件
                if (searchBtn != null) searchBtn.setOnAction(null);
                hbox.getChildren().clear();
                setGraphic(null);

                // 检查有效数据
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    return;
                }

                ESIndicesModel currentItem = getTableView().getItems().get(getIndex());
                if (currentItem == null) {
                    return;
                }

                // 创建新组件
                searchBtn = new Button("", new FontIcon(Material2MZ.SEARCH));
                searchBtn.setOnAction(e -> handleSearchIndex(currentItem));
                searchBtn.getStyleClass().addAll(Styles.FLAT);
                searchBtn.setTooltip(new Tooltip(SettingClient.bundle().getString("cluster.index.table.search")));

                setBtn = new MenuButton("", new FontIcon(Material2OutlinedMZ.SETTINGS));
                setBtn.getItems().setAll(createMenuItems(currentItem));
                setBtn.getStyleClass().addAll(Styles.FLAT, Tweaks.NO_ARROW);
                setBtn.setTooltip(new Tooltip(SettingClient.bundle().getString("cluster.index.table.moreAction")));

                hbox.getChildren().addAll(searchBtn, setBtn);
                setGraphic(hbox);
            }
        });
        return actionCol;
    }

    /**
     * 创建更多操作菜单项
     * @return
     */
    private MenuItem[] createMenuItems(ESIndicesModel currentItem) {
        return new MenuItem[] {
                createMenuItem(SettingClient.bundle().getString("cluster.index.table.viewIndex"), new FontIcon(Material2AL.INFO), e -> handleCatIndex(currentItem, e)),
                createMenuItem(SettingClient.bundle().getString("cluster.index.table.viewStatus"), new FontIcon(Material2OutlinedAL.ADDCHART), e -> handleCatStatus(currentItem, e)),
                createMenuItem(SettingClient.bundle().getString("cluster.index.table.refreshIndex"), new FontIcon(Material2OutlinedMZ.ROTATE_RIGHT), e -> handleRefreshIndex(currentItem, e)),
                createMenuItem(SettingClient.bundle().getString("cluster.index.table.flushIndex"), new FontIcon(Material2OutlinedMZ.SYNC), e -> handleFlushIndex(currentItem, e)),
                createMenuItem(SettingClient.bundle().getString("cluster.index.table.cleanCache"), new FontIcon(Material2OutlinedAL.CLEANING_SERVICES), e -> handleCleanCache(currentItem, e))
        };
    }

    /**
     * 创建菜单项（辅助方法）
     */
    private MenuItem createMenuItem(String text, FontIcon icon,
                                    EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem(text, icon);
        item.setOnAction(handler);
        return item;
    }


    /**
     * 搜索索引
     * @param currentItem
     */
    private void handleSearchIndex(ESIndicesModel currentItem) {
        if(ObjectUtil.isNotNull(currentItem)){
            new NodeNoticeEvent(TabId.SEARCH.getCode(), PayloadType.HOME_PAGE_CHANGE.name()).publish();

            // 延迟发布，等 UI 渲染和订阅完成
            Platform.runLater(() -> {
                new NodeNoticeEvent(currentItem, PayloadType.CLUSTER_SEARCH_DATA_LOADING.name()).publish();
            });
        }


    }

    /**
     * 查看索引详情
     * @param currentItem
     * @param event
     */
    private void handleCatIndex(ESIndicesModel currentItem, ActionEvent event) {

        SearchTextPane headerPane = new SearchTextPane();
        if(ObjectUtil.isNotNull(currentItem)){

            ESIndicesDetailsTask task = new ESIndicesDetailsTask(currentItem.getIndex());

            task.setOnSucceeded(resp -> {
                String taskValue = task.getValue();

                try {
                    // 1. 解析JSON字符串
                    Object jsonObj = JSON.parse(taskValue);
                    // 2. 格式化为美观的JSON字符串
                    String formattedJson = JSON.toJSONString(jsonObj, true);
                    // 3. 设置到文本区域
                    headerPane.setOutcomeTextArea(formattedJson);

                } catch (Exception e) {
                    // 解析失败时显示原始JSON
                    headerPane.setOutcomeTextArea(taskValue);
                }
            });

            task.setOnFailed(e -> {
                logger.error("查看索引详情失败：{}", e.getSource().getException().getMessage());
                Throwable translate = ElasticManage.translate(e.getSource().getException());
                AlertUtils.error(StateStore.stage, translate.getMessage());
            });
            new Thread(task).start();

        }
        StageUtils.show(headerPane, SettingClient.bundle().getString("cluster.index.table.detail")+"(" + currentItem.getIndex()+")");
    }

    /**
     * 查看索引状态
     * @param currentItem
     * @param event
     */
    private void handleCatStatus(ESIndicesModel currentItem, ActionEvent event) {
        SearchTextPane headerPane = new SearchTextPane();

        // 实际业务逻辑
        if(ObjectUtil.isNotNull(currentItem)){
            ESIndicesStatsDetailsTask task = new ESIndicesStatsDetailsTask(currentItem.getIndex());

            task.setOnSucceeded(resp -> {
                String taskValue = task.getValue();
                try {
                    // 1. 解析JSON字符串
                    Object jsonObj = JSON.parse(taskValue);
                    // 2. 格式化为美观的JSON字符串
                    String formattedJson = JSON.toJSONString(jsonObj, true);
                    // 3. 设置到文本区域
                    headerPane.setOutcomeTextArea(formattedJson);

                } catch (Exception e) {
                    // 解析失败时显示原始JSON
                    headerPane.setOutcomeTextArea(taskValue);
                }
            });

            task.setOnFailed(e -> {
                logger.error("查看索引状态失败：{}", e.getSource().getException().getMessage());
                Throwable translate = ElasticManage.translate(e.getSource().getException());
                AlertUtils.error(StateStore.stage, translate.getMessage());
            });
            new Thread(task).start();
        }
        StageUtils.show(headerPane, SettingClient.bundle().getString("cluster.index.table.detail")+"(" + currentItem.getIndex()+")");
    }

    /**
     * 刷新索引
     * @param currentItem
     * @param event
     */
    private void handleRefreshIndex(ESIndicesModel currentItem, ActionEvent event) {

        AlertUtils.confirm(SettingClient.bundle().getString("cluster.index.action.refreshTip")+" ["+currentItem.getIndex()+"]?").ifPresent(type ->{
            if(ObjectUtil.isNotNull(currentItem)){
                ESIndicesRefreshTask task = new ESIndicesRefreshTask(currentItem.getIndex());

                task.setOnSucceeded(resp -> {
                    loadNodeData();
                    MessageUtils.success(SettingClient.bundle().getString("action.alert.success"));
                });

                task.setOnFailed(e -> {
                    logger.error("刷新索引失败：{}", e.getSource().getException().getMessage());
                    Throwable translate = ElasticManage.translate(e.getSource().getException());
                    AlertUtils.error(StateStore.stage, translate.getMessage());
                });
                new Thread(task).start();
            }
        });
    }

    /**
     * Flush索引
     * @param currentItem
     * @param event
     */
    private void handleFlushIndex(ESIndicesModel currentItem, ActionEvent event) {
        AlertUtils.confirm(SettingClient.bundle().getString("cluster.index.action.flushTip")+" ["+currentItem.getIndex()+"]?").ifPresent(type ->{
            if(ObjectUtil.isNotNull(currentItem)){
                ESIndicesFlushTask task = new ESIndicesFlushTask(currentItem.getIndex());

                task.setOnSucceeded(resp -> {
                    loadNodeData();
                    MessageUtils.success(SettingClient.bundle().getString("action.alert.success"));
                });

                task.setOnFailed(e -> {
                    logger.error("Flush索引失败：{}", e.getSource().getException().getMessage());
                    Throwable translate = ElasticManage.translate(e.getSource().getException());
                    AlertUtils.error(StateStore.stage, translate.getMessage());
                });
                new Thread(task).start();
            }
        });
    }

    /**
     * 清除 ES指定索引缓存
     * @param currentItem
     * @param event
     */
    private void handleCleanCache(ESIndicesModel currentItem, ActionEvent event) {
        AlertUtils.confirm(SettingClient.bundle().getString("cluster.index.action.cleanTip")+" ["+currentItem.getIndex()+"]?").ifPresent(type ->{
            if(ObjectUtil.isNotNull(currentItem)){
                ESIndicesCleanCacheTask task = new ESIndicesCleanCacheTask(currentItem.getIndex());

                task.setOnSucceeded(resp -> {
                    loadNodeData();
                    MessageUtils.success(SettingClient.bundle().getString("action.alert.success"));
                });

                task.setOnFailed(e -> {
                    logger.error("执行清除缓存失败：{}", e.getSource().getException().getMessage());
                    Throwable translate = ElasticManage.translate(e.getSource().getException());
                    AlertUtils.error(StateStore.stage, translate.getMessage());
                });
                new Thread(task).start();
            }
        });
    }
    @FXML
    public void handleRefresh(ActionEvent event) {
        loadNodeData();
        pagingControl.pageNumProperty().setValue(0);
    }

    private void loadNodeData() {
        // 清空表格并设置加载中状态
        tableView.getItems().clear();

        ESIndicesTask task = new ESIndicesTask(ElasticManage.INDICES_FORMAT);
        new LoadingEvent(Boolean.TRUE, task).publish();
        task.setOnSucceeded(event -> {
            LoadingEvent.STOP.publish();

            originalNodeData = task.getValue(); // 原始数据
            filteredData = FXCollections.observableArrayList(originalNodeData); // 初始化过滤数据
            viewModel.totalProperty().set(filteredData.size());
            updateTableData(); // 加载当前页数据
        });

        task.setOnFailed(e -> {
            logger.error("加载索引列表失败：{}", e.getSource().getException().getMessage());
            LoadingEvent.STOP.publish();
            Throwable translate = ElasticManage.translate(e.getSource().getException());
            AlertUtils.error(StateStore.stage, translate.getMessage());
        });

        new Thread(task).start();
    }

    private void updateTableData() {
        if (filteredData == null) {
            tableView.setItems(FXCollections.observableArrayList());
            return;
        }

        int pageSize = viewModel.pageSizeProperty().get();
        int pageNum = viewModel.pageNumProperty().get();

        // 保证 pageNum >= 1
        pageNum = Math.max(pageNum, 1);

        int fromIndex = Math.max((pageNum - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, filteredData.size());

        if (fromIndex >= filteredData.size()) {
            tableView.setItems(FXCollections.observableArrayList()); // 空页
        } else {
            tableView.setItems(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));
        }
        // 强制刷新表格
        tableView.refresh();
    }



    private void filterData(KeyEvent keyEvent) {

        SearchTextField searchText = (SearchTextField) searchInputGroup.getChildren().get(0);
        String keyword = searchText.getText().trim().toLowerCase();

        if (originalNodeData == null) return;

        if (keyword.isEmpty()) {
            filteredData = FXCollections.observableArrayList(originalNodeData);
        } else {
            filteredData = originalNodeData.stream()
                    .filter(entry -> {
                        String combined = String.join(" ",
                                safe(entry.getIndex()),
                                safe(entry.getStatus()),
                                safe(entry.getHealth()),
                                safe(entry.getPri()),
                                safe(entry.getRep()),
                                safe(entry.getDocsCount()),
                                safe(entry.getUuid()),
                                safe(entry.getStoreSize()),
                                safe(entry.getMemoryTotal()),
                                safe(entry.getCreationDate())
                        );
                        return combined.contains(keyword);
                    })
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }

        viewModel.totalProperty().set(filteredData.size());
        updateTableData();
    }


    private String safe(String val) {
        return val == null ? "" : val.toLowerCase();
    }
}
