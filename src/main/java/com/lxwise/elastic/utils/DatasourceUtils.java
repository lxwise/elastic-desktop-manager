package com.lxwise.elastic.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.sqlite.JDBC;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author lstar
 * @create 2025-02
 * @description: 数据源工具类
 */
public class DatasourceUtils {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceUtils.class);
    public static HikariDataSource DATASOURCE = init();

    private static JdbcTemplate TEMPLATE = new JdbcTemplate(DATASOURCE);

    private static HikariDataSource init() {
        try {
            Class.forName(JDBC.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(JDBC.class.getName());
        File db = FileUtils.file("elastic-desktop-manager.db");
        config.setJdbcUrl("jdbc:sqlite:" + db);
        return new HikariDataSource(config);
    }

    public static HikariDataSource getDatasource() {
        return DATASOURCE;
    }

    public static Connection getConnection() throws SQLException {
        return DATASOURCE.getConnection();
    }

    /**
     * query4 列表
     *
     * @param sql   SQL格式
     * @param clazz 类型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> query4List(String sql, Class<T> clazz) {
        logger.info("查询集合:{}", sql);
        return TEMPLATE.query(sql, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * Query4 对象
     *
     * @param sql   SQL格式
     * @param clazz 克拉兹
     * @return {@link T}
     */
    public static <T> T query4Object(String sql, Class<T> clazz) {
        List<T> result = query4List(sql, clazz);
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return result.getFirst();
    }

    public static void execute(String sql) {
        logger.info("执行sql:{}", sql);
        TEMPLATE.execute(sql);
    }


    public static void execute(String sql, Object... args) {
        logger.info("执行 SQL: {}, 参数: {}", sql, args);
        TEMPLATE.update(sql, args);
    }

    /**
     * 分页查询
     *
     * @param sql      SQL格式
     * @param clazz    类型
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页大小
     * @return {@link List}<{@link T}>
     *
     *  int pageNum = 1; // 页码
     * int pageSize = 10; // 每页大小
     * String sql = "SELECT * FROM command_history WHERE id = 10"; // 你的查询 SQL
     *
     * // 分页查询
     * List<EsCommandHistoryProperty> pageData = DatasourceUtils.query4Page(sql, EsCommandHistoryProperty.class, pageNum, pageSize);
     *
     */
    public static <T> List<T> query4Page(String sql, Class<T> clazz, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String pageSql = sql + " LIMIT " + pageSize + " OFFSET " + offset;
        logger.info("分页查询:{}", pageSql);
        return TEMPLATE.query(pageSql, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 查询总记录数
     *
     * @param sql SQL格式
     * @return int
     *
     * 查询总记录数
     * int totalCount = DatasourceUtils.queryTotalCount(sql);
     */
    public static int queryTotalCount(String sql) {
        String countSql = "SELECT COUNT(*) FROM (" + sql + ")";
        logger.info("查询总数:{}", countSql);
        return TEMPLATE.queryForObject(countSql, Integer.class);
    }
}
