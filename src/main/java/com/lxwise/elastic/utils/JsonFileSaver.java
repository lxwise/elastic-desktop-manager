package com.lxwise.elastic.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.entity.SettingProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author lstar
 * @create 2025-02
 * @description: json/csv 文件保存工具类
 */
public class JsonFileSaver {

    /**
     * 弹出文件选择框，将对象保存为 JSON 文件。如果转换失败，则写入 toString() 文本。
     *
     * @param stage         当前窗口 Stage，用于显示文件选择器
     * @param object        要保存的对象
     * @param defaultFileName 默认的文件名（不含扩展名）
     */
    public static void saveToJsonFile(Stage stage, String object, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();

        SettingProperty property = SettingClient.get();
        if(ObjectUtil.isNotNull(property) && StrUtil.isNotBlank(property.getDownloadFolder()) && !"null".equals(property.getDownloadFolder())){
            fileChooser.setInitialDirectory(new File(property.getDownloadFolder()));
        }
        fileChooser.setTitle(SettingClient.bundle().getString("file.save.json"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON 文件", "*.json"),
                new FileChooser.ExtensionFilter("文本文件", "*.txt"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        fileChooser.setInitialFileName(defaultFileName + ".json");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            writeToFile(file, object);
        }
    }

    /**
     * 直接保存为 JSON 文件（不弹出对话框）。转换失败时写入 toString()。
     *
     * @param object   要保存的对象
     * @param filePath 文件路径
     */
    public static void saveToJsonFileDirectly( String object, String filePath) {
        File file = new File(filePath);
        writeToFile(file, object);
    }

    /**
     * 实际写入文件的方法，尝试转换为 JSON，失败则写入 toString()
     *
     * @param file   要写入的目标文件
     * @param object 要写入的对象
     */
    private static void writeToFile(File file, String object) {
        String content = JsonUtil.formatJson(object);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            MessageUtils.success(SettingClient.bundle().getString("file.save.address")+": " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 下载 TableView 数据为 CSV 文件（支持列名 + 行数据导出）
     * @param stage 当前窗口 Stage，用于显示保存文件对话框
     * @param tableView TableView 控件
     * @param defaultFileName 默认保存文件名（不含扩展名）
     */
    public static void saveTableViewAsCsv(Stage stage, TableView<Map<String, Object>> tableView, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();

        SettingProperty property = SettingClient.get();
        if (ObjectUtil.isNotNull(property) && StrUtil.isNotBlank(property.getDownloadFolder()) && !"null".equals(property.getDownloadFolder())) {
            fileChooser.setInitialDirectory(new File(property.getDownloadFolder()));
        }

        fileChooser.setTitle(SettingClient.bundle().getString("file.save.csv"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV 文件", "*.csv"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        fileChooser.setInitialFileName(defaultFileName + ".csv");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (
                    OutputStreamWriter writer = new OutputStreamWriter(
                            new FileOutputStream(file), StandardCharsets.UTF_8
                    )
            ) {
                // 可选：写入 UTF-8 BOM 头，确保 Windows Excel 打开时中文正常
                writer.write('\uFEFF');

                // 写入列名
                List<TableColumn<Map<String, Object>, ?>> columns = tableView.getColumns();
                for (int i = 0; i < columns.size(); i++) {
                    String header = columns.get(i).getText();
                    writer.write(escapeCsv(header));
                    if (i < columns.size() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");

                // 写入数据行
                ObservableList<Map<String, Object>> items = tableView.getItems();
                for (Map<String, Object> row : items) {
                    for (int i = 0; i < columns.size(); i++) {
                        String key = columns.get(i).getText(); // 这里的 key 可能不是实际 Map 的 key
                        Object value = null;
                        try {
                            value = row.get(key); // 建议统一 key 字段，否则这里可自定义列绑定逻辑
                        } catch (Exception ex) {
                            value = "[ERROR]";
                        }
                        writer.write(escapeCsv(value == null ? "" : value.toString()));
                        if (i < columns.size() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }

                MessageUtils.success(SettingClient.bundle().getString("file.save.address")+": " + file.getAbsolutePath());

            } catch (IOException e) {
                throw new RuntimeException("Unable to write to CSV file: " + file.getAbsolutePath(), e);
            }
        }
    }


    /**
     * 转义 CSV 字段（包含逗号、双引号或换行符时加引号并转义）
     */
    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}