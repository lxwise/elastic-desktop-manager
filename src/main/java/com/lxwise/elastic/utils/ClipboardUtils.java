package com.lxwise.elastic.utils;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * @author lstar
 * @create 2025-02
 * @description: 剪贴板工具类
 */
public class ClipboardUtils {
    /**
     * 复制文本到剪贴板
     * @param text 要复制的文本
     */
    public static void copy(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    /**
     * 从剪贴板获取文本内容
     * @return 剪贴板中的文本内容，若没有内容则返回空字符串
     */
    public static String paste() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            return clipboard.getString();
        }
        return "";  // 如果剪贴板没有内容，则返回空字符串
    }
}
