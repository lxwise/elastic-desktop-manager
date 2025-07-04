package com.lxwise.elastic.enums;

/**
 * @author lstar
 * @create 2025-02
 * @description: 首页TabPane的标识ID
 *
 */
public enum TabId {
    MAIN("main"),
    NODE("node"),
    SHARDING("sharding"),
    INDEX("index"),
    REST("rest"),
    SQL("sql"),
    SEARCH("search"),
    MORE("more");

    private final String code;

    private TabId(String code){
        this.code = code;
    }

    public String getCode(){
        return code;
    }

}
