package com.lxwise.elastic;

import javafx.application.HostServices;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author lstar
 * @create 2025-02
 * @description: 状态存储
 */
public class StateStore {
    public static String PRODUCT="elastic-desktop-manager";
    public final static String FILE_PATH = "/com/lxwise/elastic";
    public final static String CSS_FILE = "/css/style.css";
    public static HostServices hostServices;
    public static Stage stage;
    public static Pane pane;
    public static String[] args;
    public static Pane pane() {
        return pane;
    }
    public static Stage stage() {
        return stage;
    }

    public static HostServices hostServices(){
        return hostServices;
    }

    public static String[] args() {
        return args;
    }
}
