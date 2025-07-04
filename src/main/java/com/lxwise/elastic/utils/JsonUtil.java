package com.lxwise.elastic.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.lxwise.elastic.StateStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lstar
 * @create 2025-02
 * @description: json工具类
 */
public class JsonUtil {

    /**
     * 扁平化JSON到Map
     */
    public static Map<String, String> flattenToMap(String jsonString) {
        Map<String, Object> parsed = JSON.parseObject(jsonString,
                new TypeReference<Map<String, Object>>() {});
        Map<String, String> flatMap = new LinkedHashMap<>();
        flatten(parsed, "", flatMap);
        return flatMap;
    }

    private static void flatten(Object obj, String parentKey, Map<String, String> result) {
        if (obj instanceof Map<?, ?>) {
            ((Map<String, Object>) obj).forEach((key, value) -> {
                String newKey = parentKey.isEmpty() ? key : parentKey + "." + key;
                flatten(value, newKey, result);
            });
        } else if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                flatten(list.get(i), parentKey + "[" + i + "]", result);
            }
        } else {
            result.put(parentKey, obj != null ? obj.toString() : "null");
        }
    }

    /**
     * 基础格式化（静默失败）
     */
    public static String formatJson(String json) {
        try {
            return JSON.toJSONString(
                    JSON.parse(json),
                    JSONWriter.Feature.PrettyFormat,
                    JSONWriter.Feature.WriteNulls
            );
        } catch (Exception e) {
            return json;
        }
    }
    /**
     * 基础格式化（静默失败）
     */
    public static String toJSONString(Object object) {
        try {
            return JSON.toJSONString(
                    object,
                    JSONWriter.Feature.PrettyFormat,
                    JSONWriter.Feature.WriteNulls
            );
        } catch (Exception e) {
            AlertUtils.error(StateStore.stage,  e.getMessage());
            return null;
        }
    }

    /**
     * 带错误提示的格式化
     */
    public static String format(String json) {
        try {
            return JSON.toJSONString(
                    JSON.parse(json),
                    JSONWriter.Feature.PrettyFormat,
                    JSONWriter.Feature.MapSortField
            );
        } catch (Exception e) {
            AlertUtils.error(StateStore.stage,  e.getMessage());
            return null;
        }
    }
}