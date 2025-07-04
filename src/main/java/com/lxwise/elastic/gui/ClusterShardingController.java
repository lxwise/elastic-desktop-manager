package com.lxwise.elastic.gui;

import atlantafx.base.theme.Styles;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.control.LoadingPane;
import com.lxwise.elastic.control.PagingControl;
import com.lxwise.elastic.control.SearchTextField;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.event.LoadingEvent;
import com.lxwise.elastic.core.model.ESShardsModel;
import com.lxwise.elastic.core.model.view.ClusterShardingView;
import com.lxwise.elastic.core.task.ESShardsInfoTask;
import com.lxwise.elastic.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lstar
 * @create 2025-03
 * @description: 分片管理
 */
public class ClusterShardingController {

    private static Logger logger = LoggerFactory.getLogger(ClusterShardingController.class);
    @FXML
    public Button handleNodeRefreshBtn;
    @FXML
    public StackPane contentPane;
    @FXML
    public BorderPane rootPane;

    private PagingControl pagingControl;

    private List<ESShardsModel> shardDataList; // 原始数据
    private List<ESShardsModel> filteredDataList; // 过滤后的数据

    private ClusterShardingView  viewModel = new ClusterShardingView();
    @FXML
    public HBox searchInputGroup;

    @FXML
    public void initialize() {

        SearchTextField searchText = new SearchTextField();
        searchText.setPromptText("Search");
        searchInputGroup.getChildren().add(0, searchText);
        searchText.setOnKeyReleased(this::filterData);

        pagingControl = new PagingControl();

        pagingControl.setPageItemList(Arrays.asList(18, 24, 30, 36,42));
        pagingControl.totalProperty().bind(viewModel.totalProperty());
        viewModel.pageNumProperty().bind(pagingControl.pageNumProperty());
        viewModel.pageSizeProperty().bind(pagingControl.pageSizeProperty());

        pagingControl.pageNumProperty().addListener((observable, oldValue, newValue) -> {
            refreshPageData();
        });

        pagingControl.pageSizeProperty().addListener((observable, oldValue, newValue) -> {
            pagingControl.pageNumProperty().setValue(1);
            refreshPageData();
        });

        getShardDataFromES(); // 异步加载数据


        handleNodeRefreshBtn.setGraphic(new FontIcon(Material2MZ.REFRESH));
        handleNodeRefreshBtn.getStyleClass().addAll(Styles.FLAT);

        //分页器和内容都在一个 VBox 中，ScrollPane 自动占据中间高度
        VBox centerBox = new VBox();
        centerBox.setSpacing(0);
        centerBox.setPadding(new Insets(0));
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        centerBox.getChildren().addAll(contentPane, pagingControl);
        rootPane.setCenter(centerBox);

    }



    @FXML
    public void handleRefresh(ActionEvent event) {
//        LoadingPane.showRefreshLoading(contentPane, 100, this::getShardDataFromES);
        getShardDataFromES();
    }

    /**
     * 创建分页内容
     */
    private void showDataInGrid(List<ESShardsModel> pageList) {
        GridPane newGridPane = new GridPane();
        newGridPane.setHgap(10);
        newGridPane.setVgap(10);
        newGridPane.setPadding(new Insets(10));

        int columns = 6;
        newGridPane.getColumnConstraints().clear();
        for (int i = 0; i < columns; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / columns);
            newGridPane.getColumnConstraints().add(col);
        }

        for (int i = 0; i < pageList.size(); i++) {
            VBox card = createCard(pageList.get(i));
            newGridPane.add(card, i % columns, i / columns);
        }

        ScrollPane scrollPane = new ScrollPane(newGridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        contentPane.getChildren().clear();
        contentPane.getChildren().add(scrollPane);
    }




    /**
     * 创建单个卡片
     */
    private VBox createCard(ESShardsModel model) {
        VBox card = new VBox(25);
        card.setPadding(new Insets(10));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: -color-bg-default;");
        card.getStyleClass().add("custom-sharding-box-container");

        // 标题
        Label title = new Label(model.getIndex());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // 节点 + IP
        HBox nodeRow = new HBox(45,
                new Label(SettingClient.bundle().getString("cluster.shard.node")+"   " + model.getNode()),
                new Label("IP   " + model.getIp()));

        // 存储 + 数量
        HBox storageRow = new HBox(45,
                new Label(SettingClient.bundle().getString("cluster.shard.storage")+"   " + model.getStore()),
                new Label(SettingClient.bundle().getString("cluster.shard.quantity")+"   " + model.getDocs()));

        // 创建 shard 容器
        HBox shardStatusP0 = new HBox(5);
        HBox shardStatusR0 = new HBox(5);
        for (ESShardsModel.ShardType type : model.getTypes()) {
            String text = type.getPrirep() + type.getShard();
            if ("p".equalsIgnoreCase(type.getPrirep())) {
                shardStatusP0.getChildren().add(createGreenBox(text));
            } else if ("r".equalsIgnoreCase(type.getPrirep())) {
                shardStatusR0.getChildren().add(createGreenDashedBox(text));
            }
        }

        // 横向滚动容器
        VBox shardsVBox = new VBox(5);
        if (!shardStatusP0.getChildren().isEmpty()) {
            ScrollPane scrollP = createShardScrollPane(shardStatusP0);
            shardsVBox.getChildren().add(scrollP);
        }
        if (!shardStatusR0.getChildren().isEmpty()) {
            ScrollPane scrollR = createShardScrollPane(shardStatusR0);
            shardsVBox.getChildren().add(scrollR);
        }

        // 最终组合
        card.getChildren().addAll(title, nodeRow, storageRow, shardsVBox);
        return card;
    }


