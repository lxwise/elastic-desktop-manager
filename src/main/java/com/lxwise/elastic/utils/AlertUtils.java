package com.lxwise.elastic.utils;

import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.control.CardHeaderPane;
import com.lxwise.elastic.core.client.SettingClient;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

/**
 * @author lstar
 * @create 2025-02
 * @description: alert 工具类
 */
public class AlertUtils {
    /**
     * 确认
     *
     * @param content 内容
     * @return {@link Optional}<{@link ButtonType}>
     */
    public static Optional<ButtonType> confirm(String content) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Resources.getResource(StateStore.CSS_FILE).toExternalForm());
        CardHeaderPane header = new CardHeaderPane(null, SettingClient.bundle().getString("alert.title"), alert::close);
        AnchorPane box = new AnchorPane(header);
        AnchorPane.setLeftAnchor(header, 10.0);
        AnchorPane.setTopAnchor(header, 5.0);
        AnchorPane.setRightAnchor(header, 0.0);
        AnchorPane.setBottomAnchor(header, 10.0);
        alert.getDialogPane().setHeader(box);
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.setContentText(content);
        alert.initOwner(StateStore.stage());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && ButtonType.OK.equals(result.get())) {
            return Optional.of(ButtonType.OK);
        } else {
            return Optional.empty();
        }
    }

    /**
     * 错误
     *
     * @param parent  父母
     * @param content 内容
     */
    public static void error(Window parent, String content) {
        var alert = new Alert(Alert.AlertType.ERROR,content,ButtonType.OK);
        alert.setTitle(SettingClient.bundle().getString("alert.error.title"));
        alert.setHeaderText(null);
        alert.initOwner(parent);
        alert.showAndWait();
    }

    /**
     * 错误
     *
     * @param parent  父母
     * @param content 内容
     */
    public static void warn(Window parent, String content) {
        var alert = new Alert(Alert.AlertType.WARNING,content,ButtonType.OK);
        alert.setTitle(SettingClient.bundle().getString("alert.warn.title"));
        alert.setHeaderText(null);
        alert.initOwner(parent);
        alert.showAndWait();
    }
    /**
     * 提示信息
     *
     * @param parent  父母
     * @param content 内容
     */
    public static void info(Window parent, String content) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(SettingClient.bundle().getString("alert.title"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(parent);
        alert.showAndWait();
    }
}
