package com.lxwise.elastic.core.exception;

import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.gui.ClusterSqlController;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;

/**
 * @author lstar
 * @create 2025-02
 * @description: 默认异常处理程序
 */
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    private final Stage stage;

    public DefaultExceptionHandler(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.info("线程:{},异常:{}", t.getName(), e.getMessage(),e);
        var dialog = createExceptionDialog(e);
        if (dialog != null) {
            dialog.showAndWait();
        }
    }


    private Alert createExceptionDialog(Throwable throwable) {
        Objects.requireNonNull(throwable);
        throwable.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(SettingClient.bundle().getString("alert.error.title"));
        alert.setHeaderText(null);

        // 处理 null 安全
        String message = Optional.ofNullable(throwable.getMessage()).orElse("未知错误");

        // 使用 Label 而非 ContentText，防止撑破窗口
        Label messageLabel = new Label();
        messageLabel.setText(message.length() > 300
                ? message.substring(0, 300) + "..." // 截断太长的 message
                : message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(600); // 限制最大宽度
        alert.getDialogPane().setContent(messageLabel);

        try (StringWriter sw = new StringWriter(); PrintWriter printWriter = new PrintWriter(sw)) {
            throwable.printStackTrace(printWriter);

            Label label = new Label(SettingClient.bundle().getString("alert.error.title.stacktrace"));
            TextArea textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            VBox.setVgrow(textArea, Priority.ALWAYS);

            VBox content = new VBox(5, label, textArea);
            content.setMaxWidth(Double.MAX_VALUE);
            content.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(content, Priority.ALWAYS);

            alert.getDialogPane().setExpandableContent(content);
            alert.getDialogPane().setExpanded(true);

            // 限制弹窗大小，防止 UI 变形
            alert.getDialogPane().setMinWidth(500);
            alert.getDialogPane().setPrefWidth(600);
            alert.getDialogPane().setMaxWidth(800);

            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            alert.initOwner(StateStore.stage);
            return alert;
        } catch (IOException e) {
            return null;
        }
    }



}
