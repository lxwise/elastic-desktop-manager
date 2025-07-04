package com.lxwise.elastic.utils;

import com.lxwise.elastic.core.model.KeyValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * @author lstar
 * @create 2025-02
 * @description: Elasticsearch查询条件构建器工具类

 * 用于将UI中的查询条件转换为Elasticsearch查询JSON结构
 */
public class ESQueryBuilder {

    /**
     * 构建完整的Elasticsearch查询
     *
     * @param conditionsContainer 条件查询容器
     * @param aggregationsContainer 聚合查询容器
     * @param timeoutSeconds 超时时间（秒）
     * @param trackTotalHits 是否跟踪总命中数
     * @param isAggregateQuery 是否为聚合查询
     * @return 完整的ES查询Map结构
     */
    public static Map<String, Object> buildElasticsearchQuery(
            VBox conditionsContainer,
            VBox aggregationsContainer,
            int timeoutSeconds,
            boolean trackTotalHits,
            boolean isAggregateQuery
    ) {
        Map<String, Object> queryMap = new LinkedHashMap<>();

        // 构建bool查询主体
        Map<String, Object> boolMap = new LinkedHashMap<>();
        processConditionsContainer(conditionsContainer, boolMap);

        // 添加外层query
        Map<String, Object> queryBody = new LinkedHashMap<>();
        queryBody.put("bool", boolMap);
        queryMap.put("query", queryBody);

        // 添加聚合查询
        if (isAggregateQuery) {
            Map<String, Object> aggregations = buildAggregations(aggregationsContainer);
            if (!aggregations.isEmpty()) {
                queryMap.put("aggregations", aggregations);
            }
        }

        // 添加其他参数
        queryMap.put("timeout", timeoutSeconds + "s");
        queryMap.put("track_total_hits", trackTotalHits ? true : 2147483647);
        queryMap.put("from", 0);
        queryMap.put("size", isAggregateQuery ? 0 : 15); // 聚合查询时不返回原始文档

        return queryMap;
    }
    /**
     * 构建不带聚合的Elasticsearch查询
     *
     * @param conditionsContainer 条件查询容器
     * @return 完整的ES查询Map结构
     */
    public static Map<String, Object> buildElasticsearchEasyQuery(VBox conditionsContainer) {
        Map<String, Object> queryMap = new LinkedHashMap<>();

        // 构建bool查询主体
        Map<String, Object> boolMap = new LinkedHashMap<>();
        processConditionsContainer(conditionsContainer, boolMap);

        // 添加外层query
        Map<String, Object> queryBody = new LinkedHashMap<>();
        queryBody.put("bool", boolMap);
        queryMap.put("query", queryBody);

        // 添加其他参数
        return queryMap;
    }


    /**
     * 递归处理条件容器，构建bool查询结构
     *
     * @param container 当前处理的容器（VBox）
     * @param parentBool 父级bool查询Map
     */
    private static void processConditionsContainer(
            VBox container,
            Map<String, Object> parentBool
    ) {
        // 初始化三个子句列表
        List<Object> mustList = new ArrayList<>();
        List<Object> mustNotList = new ArrayList<>();
        List<Object> shouldList = new ArrayList<>();

        // 遍历容器中的所有条件行
        for (Node node : container.getChildren()) {
            if (node instanceof VBox) {
                VBox wrapper = (VBox) node;
                if (wrapper.getChildren().isEmpty()) continue;

                HBox conditionRow = (HBox) wrapper.getChildren().get(0);
                VBox childrenContainer = wrapper.getChildren().size() > 1 ?
                        (VBox) wrapper.getChildren().get(1) : null;

                // 检查是否启用
                CheckBox enabledCheckBox = (CheckBox) conditionRow.getChildren().get(1);
                if (!enabledCheckBox.isSelected()) continue;

                // 获取连接类型
                ComboBox<String> splicerComboBox = (ComboBox<String>) conditionRow.getChildren().get(3);
                String splicer = splicerComboBox.getValue();

                // 检查是否为条件组
                CheckBox groupCheckBox = (CheckBox) conditionRow.getChildren().get(2);
                if (groupCheckBox.isSelected() && childrenContainer != null) {
                    // 创建嵌套的bool查询
                    Map<String, Object> nestedBool = new LinkedHashMap<>();
                    processConditionsContainer(childrenContainer, nestedBool);

                    // 添加到对应的子句列表
                    Map<String, Object> nestedQuery = Collections.singletonMap("bool", nestedBool);
                    addToClauseList(splicer, nestedQuery, mustList, mustNotList, shouldList);
                } else {
                    // 构建叶子查询
                    Map<String, Object> leafQuery = buildLeafQuery(conditionRow);
                    if (leafQuery != null) {
                        addToClauseList(splicer, leafQuery, mustList, mustNotList, shouldList);
                    }
                }
            }
        }

        // 将收集到的子句添加到父bool查询
        if (!mustList.isEmpty()) parentBool.put("must", mustList);
        if (!mustNotList.isEmpty()) parentBool.put("must_not", mustNotList);
        if (!shouldList.isEmpty()) parentBool.put("should", shouldList);
    }

