package com.lxwise.elastic.gui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lxwise.elastic.ElasticApplication;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.control.ProgressPane;
import com.lxwise.elastic.control.SearchTextPane;
import com.lxwise.elastic.core.es.ElasticManage;
import com.lxwise.elastic.core.event.*;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.enums.PayloadType;
import com.lxwise.elastic.enums.TabId;
import com.lxwise.elastic.enums.Themes;
import com.lxwise.elastic.utils.*;
import com.lxwise.updater.service.FXUpdater;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsFilled;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author lstar
 * @create 2025-02
 * @description: 主界面
 */
public class HomeController {

    private static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @FXML
    public AnchorPane rootPane;

    @FXML
    public ImageView logoImg;

    @FXML
    public BorderPane contentPane;
    @FXML
    public HBox tooltipBox;
    @FXML
    public HBox state;
    @FXML
    public MenuBar menuBar;

    /**
     * 提示框信息息定时器
     */
    private final Timeline tooltipTimer = new Timeline(new KeyFrame(Duration.seconds(3)));

    public SimpleBooleanProperty isMoon = new SimpleBooleanProperty();
    @FXML
    public  Button selectorBtn;
    @FXML
    public Button aboutMeButton;
    @FXML
    public Button codeButton;
    @FXML
    public Button giftButton;
    @FXML
    public Button themeButton;
    @FXML
    public Button settingButton;

    public Label textLabel;
    public HBox graphicBox;
    public Circle greenCircle;
    public FontIcon arrowIcon;
    @FXML
    public StackPane pagePane;
    @FXML
    public TabPane homeTab;
    @FXML
    public StackPane modalPanePage;
    @FXML
    public ProgressBar loadingBar;
    @FXML
    public Tab moreTab;

    //模态框ID
    public static final String MAIN_MODAL_ID = "modal-pane";

    //进度条绑定任务
    private Task<?> work;

    //使用 Map 缓存每个页面的 Parent
    private final Map<String, Parent> pageCache = new HashMap<>();


    @FXML
    public void initialize() {

        var modalPane = new ModalPane();
        modalPane.setId(MAIN_MODAL_ID);
        modalPane.usePredefinedTransitionFactories(Side.RIGHT);
        modalPane.setAlignment(Pos.TOP_RIGHT);
        modalPanePage.getChildren().add(0, modalPane);


        tooltipTimer.setOnFinished(event -> {
            tooltipBox.getChildren().clear();
        });
        /**
         * 初始化主题
         */
        initTheme();

        /**
         * 初始化菜单
         */
        initMenu();

        /**
         * 初始化通知消息
         */
        initMessage();

        /**
         * 初始化按钮
         */
        initNode();

        initHomeTab();

        resetTheme();
        openDialog(true);


    }

