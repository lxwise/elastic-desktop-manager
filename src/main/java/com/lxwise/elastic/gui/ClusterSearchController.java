package com.lxwise.elastic.gui;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.control.*;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.event.LoadingEvent;
import com.lxwise.elastic.core.event.NodeNoticeEvent;
import com.lxwise.elastic.core.exception.BusinessException;
import com.lxwise.elastic.core.model.ESIndicesModel;
import com.lxwise.elastic.core.model.TableFieldValueModel;
import com.lxwise.elastic.core.model.event.RestDataEventModel;
import com.lxwise.elastic.core.model.view.ClusterSearchView;
import com.lxwise.elastic.core.task.ESIndicesTask;
import com.lxwise.elastic.core.task.EsSearchByIndexTask;
import com.lxwise.elastic.enums.PayloadType;
import com.lxwise.elastic.enums.TabId;
import com.lxwise.elastic.utils.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lstar
 * @create 2025-03
 * @description: 搜索管理
 */
public class ClusterSearchController {

    private static Logger logger = LoggerFactory.getLogger(ClusterSearchController.class);

    @FXML public HBox indexHBox;
    @FXML private Tab conditionQueryTab;
    @FXML private Tab attributeQueryTab;
    @FXML
    public StackPane conditionsBoxPane;
    @FXML
    public TabPane conditionsTabPane;
    @FXML private ToggleButton simpleRadioButton;
    @FXML private ToggleButton aggregateRadioButton;
    @FXML private ToggleGroup queryTypeGroup;
    @FXML private Spinner<Integer> timeoutSpinner;
    @FXML private CheckBox trackTotalHitsCheckBox;
    @FXML private Button searchButton;
    @FXML private Button showQueryButton;
    @FXML private Button deleteDataButton;
    @FXML private Button updateByQueryButton;
    @FXML private VBox conditionsVBox;
    @FXML private VBox conditionsAttrVBox;
    @FXML private Button addConditionButton;
    @FXML public Button addAttributeButton;
    /*结果内容相关*/
    @FXML public Tab tableTab;
    @FXML public Tab jsonTab;
    @FXML private TabPane resultTabPane;
    @FXML private TableView<Map<String, Object>> dataTableView;

    @FXML
    private StackPane dataPane;
    @FXML
    public BorderPane dataTablePane;

    @FXML
    public HBox searchInputGroup;

    @FXML
    public Button filterBtn;
    @FXML
    public Button downloadBtn;
    @FXML
    public StyleClassedTextArea outcomeTextArea;

    @FXML
    public VBox dataJsonPane;

    private final SearchableTableHeardFilterPane checkListView = new SearchableTableHeardFilterPane();

    private ClusterSearchView viewModel = new ClusterSearchView();
    private List<TableColumn<Map<String, Object>, String>> allColumns = new ArrayList<>();

    private SearchableComboBox<ESIndicesModel> indexComboBox;

    private final SimpleBooleanProperty isSimple = new SimpleBooleanProperty(true);

    private PagingControl pagingControl;

    //将事件值缓存起来
    private String pendingSelectIndexName = null;

    //将索引详细字段信息缓存起来
    private List<String> cachedFields = Collections.synchronizedList(new ArrayList<>());
    //缓存索引字段信息
    private List<String> indexFields;

    //表格原数据
    private ObservableList<Map<String, Object>> originalTableData = FXCollections.observableArrayList();
    //表格过滤数据
    private FilteredList<Map<String, Object>> filteredTableData;

