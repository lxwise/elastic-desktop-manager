package com.lxwise.elastic.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author lstar
 * @create 2025-02
 * @description: JavaFX 窗口工具类 提供获取当前激活窗口、场景和舞台的方法
 */
public class StageSceneUtils {

    private static final List<Stage> openStages = new ArrayList<>();

    /**
     * 注册舞台，用于跟踪所有打开的窗口
     * @param stage 要注册的舞台
     */
    public static void registerStage(Stage stage) {
        if (!openStages.contains(stage)) {
            openStages.add(stage);
            stage.setOnCloseRequest(e -> unregisterStage(stage));
        }
    }

    /**
     * 注销舞台
     * @param stage 要注销的舞台
     */
    public static void unregisterStage(Stage stage) {
        openStages.remove(stage);
    }

    /**
     * 获取当前激活的舞台
     * @return 当前激活的Stage对象，如果没有则返回null
     */
    public static Stage getActiveStage() {
        // 方法1：通过焦点窗口获取
        Optional<Stage> focusedStage = openStages.stream()
                .filter(Stage::isFocused)
                .findFirst();

        if (focusedStage.isPresent()) {
            return focusedStage.get();
        }

        // 方法2：通过显示中的窗口获取
        Optional<Stage> showingStage = openStages.stream()
                .filter(Stage::isShowing)
                .findFirst();

        return showingStage.orElse(null);
    }

    /**
     * 获取当前激活的场景
     * @return 当前激活的Scene对象，如果没有则返回null
     */
    public static Scene getActiveScene() {
        Stage activeStage = getActiveStage();
        return (activeStage != null) ? activeStage.getScene() : null;
    }

    /**
     * 通过节点获取所在的舞台
     * @param node 场景中的任意节点
     * @return 节点所在的Stage对象
     */
    public static Stage getStageFromNode(Node node) {
        if (node == null) return null;

        Scene scene = node.getScene();
        if (scene == null) return null;

        Window window = scene.getWindow();
        return (window instanceof Stage) ? (Stage) window : null;
    }

    /**
     * 通过节点获取所在的场景
     * @param node 场景中的任意节点
     * @return 节点所在的Scene对象
     */
    public static Scene getSceneFromNode(Node node) {
        return (node != null) ? node.getScene() : null;
    }

    /**
     * 获取所有打开的舞台列表
     * @return 当前所有打开的Stage列表
     */
    public static List<Stage> getAllOpenStages() {
        return new ArrayList<>(openStages);
    }
}