    private void initHomeTab() {
        homeTab.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newTab) {
                if (newTab != null && !"more".equals(newTab.getId())) {
                    loadTabContent(newTab);
                }
            }
        });


        // “更多”菜单项
        MenuItem esItem = new MenuItem(SettingClient.bundle().getString("es.document"));
        esItem.setOnAction(event -> {
            StateStore.hostServices.showDocument("https://www.elastic.co/guide/en/elasticsearch/reference/7.10/getting-started.html");
        });
        esItem.getStyleClass().add(Styles.TEXT_SMALL);
        MenuItem fxItem = new MenuItem(SettingClient.bundle().getString("fx.document"));
        fxItem.setOnAction(event -> {
            StateStore.hostServices.showDocument("https://openjfx.cn/");
        });
        fxItem.getStyleClass().add(Styles.TEXT_SMALL);

        MenuItem logItem = new MenuItem(SettingClient.bundle().getString("fx.logs"));
        logItem.setOnAction(event -> {
            String logPath = System.getProperty("LOG_PATH");
            // 获取当天日志文件名
            String todayLogFileName = "log-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".log";

            // 构建完整路径
            Path logFile = Paths.get(logPath, todayLogFileName);
            String content;
            try {
                content = Files.exists(logFile)
                        ? Files.readString(logFile, StandardCharsets.UTF_8)
                        : null;
            } catch (IOException e) {
                content =  e.getMessage();
            }

            // 构建面板
            SearchTextPane headerPane = new SearchTextPane();
            Stage stage = StateStore.stage();

            headerPane.setPrefWidth(stage.getWidth() - 500);

            headerPane.setPrefHeight(stage.getHeight() - 500);
            headerPane.setOutcomeTextArea(content);

            // 展示窗口，标题为日志文件名
            StageUtils.show(headerPane, todayLogFileName);
        });
        logItem.getStyleClass().add(Styles.TEXT_SMALL);
        // 弹出菜单
        ContextMenu dropdownMenu = new ContextMenu(esItem, fxItem,logItem);
        dropdownMenu.getStyleClass().addAll(Styles.ACCENT, Tweaks.EDGE_TO_EDGE);

        // 获取 moreTab 并设置箭头图标
        Tab moreTab = homeTab.getTabs().stream()
                .filter(tab -> TabId.MORE.getCode().equals(tab.getId()))
                .findFirst()
                .orElse(null);

        if (moreTab != null) {
            FontIcon arrowIcon = new FontIcon(Material2OutlinedAL.KEYBOARD_ARROW_DOWN);
            moreTab.setGraphic(arrowIcon);

            // 设置点击图标或 Tab 时弹出菜单
            Node graphic = moreTab.getGraphic();
            graphic.setOnMouseClicked(e -> dropdownMenu.show(graphic, Side.BOTTOM, 0, 0));

            // 选中 moreTab 时不做内容加载，而是自动切回上一个 Tab
            homeTab.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null && "more".equals(newTab.getId())) {
                    Platform.runLater(() -> {
                        homeTab.getSelectionModel().select(oldTab); // 切换回原先 tab
                        dropdownMenu.show(moreTab.getGraphic(), Side.BOTTOM, 0, 0);
                    });
                }
            });
        }
    }

    /**
     * 加载页面Tab 内容
     * @param tab
     */
    private void loadTabContent(Tab tab) {
        if (tab == null) return;
        if (CharSequenceUtil.isBlank(ElasticManage.LATEST_CLUSTER_ID) || ElasticManage.get() == null) return;

        String fileName;
        switch (tab.getId()) {
            case "main" -> fileName = "es_cluster_health_monitor.fxml";
            case "node" -> fileName = "es_cluster_node.fxml";
            case "sharding" -> fileName = "es_cluster_sharding.fxml";
            case "index" -> fileName = "es_cluster_index.fxml";
            case "rest" -> fileName = "es_cluster_rest.fxml";
            case "sql" -> fileName = "es_cluster_sql.fxml";
            case "search" -> fileName = "es_cluster_search.fxml";
            default -> fileName = "es_cluster_health_monitor.fxml";
        }

//        pagePane.getChildren().clear();
//        FXMLLoader loader = Resources.getLoader("/gui/" + fileName);
//        Parent parent = loader.getRoot();
//        pagePane.getChildren().add(parent);

        // 页面缓存命名建议用 tab id
        String cacheKey = tab.getId();

        Platform.runLater(() -> {
            Parent parent;

            if (pageCache.containsKey(cacheKey)) {
                parent = pageCache.get(cacheKey);  // 从缓存中取
            } else {
                FXMLLoader loader = Resources.getLoader("/gui/" + fileName);
                parent = loader.getRoot();
                pageCache.put(cacheKey, parent);  // 加入缓存
            }

            pagePane.getChildren().setAll(parent);  // 替换页面内容
        });
    }



    /**
     * 初始化按钮
     */
    private void initNode() {

        logoImg.setImage(new Image("/images/elasticsearch.png"));
        settingButton.setGraphic(new FontIcon(Material2OutlinedMZ.SETTINGS));
        settingButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        // 创建旋转动画：360度，每帧5毫秒，共72帧
        Timeline rotateAnimation = new Timeline(
                new KeyFrame(Duration.millis(80), e -> {
                    settingButton.setRotate(settingButton.getRotate() + 5); // 每次增加5度
                })
        );

        // 设置为无限循环播放
        rotateAnimation.setCycleCount(Timeline.INDEFINITE);
        rotateAnimation.play();

        aboutMeButton.setGraphic(new FontIcon(FontAwesomeBrands.TRIPADVISOR));
        aboutMeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);

        codeButton.setGraphic(new FontIcon(AntDesignIconsOutlined.GITHUB));
        codeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);

        giftButton.setGraphic(new FontIcon(AntDesignIconsFilled.GIFT));
        giftButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);

        // 创建绿色小圆球
        greenCircle = new Circle(5, Color.GREEN);
        // 文字标签
        textLabel = new Label(SettingClient.bundle().getString("home.menu.selector"));
        textLabel.setFont(Font.font(13));
        // 图标
        arrowIcon = new FontIcon(Material2OutlinedAL.ARROW_DROP_DOWN);
        // 组合小圆球、文字和图标
        graphicBox = new HBox(5, textLabel, arrowIcon);
        graphicBox.setAlignment(Pos.CENTER_LEFT); // 确保对齐

        // 设置按钮的 Graphic
        selectorBtn.setGraphic(graphicBox);
        selectorBtn.setContentDisplay(ContentDisplay.LEFT); // 确保图形在左侧
        selectorBtn.setMnemonicParsing(true);

        selectorBtn.getStyleClass().addAll(
                Styles.FLAT, Styles.TEXT_NORMAL
        );

        selectorBtn.setOnAction(event -> {
            openDialog(false);
        });

        //进度条
        loadingBar.getStylesheets().add(Styles.SMALL);
    }

    /**
     * 初始化通知消息
     */
    private void initMessage() {

        // 订阅通知消息事件
        EventBus.getInstance().subscribe(NoticeEvent.class, event -> {
            Notification notification = event.notification();
            // 避免重复添加
            if (!rootPane.getChildren().contains(notification)) {
                rootPane.getChildren().add(notification);
            }
            Duration duration = event.duration();
            if (Duration.ZERO != duration) {
                var timeline = new Timeline(new KeyFrame(duration));
                timeline.setOnFinished(event1 -> notification.getOnClose().handle(new Event(Event.ANY)));
                timeline.play();
            }
        });

        // 订阅关闭通知消息事件
        EventBus.getInstance().subscribe(NoticeCloseEvent.class, event -> {
            Notification notification = event.notification();
            rootPane.getChildren().remove(notification);
        });

        // 订阅提示信息事件
        EventBus.getInstance().subscribe(TooltipEvent.class, event -> {
            Platform.runLater(() -> {
                tooltipTimer.stop();
                tooltipTimer.play();
                tooltipBox.getChildren().clear();
                Label label = new Label(event.tooltip());
                tooltipBox.getChildren().add(label);
            });
        });

        // 订阅加载事件
        EventBus.getInstance().subscribe(LoadingEvent.class, event -> {
            loadingBar.setVisible(false);
            if (event.loading()) {
                if (work != event.getWork()) {
                    work = event.getWork();
                    // 任务存在，显示并绑定进度条
                    loadingBar.progressProperty().unbind(); // 先解绑之前的
                    loadingBar.setProgress(0);  // 重置为起始
                    loadingBar.setProgress(-1); // 不可预测模式
//                    loadingBar.progressProperty().bind(work.progressProperty());
                    loadingBar.setVisible(true);

                    // 自动隐藏逻辑（当任务完成/失败/取消）
                    work.setOnSucceeded(e -> Platform.runLater(() -> loadingBar.setVisible(false)));
                    work.setOnFailed(e -> Platform.runLater(() -> loadingBar.setVisible(false)));
                    work.setOnCancelled(e -> Platform.runLater(() -> loadingBar.setVisible(false)));

                } else {
                    // 无具体任务，显示模态加载框
                    loadingBar.setVisible(false);
                    ProgressPane.of(StateStore.stage, "加载中...");
                }

            } else {
                // 通知停止，关闭 loadingBar 和 ProgressPane
                loadingBar.setVisible(false);
                ProgressPane.closeStatic();
            }
//            state.getChildren().clear();
//            if (event.loading()) {
//                state.getChildren().add(progress);
//            } else {
//                state.getChildren().add(right);
//            }
        });

        //切换页面事件
        NodeNoticeEvent.subscribeByKey(PayloadType.HOME_PAGE_CHANGE.name(), event -> {
                // 根据 Tab 的 ID 选择目标 Tab
                Optional.ofNullable(event.getPayload())
                        .map(String.class::cast).flatMap(payload -> homeTab.getTabs().stream()
                                .filter(tab -> payload.equals(tab.getId()))
                                .findFirst()).ifPresent(tab -> homeTab.getSelectionModel().select(tab));

        });

    }

    private void initTheme() {
        EventBus.getInstance().subscribe(ThemeChangeEvent.class, event -> {
            resetThemeIcon();
        });
        themeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        isMoon.addListener((observable, oldValue, newValue) -> {
            changeThemeIcon(newValue);
        });
        resetThemeIcon();
    }

    private void changeThemeIcon(Boolean newValue) {
        if (newValue) {
            themeButton.setGraphic(new FontIcon(FontAwesomeRegular.MOON));
        } else {
            themeButton.setGraphic(new FontIcon(Material2OutlinedMZ.WB_SUNNY));
        }
    }

    private void resetThemeIcon() {
        if (SettingClient.get().getAutoTheme()) {
            ColorScheme scheme = Platform.getPreferences().getColorScheme();
            if (ColorScheme.DARK.equals(scheme)) {
                isMoon.setValue(false);
            } else {
                isMoon.setValue(true);
            }
        } else {
            Themes theme = SettingClient.get().getTheme();
            switch (theme) {
                case nord_dark:
                case primer_dark:
                case cupertino_dark:
                case dracula:
                    isMoon.setValue(false);
                    break;
                case nord_light:
                case primer_light:
                case cupertino_light:
                    isMoon.setValue(true);
                    break;
            }
        }
        changeThemeIcon(isMoon.getValue());
    }

    private void resetTheme() {

    }

    private void openDialog(boolean flag) {
        if(flag){
            if (Boolean.TRUE.equals(SettingClient.get().getOpenDialog())) {
                showConfig();
            }
        }else {
            showConfig();
        }

    }

    private void showConfig() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.3)));
        timeline.setOnFinished(e -> {
            FXMLLoader loader = Resources.getLoader("/gui/es_config.fxml");
            Parent parent = loader.getRoot();
            Stage stage = StageUtils.showNone(parent, SettingClient.bundle().getString("config.title"), StateStore.stage());
            ConfigController controller = loader.getController();
            controller.setHomeController(this);
            controller.setParentStage(stage);
        });
        timeline.play();
    }

    /**
     * 初始化初始化菜单
     */
    private void initMenu() {

        // 定义快捷键和对应的方法
        Map<String, Runnable> actionMap = new HashMap<>();
        actionMap.put("menu.item.new", this::newFile);
        actionMap.put("menu.item.open", this::openFile);
        actionMap.put("menu.item.setting", this::openSettings);
        actionMap.put("menu.item.exit", this::exitApp);
        actionMap.put("menu.item.paste", this::paste);
        actionMap.put("menu.item.copy", this::copy);
        actionMap.put("menu.item.undo", this::undo);
        actionMap.put("menu.item.redo", this::redo);
        actionMap.put("menu.item.cut", this::cut);
        actionMap.put("menu.item.selectAll", this::selectAll);
        actionMap.put("menu.item.reload", this::reload);
        actionMap.put("menu.item.forceReload", this::forceReload);
        actionMap.put("menu.item.max", this::maximizeWindow);
        actionMap.put("menu.item.min", this::minimizeWindow);
        actionMap.put("menu.item.code", this::openCode);
        actionMap.put("menu.item.update", this::checkUpdate);
        actionMap.put("menu.item.feedback", this::sendFeedback);
        actionMap.put("menu.item.about", this::showAbout);

        // 绑定快捷键
        Map<String, String> shortcutMap = new HashMap<>();
        shortcutMap.put("menu.item.new", "Ctrl+N");
        shortcutMap.put("menu.item.open", "Ctrl+O");
        shortcutMap.put("menu.item.setting", "Ctrl+S");
        shortcutMap.put("menu.item.exit", "Ctrl+Q");
        shortcutMap.put("menu.item.paste", "Ctrl+V");
        shortcutMap.put("menu.item.copy", "Ctrl+C");
        shortcutMap.put("menu.item.undo", "Ctrl+Z");
        shortcutMap.put("menu.item.redo", "Ctrl+Y");
        shortcutMap.put("menu.item.cut", "Ctrl+X");
        shortcutMap.put("menu.item.selectAll", "Ctrl+A");
        shortcutMap.put("menu.item.reload", "F5");
        shortcutMap.put("menu.item.forceReload", "Ctrl+F5");
        shortcutMap.put("menu.item.max", "Ctrl+Shift+M");
        shortcutMap.put("menu.item.min", "Ctrl+Shift+N");
        shortcutMap.put("menu.item.code", "Ctrl+Alt+C");
        shortcutMap.put("menu.item.update", "Ctrl+Shift+U");
        shortcutMap.put("menu.item.feedback", "Ctrl+Shift+F");
        shortcutMap.put("menu.item.about", "Ctrl+Shift+A");

        // 遍历 MenuBar 绑定快捷键和事件
        for (Menu menu : menuBar.getMenus()) {
            for (MenuItem item : menu.getItems()) {
                String key = (String) item.getUserData();
                if (actionMap.containsKey(key)) {
                    item.setOnAction(event -> actionMap.get(key).run());
                }
                if (shortcutMap.containsKey(key)) {
                    item.setAccelerator(KeyCombination.keyCombination(shortcutMap.get(key)));
                }
            }
        }
    }

    // 具体的事件处理方法
    private void newFile() {
        openDialog(false);
    }

    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开文件");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {

        }
    }

    private void openSettings() {
        loadSetting();
    }

    private static void loadSetting() {
        FXMLLoader loader = Resources.getLoader("/gui/es_setting.fxml");
        Parent parent = loader.getRoot();
        StageUtils.show(parent, SettingClient.bundle().getString("setting.title"));
    }

    private void exitApp() {

        Platform.exit();
    }

    private void undo() {
        Node focusNode = StateStore.stage().getScene().getFocusOwner();
        if (focusNode instanceof TextInputControl tic) {
            tic.undo();
        } else if (focusNode instanceof StyleClassedTextArea sca) {
            sca.undo();
        }
    }

    private void redo() {
        Node focusNode = StateStore.stage().getScene().getFocusOwner();
        if (focusNode instanceof TextInputControl tic) {
            tic.redo();
        } else if (focusNode instanceof StyleClassedTextArea sca) {
            sca.redo();
        }
    }

    private void cut() {
        Node focusNode = StateStore.stage().getScene().getFocusOwner();
        if (focusNode instanceof TextInputControl tic) {
            tic.cut();
        } else if (focusNode instanceof StyleClassedTextArea sca) {
            sca.cut();
        }
    }

    private void paste() {
        Node focusNode = StateStore.stage().getScene().getFocusOwner();
        if (focusNode instanceof TextInputControl tic) {
            tic.paste();
        } else if (focusNode instanceof StyleClassedTextArea sca) {
            sca.paste();
        }
    }

    private void copy() {
        Node focusNode = StateStore.stage().getScene().getFocusOwner();
        if (focusNode instanceof TextInputControl tic) {
            tic.copy();
        } else if (focusNode instanceof StyleClassedTextArea sca) {
            sca.copy();
        }else if(focusNode instanceof  Labeled lab){
            ClipboardUtils.copy(lab.getText());
        }
    }

    private void selectAll() {
        Node focusNode = StateStore.stage().getScene().getFocusOwner();
        if (focusNode instanceof TextInputControl tic) {
            tic.selectAll();
        } else if (focusNode instanceof StyleClassedTextArea sca) {
            sca.selectAll();
        }
    }


    private void reload() {
        Tab selectedTab = homeTab.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            return;
        }
        loadTabContent(selectedTab); // 简单刷新
    }

    private void forceReload() {

        Tab selectedTab = homeTab.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            return;
        }
        loadTabContent(selectedTab); // 重新加载页面
    }

    private void maximizeWindow() {
        StateStore.stage().setMaximized(true);
    }

    private void minimizeWindow() {
        StateStore.stage().setIconified(true);
    }

    private void openCode() {
        StateStore.hostServices().showDocument("https://gitee.com/lxwise/elastic-desktop-manager");
    }

    private void checkUpdate() {
        try {
            FXUpdater updater = new FXUpdater(ElasticApplication.class);
            updater.checkAppUpdate();
        } catch (IOException e) {
            MessageUtils.error("检查更新失败");
        }
    }

    private void sendFeedback() {
//        MessageUtils.error("发送反馈");
    }

    private void showAbout() {

        FXMLLoader loader = Resources.getLoader("/gui/es_about_me.fxml");
        Parent parent = loader.getRoot();
        Stage stage = StageUtils.none(parent);
        stage.show();

    }

    @FXML
    public void showGift(ActionEvent event) {
        FXMLLoader loader = Resources.getLoader("/gui/es_gifts.fxml");
        ObservableMap<String, Object> namespace = loader.getNamespace();
        ImageView alipayImg = (ImageView) namespace.get("alipayImg");
        ImageView wechatImg = (ImageView) namespace.get("wechatImg");
        FontIcon alipayIcon = (FontIcon) namespace.get("alipayIcon");
        FontIcon wechatIcon = (FontIcon) namespace.get("wechatIcon");
        FontIcon teaIcon = (FontIcon) namespace.get("teaIcon");
        alipayImg.setImage(new Image(Resources.getResourceAllPath("/images/alipay.jpg").toExternalForm(),0,0,true,true));
        wechatImg.setImage(new javafx.scene.image.Image(Resources.getResourceAllPath("/images/wxpay.jpg").toExternalForm(),0,0,true,true));
        alipayIcon.setIconCode(AntDesignIconsFilled.ALIPAY_CIRCLE);
        wechatIcon.setIconCode(AntDesignIconsFilled.WECHAT);
        teaIcon.setIconCode(AntDesignIconsOutlined.COFFEE);
        Parent parent = loader.getRoot();
        Stage stage = StageUtils.none(parent);
        stage.show();
    }

    @FXML
    public void changeTheme(ActionEvent event) {
        isMoon.setValue( !isMoon.getValue());
        playThemeTransition();
        if (Boolean.TRUE.equals(isMoon.getValue())) {
            Application.setUserAgentStylesheet(Themes.primer_light.theme().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(Themes.primer_dark.theme().getUserAgentStylesheet());
        }
    }


    public static void playThemeTransition() {
        if (Platform.isSupported(ConditionalFeature.SHAPE_CLIP)) {
            Point point = MouseInfo.getPointerInfo().getLocation();
            Scene scene = StateStore.stage().getScene();

            // 创建一个圆形裁剪区域
            Circle circle1 = new Circle(point.getX(), point.getY(), 20);
            StateStore.pane().setClip(circle1);

            // 创建一个Timeline，增加圆形扩展的动画
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(circle1.radiusProperty(), Math.max(scene.getWidth(), scene.getHeight()) * 1.2, Interpolator.EASE_BOTH)
                    )
            );

            // 在动画结束时移除裁剪
            tl.setOnFinished(event -> {
                StateStore.pane().setClip(null);
            });

            // 播放动画
            tl.play();
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), StateStore.pane());
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
            // 播放渐变背景过渡动画
            fadeTransition.play();
        }
    }

    @FXML
    public void showSetting(ActionEvent event) {
        loadSetting();
    }

    public void setSelectorBtn(String name){
        textLabel.setText(name);
        graphicBox.getChildren().clear();
        graphicBox.getChildren().addAll(greenCircle, textLabel, arrowIcon);
        selectorBtn.setGraphic(graphicBox);
        homeTab.setVisible(true);

        pagePane.getChildren().clear();
        FXMLLoader loader = Resources.getLoader("/gui/es_cluster_health_monitor.fxml");
        Parent parent = loader.getRoot();
        pagePane.getChildren().addAll(parent);
        homeTab.getSelectionModel().select(0);
    }
    @FXML
    public void changeAboutMe(ActionEvent event) {
        showAbout();
    }

    @FXML
    public void changeOpenCode(ActionEvent event) {
        openCode();
    }
}
