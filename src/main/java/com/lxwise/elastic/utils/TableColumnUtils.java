package com.lxwise.elastic.utils;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author lstar
 * @create 2025-02
 * @description: 表格列工具类
 */
public class TableColumnUtils {

    /**
     *
     * 根据类的字段自动生成 TableColumn 列表
     *
     * @param clazz              数据模型类
     * @param columnNames        字段名与列名的映射（可选），如果为空则使用字段名
     * @param cellValueFactories 自定义列值工厂映射（可选）
     * @param <T>                泛型类型
     * @return 生成的 TableColumn 列表
     * @param excludedFields     需要过滤的字段列表（可选）
     * @param <T>
     *     Map<String, Callback<TableColumn.CellDataFeatures<EsCommandHistoryProperty, String>, ObservableValue<String>>> cellValueFactories = new HashMap<>();
     * cellValueFactories.put("createTime", param -> {
     *     LocalDate createTime = param.getValue().getCreateTime();
     *     String formattedBirthday = createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
     *     return new SimpleStringProperty(formattedBirthday);
     * });
     *     List<TableColumn<EsCommandHistoryProperty, String>> columns = TableColumnUtils.createTableColumns(
     *     EsCommandHistoryProperty.class,
     *     Map.of("command", "命令", "user", "用户", "createTime", "创建时间"),
     *     cellValueFactories,
     *     column -> {
     *         if ("createTime".equals(column.getText())) {
     *             column.setPrefWidth(50);
     *             column.setSortable(false);
     *         }
     *         return null;
     *     }
     * );
     *
     * tableView.getColumns().addAll(columns);
     */

    public static <T> List<TableColumn<T, String>> createTableColumns(
            Class<T> clazz,
            Map<String, String> columnNames,
            Map<String, Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>>> cellValueFactories,
            Callback<TableColumn<T, String>, Void> columnCustomizer,
            String... excludedFields
    ) {
        List<TableColumn<T, String>> columns = new ArrayList<>();
        List<String> excludedFieldsList = excludedFields != null ? Arrays.asList(excludedFields) : new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();

            // 如果字段在过滤列表中，则跳过
            if (excludedFieldsList.contains(fieldName)) {
                continue;
            }

            String columnName = columnNames != null && columnNames.containsKey(fieldName)
                    ? columnNames.get(fieldName)
                    : capitalize(fieldName);

            TableColumn<T, String> column = new TableColumn<>(columnName);

            if (cellValueFactories != null && cellValueFactories.containsKey(fieldName)) {
                column.setCellValueFactory(cellValueFactories.get(fieldName));
            } else {
                column.setCellValueFactory(param -> {
                    try {
                        Field declaredField = clazz.getDeclaredField(fieldName);
                        declaredField.setAccessible(true);
                        Object value = declaredField.get(param.getValue());

                        // 明确地从 SimpleStringProperty 中提取字符串值
                        if (value instanceof SimpleStringProperty) {
                            return new ReadOnlyStringWrapper(((SimpleStringProperty) value).get());
                        } else if (value != null) {
                            return new ReadOnlyStringWrapper(value.toString());
                        } else {
                            return new ReadOnlyStringWrapper("");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ReadOnlyStringWrapper("");
                    }
                });
            }

            if (columnCustomizer != null) {
                columnCustomizer.call(column);
            }

            columns.add(column);
        }

        return columns;
    }

    /**
     *
     * 根据类的字段自动生成 TableColumn 列表
     *
     * @param clazz              数据模型类
     * @param columnNames        字段名与列名的映射（可选），如果为空则使用字段名
     * @param cellValueFactories 自定义列值工厂映射（可选）
     * @param <T>                泛型类型
     * @return 生成的 TableColumn 列表
     * @param excludedFields     需要过滤的字段列表（可选）
     * @param <T>
     *    // 获取 ResourceBundle
     *   ResourceBundle bundle = ResourceBundle.getBundle("messages"); // 替换为你的国际化资源文件
     *
     *   List<TableColumn<EsCommandHistoryProperty, String>> columns = TableColumnUtils.createTableColumns(
     *   EsCommandHistoryProperty.class,
     *   Map.of(
     *   "id", bundle.getString("config.table.id"),
     *   "method", bundle.getString("config.table.Method"),
     *   "command", "Command",
     *   "createTime", "CreateTime"
     *   ),
     *   null,
     *   column -> {
     *   if (bundle.getString("config.table.Method").equals(column.getText())) {
     *   column.setPrefWidth(80);
     *   column.setSortable(true);
     *   }
     *   return null;
     *   },
     *   bundle, // 传入 ResourceBundle
     *   "commandValue"
     *   );
     *
     * tableView.getColumns().addAll(columns);
     */
    public static <T> List<TableColumn<T, String>> createTableColumns(
            Class<T> clazz,
            Map<String, String> columnNames,
            Map<String, Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>>> cellValueFactories,
            Callback<TableColumn<T, String>, Void> columnCustomizer,
            ResourceBundle bundle,
            String... excludedFields
    ) {
        List<TableColumn<T, String>> columns = new ArrayList<>();
        List<String> excludedFieldsList = excludedFields != null ? Arrays.asList(excludedFields) : new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();

            // 如果字段在过滤列表中，则跳过
            if (excludedFieldsList.contains(fieldName)) {
                continue;
            }

            String columnNameKey = columnNames != null && columnNames.containsKey(fieldName)
                    ? columnNames.get(fieldName)
                    : fieldName;

            String localizedColumnName = bundle != null && bundle.containsKey(columnNameKey)
                    ? bundle.getString(columnNameKey)
                    : capitalize(fieldName);

            TableColumn<T, String> column = new TableColumn<>(localizedColumnName);

            if (cellValueFactories != null && cellValueFactories.containsKey(fieldName)) {
                column.setCellValueFactory(cellValueFactories.get(fieldName));
            } else {
                column.setCellValueFactory(param -> {
                    try {
                        Field declaredField = clazz.getDeclaredField(fieldName);
                        declaredField.setAccessible(true);
                        Object value = declaredField.get(param.getValue());

                        // 明确地从 SimpleStringProperty 中提取字符串值
                        if (value instanceof SimpleStringProperty) {
                            return new ReadOnlyStringWrapper(((SimpleStringProperty) value).get());
                        } else if (value != null) {
                            return new ReadOnlyStringWrapper(value.toString());
                        } else {
                            return new ReadOnlyStringWrapper("");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ReadOnlyStringWrapper("");
                    }
                });
            }

            if (columnCustomizer != null) {
                columnCustomizer.call(column);
            }

            columns.add(column);
        }

        return columns;
    }


    /**
     * 将字符串首字母大写
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
