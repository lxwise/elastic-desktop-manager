package com.lxwise.elastic.utils;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.core.event.NoticeCloseEvent;
import com.lxwise.elastic.core.event.NoticeEvent;
import com.lxwise.elastic.enums.PopupPosition;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

/**
 * @author lstar
 * @create 2025-02
 * @description: Message 工具类
 */
public class MessageUtils {

    /**
     * 弹窗宽度
     */
    private final static double WIDTH = 350;

    /**
     * 默认显示时长
     */
    private final static Duration DEFAULT_DURATION = Duration.seconds(3);

    /**
     * 显示成功消息
     * @param message
     */
    public static void success(String message) {
        success(message, DEFAULT_DURATION, PopupPosition.TOP_CENTER, null);
    }

    /**
     *  显示成功消息
     * @param message
     * @param duration 显示时长
     * @param buttons
     */
    public static void success(String message, Duration duration, Button... buttons) {
        success(message, duration, PopupPosition.TOP_CENTER, buttons);
    }

    /**
     * 显示成功消息
     * @param message
     * @param duration 显示时长
     * @param position 显示位置
     * @param buttons
     */
    public static void success(String message, Duration duration, PopupPosition position, Button... buttons) {
        Notification notice = create(message, Styles.SUCCESS, new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE), buttons);
        showNotification(notice, duration, position);
    }

    /**
     * 显示错误消息
     * @param message
     */
    public static void error(String message) {
        error(message, DEFAULT_DURATION, PopupPosition.TOP_CENTER);
    }

    /**
     * 显示错误消息
     * @param message
     * @param duration 显示时长
     * @param position 显示位置
     */

    public static void error(String message, Duration duration, PopupPosition position) {
        Notification notice = create(message, Styles.DANGER, new FontIcon(Material2OutlinedAL.ERROR));
        showNotification(notice, duration, position);
    }

    /**
     * 显示警告消息
     * @param message
     */

    public static void warning(String message) {
        warning(message, DEFAULT_DURATION, PopupPosition.TOP_CENTER);
    }

    /**
     * 显示警告消息
     * @param message 显示消息
     * @param duration 显示时长
     * @param position 显示位置
     */

    public static void warning(String message, Duration duration, PopupPosition position) {
        Notification notice = create(message, Styles.WARNING, new FontIcon(Material2OutlinedAL.ASSIGNMENT));
        showNotification(notice, duration, position);
    }

    /**
     * 显示信息消息
     * @param message
     */
    public static void info(String message) {
        info(message, DEFAULT_DURATION, PopupPosition.TOP_CENTER);
    }

    /**
     * 显示信息消息
     * @param message
     * @param duration 显示时长
     * @param position 显示位置
     */
    public static void info(String message, Duration duration, PopupPosition position) {
        Notification notice = create(message, Styles.ACCENT, new FontIcon(Material2OutlinedAL.HELP_OUTLINE));
        showNotification(notice, duration, position);
    }

    /**
     * 创建通知
     * @param message
     * @param style
     * @param icon
     * @param buttons
     * @return
     */
    private static Notification create(String message, String style, FontIcon icon, Button... buttons) {
        final var notice = new Notification(message, icon);
        notice.setPrefWidth(WIDTH);
        notice.getStyleClass().addAll(style, Styles.ELEVATED_1);
        if (buttons != null && buttons.length > 0) {
            notice.setPrimaryActions(buttons);
        }
        return notice;
    }

    /**
     * 显示通知
     * @param notice
     * @param duration
     * @param position
     */
    private static void showNotification(Notification notice, Duration duration, PopupPosition position) {
        switch (position) {
            case RIGHT_TOP -> {
                AnchorPane.setTopAnchor(notice, 5.0);
                AnchorPane.setRightAnchor(notice, 5.0);
                notice.setOnClose(e -> {
                    var out = Animations.slideOutRight(notice, Duration.millis(300));
                    out.playFromStart();
                    out.setOnFinished(event -> new NoticeCloseEvent(notice).publish());
                });
            }
            case TOP_CENTER -> {
                AnchorPane.setTopAnchor(notice, 5.0);
                AnchorPane.setLeftAnchor(notice, (StateStore.stage().getWidth() - WIDTH) / 2); // 水平居中
                notice.setOnClose(e -> {
                    var out = Animations.fadeOut(notice, Duration.millis(300)); // 顶部不适合 slide
                    out.playFromStart();
                    out.setOnFinished(event -> new NoticeCloseEvent(notice).publish());
                });
            }
        }

        new NoticeEvent(notice).duration(duration).publish();
    }
}
