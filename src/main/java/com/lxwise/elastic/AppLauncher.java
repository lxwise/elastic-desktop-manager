package com.lxwise.elastic;

import com.podigua.path.Paths;
import javafx.application.Application;

/**
 * @author lstar
 * @create 2025-02
 * @description: 应用启动器
 */
public class AppLauncher
{
    public static void main( String[] args )
    {
        Paths.identifier(StateStore.PRODUCT);
        System.setProperty("LOG_PATH", Paths.appLog());
        //// 禁用LCD渲染
        System.setProperty("prism.lcdtext", "false");
        Application.launch(ElasticApplication.class, args);
    }
}
