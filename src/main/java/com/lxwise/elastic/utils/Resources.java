package com.lxwise.elastic.utils;

import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.core.client.SettingClient;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author lstar
 * @create 2025-02
 * @description: 资源工具类
 */
public class Resources {

    private final static Map<Class<?>, Object> CACHE = new ConcurrentHashMap<>();

    public static URL getResource(String name) {
        return Resources.class.getResource(StateStore.FILE_PATH+name);
    }
    public static URL getResourceAllPath(String name) {
        return Resources.class.getResource(name);
    }
    public static InputStream getResourceAsStream(String name) {
        return Resources.class.getResourceAsStream(name);
    }

    /**
     * 获取fxml加载器
     *
     * @param file 文件
     * @return {@link FXMLLoader}
     */
    public static FXMLLoader getLoader(String file) {
        FXMLLoader loader = new FXMLLoader(Resources.getResource(file));
        loadFile(loader);
        return loader;

    }

    private static void loadFile(FXMLLoader loader) {
        loader.setControllerFactory(clazz -> {
            Object o = CACHE.get(clazz);
            if (o == null) {
                try {
                    o = clazz.getDeclaredConstructor().newInstance();
                    CACHE.put(clazz, o);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return o;
        });
        loader.setResources(SettingClient.bundle());
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取fxml加载器
     *
     * @param path 路径
     * @return {@link FXMLLoader}
     */
    public static FXMLLoader getFXMLLoader(String path) {
        FXMLLoader loader = new FXMLLoader(Resources.getResourceAllPath(path));
        loadFile(loader);
        return loader;

    }
}
