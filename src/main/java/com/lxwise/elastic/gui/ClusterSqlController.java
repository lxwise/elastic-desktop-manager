package com.lxwise.elastic.gui;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.control.SearchTextField;
import com.lxwise.elastic.control.SearchToolbarPane;
import com.lxwise.elastic.control.SearchableTableHeardFilterPane;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.event.LoadingEvent;
import com.lxwise.elastic.core.model.ESSqlScrollResultModel;
import com.lxwise.elastic.core.task.ESExecuteNextSqlTask;
import com.lxwise.elastic.core.task.ESExecuteSqlTask;
import com.lxwise.elastic.utils.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;


/**
 * @author lstar
 * @create 2025-02
 * @description: sql查询
 */
public class ClusterSqlController {

    private final static Logger logger = LoggerFactory.getLogger(ClusterSqlController.class);

    @FXML
    private Spinner<Integer> bathSizeSpinner;

    @FXML
    private BorderPane contentPane;

    @FXML
    private StackPane dataPane;

    @FXML
    private TabPane dataTabPane;

    @FXML
    private CheckBox loadCheckBox;

    @FXML
    private Button searchBtn;

    @FXML
    public Label resultTipLabel;

    @FXML
    public TextArea sqlInputArea;
    /*表格内容相关*/
    @FXML
    public BorderPane dataTablePane;

    @FXML
    public HBox searchInputGroup;
    @FXML
    public Button filterBtn;
    @FXML
    public Button downloadBtn;
    @FXML
    public TableView<Map<String, Object>> dataTableView;

    /*Json内容相关*/
    @FXML
    public VBox dataJsonPane;
    //    @FXML
//    public HBox searchInputGroup;
    @FXML
    public StyleClassedTextArea outcomeTextArea;
//    @FXML
//    public Button copyBtn;
//    @FXML
//    public Button downloadJsonBtn;

    private final SearchableTableHeardFilterPane checkListView = new SearchableTableHeardFilterPane();

    private List<TableColumn<Map<String, Object>, String>> allColumns = new ArrayList<>();
    private ESSqlScrollResultModel historyScrollResult = new ESSqlScrollResultModel();
    private ESSqlScrollResultModel scrollResult;
    private int pageIndex = 1;
    private String cursor = null;
    // 是否自动拉取所有分页
    private boolean isFetch = false;
    // 标记是否自动分页运行中
    private volatile boolean isRunning = false;

    //表格原数据
    private ObservableList<Map<String, Object>> originalTableData = FXCollections.observableArrayList();
    //表格过滤数据
    private FilteredList<Map<String, Object>> filteredTableData;


    public void initialize() {
        initButton();
        initControl();
        initTab();
        initSearchFeature();
        initDataTableView();
        // 用 VirtualizedScrollPane 包装富文本
        VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>(outcomeTextArea);

        // 设置它在 VBox 中自动扩展填满
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // 替换旧的组件
        dataJsonPane.getChildren().remove(outcomeTextArea);
        dataJsonPane.getChildren().add(scrollPane);
    }


    private void initButton() {
        FontIcon icon = new FontIcon(Material2MZ.SEARCH);
        searchBtn.setGraphic(icon);
        searchBtn.getStyleClass().add(Styles.ACCENT);

        /*表格内容相关*/
        filterBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.SETTING));
        filterBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_ICON, Styles.FLAT);
        downloadBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.DOWNLOAD));
        downloadBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_ICON, Styles.FLAT);
