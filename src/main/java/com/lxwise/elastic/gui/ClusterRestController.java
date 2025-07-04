package com.lxwise.elastic.gui;

import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.Styles;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.control.ModalContentDialogPane;
import com.lxwise.elastic.control.SearchToolbarPane;
import com.lxwise.elastic.core.client.CommandHistoryClient;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.event.LoadingEvent;
import com.lxwise.elastic.core.event.NodeNoticeEvent;
import com.lxwise.elastic.core.model.event.RestDataEventModel;
import com.lxwise.elastic.core.task.ESExecuteRestTask;
import com.lxwise.elastic.entity.EsCommandHistoryProperty;
import com.lxwise.elastic.enums.PayloadType;
import com.lxwise.elastic.utils.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import java.util.Map;
import java.util.Optional;

/**
 * @author lstar
 * @create 2025-03
 * @description: rest api
 */

public class ClusterRestController {

    private static Logger logger = LoggerFactory.getLogger(ClusterRestController.class);

    @FXML
    private TextArea codeTextArea;

    @FXML
    private BorderPane contentPane;

    @FXML
    private Button executeBtn;

    @FXML
    private Button formatBtn;

    @FXML
    private Button historyBtn;
    @FXML
    public VBox resultBox;

    @FXML
    public StyleClassedTextArea outcomeTextArea;

    //左侧选择器
    @FXML
    private InputGroup selectorInputGroup;
    private  TextField urlTextField;
    private  ComboBox<String> methodComboBox;

    //历史记录模态框
    private  ModalContentDialogPane historyDialog;


    @FXML
    public void initialize() {

        initNode();

        initButton();

        initSearchFeature();

        NodeNoticeEvent.subscribeByKeys(Map.of(
                PayloadType.CLUSTER_REST_HISTORY.name(), event -> {
                    try {
                        Optional.ofNullable(event.getPayload())
                                .map(EsCommandHistoryProperty.class::cast)
                                .ifPresent(payload -> {
                                    Optional.ofNullable(payload.getCommand()).ifPresent(urlTextField::setText);
                                    Optional.ofNullable(payload.getCommandValue()).ifPresent(codeTextArea::setText);
                                    Optional.ofNullable(payload.getMethod()).ifPresent(methodComboBox.getSelectionModel()::select);
                                });
                        outcomeTextArea.clear();
                        historyDialog.close();
                    } catch (Exception e) {
                        logger.error("事件加载报错,订阅事件:{}", PayloadType.CLUSTER_REST_DATA_LOADING.name());
                    }
                },
                PayloadType.CLUSTER_REST_DATA_LOADING.name(), event -> {
                    try {
                        Optional.ofNullable(event.getPayload())
                                .map(RestDataEventModel.class::cast)
                                .ifPresent(payload -> {
                                    Optional.ofNullable(payload.getUrl()).ifPresent(urlTextField::setText);
                                    Optional.ofNullable(payload.getBody()).ifPresent(codeTextArea::setText);
                                    Optional.ofNullable(payload.getMethod()).ifPresent(methodComboBox.getSelectionModel()::select);
                                });
                        outcomeTextArea.clear();
                    } catch (Exception e) {
                        logger.error("事件加载报错,订阅事件:{}", PayloadType.CLUSTER_REST_DATA_LOADING.name());
                    }
                }
        ));


    }

