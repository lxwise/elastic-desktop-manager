package com.lxwise.elastic;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen extends Application {
    private static Label infoLb;
    @Override
    public void start(Stage stage) {
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
        Label author = new Label("作者：lxwise");
        author.setFont(Font.font("Segoe UI", 14));
        author.setTextFill(Color.web("#999999"));

        Label version = new Label("版本：V1.0.1");
        version.setFont(Font.font("Segoe UI", 13));
        version.setTextFill(Color.web("#999999"));

        VBox authorBox = new VBox(author, version);
        authorBox.setSpacing(2);
        authorBox.setAlignment(Pos.BOTTOM_LEFT);

        // 3. 初始化提示（右下）
        infoLb = new Label("正在初始化...");
        infoLb.setFont(Font.font("Segoe UI", 12));
        infoLb.setTextFill(Color.web("#999999"));

        // 4. 底部栏（HBox 左右分布）
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(10, 20, 10, 20));
        bottomBar.setSpacing(10);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPrefHeight(40);
        bottomBar.setStyle("-fx-background-color: transparent;");
        HBox.setHgrow(authorBox, Priority.ALWAYS);
        HBox.setHgrow(infoLb, Priority.ALWAYS);

        bottomBar.getChildren().addAll(authorBox, new Pane(), infoLb); // 中间用 Pane 分隔撑开

        // 5. 主布局使用 BorderPane
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f7f9fb, #e0e4ec);");
        root.setCenter(centerBox);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 640, 400);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        // 动画：淡入 + 旋转
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), centerBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        RotateTransition rotate = new RotateTransition(Duration.seconds(2), logo);
        rotate.setByAngle(360);

        ParallelTransition animation = new ParallelTransition(fadeIn, rotate);
        animation.setOnFinished(e -> {
            new Thread(() -> {
                initSystem();
//                Platform.runLater(stage::close);
//                launchMainApp();
            }).start();
        });
        animation.play();
    }

    private void initSystem() {
        try {
            showInfo("初始化目录...");
            Thread.sleep(1000);
            showInfo("初始化配置...");
            Thread.sleep(1000);
            showInfo("检测版本...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void showInfo(String msg) {
        Platform.runLater(() -> infoLb.setText(msg));
    }

    private void launchMainApp() {
        try {
            Stage mainStage = new Stage();
            StackPane mainRoot = new StackPane(new Label("欢迎使用主程序"));
            Scene mainScene = new Scene(mainRoot, 800, 600);
            mainStage.setScene(mainScene);
            mainStage.setTitle("Elastic Desktop Manager");
            mainStage.getIcons().add(new Image("/images/elasticsearch.png"));
            mainStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
