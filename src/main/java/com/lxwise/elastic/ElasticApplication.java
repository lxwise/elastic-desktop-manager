package com.lxwise.elastic;

import com.lxwise.elastic.core.event.EventBus;
import com.lxwise.elastic.core.event.ExitPublishEvent;
import com.lxwise.elastic.core.event.ThemeChangeEvent;
import com.lxwise.elastic.core.exception.DefaultExceptionHandler;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.entity.SettingProperty;
import com.lxwise.elastic.enums.Themes;
import com.lxwise.elastic.store.ThemeManager;
import com.lxwise.elastic.utils.DatasourceUtils;
import com.lxwise.elastic.utils.Resources;
import com.lxwise.elastic.utils.StageUtils;
import com.lxwise.updater.service.FXUpdater;
import com.zaxxer.hikari.HikariDataSource;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

/**
 * @author lstar
 * @create 2025-02
 * @description: 应用启动入口
 */
public class ElasticApplication extends Application {
    private static Logger logger = LoggerFactory.getLogger(ElasticApplication.class);
    private static Label infoLb;

    @Override
    public void init() throws Exception {
        logger.info("启动程序, args:{}", Arrays.toString(StateStore.args()));
        logger.info("启动程序, properties:{}", System.getProperties());
        logger.info("启动程序, env:{}", System.getenv());
        HikariDataSource datasource = DatasourceUtils.getDatasource();
        Flyway flyway = Flyway.configure().dataSource(datasource).load();
        flyway.migrate();
        Platform.Preferences preferences = Platform.getPreferences();
        preferences.colorSchemeProperty().addListener((observable, oldValue, newValue) -> new ThemeChangeEvent().publish());
        SettingClient.get();
        subscribe();
        new ThemeChangeEvent().publish();
    }


    private void subscribe() {
        onThemeChange();
    }

    private static void onThemeChange() {
        EventBus.getInstance().subscribe(ThemeChangeEvent.class, event -> {
            SettingProperty property = SettingClient.get();
            if (property.getAutoTheme()) {
                ColorScheme scheme = Platform.getPreferences().getColorScheme();
                if (ColorScheme.DARK.equals(scheme)) {
                    Application.setUserAgentStylesheet(Themes.primer_dark.theme().getUserAgentStylesheet());
                } else {
                    Application.setUserAgentStylesheet(Themes.primer_light.theme().getUserAgentStylesheet());
                }
            } else {
                Application.setUserAgentStylesheet(property.getTheme().theme().getUserAgentStylesheet());
            }
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        Thread.currentThread().setUncaughtExceptionHandler(new DefaultExceptionHandler(stage));
        //初始化启动页面
        initAppPage(stage);

//        loadMainApp(stage);
    }

    /**
     * 初始化启动页面
     *
     * @param stage
     */
    private void initAppPage(Stage stage) {
        // 1. Logo + 应用名 + 副标题
        ImageView logo = new ImageView(new Image("/images/elasticsearch.png"));
        logo.setFitWidth(100);
        logo.setFitHeight(100);
        logo.setPreserveRatio(true);

        Label appName = new Label("Elastic Desktop Manager");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        appName.setTextFill(Color.web("#2c3e50"));

        Label subTitle = new Label("The is a Powerful ES Visualization Tool");
        subTitle.setFont(Font.font("Segoe UI", 14));
        subTitle.setTextFill(Color.web("#7f8c8d"));

        VBox centerBox = new VBox(logo, appName, subTitle);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(10);

        // 2. 作者信息（左下）
        Label author = new Label("Author：lxwise");
        author.setFont(Font.font("Segoe UI", 13));
        author.setTextFill(Color.web("#555555"));

        Label version = new Label("Version：v1.0.1");
        version.setFont(Font.font("Segoe UI", 13));
        version.setTextFill(Color.web("#555555"));

        // 3. 初始化提示（右下）
        infoLb = new Label("Initializing...");
        infoLb.setFont(Font.font("Segoe UI", 12));
        infoLb.setTextFill(Color.web("#999999"));

        // 4. 底部信息区域
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(10, 20, 10, 20));
        bottomBar.setSpacing(10);
        bottomBar.setPrefHeight(40);
        bottomBar.setStyle("-fx-background-color: transparent;");

        // 作者信息
        VBox authorBox = new VBox(author, version);
        authorBox.setSpacing(2);
        authorBox.setAlignment(Pos.BOTTOM_LEFT);

        // 中间撑开的空间
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 左右对齐
        bottomBar.getChildren().addAll(authorBox, spacer, infoLb);

        // 5. 使用 BorderPane 进行布局
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #fffffc);");
        root.setCenter(centerBox);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 640, 400);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        // 6. 动画：LOGO旋转 + 整体淡入
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), centerBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        RotateTransition rotate = new RotateTransition(Duration.seconds(2), logo);
        rotate.setByAngle(360);

