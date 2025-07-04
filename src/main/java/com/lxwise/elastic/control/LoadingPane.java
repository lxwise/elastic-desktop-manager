package com.lxwise.elastic.control;

import atlantafx.base.controls.RingProgressIndicator;
import atlantafx.base.theme.Styles;
import com.lxwise.elastic.core.client.SettingClient;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

/**
 * @author lstar
 * @create 2025-02
 * @description: 加载中窗格
 */
public class LoadingPane extends BorderPane {
    public LoadingPane(EventHandler<ActionEvent> value) {
        setCenter(center());
        setBottom(getBottom(value));
        this.setPrefSize(350, 130);
    }

    private static HBox getBottom(EventHandler<ActionEvent> value) {
        HBox bottom = new HBox();
        bottom.setAlignment(Pos.CENTER);
        Button button = new Button(SettingClient.bundle().getString("form.cancel"));
        button.setGraphic(new FontIcon(Material2AL.CANCEL));
        button.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.TEXT);
        button.setOnAction(value);
        bottom.getChildren().add(button);
        return bottom;
    }

    private HBox center() {
        HBox center = new HBox();
        center.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        var ring = new RingProgressIndicator();
        ring.setMinSize(20, 20);
        center.getChildren().addAll(ring, new Label(SettingClient.bundle().getString("form.loading")));
        return center;
    }

    /**
     * 显示“刷新中”动画并在延迟后执行回调（用于刷新场景）
     *
     * @param container   要刷新的内容容器（如 contentPane）
     * @param delayMillis 延迟毫秒数
     * @param onFinish    刷新完成时的处理逻辑（如调用 getShardDataFromES）
     */
    public static void showRefreshLoading(Pane container, int delayMillis, Runnable onFinish) {
        // 构建加载动画视图
        HBox center = new HBox(10);
        center.setAlignment(Pos.CENTER);
        var ring = new RingProgressIndicator();
        ring.setMinSize(20, 20);

        Label label = new Label(SettingClient.bundle().getString("form.loading"));
        center.getChildren().addAll(ring, label);

        // 设置为“刷新中”界面
        container.getChildren().clear();
        container.getChildren().add(new StackPane(center));

        // 延迟执行刷新逻辑
        PauseTransition delay = new PauseTransition(Duration.millis(delayMillis));
        delay.setOnFinished(e -> onFinish.run());
        delay.play();
    }
}