    private void initNode() {
        urlTextField = new TextField("/_cat/indices?v");
        urlTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            executeBtn.setDisable(false);
                if (!StringUtils.hasText(newValue)) {
                executeBtn.setDisable( true);
                urlTextField.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                }else {
                    urlTextField.pseudoClassStateChanged(Styles.STATE_DANGER, false);
                }
        });
        methodComboBox = new ComboBox<String>();
        historyDialog = new ModalContentDialogPane();
    }


    private void initSearchFeature() {

        // 添加样式
        outcomeTextArea.getStyleClass().addAll("style-classed-text-area", "styled-text-area");
        // 添加默认文本
        outcomeTextArea.appendText(SettingClient.bundle().getString("classed.textArea.NoData"));

        // 用 VirtualizedScrollPane 包装
        VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>(outcomeTextArea);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        SearchToolbarPane searchToolbar = new SearchToolbarPane(outcomeTextArea);
        resultBox.getChildren().addAll(searchToolbar,scrollPane);

    }

    private void initButton() {

        methodComboBox.getItems().addAll("POST", "GET", "PUT", "PATCH", "DELETE");
        methodComboBox.getSelectionModel().selectFirst();

        HBox.setHgrow(urlTextField, Priority.ALWAYS);
        selectorInputGroup.getChildren().addAll(methodComboBox, urlTextField);

        historyBtn.getStyleClass().addAll(
                Styles.FLAT, Styles.ACCENT
        );
        historyBtn.setGraphic(new FontIcon(AntDesignIconsOutlined.HISTORY));
        historyBtn.setMnemonicParsing(true);

        formatBtn.getStyleClass().add(Styles.ACCENT);
        formatBtn.setMnemonicParsing(true);

        executeBtn.getStyleClass().add(Styles.ACCENT);
        executeBtn.setMnemonicParsing(true);

        codeTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(StrUtil.isNotBlank(newValue)){
                formatBtn.setDisable(false);
            }else {
                formatBtn.setDisable(true);
            }
        });
    }

    @FXML
    void executeAction(ActionEvent event) {
        String areaText = codeTextArea.getText();
        String body;
        if(StrUtil.isNotBlank(areaText)){
            body = JsonUtil.format(areaText);
        } else {
            body = null;
        }
        String url = urlTextField.getText();

        ESExecuteRestTask task = new ESExecuteRestTask(methodComboBox.getValue(),url , body);
        new LoadingEvent(Boolean.TRUE, task).publish();
        executeBtn.setDisable(true);

        task.setOnSucceeded(result -> {
            LoadingEvent.STOP.publish();
            String taskValue = task.getValue();
            String formattedJson = JsonUtil.formatJson(taskValue);
            outcomeTextArea.clear();
            outcomeTextArea.appendText(formattedJson);
            executeBtn.setDisable(false);
            insertHistory(methodComboBox.getValue(), url, body);
        });
        task.setOnFailed(e -> {
            logger.error("执行Rest查询报错:{}", e.getSource().getException().getMessage());
            LoadingEvent.STOP.publish();
            executeBtn.setDisable(false);

            Throwable translate = ElasticManage.translate(e.getSource().getException());
            AlertUtils.error(StateStore.stage, translate.getMessage());
        });
        new Thread(task).start();

    }

    private void insertHistory(String method, String url, String body) {
        EsCommandHistoryProperty historyProperty = new EsCommandHistoryProperty();
        historyProperty.setId(IdUtil.getSnowflakeNextIdStr());
        historyProperty.setMethod(method);
        historyProperty.setCommand(url);
        historyProperty.setCommandValue(body);
        historyProperty.setCreateTime(DateUtil.now());
        CommandHistoryClient.save(historyProperty);
    }

    @FXML
    void formatJsonAction(ActionEvent event) {

        String areaText = codeTextArea.getText();
        String formatJson = JsonUtil.format(areaText);
        codeTextArea.setText(formatJson);
    }

    @FXML
    void showHistoryAction(ActionEvent event) {
        FXMLLoader loader = Resources.getLoader("/gui/es_cluster_rest_history.fxml");
        Parent parent = loader.getRoot();
        // 获取屏幕高度
        double screenHeight = Screen.getPrimary().getBounds().getHeight() - 65.0;
        // 设置模态框高度
        historyDialog.setPrefHeight(screenHeight);
        historyDialog.setPrefWidth(1000);
        historyDialog.setHeaderIcon(FontIcon.of(AntDesignIconsOutlined.HISTORY));
        historyDialog.setHeaderText(SettingClient.bundle().getString("cluster.rest.history"));
        historyDialog.setContent(parent);
        historyDialog.show(StateStore.stage().getScene());
    }

}
