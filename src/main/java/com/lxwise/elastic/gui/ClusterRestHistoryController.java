package com.lxwise.elastic.gui;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import cn.hutool.core.util.StrUtil;
import com.lxwise.elastic.control.PagingControl;
import com.lxwise.elastic.control.SearchTextField;
import com.lxwise.elastic.core.client.CommandHistoryClient;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.event.NodeNoticeEvent;
import com.lxwise.elastic.core.model.view.ClusterRestHistoryView;
import com.lxwise.elastic.entity.EsCommandHistoryProperty;
import com.lxwise.elastic.enums.PayloadType;
import com.lxwise.elastic.utils.AlertUtils;
import com.lxwise.elastic.utils.JsonUtil;
import com.lxwise.elastic.utils.MessageUtils;
import com.lxwise.elastic.utils.TableColumnUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lstar
 * @create 2025-02
 * @description: rest 历史记录
 */

public class ClusterRestHistoryController {

    private static Logger logger = LoggerFactory.getLogger(ClusterRestHistoryController.class);

    @FXML
    private Button cancelBtn;

    @FXML
    private Button confirmBtn;

    @FXML
    private VBox contentPane;

    @FXML
    private TextArea historyCommandValue;

    @FXML
    private TableView<EsCommandHistoryProperty> historyTableView;

    @FXML
    private HBox searchInputGroup;

    @FXML
    private BorderPane tablePane;

    private PagingControl pagingControl;

    private ObservableList<EsCommandHistoryProperty> originalNodeData;
    private ObservableList<EsCommandHistoryProperty> filteredData;

    private final ClusterRestHistoryView viewModel = new ClusterRestHistoryView();

    @FXML
    public void initialize() {
        configureHistoryTableView();
        initButton();
        initPageControl();
        initSearchFeature();
        loadAllHistoryData();

    }

    private void configureHistoryTableView() {

        List<TableColumn<EsCommandHistoryProperty, String>> columns = TableColumnUtils.createTableColumns(
                EsCommandHistoryProperty.class,
                Map.of("Id", "Method", "Command", "CreateTime"),                null,
                column -> {
                    if ("CreateTime".equals(column.getText())) {
                        column.setPrefWidth(80);
                        column.setSortable(true);
                    }
                    if ("Method".equals(column.getText())) {
                        column.setPrefWidth(40);
                        column.setSortable(true);
                    }
                    return null;
                },"commandValue"
        );

        // 添加操作列
        TableColumn<EsCommandHistoryProperty, String> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(30);
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<EsCommandHistoryProperty, String> call(TableColumn<EsCommandHistoryProperty, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Button deleteButton = new Button("Delete");
                            deleteButton.setGraphic(new FontIcon(Material2AL.DELETE));
                            deleteButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);

                            deleteButton.setOnAction(event -> {

                                AlertUtils.confirm(SettingClient.bundle().getString("alert.delete.prompt")).ifPresent(type -> {
                                    EsCommandHistoryProperty historyItem = getTableView().getItems().get(getIndex());
                                    CommandHistoryClient.deleteById(historyItem.getId());
                                    MessageUtils.success(SettingClient.bundle().getString("form.delete.success"));
                                    loadAllHistoryData();
                                });
                            });

                            HBox actionsPane = new HBox(deleteButton);
                            setGraphic(actionsPane);
                        }
                    }
                };
            }
        });
        columns.add(actionsColumn);

        historyTableView.getColumns().addAll(columns);

        // 设定固定的列宽策略
        historyTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // 设置表格样式
        historyTableView.setEditable(false);
        historyTableView.getStyleClass().addAll(Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE);

        historyTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            historyCommandValue.clear();
            if(null == newValue || StrUtil.isBlank(newValue.getCommandValue())){
             return;
            }else {
                historyCommandValue.setText(JsonUtil.formatJson(newValue.getCommandValue()));
            }
        });

        historyTableView.setRowFactory(tv -> {
            TableRow<EsCommandHistoryProperty> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    getCommandValueAndSendMsg();
                }
            });
            return row;
        });
    }

    private void initSearchFeature() {
        SearchTextField searchText = new SearchTextField();
        searchText.setPromptText("Search");
        searchInputGroup.getChildren().add(0, searchText);
        searchText.setOnKeyReleased(this::filterData);

    }

    private void initPageControl() {
        pagingControl = new PagingControl();
        tablePane.setBottom(pagingControl);

        pagingControl.totalProperty().bind(viewModel.totalProperty());
        viewModel.pageNumProperty().bind(pagingControl.pageNumProperty());
        viewModel.pageSizeProperty().bind(pagingControl.pageSizeProperty());

        pagingControl.pageNumProperty().addListener((observable, oldValue, newValue) -> {
            updateTableData();
        });

        pagingControl.pageSizeProperty().addListener((observable, oldValue, newValue) -> {
            updateTableData();
        });
    }



    private void initButton() {
        cancelBtn.setGraphic(new FontIcon(Material2AL.CANCEL));
        cancelBtn.getStyleClass().addAll(Styles.DANGER);
        confirmBtn.setGraphic(new FontIcon(Material2MZ.SAVE));
        confirmBtn.getStyleClass().addAll(Styles.ACCENT);
    }

    private void filterData(KeyEvent event) {
        SearchTextField searchText = (SearchTextField) searchInputGroup.getChildren().get(0);
        String keyword = searchText.getText().trim().toLowerCase();

        if (originalNodeData == null) return;

        if (keyword.isEmpty()) {
            filteredData = FXCollections.observableArrayList(originalNodeData);
        } else {
            filteredData = originalNodeData.stream()
                    .filter(entry -> {
                        String combined = String.join(" ",
                                safe(entry.getMethod()),
                                safe(entry.getCommand())
                        );
                        return combined.contains(keyword);
                    })
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }

        viewModel.totalProperty().set(filteredData.size());
        updateTableData();
    }

    private void loadAllHistoryData() {
        // 清空表格并设置加载中状态
        historyTableView.getItems().clear();
        originalNodeData = FXCollections.observableArrayList(CommandHistoryClient.query4List());
        filteredData = FXCollections.observableArrayList(originalNodeData); // 初始化过滤数据
        viewModel.totalProperty().set(filteredData.size());
        updateTableData(); // 加载当前页数据
    }

    private void updateTableData() {
        if (filteredData == null) {
            historyTableView.setItems(FXCollections.observableArrayList());
            return;
        }

        int pageSize = viewModel.pageSizeProperty().get();
        int pageNum = viewModel.pageNumProperty().get();

        // 保证 pageNum >= 1
        pageNum = Math.max(pageNum, 1);

        int fromIndex = Math.max((pageNum - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, filteredData.size());

        if (fromIndex >= filteredData.size()) {
            historyTableView.setItems(FXCollections.observableArrayList()); // 空页
        } else {
            historyTableView.setItems(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));
        }
        // 强制刷新表格
        historyTableView.refresh();
    }

    @FXML
    void cancelAction(ActionEvent event) {

        new NodeNoticeEvent(null, PayloadType.CLUSTER_REST_HISTORY.name()).publish();
    }

    @FXML
    void confirmAction(ActionEvent event) {

        getCommandValueAndSendMsg();
    }

    private void getCommandValueAndSendMsg() {
        EsCommandHistoryProperty property = historyTableView.getSelectionModel().selectedItemProperty().get();
        if(null == property){
            return;
        }
        new NodeNoticeEvent(property, PayloadType.CLUSTER_REST_HISTORY.name()).publish();
    }

    private String safe(String val) {
        return val == null ? "" : val.toLowerCase();
    }


}