    @FXML
    public void initialize() {


        pagingControl = new PagingControl();
        dataTablePane.setBottom(pagingControl);
        BorderPane.setMargin(pagingControl, new Insets(0, 20, 20, 0)); // 仅底部 10px
        pagingControl.totalProperty().bind(viewModel.totalProperty());
        viewModel.pageNumProperty().bind(pagingControl.pageNumProperty());
        viewModel.pageSizeProperty().bind(pagingControl.pageSizeProperty());
        pagingControl.pageNumProperty().addListener((observable, oldValue, newValue) -> {
            // 只有页码真正变化才触发查询
            if (!Objects.equals(oldValue, newValue)) {
                onSearch();
            }
        });

        pagingControl.pageSizeProperty().addListener((observable, oldValue, newValue) -> {

            if (!Objects.equals(oldValue, newValue)) {
                onSearch();
            }
        });

        initIndexComboBox();

        initButton();

        initQueryNode();

        initResultNode();

        loadData();

        //接收事件消息设置索引选中
        NodeNoticeEvent.subscribeByKey(PayloadType.CLUSTER_SEARCH_DATA_LOADING.name(), event -> {
            try {
                Optional.ofNullable(event.getPayload())
                        .map(ESIndicesModel.class::cast)
                        .ifPresent(indexName -> {
                            // 如果 ComboBox 已经有数据，直接选中；否则缓存起来
                            if (indexComboBox.getItems() != null && !indexComboBox.getItems().isEmpty()) {
                                indexComboBox.getItems().stream()
                                        .filter(item -> item.getIndex().equals(indexName))
                                        .findFirst()
                                        .ifPresent(indexComboBox.getSelectionModel()::select);
                            } else {
                                //缓存 indexName（数据还没加载完）
                                pendingSelectIndexName = indexName.getIndex();
                            }
                        });
            } catch (Exception e) {
                logger.error("事件加载报错,订阅事件:{}", PayloadType.CLUSTER_REST_DATA_LOADING.name());
            }
        });


    }


    /***
     * 初始化结果相关组件
     */
    private void initResultNode() {

        SearchTextField searchText = new SearchTextField();
        searchText.setPromptText("Search");
        searchInputGroup.getChildren().add(0, searchText);
        searchText.setOnKeyReleased(this::filterData);


        isSimple.addListener((observable, oldValue, newValue) -> {
            if(newValue) { // 简单模式
                dataTablePane.setVisible(true);
                dataJsonPane.setVisible(false);
                tableTab.setDisable(false);
                resultTabPane.getSelectionModel().select(tableTab);
            } else { // 高级模式
                dataTablePane.setVisible(false);
                dataJsonPane.setVisible(true);
                tableTab.setDisable(true);
                resultTabPane.getSelectionModel().select(jsonTab);
            }
        });


        //结果界面切换
        resultTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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

        //-----------------json结果布局---------------------
        SearchToolbarPane searchToolbar = new SearchToolbarPane(outcomeTextArea);
        dataJsonPane.getChildren().add(0, searchToolbar);
        VBox.setMargin(searchToolbar, new Insets(0, 20, 0, 0));
        // 设置文本
        outcomeTextArea.getStyleClass().addAll("style-classed-text-area", "styled-text-area");
        // 用 VirtualizedScrollPane 包装富文本
        VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>(outcomeTextArea);
        // 设置它在 VBox 中自动扩展填满
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        // 替换旧的组件
        dataJsonPane.getChildren().remove(outcomeTextArea);
        dataJsonPane.getChildren().add(scrollPane);


        //-----------------TableView结果布局---------------------
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
        dataTableView.getStyleClass().addAll(Styles.BORDERED,Styles.BORDER_SUBTLE, Styles.STRIPED, Tweaks.EDGE_TO_EDGE,Styles.CENTER);

    }

    private void loadJsonData() {
        ObservableList<Map<String, Object>> currentPageItems = dataTableView.getItems();
        try {
            if (CollUtil.isNotEmpty(currentPageItems)) {
                String jsonString = JSON.toJSONString(
                        currentPageItems,
                        JSONWriter.Feature.PrettyFormat,
                        JSONWriter.Feature.WriteNulls
                );
                outcomeTextArea.clear();
                outcomeTextArea.appendText(jsonString);
            }
        } catch (Exception e) {
            outcomeTextArea.appendText(e.getMessage());
        }
    }