//        /*json内容相关*/
//        copyBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.COPY));
//        copyBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
//        downloadJsonBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.DOWNLOAD));
//        downloadJsonBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);

    }

    private void initTab() {

        dataTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            // 根据 Tab ID 切换界面
            switch (newValue.getId()) {
                case "table":
                    dataPane.getChildren().setAll(dataTablePane);
                    break;
                case "json":
                    dataPane.getChildren().setAll(dataJsonPane);
                    dataJsonPane.setVisible(true);
                    loadJsonData();
                    break;
                default:
                    dataPane.getChildren().setAll(dataTablePane);
                    break;
            }
        });
    }

    private void loadJsonData() {
        if (CollUtil.isNotEmpty(historyScrollResult.getColumns())) {
            String jsonString = JSON.toJSONString(historyScrollResult, true);
            outcomeTextArea.clear();
            outcomeTextArea.appendText(jsonString);
        }
    }


    private void initControl() {

        bathSizeSpinner.setEditable(true);
        bathSizeSpinner.setPrefWidth(130);
        bathSizeSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        // 创建一个SpinnerValueFactory，并设置最小值、最大值、初始值和步长
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 10);
        bathSizeSpinner.setValueFactory(valueFactory);

        loadCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isFetch = newVal;
        });

    }

    private void initSearchFeature() {

        SearchTextField searchText = new SearchTextField();
        searchText.setPromptText("Search");
        searchInputGroup.getChildren().add(0, searchText);
        searchText.setOnKeyReleased(this::filterData);

        SearchToolbarPane searchToolbar = new SearchToolbarPane(outcomeTextArea);
        dataJsonPane.getChildren().add(0, searchToolbar);
        // 设置文本
        outcomeTextArea.getStyleClass().addAll("style-classed-text-area", "styled-text-area");
        outcomeTextArea.appendText(SettingClient.bundle().getString("classed.textArea.NoData"));
    }

    private void initDataTableView() {
        // 设置默认提示信息
        Label placeholder = new Label(SettingClient.bundle().getString("cluster.sql.table.tip"));
        placeholder.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
        dataTableView.setPlaceholder(placeholder);
        dataTableView.setEditable(true);
        dataJsonPane.setVisible(false);

        // 设置列宽策略为不约束列宽
        dataTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // 设置表格样式
        dataTableView.setEditable(false);
        dataTableView.getStyleClass().addAll(Styles.BORDERED, Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE, Styles.CENTER);

    }

    /**
     * 筛选表格数据
     * @param keyEvent
     */
    private void filterData(KeyEvent keyEvent) {

        SearchTextField searchText = (SearchTextField) searchInputGroup.getChildren().get(0);

        String keyword = searchText.getText().trim().toLowerCase();

        if (filteredTableData == null) return;

        if (keyword.isEmpty()) {
            filteredTableData.setPredicate(entry -> true);
        } else {
            filteredTableData.setPredicate(entry ->
                    entry.values().stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .anyMatch(val -> val.toLowerCase().contains(keyword))
            );
        }

        // 可选更新总数视图
//            viewModel.totalProperty().set(filteredTableData.size());

    }

    @FXML
    public void searchAction(ActionEvent event) {

        if (isFetch && isRunning) {
            // 若正在分页中，点击即为中止
            executeClose();
            isRunning = false;
            resetSearchBtn();
            return;
        }

        String query = sqlInputArea.getText();

        if (StrUtil.isBlank(query)) {
            MessageUtils.error(SettingClient.bundle().getString("action.sql.alert.error"));
            return;
        }

        logger.info("执行SQL查询：{}", query);
        int batchSize = bathSizeSpinner.getValue();
        ESExecuteSqlTask task = new ESExecuteSqlTask(query, batchSize);
        new LoadingEvent(Boolean.TRUE, task).publish();

        task.setOnSucceeded(result -> {

            LoadingEvent.STOP.publish();
            scrollResult = task.getValue();
            // 保存查询结果
            historyScrollResult = scrollResult;

            cursor = scrollResult.getCursor();
            pageIndex = 1;

            List<ESSqlScrollResultModel.ColumnMeta> columnMetas = scrollResult.getColumns();
            renderTableColumns(columnMetas);
            renderTableRows(scrollResult.getRows(), columnMetas);

            dataTabPane.getSelectionModel().select(0);


            resultTipLabel.setVisible(true);

            // 每次都设置提示文字（初始查询后）
            resultTipLabel.setText(MessageFormat.format(SettingClient.bundle().getString("cluster.sql.result.tip"), pageIndex, scrollResult.getRows().size()));

            if (StrUtil.isNotBlank(cursor)) {
                if (isFetch) {
                    isRunning = true; // 启动状态
                    switchToStopBtn(); // 按钮切换为“停止”
                    executeAllIfNeeded(); // 递归自动执行分页
                } else {
                    // 非自动模式下提示是否继续
                    AlertUtils.confirm(MessageFormat.format(SettingClient.bundle().getString("cluster.sql.confirm.next"), pageIndex))
                            .ifPresent(type -> {
                                if (type == ButtonType.OK) {
                                    executeNextAction();
                                } else {
                                    //用户取消继续分页，更新提示信息
                                    resultTipLabel.setText(MessageFormat.format(SettingClient.bundle().getString("cluster.sql.result.tip"), pageIndex, scrollResult.getRows().size()));
                                }
                            });
                }
            }
        });

        task.setOnFailed(e -> {
            logger.error("执行SQL查询失败：{}", e.getSource().getException().getMessage());
            searchBtn.setDisable(false);
            isRunning = false;
            resetSearchBtn(); // 恢复按钮

            LoadingEvent.STOP.publish();
            Throwable ex = ElasticManage.translate(e.getSource().getException());
            AlertUtils.error(StateStore.stage, ex.getMessage());
        });

        new Thread(task).start();
    }


    /**
     * 渲染表格列方法
     *
     * @param columns
     */
    private void renderTableColumns(List<ESSqlScrollResultModel.ColumnMeta> columns) {
        dataTableView.getColumns().clear();
        allColumns.clear();

        TableColumn<Map<String, Object>, String> indexCol = new TableColumn<>("ID");
        indexCol.setPrefWidth(100);


        indexCol.setCellFactory(col -> {
            TableCell<Map<String, Object>, String> cell = new TableCell<>();
            StringBinding binding = Bindings.when(cell.emptyProperty())
                    .then("")
                    .otherwise(cell.indexProperty().add(1).asString());
            cell.textProperty().bind(binding);
            return cell;
        });

        allColumns.add(indexCol);

        // 动态添加数据列
        for (ESSqlScrollResultModel.ColumnMeta col : columns) {
            String colName = col.getName();
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(colName);
            column.setCellValueFactory(data -> {
                Object value = data.getValue().get(colName);
                return new SimpleStringProperty(value == null ? "" : value.toString());
            });

            // 设置 Tooltip + 截断显示逻辑
            column.setCellFactory(tc -> new TableCell<>() {
                private final TextField textField = new TextField();
                private final Tooltip tooltip = new Tooltip();
//                private final ContextMenu contextMenu = new ContextMenu();

                {
                    textField.setEditable(false); // 禁止编辑
                    textField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
                    textField.setFocusTraversable(false);
                    textField.setPrefHeight(USE_COMPUTED_SIZE);
                    textField.setMouseTransparent(false); // 关键：允许鼠标选中
//                    textField.setOnMouseClicked(e -> textField.selectAll()); // 可选：点击全选

                    tooltip.setWrapText(true);
                    tooltip.setHideDelay(Duration.millis(2));

//                    MenuItem copyItem = new MenuItem("复制");
//                    copyItem.setOnAction(e -> {
//                        ClipboardUtils.copy(textField.getText());
//                    });
//                    contextMenu.getItems().add(copyItem);

//                    setContextMenu(contextMenu);
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

            allColumns.add(column);
        }
        dataTableView.getColumns().addAll(allColumns);
    }


    /**
     * 渲染数据方法
     *
     * @param rows
     */
    private void renderTableRows(List<List<Object>> rows, List<ESSqlScrollResultModel.ColumnMeta> columns) {
//        ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList();
//
//        for (List<Object> row : rows) {
//            Map<String, Object> map = new HashMap<>();
//            for (int i = 0; i < row.size() && i < columns.size(); i++) {
//                map.put(columns.get(i).getName(), row.get(i));
//            }
//            tableData.add(map);
//        }
//
//        dataTableView.setItems(tableData);

        originalTableData.clear();

        for (List<Object> row : rows) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < row.size() && i < columns.size(); i++) {
                map.put(columns.get(i).getName(), row.get(i));
            }
            originalTableData.add(map);
        }

        // 包装为可过滤的列表
        filteredTableData = new FilteredList<>(originalTableData, p -> true);
        dataTableView.setItems(filteredTableData);
    }


    @FXML
    public void executeNextAction() {
        if (cursor == null) {
            AlertUtils.confirm(SettingClient.bundle().getString("cluster.sql.no.more.data"));
            return;
        }

        ESExecuteNextSqlTask task = new ESExecuteNextSqlTask(cursor);
        new LoadingEvent(Boolean.TRUE, task).publish();

        task.setOnSucceeded(result -> {
            LoadingEvent.STOP.publish();
            ESSqlScrollResultModel nextResult = task.getValue();
            cursor = nextResult.getCursor();
            scrollResult.setCursor(cursor);

            List<ESSqlScrollResultModel.ColumnMeta> columnMetas = scrollResult.getColumns();
            List<Map<String, Object>> newRows = new ArrayList<>();

            for (List<Object> row : nextResult.getRows()) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 0; i < row.size() && i < columnMetas.size(); i++) {
                    rowMap.put(columnMetas.get(i).getName(), row.get(i));
                }
                newRows.add(rowMap);
            }

//            dataTableView.getItems().addAll(newRows);
            originalTableData.addAll(newRows);
            scrollResult.getRows().addAll(nextResult.getRows()); // 累加原始数据
            pageIndex++;

            // 更新查询结果提示信息
            resultTipLabel.setText(MessageFormat.format(SettingClient.bundle().getString("cluster.sql.result.tip"), pageIndex, scrollResult.getRows().size()));

            if (StrUtil.isNotBlank(cursor) && isFetch) {
                executeAllIfNeeded();
            } else if (StrUtil.isNotBlank(cursor)) {
                AlertUtils.confirm(MessageFormat.format(SettingClient.bundle().getString("cluster.sql.confirm.next"), pageIndex)).ifPresent(type -> {
                    if (type == ButtonType.OK) {
                        executeNextAction();
                    } else {
                        executeClose();
                    }
                });
            } else {
                AlertUtils.confirm(MessageFormat.format(SettingClient.bundle().getString("cluster.sql.confirm.total"), pageIndex));
            }
        });

        task.setOnFailed(e -> {
            logger.error("执行下一条Sql查询失败,cursor:{},错误:{}", cursor, e.getSource().getException().getMessage());
            LoadingEvent.STOP.publish();
            Throwable ex = ElasticManage.translate(e.getSource().getException());
            AlertUtils.error(StateStore.stage, ex.getMessage());
        });

        new Thread(task).start();
    }

    // 自动分页逻辑
    private void executeAllIfNeeded() {
        if (cursor == null || !isFetch || !isRunning) return; // 防止继续递归

        ESExecuteNextSqlTask task = new ESExecuteNextSqlTask(cursor);
        new LoadingEvent(Boolean.TRUE, task).publish();

        task.setOnSucceeded(e -> {
            LoadingEvent.STOP.publish();

            // 再次检查防止中间被停止
            if (!isRunning) {
                return;
            }

            ESSqlScrollResultModel nextResult = task.getValue();

            if (CollUtil.isEmpty(nextResult.getRows())) {
                cursor = null;
                isRunning = false;
                resetSearchBtn();

                AlertUtils.confirm(MessageFormat.format(SettingClient.bundle().getString("cluster.sql.confirm.total"), pageIndex));
                return;
            }

            cursor = nextResult.getCursor();
            scrollResult.setCursor(cursor);

            List<ESSqlScrollResultModel.ColumnMeta> columnMetas = scrollResult.getColumns();
            List<Map<String, Object>> newRows = new ArrayList<>();

            for (List<Object> row : nextResult.getRows()) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 0; i < row.size() && i < columnMetas.size(); i++) {
                    rowMap.put(columnMetas.get(i).getName(), row.get(i));
                }
                newRows.add(rowMap);
            }

//            dataTableView.getItems().addAll(newRows);
            originalTableData.addAll(newRows);
            scrollResult.getRows().addAll(nextResult.getRows());
            pageIndex++;

            // 实时更新分页信息
            resultTipLabel.setText(MessageFormat.format(SettingClient.bundle().getString("cluster.sql.result.tip"), pageIndex, scrollResult.getRows().size()));

            // 继续分页时再次判断运行状态
            if (StrUtil.isNotBlank(cursor) && isFetch && isRunning) {
                executeAllIfNeeded();
            } else {
                isRunning = false;
                resetSearchBtn();
            }
        });

        task.setOnFailed(e -> {
            isRunning = false; // 避免继续分页
            resetSearchBtn();

            LoadingEvent.STOP.publish();
            Throwable ex = ElasticManage.translate(e.getSource().getException());
            if (ex.getMessage().contains("search_context_missing_exception")) {
//                AlertUtils.warn(StateStore.stage, "查询上下文已过期，分页已终止");
            } else {
                AlertUtils.error(StateStore.stage, ex.getMessage());
            }
        });

        new Thread(task).start();

        if (cursor == null || !isFetch) {
            isRunning = false;
            resetSearchBtn();
            return;
        }

    }


    // 关闭 scroll 查询
    private void executeClose() {
        if (cursor == null) return;

        Task<Void> closeTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ElasticManage.executeCloseSql(ElasticManage.get(), cursor);
                return null;
            }
        };

        closeTask.setOnSucceeded(e -> {
            cursor = null;
            isRunning = false;
            resetSearchBtn();
            AlertUtils.info(StateStore.stage, SettingClient.bundle().getString("cluster.sql.cancel.query"));
        });


        closeTask.setOnFailed(e -> {
            Throwable ex = ElasticManage.translate(e.getSource().getException());
            AlertUtils.error(StateStore.stage, SettingClient.bundle().getString("cluster.sql.cancel.error") + "：" + ex.getMessage());
        });

        new Thread(closeTask).start();
    }


    // 将按钮变为“停止”
    private void switchToStopBtn() {
        searchBtn.setText(SettingClient.bundle().getString("cluster.sql.search.stop"));
        searchBtn.setGraphic(new FontIcon(Material2OutlinedMZ.STOP_CIRCLE));
        searchBtn.setDisable(false);
    }

    // 恢复按钮为“搜索”
    private void resetSearchBtn() {
        searchBtn.setText(SettingClient.bundle().getString("cluster.sql.search"));
        searchBtn.setGraphic(new FontIcon(Material2MZ.SEARCH));
        searchBtn.setDisable(false);
    }


    @FXML
    public void filterAction(ActionEvent event) {
        VBox vBox = new VBox(5);
        vBox.setPrefWidth(500);
        vBox.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancelBtn = new Button(SettingClient.bundle().getString("cluster.rest.history.cancel"));
        cancelBtn.setPrefHeight(25);
        Button confirmBtn = new Button(SettingClient.bundle().getString("cluster.rest.history.confirm"));
        confirmBtn.setPrefHeight(25);
        cancelBtn.setGraphic(new FontIcon(Material2AL.CANCEL));
        confirmBtn.setGraphic(new FontIcon(Material2MZ.SAVE));
        confirmBtn.getStyleClass().addAll(Styles.ACCENT);
        // 动态列名
        List<String> allColumnNames = allColumns.stream()
                .map(TableColumn::getText)
                .toList();

        checkListView.setItems(allColumnNames);

        // 当前显示的列名
        List<String> currentColumns = dataTableView.getColumns().stream()
                .map(TableColumn::getText)
                .toList();
        // 设置 checkListView 的选项
        checkListView.setCheckedItems(currentColumns);
        // 获取当前显示的列名

        // 设置 checkListView 的选中项
        checkListView.setCheckedItems(currentColumns);
        hBox.getChildren().addAll(cancelBtn, confirmBtn);
        vBox.getChildren().addAll(checkListView, hBox);
        // 显示窗口，并获取 Stage
        Stage stage = StageUtils.show(vBox, SettingClient.bundle().getString("cluster.sql.table.filter"));
        // 取消按钮 - 关闭窗口
        cancelBtn.setOnAction(e -> stage.close());
        // 确认按钮 - 处理选中的数据后关闭窗口
        confirmBtn.setOnAction(e -> {
            List<String> selectedItems = checkListView.getSelectedItems();
            updateTableViewColumns(selectedItems); // 调用更新方法
            stage.close();
        });
    }

    private void updateTableViewColumns(List<String> selectedItems) {
        List<TableColumn<Map<String, Object>, String>> selectedColumns = allColumns.stream()
                .filter(c -> selectedItems.contains(c.getText()))
                .toList();

        dataTableView.getColumns().setAll(selectedColumns); // 一次性替换，避免 add/remove 反复操作
        // 设置列宽策略为不约束列宽
        dataTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // 设置表格样式
        dataTableView.setEditable(false);
        dataTableView.getStyleClass().addAll(Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE);
        dataTableView.refresh();
    }

    @FXML
    public void cancelQuery() {
        if (cursor != null) {
            try {
                ElasticManage.executeCloseSql(ElasticManage.get(), cursor);
                AlertUtils.confirm(SettingClient.bundle().getString("cluster.sql.cancel.query"));
                cursor = null;
            } catch (Exception e) {
                AlertUtils.error(StateStore.stage, SettingClient.bundle().getString("cluster.sql.cancel.error") + "：" + e.getMessage());
            }
        }
    }


    @FXML
    public void downloadAction(ActionEvent event) {

        if (CollUtil.isEmpty(dataTableView.getItems())) {
            return;
        }
        JsonFileSaver.saveTableViewAsCsv(StateStore.stage, dataTableView, "result" + DateUtil.today());


    }

    @FXML
    public void copyAction(ActionEvent event) {
        ClipboardUtils.copy(outcomeTextArea.getText());
    }

    @FXML
    public void downloadJsonAction(ActionEvent event) {
        if (StrUtil.isBlank(outcomeTextArea.getText())) {
            return;
        }
        JsonFileSaver.saveToJsonFile(StateStore.stage, outcomeTextArea.getText(), "result" + DateUtil.today());

    }
}