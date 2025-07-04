package com.lxwise.elastic.control;

import cn.hutool.core.util.StrUtil;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Objects;

/**
 * @author lstar
 * @create 2025-02
 * @description: 进度条控件
 */
public class ProgressPane {

    private static Stage stageRoot;
    private static Stage staticStage; // 全局共享的无任务进度框
    private Stage stage;
    private Task<?> work;

    private static PauseTransition timeoutTimer; // 自动关闭定时器


    private ProgressPane() {
    }

    /** 带任务版本 */
    public static ProgressPane of(Stage parent, Task<?> work, String ad) {
        ProgressPane ps = new ProgressPane();
        stageRoot = parent;
        ps.work = Objects.requireNonNull(work);
        ps.initUI(parent, ad, false);
        return ps;
    }

    /** 无任务版本（用于仅显示 Loading 提示） */
    public static void of(Stage parent, String ad) {
        if (staticStage != null && staticStage.isShowing()) {
            return;
        }

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(-1);
        indicator.setStyle("-fx-progress-color: #0969DA");

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setBackground(Background.EMPTY);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(indicator);

        if (StrUtil.isNotBlank(ad)) {
            Label adLbl = new Label(ad);
            adLbl.setTextFill(Color.GRAY);
            vBox.getChildren().add(adLbl);
        }

        Scene scene = new Scene(vBox);
        scene.setFill(null);

        staticStage = new Stage();
        staticStage.setScene(scene);
        staticStage.initOwner(parent);
        staticStage.initModality(Modality.APPLICATION_MODAL);
        staticStage.initStyle(StageStyle.UNDECORATED);
        staticStage.initStyle(StageStyle.TRANSPARENT);

        staticStage.setWidth(ad.length() * 8 + 18);
        staticStage.setHeight(100);

        double x = parent.getX() + (parent.getWidth() - staticStage.getWidth()) / 2;
        double y = parent.getY() + (parent.getHeight() - staticStage.getHeight()) / 2;
        staticStage.setX(x);
        staticStage.setY(y);

        staticStage.show();

        // 启动超时关闭逻辑（默认 10 秒）
        if (timeoutTimer != null) {
            timeoutTimer.stop();
        }
        timeoutTimer = new PauseTransition(Duration.seconds(60));
        timeoutTimer.setOnFinished(e -> {
            if (staticStage != null && staticStage.isShowing()) {
                staticStage.close();
                staticStage = null;
            }
        });
        timeoutTimer.play();
    }


    /** 关闭无任务进度框 */
    public static void closeStatic() {
        if (timeoutTimer != null) {
            timeoutTimer.stop();
            timeoutTimer = null;
        }
        if (staticStage != null) {
            staticStage.close();
            staticStage = null;
        }
    }


    public void show() {
        new Thread(work).start();
        stage.show();
    }

    public void showAndHide() {
        new Thread(work).start();
        stageRoot.hide();
        stage.show();
    }

    private void initUI(Stage parent, String ad, boolean isStatic) {
        stage = new Stage();
        stage.initOwner(parent);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(-1);
        indicator.progressProperty().bind(work.progressProperty());
        indicator.setStyle("-fx-progress-color: #0969DA");

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setBackground(Background.EMPTY);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(indicator);

        if (StrUtil.isNotBlank(ad)) {
            Label adLbl = new Label(ad);
            adLbl.setTextFill(Color.GRAY);
            vBox.getChildren().add(adLbl);
        }

        Scene scene = new Scene(vBox);
        scene.setFill(null);

        stage.setScene(scene);
        stage.setWidth(ad.length() * 8 + 18);
        stage.setHeight(100);

        double x = parent.getX() + (parent.getWidth() - stage.getWidth()) / 2;
        double y = parent.getY() + (parent.getHeight() - stage.getHeight()) / 2;
        stage.setX(x);
        stage.setY(y);

        // 关闭时机
        work.setOnSucceeded(e -> stage.close());
        work.setOnFailed(e -> stage.close());
        work.setOnCancelled(e -> stage.close());
    }
}