    /***
     * 初始化查询相关组件
     */
    private void initQueryNode() {
        // 初始化只添加条件查询 tab
        conditionsTabPane.getTabs().setAll(conditionQueryTab);
        conditionsTabPane.getSelectionModel().select(conditionQueryTab);
        showConditionsVBox(true);
        // 切换查询类型：决定是否启用聚合查询 Tab
        queryTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == aggregateRadioButton) {
                isSimple.setValue( false);
                if (!conditionsTabPane.getTabs().contains(attributeQueryTab)) {
                    conditionsTabPane.getTabs().add(attributeQueryTab);
                }
            } else {
                isSimple.setValue( true);
                // 切换回简单查询时，移除 Tab 和清空聚合条件内容
                conditionsTabPane.getTabs().remove(attributeQueryTab);
                conditionsAttrVBox.getChildren().clear();
                // 如果当前选中的 Tab 是 attributeQueryTab，要切回 conditionQueryTab
                if (conditionsTabPane.getSelectionModel().getSelectedItem() == attributeQueryTab) {
                    conditionsTabPane.getSelectionModel().select(conditionQueryTab);
                }
            }
        });

        // Tab 切换仅控制显示隐藏，不清空内容
        conditionsTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == conditionQueryTab) {
                showConditionsVBox(true);
            } else if (newTab == attributeQueryTab) {
                showConditionsVBox(false);
            }
        });

        SpinnerValueFactory<Integer> timeoutFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 300, 30, 1);
        timeoutSpinner.setValueFactory(timeoutFactory);
    }

    /**
     * 初始化索引下拉框
     */
    private void initIndexComboBox() {
        // 选中项显示：只显示 index 名称
        indexComboBox = new SearchableComboBox<>();
        indexComboBox.setPrefWidth(300.0);
        indexComboBox.setEditable(true);
        HBox.setHgrow(indexComboBox, Priority.ALWAYS);
        indexHBox.getChildren().add(indexComboBox);

        indexComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ESIndicesModel object) {
                return object != null ? object.getIndex() : "";
            }

            @Override
            public ESIndicesModel fromString(String string) {
                // 从显示文本反推模型（必须保证唯一）
                return indexComboBox.getItems().stream()
                        .filter(item -> string.equals(item.getIndex()))
                        .findFirst()
                        .orElse(null);
            }
        });


        indexComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!(newVal instanceof ESIndicesModel model)) {
                return;
            }
            addAttributeButton.setDisable(true);
            addConditionButton.setDisable(true);
            if (newVal == null) return;

            addAttributeButton.setDisable(false);
            addConditionButton.setDisable(false);
            // 标准清空操作
            conditionsVBox.getChildren().clear();
            conditionsAttrVBox.getChildren().clear();
            new Thread(() -> {
                try {
                    String details = ElasticManage.indicesDetails(ElasticManage.get(), newVal.getIndex());
                    if (StrUtil.isBlank(details)) return;

                    indexFields = JSON.parseObject(details).values().stream()
                            .map(obj -> (JSONObject) obj)
                            .filter(index -> index.containsKey("mappings"))
                            .flatMap(index -> index.getJSONObject("mappings").getJSONObject("properties").keySet().stream())
                            .distinct()
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .toList();

                    cachedFields.clear();
                    cachedFields.addAll(indexFields);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void loadData() {

        //加载数据设置indexComboBox
        ESIndicesTask task = new ESIndicesTask(ElasticManage.INDICES_SIMPLE_FORMAT);

        task.setOnSucceeded(event -> {
            ObservableList<ESIndicesModel> value = task.getValue();
            indexComboBox.setItems(value);

            indexComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ESIndicesModel item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Label label = new Label(item.getIndex());
                        label.setFont(Font.font(13));

                        Circle circle = new Circle(5);
                        switch (item.getHealth()) {
                            case "green" -> circle.setFill(Color.GREEN);
                            case "yellow" -> circle.setFill(Color.GOLD);
                            case "red" -> circle.setFill(Color.RED);
                            default -> circle.setFill(Color.GRAY);
                        }

                        HBox hBox = new HBox(5, circle, label);
                        hBox.setAlignment(Pos.CENTER_LEFT);
                        setGraphic(hBox);
                    }
                }
            });

            // === 如果之前有缓存的 indexName，就选中 ===
            if (pendingSelectIndexName != null) {
                value.stream()
                        .filter(item -> item.getIndex().equals(pendingSelectIndexName))
                        .findFirst()
                        .ifPresent(indexComboBox.getSelectionModel()::select);
                pendingSelectIndexName = null; // 清除缓存
            }

        });


        task.setOnFailed(e -> {
            logger.error("加载索引列表失败：{}", e.getSource().getException().getMessage());

            Throwable translate = ElasticManage.translate(e.getSource().getException());
            AlertUtils.error(StateStore.stage, translate.getMessage());
        });

        new Thread(task).start();
    }

    private void initButton() {

        //右侧查询/修改/删除按钮
        showQueryButton.getStyleClass().addAll(Styles.FLAT,Styles.ACCENT);
        deleteDataButton.getStyleClass().addAll(Styles.FLAT,Styles.ACCENT);
        updateByQueryButton.getStyleClass().addAll(Styles.FLAT,Styles.ACCENT);

        //搜索按钮
        FontIcon icon = new FontIcon(Material2MZ.SEARCH);
        searchButton.setGraphic(icon);
        searchButton.getStyleClass().addAll(Styles.ACCENT);

        //条件添加按钮
        FontIcon  addConditionIcon = new FontIcon(Material2AL.ADD);
        addConditionButton.setGraphic(addConditionIcon);
        addConditionButton.getStyleClass().addAll(Styles.ACCENT,Styles.SMALL);

        FontIcon  addAttributeIcon = new FontIcon(Material2AL.ADD);
        addAttributeButton.setGraphic(addAttributeIcon);
        addAttributeButton.getStyleClass().addAll(Styles.ACCENT,Styles.SMALL);

        searchButton.setOnAction(event -> onSearch());
        // 条件添加事件
        addConditionButton.setOnAction(event -> addCondition());
        addAttributeButton.setOnAction(event -> addAttribute());
        simpleRadioButton.getStyleClass().add(Styles.LEFT_PILL);
        aggregateRadioButton.getStyleClass().add(Styles.RIGHT_PILL);

        /*结果表格内容相关*/
        filterBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.SETTING));
        filterBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_ICON, Styles.FLAT);
        downloadBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.DOWNLOAD));
        downloadBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_ICON, Styles.FLAT);
    }

    /**
     * 动态条件查询面板切换
     * @param showSimple
     */
    private void showConditionsVBox(boolean showSimple) {
        conditionsVBox.setVisible(showSimple);
        conditionsVBox.setManaged(showSimple);

        conditionsAttrVBox.setVisible(!showSimple);
        conditionsAttrVBox.setManaged(!showSimple);
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
        pagingControl.pageNumProperty().setValue(1);
        viewModel.totalProperty().set(filteredTableData.size());

    }

    @FXML
    private void onSearch() {
        try {
            if (checkIndexValue()) return;
            Map<String, Object> query = getQueryConditionsParms();
            if (query == null) return;

            logger.info("执行search查询：{}", JSON.toJSONString(query));
            EsSearchByIndexTask task = new EsSearchByIndexTask(
                    indexComboBox.getValue().getIndex(),
                    JSON.toJSONString(query),
                    timeoutSpinner.getValue()
            );
            new LoadingEvent(Boolean.TRUE, task).publish();

            task.setOnSucceeded(ev -> {
                LoadingEvent.STOP.publish();
                handleSearchResult(task.getValue());
            });
            task.setOnFailed(e -> {
                logger.error("执行search查询失败：{}", e.getSource().getException().getMessage());
                LoadingEvent.STOP.publish();
                throw new BusinessException(task.getException());
            });

            new Thread(task).start();
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private boolean checkIndexValue() {
        if(ObjectUtil.isNull(indexComboBox.getValue()) || StrUtil.isBlank(indexComboBox.getValue().getIndex())){
            MessageUtils.error(SettingClient.bundle().getString("action.search.alert.selectIndex"));
            return true;
        }
        return false;
    }

    /**
     * 获取查询参数
     * @return
     */
    private Map<String, Object> getQueryConditionsParms() {
        // 参数获取
        Map<String, Object> query = ESQueryBuilder.buildElasticsearchQuery(
                conditionsVBox,
                conditionsAttrVBox,
                timeoutSpinner.getValue(),
                trackTotalHitsCheckBox.isSelected(),
                queryTypeGroup.getSelectedToggle() == aggregateRadioButton
        );

        int pageNum = viewModel.pageNum.get();
        int pageSize = viewModel.pageSize.get();
        int from = (pageNum - 1) * pageSize;

        if (isSimple.getValue()){
            query.put("from", from);
            query.put("size", pageSize);
        }else {
            query.put("from", 0);
            query.put("size", 0);
        }

        if (from > 5000) {
            MessageUtils.error(SettingClient.bundle().getString("action.search.alert.pageLimit"));
            return null;
        }
        return query;
    }


    private void handleSearchResult(String response) {
        if(StrUtil.isBlank(response)){
            return;
        }

        if (!isSimple.getValue()){
            String formattedJson = JsonUtil.formatJson(response);
            outcomeTextArea.clear();
            outcomeTextArea.appendText(formattedJson);

            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();
            return;
        }
        JSONObject json = JSON.parseObject(response);
        JSONObject hits = json.getJSONObject("hits");
        JSONArray hitList = hits.getJSONArray("hits");

        int total = hits.getJSONObject("total").getIntValue("value");

        if (hitList == null || hitList.isEmpty()) {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();
            MessageUtils.info(SettingClient.bundle().getString("action.search.alert.noData"));
            return;
        }

        viewModel.totalProperty().set(total);

        // 动态创建表头和数据
        createTableColumns(extractFieldNames(hitList));

        // 原始数据加载
        ObservableList<Map<String, Object>> extractedData = extractTableData(hitList);
        originalTableData.clear();
        originalTableData.addAll(extractedData);

        // 包装为 FilteredList 并设置给 TableView
        filteredTableData = new FilteredList<>(originalTableData, p -> true);
        dataTableView.setItems(filteredTableData);

        // 切换到结果页签
        resultTabPane.getSelectionModel().select(0);

    }

    private Set<String> extractFieldNames(JSONArray hitList) {
        Set<String> fieldNames = new LinkedHashSet<>();
        hitList.forEach(item -> {
            JSONObject source = ((JSONObject)item).getJSONObject("_source");
            if (source != null) fieldNames.addAll(source.keySet());
        });
        return fieldNames;
    }

    private ObservableList<Map<String, Object>> extractTableData(JSONArray hitList) {
        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
        hitList.forEach(item -> {
            JSONObject source = ((JSONObject)item).getJSONObject("_source");
            if (source != null) data.add(new LinkedHashMap<>(source));
        });
        return data;
    }

    private void createTableColumns(Set<String> fields) {
        dataTableView.getColumns().clear();
        allColumns.clear();

        // 序号列
        TableColumn<Map<String, Object>, String> indexCol = new TableColumn<>("ID");
        indexCol.setPrefWidth(60);
        indexCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        allColumns.add(indexCol);

        // 数据列
        fields.forEach(field -> {
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(field);
            col.setPrefWidth(180);
            col.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getOrDefault(field, "").toString())
            );
            col.setCellFactory(tc -> new TableCell<>() {
                private final TextField textField = new TextField();
                private final Tooltip tooltip = new Tooltip();
                private final ContextMenu contextMenu = new ContextMenu();

                {
                    textField.setEditable(false); // 禁止编辑
                    textField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
                    textField.setFocusTraversable(false);
                    textField.setPrefHeight(USE_COMPUTED_SIZE);
                    textField.setMouseTransparent(false); // 关键：允许鼠标选中
//                    textField.setOnMouseClicked(e -> textField.selectAll()); // 可选：点击全选

                    tooltip.setWrapText(true);
                    tooltip.setHideDelay(Duration.millis(2));

                    MenuItem copyItem = new MenuItem("复制");
                    copyItem.setOnAction(e -> {
                        ClipboardUtils.copy(textField.getText());
                    });
                    contextMenu.getItems().add(copyItem);

                    setGraphic(textField);
                    setContextMenu(contextMenu);
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

            allColumns.add(col);
        });

        dataTableView.getColumns().addAll(allColumns);
    }

    @FXML
    public void addCondition() {
        VBox wrapper = ConditionAttributeComponent.createConditionRow(cachedFields); // 创建包含子组件容器的结构
        conditionsVBox.getChildren().add(wrapper);
    }

    @FXML
    public void addAttribute() {
        // 添加条件组件
        VBox wrapper = ConditionAttributeComponent.createAttributeRow(cachedFields); // 创建包含子组件容器的结构
        conditionsAttrVBox.getChildren().add(wrapper);
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
    public void downloadAction(ActionEvent event) {
        if(CollUtil.isEmpty(dataTableView.getItems())){
            return;
        }
        JsonFileSaver.saveTableViewAsCsv(StateStore.stage, dataTableView, "result"+ DateUtil.today());

    }

    @FXML
    public void showQueryAction(ActionEvent event) {

        if(ObjectUtil.isNull(indexComboBox.getValue()) || StrUtil.isBlank(indexComboBox.getValue().getIndex())){
            MessageUtils.error(SettingClient.bundle().getString("action.search.alert.selectIndex"));
            return;
        }

        Map<String, Object> query = getQueryConditionsParms();
        if (query == null) return;

        SearchTextPane headerPane = new SearchTextPane();

        String formattedJson = JsonUtil.toJSONString(query);
        headerPane.setOutcomeTextArea(formattedJson);

        StageUtils.showWithConfirm(headerPane, SettingClient.bundle().getString("cluster.search.searchCondition"), StateStore.stage()).ifPresent(buttonType -> {
            new NodeNoticeEvent(TabId.REST.getCode(), PayloadType.HOME_PAGE_CHANGE.name()).publish();

            // 延迟发布，等 UI 渲染和订阅完成
            Platform.runLater(() -> {
                RestDataEventModel model = new RestDataEventModel();
                model.setMethod("POST");
                model.setBody(formattedJson);
                String index = indexComboBox.getValue().getIndex();
                String url = String.format("/%s/_search", index);
                model.setUrl(url);

                new NodeNoticeEvent(model, PayloadType.CLUSTER_REST_DATA_LOADING.name()).publish();
            });
        });

    }

    @FXML
    public void deleteDataAction(ActionEvent event) {

        if(ObjectUtil.isNull(indexComboBox.getValue()) || StrUtil.isBlank(indexComboBox.getValue().getIndex())){
            MessageUtils.error(SettingClient.bundle().getString("action.search.alert.selectIndex"));
            return;
        }
        // 参数获取
        Map<String, Object> query = ESQueryBuilder.buildElasticsearchEasyQuery(conditionsVBox);

        SearchTextPane headerPane = new SearchTextPane();
        String formattedJson = JsonUtil.toJSONString(query);
        headerPane.setOutcomeTextArea(formattedJson);

        StageUtils.showWithConfirm(headerPane, SettingClient.bundle().getString("cluster.search.deleteCondition"), StateStore.stage()).ifPresent(buttonType -> {
            new NodeNoticeEvent(TabId.REST.getCode(), PayloadType.HOME_PAGE_CHANGE.name()).publish();
            // 延迟发布，等 UI 渲染和订阅完成
            Platform.runLater(() -> {
                RestDataEventModel model = new RestDataEventModel();
                model.setMethod("POST");
                model.setBody(formattedJson);
                String index = indexComboBox.getValue().getIndex();
                String url = String.format("/%s/_delete_by_query", index);
                model.setUrl(url);

                new NodeNoticeEvent(model, PayloadType.CLUSTER_REST_DATA_LOADING.name()).publish();
            });
        });
    }

    @FXML
    public void updateByQueryAction(ActionEvent event) {

        if(ObjectUtil.isNull(indexComboBox.getValue()) || StrUtil.isBlank(indexComboBox.getValue().getIndex()) || CollUtil.isEmpty(indexFields)){
            MessageUtils.error(SettingClient.bundle().getString("action.search.alert.selectIndex"));
            return;
        }

        // 获取原始查询条件（只包含 query）
        Map<String, Object> originalQueryMap = ESQueryBuilder.buildElasticsearchEasyQuery(conditionsVBox);
        if (originalQueryMap == null || originalQueryMap.isEmpty()) {
            MessageUtils.error(SettingClient.bundle().getString("action.search.alert.noQueryCondition"));
            return;
        }

        // 弹出修改表格（用于设置 script 更新内容）
        showUpdateTable(indexFields).ifPresent(modifiedList -> {
            // 生成 script.inline 内容
            String scriptInline = modifiedList.stream()
                    .filter(e -> !e.getField().isBlank() && !e.getValue().isBlank())
                    .map(e -> String.format("ctx._source['%s']=%s;", e.getField(), formatValue(e.getValue())))
                    .collect(Collectors.joining(" "));

            if (scriptInline.isBlank()) {
                MessageUtils.error(SettingClient.bundle().getString("action.search.alert.noInputUpdateField"));
                return;
            }

            logger.info("scriptInline: {}", scriptInline);
            // 将 script 合并进原始 query 条件
            Map<String, Object> finalPayload = new LinkedHashMap<>(originalQueryMap);
            Map<String, Object> scriptPart = Map.of("inline", scriptInline);
            finalPayload.put("script", scriptPart);

            String formattedJson = JsonUtil.toJSONString(finalPayload);

            new NodeNoticeEvent(TabId.REST.getCode(), PayloadType.HOME_PAGE_CHANGE.name()).publish();

            logger.info("finalPayload: {}", formattedJson);

            Platform.runLater(() -> {
                RestDataEventModel model = new RestDataEventModel();
                model.setMethod("POST");
                model.setBody(formattedJson);
                String index = indexComboBox.getValue().getIndex();
                model.setUrl("/" + index + "/_update_by_query");

                new NodeNoticeEvent(model, PayloadType.CLUSTER_REST_DATA_LOADING.name()).publish();
            });
        });
    }
    private String formatValue(String val) {
        if (val.matches("^[0-9]+(\\.[0-9]+)?$")) {
            return val; // 数值类型
        }
        return "'" + val.replace("'", "\\'") + "'"; // 字符串加引号并转义
    }



    public static Optional<List<TableFieldValueModel>> showUpdateTable(List<String> fieldOptions) {
        TableView<TableFieldValueModel> tableView = new TableView<>();
        tableView.setMinWidth(650);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.getStyleClass().addAll(Styles.BORDER_SUBTLE, Tweaks.EDGE_TO_EDGE, Styles.BORDERED);

        ObservableList<TableFieldValueModel> data = FXCollections.observableArrayList();

        // 字段列
        TableColumn<TableFieldValueModel, String> fieldCol = new TableColumn<>(SettingClient.bundle().getString("cluster.search.update.field"));
        fieldCol.setPrefWidth(250);
        fieldCol.setCellValueFactory(cell -> cell.getValue().fieldProperty());
        fieldCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            {
                comboBox.setItems(FXCollections.observableArrayList(fieldOptions));
                comboBox.setPrefWidth(200);
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.setMinWidth(Region.USE_PREF_SIZE);
                comboBox.setPromptText(SettingClient.bundle().getString("action.search.alert.noSelectField"));
                comboBox.setEditable(true);

                comboBox.getStyleClass().add("combo-box-search");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(item);
                    comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                        TableFieldValueModel entry = getTableRow().getItem();
                        if (entry != null) entry.setField(newVal);
                    });

                    HBox wrapper = new HBox(comboBox);
                    wrapper.setPadding(Insets.EMPTY);
                    wrapper.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(comboBox, Priority.ALWAYS);
                    setGraphic(wrapper);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });



        // 值列
        TableColumn<TableFieldValueModel, String> valueCol = new TableColumn<>(SettingClient.bundle().getString("cluster.search.update.value"));
        valueCol.setPrefWidth(250);
        valueCol.setCellValueFactory(cell -> cell.getValue().valueProperty());
        valueCol.setCellFactory(col -> new TableCell<>() {
            private final TextField textField = new TextField();
            private TableFieldValueModel currentEntry;

            {
                textField.setPrefWidth(200);
                textField.setMaxWidth(Double.MAX_VALUE);
                textField.setPromptText("'elastic'");
                textField.getStyleClass().add("update-text-field");

                // 单独注册一次监听器，不在 updateItem 中重复绑定
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (currentEntry != null && !newVal.equals(currentEntry.getValue())) {
                        currentEntry.setValue(newVal);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    currentEntry = null;
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                } else {
                    currentEntry = getTableRow().getItem();

                    // 只在不相等时设置，避免光标跳动
                    if (!textField.getText().equals(item)) {
                        textField.setText(item);
                    }

                    // 包装布局
                    HBox wrapper = new HBox(textField);
                    wrapper.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(textField, Priority.ALWAYS);

                    setGraphic(wrapper);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });




        // 操作列：删除按钮
        TableColumn<TableFieldValueModel, Void> actionCol = new TableColumn<>(SettingClient.bundle().getString("cluster.search.table.action"));
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("", new FontIcon(Material2AL.DELETE));

            {
                deleteBtn.getStyleClass().addAll(Styles.FLAT, Styles.DANGER);
                deleteBtn.setOnAction(e -> {
                    TableFieldValueModel item = getTableView().getItems().get(getIndex());
                    tableView.getItems().remove(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        // 添加列到表格
        tableView.getColumns().addAll(fieldCol, valueCol, actionCol);
        tableView.setItems(data);
        tableView.setEditable(true);

        // 按钮行
        Button addBtn = new Button(SettingClient.bundle().getString("cluster.search.table.add"));
        Button clearBtn = new Button(SettingClient.bundle().getString("cluster.search.table.clean"));
        addBtn.getStyleClass().add(Styles.ACCENT);
        addBtn.setGraphic(new FontIcon(Material2AL.ADD));
        addBtn.getStyleClass().addAll(Styles.FLAT, Styles.ACCENT);
        clearBtn.setGraphic(new FontIcon(Material2AL.DELETE));
        clearBtn.getStyleClass().addAll(Styles.FLAT, Styles.DANGER);

        addBtn.setOnAction(e -> data.add(new TableFieldValueModel("", "")));
        clearBtn.setOnAction(e -> data.clear());

        HBox topButtons = new HBox(10, addBtn, clearBtn);
        topButtons.setPadding(new Insets(5));

        VBox content = new VBox(10, topButtons, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        content.setPadding(new Insets(10));

        return StageUtils.showWithConfirm(content, SettingClient.bundle().getString("cluster.search.updateCondition"), StateStore.stage())
                .map(btn -> new ArrayList<>(data));
    }



}