        ParallelTransition animation = new ParallelTransition(fadeIn, rotate);
        animation.setOnFinished(e -> {
            // 动画完成后异步初始化系统配置
            new Thread(() -> {
                initSystem();
                Platform.runLater(() -> {
                    // 切换到主页面
                    loadMainApp(stage);
                });
            }).start();
        });
        animation.play();
    }


    /**
     * 主页面
     *
     * @param splashStage
     */
    private void loadMainApp(Stage splashStage) {
        try {
            FXMLLoader loader = Resources.getLoader("/gui/es_home.fxml");
            AnchorPane root = loader.getRoot();
            StateStore.pane = root;

            Stage homeStage = new Stage();
            Scene scene = new Scene(root);
            homeStage.setScene(scene);
            homeStage.setMinHeight(766);
            homeStage.setMinWidth(1216);
            homeStage.setMaximized(true);
            homeStage.getIcons().add(new Image("/images/elasticsearch.png"));
            homeStage.setTitle("Elastic Desktop Manager");

            splashStage.hide(); // 关闭启动页面
            StateStore.stage = homeStage;
            StateStore.hostServices = getHostServices();

            homeStage.setOnCloseRequest(event -> {
                if (!handleCloseAction(homeStage)) {
                    event.consume(); // 阻止关闭
                }
            });

            homeStage.show();
            homeStage.requestFocus();

            // 设置主题
            ThemeManager.getInstance().setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean handleCloseAction(Stage stage) {
        // 判断是否需要弹窗
        String choice = SettingClient.get().getCloseBehavior(); // "minimize", "exit", or null
        boolean noMorePrompt = SettingClient.get().getCloseRemember();

        if (!noMorePrompt && choice != null) {
            return performCloseAction(stage, choice);
        }

        // 构建自定义确认面板
        Label label = new Label(SettingClient.bundle().getString("exit.tip.info"));
        label.setFont(Font.font("Segoe UI", 17));

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setMargin(label, new Insets(0, 0, 10, 0));
        content.setPrefWidth(500);
        content.setPrefHeight(80);

        ToggleGroup group = new ToggleGroup();
        RadioButton minimizeBtn = new RadioButton(SettingClient.bundle().getString("exit.tip.min"));
        minimizeBtn.setToggleGroup(group);
        minimizeBtn.setSelected(true);

        RadioButton exitBtn = new RadioButton(SettingClient.bundle().getString("exit.tip.exit"));
        exitBtn.setToggleGroup(group);

        CheckBox noPromptCb = new CheckBox(SettingClient.bundle().getString("exit.tip.remind"));
        content.setMargin(noPromptCb, new Insets(10, 0, 0, 0));
        content.getChildren().addAll(label, minimizeBtn, exitBtn, noPromptCb);

        // 调用自定义确认弹窗
        StageUtils.showWithConfirm(content, SettingClient.bundle().getString("exit.tip.exit"), stage).ifPresent(buttonType -> {
            String selected = minimizeBtn.isSelected() ? "minimize" : "exit";
            SettingProperty settingProperty = SettingClient.get();
            settingProperty.closeBehaviorProperty().set(selected);
            if (noPromptCb.isSelected()) {
                settingProperty.closeRememberProperty().set(false);
            }
            // 延迟执行关闭操作，给 debounce 留出时间
            PauseTransition delay = new PauseTransition(Duration.millis(300));
            delay.setOnFinished(event -> performCloseAction(stage, selected));
            delay.play();
        });

        return false; // 主窗口暂不关闭，由具体操作决定
    }

    /**
     * 执行操作
     *
     * @param stage
     * @param action
     * @return
     */

    private boolean performCloseAction(Stage stage, String action) {
        switch (action) {
            case "exit":
                stopApp();
                return true; // 允许关闭
            case "minimize":
            default:
                Platform.runLater(() -> stage.setIconified(true)); // 最小化
                return false; // 不关闭窗口
        }
    }

    private void stopApp() {
        try {
            stop(); // 调用 Application.stop()
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化系统配置
     */
    private void initSystem() {
        try {
            showInfo("初始化目录...");
            Thread.sleep(100);
            showInfo("初始化系统配置...");
            Thread.sleep(100);
            showInfo("版本检测...");
            Boolean autoUpdater = SettingClient.get().getAutoUpdater();
            if (autoUpdater) {
                FXUpdater updater = new FXUpdater(ElasticApplication.class);
                updater.checkAppUpdate();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // 显示信息
    public static void showInfo(String info) {
        Platform.runLater(() -> infoLb.setText(info));
    }

    @Override
    public void stop() throws Exception {
        logger.info("退出程序,释放资源");
        EventBus.getInstance().publish(new ExitPublishEvent());
        SettingClient.debounce().cancel();
        Platform.exit();
        System.exit(0);
    }
}
