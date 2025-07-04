package com.lxwise.elastic.enums;

/**
 * @author lstar
 * @create 2025-02
 * @description: 组件通知事件 的数据载体类型
 * NodeNoticeEvent
 */
public enum PayloadType {
    /**
     * 集群请求历史订阅
     */
    CLUSTER_REST_HISTORY,
    /**
     * 主页切换订阅
     */
    HOME_PAGE_CHANGE,

    /**
     * 搜索数据初始化订阅
     */
    CLUSTER_SEARCH_DATA_LOADING,
    /**
     * 搜索数据初始化订阅
     */
    CLUSTER_REST_DATA_LOADING,

    ;
}