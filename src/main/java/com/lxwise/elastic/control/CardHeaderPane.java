package com.lxwise.elastic.control;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

/**
 * @author lstar
 * @create 2025-02
 * @description: “卡头”窗格
 */
public class CardHeaderPane extends HBox {
    private double x = 0;
    private double y = 0;
    private final Label label = new Label();

    public CardHeaderPane(Stage stage, String title, Runnable runnable) {
        Button close = new Button();
        FontIcon icon = new FontIcon(Material2OutlinedAL.CLOSE);
        close.setGraphic(icon);
        close.getStyleClass().addAll(
                Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.DANGER
        );
        if(stage!=null){
            close.setOnAction(event -> stage.close());
        }
        if(runnable!=null){
            close.setOnAction(event->runnable.run());
        }

        this.label.getStyleClass().add(Styles.TITLE_3);
        this.setAlignment(Pos.CENTER_LEFT);
        HBox center = new HBox();
        center.setAlignment(Pos.CENTER_LEFT);
        HBox right = new HBox();
        right.setAlignment(Pos.CENTER);
        center.getChildren().add(label);
        right.setPrefWidth(30);
        HBox.setHgrow(center, Priority.ALWAYS);
        right.getChildren().add(close);
        this.getStyleClass().add("card-header");
        this.getChildren().addAll(center, right);
        setTitle(title);
        addListener(stage);
    }

    private void addListener(Stage stage) {
        if(stage!=null){
            this.setOnMousePressed(event -> {
                this.x = event.getScreenX() - stage.getX();
                this.y = event.getScreenY() - stage.getY();
            });
            this.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - this.x);
                stage.setY(event.getScreenY() - this.y);
            });
        }
    }

    public void setTitle(String title) {
        this.label.setText(title);
    }


}