    /**
     * 将查询添加到对应的子句列表
     * 
     * @param splicer 连接类型（must/must_not/should）
     * @param query 查询条件
     * @param mustList must子句列表
     * @param mustNotList must_not子句列表
     * @param shouldList should子句列表
     */
    private static void addToClauseList(
        String splicer, 
        Map<String, Object> query, 
        List<Object> mustList, 
        List<Object> mustNotList, 
        List<Object> shouldList
    ) {
        switch (splicer) {
            case "must":
                mustList.add(query);
                break;
            case "must_not":
                mustNotList.add(query);
                break;
            case "should":
                shouldList.add(query);
                break;
            default:
                mustList.add(query); // 默认添加到must
        }
    }

    /**
     * 构建叶子节点的查询条件
     * 
     * @param conditionRow 条件行（HBox）
     * @return 查询条件Map，如果条件无效返回null
    支持查询类型：
    term/wildcard/match：单值查询
    terms：多值查询（逗号分隔）
    range：范围查询（gt/gte/lt/lte）
    exists：存在性查询
     */
    private static Map<String, Object> buildLeafQuery(HBox conditionRow) {
        ComboBox<String> fieldComboBox = (ComboBox<String>) conditionRow.getChildren().get(4);
        ComboBox<String> operatorComboBox = (ComboBox<String>) conditionRow.getChildren().get(5);
        
        String field = fieldComboBox.getValue();
        String operator = operatorComboBox.getValue();
        
        // 验证字段和操作符
        if (field == null || field.isEmpty() || operator == null) {
            return null;
        }
        
        Map<String, Object> query = new LinkedHashMap<>();
        
        switch (operator) {
            case "term":
            case "wildcard":
            case "match":
                // 查找值文本框
                TextField valueField = findValueTextField(conditionRow);
                if (valueField != null) {
                    String value = valueField.getText().trim();
                    if (!value.isEmpty()) {
                        query.put(operator, Collections.singletonMap(field, value));
                    }
                }
                break;
                
            case "terms":
                TextField termsField = findValueTextField(conditionRow);
                if (termsField != null) {
                    String values = termsField.getText().trim();
                    if (!values.isEmpty()) {
                        List<String> valueList = Arrays.asList(values.split("\\s*,\\s*"));
                        query.put("terms", Collections.singletonMap(field, valueList));
                    }
                }
                break;
                
            case "range":
                Map<String, String> rangeMap = new LinkedHashMap<>();
                // 获取范围查询控件
                ComboBox<String> leftOpCombo = (ComboBox<String>) conditionRow.getChildren().get(6);
                TextField leftValueField = (TextField) conditionRow.getChildren().get(7);
                ComboBox<String> rightOpCombo = (ComboBox<String>) conditionRow.getChildren().get(8);
                TextField rightValueField = (TextField) conditionRow.getChildren().get(9);
                
                String leftValue = leftValueField.getText().trim();
                String rightValue = rightValueField.getText().trim();
                
                // 添加有效的范围条件
                if (!leftValue.isEmpty()) {
                    rangeMap.put(leftOpCombo.getValue(), leftValue);
                }
                if (!rightValue.isEmpty()) {
                    rangeMap.put(rightOpCombo.getValue(), rightValue);
                }
                
                if (!rangeMap.isEmpty()) {
                    query.put("range", Collections.singletonMap(field, rangeMap));
                }
                break;
                
            case "exists":
                // 存在性查询不需要值
                query.put("exists", Collections.singletonMap("field", field));
                break;
        }
        
        // 返回有效的查询，否则返回null
        return !query.isEmpty() ? query : null;
    }

    /**
     * 在条件行中查找值文本框
     * 
     * @param conditionRow 条件行（HBox）
     * @return 找到的TextField，如果不存在返回null
     */
    private static TextField findValueTextField(HBox conditionRow) {
        // 从索引6开始查找文本框
        for (int i = 6; i < conditionRow.getChildren().size(); i++) {
            javafx.scene.Node node = conditionRow.getChildren().get(i);
            if (node instanceof TextField) {
                return (TextField) node;
            }
        }
        return null;
    }