    /**
     * 横向 ScrollPane 构建方法
     * @param shardRow
     * @return
     */
    private ScrollPane createShardScrollPane(HBox shardRow) {
        ScrollPane scrollPane = new ScrollPane(shardRow);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setPrefHeight(40); // 只显示一行盒子高度
        scrollPane.setStyle("-fx-background-color:transparent;");
        return scrollPane;
    }


    /**
     * 创建绿色 p0 小框
     */
    private Pane createGreenBox(String text) {
        StackPane box = new StackPane();
        box.setPrefSize(40, 40);
        box.setStyle("-fx-border-color: #49f666; -fx-border-width: 2; -fx-border-radius: 5;");

        Label label = new Label(text);
        label.setTextFill(Color.GREEN);
        label.setFont(new Font(14));

        box.getChildren().add(label);
        return box;
    }
    /**
     * 创建绿色 r0 小框
     */
    private Pane createGreenDashedBox(String text) {
        StackPane box = new StackPane();
        box.setPrefSize(40, 40);
        box.setStyle("-fx-border-color: #6BFF84; -fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 5;");

        Label label = new Label(text);
        label.setTextFill(Color.GREEN);
        label.setFont(new Font(14));

        box.getChildren().add(label);
        return box;
    }

    /**
     * 刷新分页数据
     */
    private void refreshPageData() {
        int pageSize = viewModel.getPageSize();
        int pageNum = viewModel.getPageNum();

        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredDataList.size());

        if (start > end) {
            // 异常容错
            start = 0;
            end = Math.min(pageSize, filteredDataList.size());
        }

        List<ESShardsModel> pageList = filteredDataList.subList(start, end);
        showDataInGrid(pageList); // 抽出展示逻辑
    }


    /**
     * 过滤数据并刷新分页
     */
    private void filterData(KeyEvent event) {
        String keyword = ((SearchTextField) searchInputGroup.getChildren().get(0)).getText().trim().toLowerCase();

        filteredDataList = shardDataList.stream()
                .filter(data -> data.getIndex().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        viewModel.totalProperty().set(filteredDataList.size());
        refreshPageData();
    }


    /**
     * 获取示例数据
     */
    private void getShardDataFromES() {
        ESShardsInfoTask task = new ESShardsInfoTask();
        new LoadingEvent(Boolean.TRUE, task).publish();
        task.setOnSucceeded(event -> {
            List<ESShardsModel> result = task.getValue();
            LoadingEvent.STOP.publish();
            // 1. 排序：index > prirep > shard
            result.sort((a, b) -> {
                int indexCompare = a.getIndex().compareToIgnoreCase(b.getIndex());
                if (indexCompare != 0) return indexCompare;
                int prirepCompare = a.getPrirep().compareToIgnoreCase(b.getPrirep());
                if (prirepCompare != 0) return prirepCompare;
                return a.getShard().compareToIgnoreCase(b.getShard());
            });

            // 2. 聚合
            Map<String, ESShardsModel> indexMap = new LinkedHashMap<>();
            for (ESShardsModel shard : result) {
                ESShardsModel model = indexMap.computeIfAbsent(shard.getIndex(), k -> {
                    ESShardsModel m = new ESShardsModel();
                    m.setIndex(k);
                    m.setDocs(shard.getDocs());
                    m.setIp(shard.getIp());
                    m.setStore(shard.getStore());
                    m.setNode(shard.getNode());
                    return m;
                });

                ESShardsModel.ShardType type = new ESShardsModel.ShardType();
                type.setPrirep(shard.getPrirep());
                type.setShard(shard.getShard());
                type.setState(shard.getState());
                model.getTypes().add(type);
            }

            shardDataList = new ArrayList<>(indexMap.values());
            filteredDataList = new ArrayList<>(shardDataList);
            viewModel.totalProperty().set(filteredDataList.size());

            pagingControl.pageNumProperty().setValue(1); // 强制第一页
            refreshPageData();

        });

        task.setOnFailed(e -> {
            logger.error("执行es分片信息查询失败：{}", e.getSource().getException().getMessage());
            LoadingEvent.STOP.publish();
            Throwable translate = ElasticManage.translate(e.getSource().getException());
            AlertUtils.error(StateStore.stage, translate.getMessage());
        });

        new Thread(task).start();
    }


}
