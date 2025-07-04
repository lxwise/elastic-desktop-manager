package com.lxwise.elastic.store;


import java.util.Random;

/**
 * @author lstar
 * @create 2024-12
 * @description: 应用状态存储
 */
public class AppStore {
    protected static final Random RANDOM = new Random();

    private static String token;

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        AppStore.token = token;
    }

}