    /**
     * 递归构建聚合查询结构
     *
     * @param container 聚合查询容器
     * @return 聚合查询Map结构
     */
    private static Map<String, Object> buildAggregations(VBox container) {
        Map<String, Object> aggregations = new LinkedHashMap<>();

        for (Node node : container.getChildren()) {
            if (node instanceof VBox) {
                VBox wrapper = (VBox) node;
                if (wrapper.getChildren().isEmpty()) continue;

                HBox aggregationRow = (HBox) wrapper.getChildren().get(0);
                VBox childrenContainer = wrapper.getChildren().size() > 1 ?
                        (VBox) wrapper.getChildren().get(1) : null;

                // 获取聚合控件
                ComboBox<String> aggComboBox = (ComboBox<String>) aggregationRow.getChildren().get(1);
                ComboBox<String> fieldComboBox = (ComboBox<String>) aggregationRow.getChildren().get(2);
                TextField aliasTextField = (TextField) aggregationRow.getChildren().get(3);

                String aggType = aggComboBox.getValue();
                String field = fieldComboBox.getValue();
                String alias = aliasTextField.getText().trim();

                // 确保别名有效
                if (alias.isEmpty()) {
                    alias = field + "_" + aggType;
                }

                // 构建当前聚合
                Map<String, Object> aggregation = new LinkedHashMap<>();
                Map<String, Object> aggConfig = new LinkedHashMap<>();

                // 设置基本字段
                aggConfig.put("field", field);

                // 根据聚合类型添加特定配置
                switch (aggType) {
                    case "terms":
                        // 查找数量Spinner
                        Spinner<Integer> sizeSpinner = findSpinner(aggregationRow);
                        if (sizeSpinner != null) {
                            aggConfig.put("size", sizeSpinner.getValue());
                        } else {
                            aggConfig.put("size", 100); // 默认值
                        }
                        aggConfig.put("min_doc_count", 1);
                        aggConfig.put("shard_min_doc_count", 0);
                        aggConfig.put("show_term_doc_count_error", false);
                        break;

                    case "date_histogram":
                        // 查找间隔Spinner和单位ComboBox
                        Spinner<Integer> intervalSpinner = findSpinner(aggregationRow);
                        ComboBox<KeyValue> unitComboBox = findKeyValueComboBox(aggregationRow);

                        if (intervalSpinner != null && unitComboBox != null) {
                            KeyValue selectedUnit = unitComboBox.getValue();
                            aggConfig.put("interval", intervalSpinner.getValue() + selectedUnit.getKey());
                        } else {
                            aggConfig.put("interval", "1d"); // 默认值
                        }
                        break;
                }

                aggregation.put(aggType, aggConfig);

                // 递归处理子聚合
                if (childrenContainer != null && !childrenContainer.getChildren().isEmpty()) {
                    Map<String, Object> subAggregations = buildAggregations(childrenContainer);
                    if (!subAggregations.isEmpty()) {
                        aggregation.put("aggregations", subAggregations);
                    }
                }

                aggregations.put(alias, aggregation);
            }
        }

        return aggregations;
    }

    /**
     * 在聚合行中查找Spinner控件
     *
     * @param aggregationRow 聚合行（HBox）
     * @return 找到的Spinner，如果不存在返回null
     */
    private static Spinner<Integer> findSpinner(HBox aggregationRow) {
        for (int i = 4; i < aggregationRow.getChildren().size(); i++) {
            Node node = aggregationRow.getChildren().get(i);
            if (node instanceof Spinner) {
                return (Spinner<Integer>) node;
            }
        }
        return null;
    }

    /**
     * 在聚合行中查找KeyValue类型的ComboBox
     *
     * @param aggregationRow 聚合行（HBox）
     * @return 找到的ComboBox，如果不存在返回null
     */
    private static ComboBox<KeyValue> findKeyValueComboBox(HBox aggregationRow) {
        for (int i = 4; i < aggregationRow.getChildren().size(); i++) {
            Node node = aggregationRow.getChildren().get(i);
            if (node instanceof ComboBox) {
                ComboBox<?> comboBox = (ComboBox<?>) node;
                if (!comboBox.getItems().isEmpty() && comboBox.getItems().get(0) instanceof KeyValue) {
                    return (ComboBox<KeyValue>) comboBox;
                }
            }
        }
        return null;
    }
}