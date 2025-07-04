package com.lxwise.elastic.utils;

import atlantafx.base.controls.Card;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.control.CardHeaderPane;
import com.lxwise.elastic.core.client.SettingClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Optional;

/**
 * @author lstar
 * @create 2025-02
 * @description: Stage 工具类
 */
public class StageUtils {
    /**
     * 显示
     *
     * @param parent 父母
     * @param title  标题
     * @return {@link Stage}
     */
    public static Stage show(Parent parent, String title, Modality modality) {
        Stage stage = new Stage();
        Card card = new Card();
        CardHeaderPane header = new CardHeaderPane(stage,title,null);
        card.setHeader(header);
        card.setBody(parent);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(modality);
        Scene scene = new Scene(card);
        scene.getStylesheets().add(Resources.getResource(StateStore.CSS_FILE).toExternalForm());
        stage.setScene(scene);
        stage.initOwner(StateStore.stage());
        stage.show();
        stage.addEventHandler(KeyEvent.KEY_PRESSED,event->{
            if(KeyCode.ESCAPE==event.getCode()){
                stage.close();
            }
        });
        parent.requestFocus();
        return stage;
    }
    /**
     * 显示
     *
     * @param parent 父母
     * @param title  标题
     * @return {@link Stage}
     */
    public static Stage show(Parent parent, String title, Window window) {
        Stage stage = new Stage();
        Card card = new Card();
        CardHeaderPane header = new CardHeaderPane(stage,title,null);
        card.setHeader(header);
        card.setBody(parent);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(card);
        scene.getStylesheets().add(Resources.getResource(StateStore.CSS_FILE).toExternalForm());
        stage.setScene(scene);
        stage.initOwner(window);
        stage.show();
        stage.addEventHandler(KeyEvent.KEY_PRESSED,event->{
            if(KeyCode.ESCAPE==event.getCode()){
                stage.close();
            }
        });
        parent.requestFocus();
        return stage;
    }
    /**
     * 显示
     *
     * @param parent 父母
     * @param title  标题
     * @return {@link Stage}
     */
    public static Stage showNone(Parent parent, String title, Window window) {
        Stage stage = new Stage();
        Card card = new Card();
        CardHeaderPane header = new CardHeaderPane(stage,title,null);
        card.setHeader(header);
        card.setBody(parent);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.NONE);
        Scene scene = new Scene(card);
        scene.getStylesheets().add(Resources.getResource(StateStore.CSS_FILE).toExternalForm());
        stage.setScene(scene);
        stage.initOwner(window);
        stage.show();
        stage.addEventHandler(KeyEvent.KEY_PRESSED,event->{
            if(KeyCode.ESCAPE==event.getCode()){
                stage.close();
            }
        });
        parent.requestFocus();
        return stage;
    }


    /**
     *
     *
     * @param body   身体
     * @param window 窗
     * @return {@link Stage}
     */
    public static Stage body(Parent body,Window window) {
        Stage stage = new Stage();
        Card card = new Card();
        card.setBody(body);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(card);
        scene.getStylesheets().add(Resources.getResource(StateStore.CSS_FILE).toExternalForm());
        stage.setScene(scene);
        stage.initOwner(window);
        stage.show();
        return stage;
    }
    /**
     * 显示
     *
     * @param parent 父母
     * @param title  标题
     * @return {@link Stage}
     */
    public static Stage show(Parent parent, String title) {
        return show(parent,title,StateStore.stage());
    }

    public static Stage none(Node parent) {
        Stage stage = new Stage();
        Card card = new Card();
        CardHeaderPane header = new CardHeaderPane(stage,null,null);
        card.setHeader(header);
        card.setBody(parent);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(card);
        scene.getStylesheets().add(Resources.getResource(StateStore.CSS_FILE).toExternalForm());
        stage.setScene(scene);
        stage.initOwner(StateStore.stage());
        stage.addEventHandler(KeyEvent.KEY_PRESSED,event->{
            if(KeyCode.ESCAPE==event.getCode()){
                stage.close();
            }
        });
        return stage;
    }

    /**
     * 确认按钮的弹窗
     * @param parent
     * @param title
     * @param window
     * @return
     */
    public static Optional<ButtonType> showWithConfirm(Parent parent, String title, Window window) {
        Stage stage = new Stage();
        Card card = new Card();

        // 顶部标题栏
        CardHeaderPane header = new CardHeaderPane(stage, title, null);
        card.setHeader(header);

        // 底部按钮区域
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(10));
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = new Button(SettingClient.bundle().getString("action.alert.button.confirm"));
        Button cancelButton = new Button(SettingClient.bundle().getString("action.alert.button.cancel"));

        okButton.setDefaultButton(true);    // 回车触发
        cancelButton.setCancelButton(true); // ESC 触发

        footer.getChildren().addAll(cancelButton, okButton);

        // 包装内容区域 + 底部按钮，保持间距
        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(15));
        contentBox.getChildren().addAll(parent, footer);

        card.setBody(contentBox);

        // 创建 Scene，设置样式
        Scene scene = new Scene(card);
        scene.getStylesheets().add(Resources.getResource(StateStore.CSS_FILE).toExternalForm());

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.initOwner(window);

        final ButtonType[] result = {null};

        okButton.setOnAction(e -> {
            result[0] = ButtonType.OK;
            stage.close();
        });

        cancelButton.setOnAction(e -> {
            result[0] = ButtonType.CANCEL;
            stage.close();
        });

        // 支持 ESC 关闭
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                result[0] = ButtonType.CANCEL;
                stage.close();
            }
        });

        parent.requestFocus();
        stage.showAndWait(); // 等待用户操作

        return ButtonType.OK.equals(result[0]) ? Optional.of(ButtonType.OK) : Optional.empty();
    }


}
