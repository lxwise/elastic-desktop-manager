package com.lxwise.elastic.core.es;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.model.ESShardsModel;
import com.lxwise.elastic.entity.ConfigProperty;
import com.lxwise.elastic.entity.SettingProperty;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @author lstar
 * @create 2025-02
 * @description: es命令管理器
 */
public class ElasticManage {

    private static final Map<String, RestHighLevelClient> CLIENTS = new HashMap<>();
    private static final Map<String, ConfigProperty> PROPERTY = new HashMap<>();

    public static String LATEST_CLUSTER_ID = "";

    public static final String INDICES_FORMAT = "/_cat/indices?format=json&h=index,health,pri,rep,docs.count,status,tm,uuid,store.size,memory.total,creation.date";
    public static final String INDICES_SIMPLE_FORMAT = "/_cat/indices?format=json&h=index,health,status,docs.count";

    private static int DEFAULT_TIMEOUT_MS = 30 * 1000; // 默认超时时间 30 秒
    private static int SQL_TIMEOUT_MS = 2 * 60 * 1000; // SQL 默认超时 2 分钟

    {
        SettingProperty property = SettingClient.get();
        if(ObjectUtil.isNotNull(property)){
         DEFAULT_TIMEOUT_MS = property.getTimeout();
         SQL_TIMEOUT_MS = property.getTimeout();
        }
    }

    /**
     * 连接
     *
     * @param property 属性
     * @return {@link RestHighLevelClient}
     */
    public static RestHighLevelClient connect(ConfigProperty property) throws Exception {
        PROPERTY.put(property.getId(), property);
        RestHighLevelClient client;


        if (CharSequenceUtil.isNotBlank(property.getUsername()) && CharSequenceUtil.isNotBlank(property.getPassword())) {
            // 创建并配置凭证提供者
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(property.getUsername(), property.getPassword()));

            // 使用凭证提供者创建客户端
            client = new RestHighLevelClient(
                    RestClient.builder(HttpHost.create(property.getServers()))
                            .setHttpClientConfigCallback(httpClientBuilder ->
                                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                            )
            );
        } else {
            // 没有提供用户名和密码，直接创建客户端
            client = new RestHighLevelClient(
                    RestClient.builder(HttpHost.create(property.getServers()))
            );
        }
        // 验证是否成功连接 ES
        try {
            health(client);
        } catch (Exception ex) {
            // 必须关闭 client，否则连接泄露
            try {
                client.close();
            } catch (IOException ignored) {}
            throw new RuntimeException("无法连接到 Elasticsearch，请检查地址/端口/权限等配置", ex);
        }
        return client;
    }

    /**
     * 获取集群健康
     * @param client
     * @return
     */
    public static String health(RestHighLevelClient client){
        return executeRequest(new Request("GET", "/_cluster/health"), client);
    }

    /**
     * 获取ES信息
     * @param client
     * @return
     */
    public static String esInfo(RestHighLevelClient client){
        return executeRequest(new Request("GET", "/"), client);
    }

    /**
     * 获取集群节点详细信息
     * @param client
     * @return
     */
    public static String nodesDetails(RestHighLevelClient client){
        return executeRequest(new Request("GET", "/_cat/nodes?format=json&h=id,pid,ip,port,http_address,version,flavor,type,build,jdk,disk.total,disk.used,disk.avail,disk.used_percent,heap.current,heap.percent,heap.max,ram.current,ram.percent,ram.max,file_desc.current,file_desc.percent,file_desc.max,cpu,load_1m,load_5m,load_15m,uptime,node.role,master,name,completion.size,fielddata.memory_size,fielddata.evictions,query_cache.memory_size,query_cache.evictions,request_cache.memory_size,request_cache.evictions,request_cache.hit_count,request_cache.miss_count,flush.total,flush.total_time,get.current,get.time,get.total,get.exists_time,get.exists_total,get.missing_time,get.missing_total,indexing.delete_current,indexing.delete_time,indexing.delete_total,indexing.index_current,indexing.index_time,indexing.index_total,indexing.index_failed,merges.current,merges.current_docs,merges.current_size,merges.total,merges.total_docs,merges.total_size,merges.total_time,refresh.total,refresh.time,refresh.external_total,refresh.external_time,refresh.listeners,script.compilations,script.cache_evictions,script.compilation_limit_triggered,search.fetch_current,search.fetch_time,search.fetch_total,search.open_contexts,search.query_current,search.query_time,search.query_total,search.scroll_current,search.scroll_time,search.scroll_total,segments.count,segments.memory,segments.index_writer_memory,segments.version_map_memory,suggest.current,suggest.time,suggest.total"), client);
    }

    /**
     * 获取ES分片信息
     * @param client
     * @return
     */
    public static String shardsInfo(RestHighLevelClient client){
        return executeRequest(new Request("GET", "/_cat/shards?format=json"), client);
    }

    /**
     * 获取ES索引信息
     * @param client
     * @return
     */
    public static String indicesInfo(RestHighLevelClient client,String format){
        return executeRequest(new Request("GET", format), client);
    }

    /**
     * 获取ES指定索引详情
     * @param client
     * @return
     */
    public static String indicesDetails(RestHighLevelClient client, String indexName){
        return executeRequest(new Request("GET", "/"+indexName), client);
    }
    /**
     * 获取ES指定索引状态
     * @param client
     * @return
     */
    public static String indicesStatsDetails(RestHighLevelClient client, String indexName){
        return executeRequest(new Request("GET", "/"+indexName+"/_stats"), client);
    }

    /**
     * 刷新ES指定索引
     * @param client
     * @return
     */
    public static String indicesRefresh(RestHighLevelClient client, String indexName){
        return executeRequest(new Request("PUT", "/"+indexName+"_refresh"), client);
    }
    /**
     * Flush ES指定索引
     * @param client
     * @return
     */
    public static String indicesFlush(RestHighLevelClient client, String indexName){
        return executeRequest(new Request("PUT", "/"+indexName+"_flush"), client);
    }
    /**
     * 清除 ES指定索引缓存
     * @param client
     * @return
     */
    public static String indicesCleanCache(RestHighLevelClient client, String indexName){
        return executeRequest(new Request("PUT", "/"+indexName+"_clear"), client);
    }

    /**
     * 执行自定义 REST 请求
     *
     * @param client  RestHighLevelClient 客户端
     * @param method  请求方法，如 GET、POST、PUT、DELETE
     * @param url     请求 URL（必须以 / 开头）
     * @param body    请求体内容（可以为 null）
     * @return 响应内容
     */
    public static String executeRest(RestHighLevelClient client, String method, String url, String body) {
        if (!url.startsWith("/")) {
            url = "/" + url;
        }

        Request request = new Request(method.toUpperCase(), url);

        // 设置 JSON 请求体（POST、PUT 等）
        if (CharSequenceUtil.isNotBlank(body)
                && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method))) {
            request.setJsonEntity(body);
        }
        // 设置请求超时
        request.setOptions(buildRequestOptions(DEFAULT_TIMEOUT_MS));
        return executeRequest(request, client);
    }


    /**
     * 根据索引搜索
     * @param client
     * @param indexName
     * @param body
     * @param timeout
     * @return
     */
    public static String searchByIndex(RestHighLevelClient client, String indexName, String body,Integer timeout) {
        Request request = new Request("POST", "/" + indexName + "/_search");
        request.setJsonEntity(body);
        timeout = timeout != null ? timeout : DEFAULT_TIMEOUT_MS;
        request.setOptions(buildRequestOptions(timeout));
        return executeRequest(request, client);
    }

    /**
     * 根据索引删除
     * @param client
     * @param indexName
     * @param body
     * @return
     */
    public static String deleteByQuery(RestHighLevelClient client, String indexName, String body) {
        Request request = new Request("POST", "/" + indexName + "/_delete_by_query");
        request.setJsonEntity(body);
        request.setOptions(buildRequestOptions(DEFAULT_TIMEOUT_MS));
        return executeRequest(request, client);
    }

    /**
     * 执行SQL
     * @param client
     * @param query
     * @param size
     * @return
     */
    public static String executeSql(RestHighLevelClient client, String query, int size) {
        Request request = new Request("POST", "/_sql?format=json");
        String json = String.format("{\"query\":\"%s\",\"fetch_size\":%d}", query.replace("\"", "\\\""), size);
        request.setJsonEntity(json);
        request.setOptions(buildRequestOptions(SQL_TIMEOUT_MS));
        return executeRequest(request, client);
    }

    /**
     * 基于cursor执行下一条sql
     * @param client
     * @param cursor
     * @return
     */
    public static String executeNextSql(RestHighLevelClient client, String cursor) {
        Request request = new Request("POST", "/_sql?format=json");
        String json = String.format("{\"cursor\":\"%s\"}", cursor);
        request.setJsonEntity(json);
        request.setOptions(buildRequestOptions(SQL_TIMEOUT_MS));
        return executeRequest(request, client);
    }

    /**
     * 关闭sql查询
     * @param client
     * @param cursor
     * @return
     */
    public static String executeCloseSql(RestHighLevelClient client, String cursor) {
        Request request = new Request("POST", "/_sql/close");
        String json = String.format("{\"cursor\":\"%s\"}", cursor);
        request.setJsonEntity(json);
        request.setOptions(buildRequestOptions(SQL_TIMEOUT_MS));
        return executeRequest(request, client);
    }

    /**
     * 删除模板
     * @param client
     * @param templateName
     * @return
     */
    public static String deleteTemplate(RestHighLevelClient client, String templateName) {
        Request request = new Request("DELETE", "/_template/" + templateName);
        request.setOptions(buildRequestOptions(DEFAULT_TIMEOUT_MS));
        return executeRequest(request, client);
    }

    /**
     * 列出模板
     * @param client
     * @return
     */
    public static String listTemplates(RestHighLevelClient client) {
        Request request = new Request("GET", "/_template");
        request.setOptions(buildRequestOptions(DEFAULT_TIMEOUT_MS));
        return executeRequest(request, client);
    }

    /**
     * 关闭索引
     * @param client
     * @param indexName
     * @return
     */
    public static String closeIndex(RestHighLevelClient client, String indexName) {
        Request request = new Request("POST", "/" + indexName + "/_close");
        request.setOptions(buildRequestOptions(DEFAULT_TIMEOUT_MS));
        return executeRequest(request, client);
    }

    /**
     * 开启索引
     * @param client
     * @param indexName
     * @return
     */
    public static String openIndex(RestHighLevelClient client, String indexName) {
        Request request = new Request("POST", "/" + indexName + "/_open");
        request.setOptions(buildRequestOptions(DEFAULT_TIMEOUT_MS));
        return executeRequest(request, client);
    }


    /**
     * 删除文档
     * @param client
     * @param index
     * @param type
     * @param id
     * @return
     */
    public static String deleteDocumentById(RestHighLevelClient client, String index, String type, String id) {
        Request request = new Request("DELETE", "/" + index + "/" + type + "/" + id);
        request.setOptions(buildRequestOptions(SQL_TIMEOUT_MS));
        return executeRequest(request, client);
    }


    /***
     * 统一构造超时配置
     * @param timeoutMillis
     * @return
     */
    private static RequestOptions buildRequestOptions(int timeoutMillis) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeoutMillis)
                .setConnectTimeout(timeoutMillis)
                .setConnectionRequestTimeout(timeoutMillis)
                .build();
        return RequestOptions.DEFAULT.toBuilder()
                .setRequestConfig(requestConfig)
                .build();
    }


    /**
     * 执行请求
     * @param request
     * @param client
     * @return
     */
    private static String executeRequest(Request request, RestHighLevelClient client){
        String result = null;
        try {
            Response response = client.getLowLevelClient().performRequest(request);
            result = new String(response.getEntity().getContent().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 获取属性
     *
     * @param clusterId 集群 ID
     * @return {@link ConfigProperty}
     */
    public static ConfigProperty property(String clusterId) {
        return PROPERTY.get(clusterId);
    }

    /**
     * put
     *
     * @param id     编号
     * @param client 客户
     */
    public static void put(String id, RestHighLevelClient client) {
        CLIENTS.put(id, client);
        if(StrUtil.isNotBlank(LATEST_CLUSTER_ID) && !LATEST_CLUSTER_ID.equals(id)){
         remove(LATEST_CLUSTER_ID);
        }
        LATEST_CLUSTER_ID = id;
    }

    /**
     * 获取
     */
    public static RestHighLevelClient get() {
        return CLIENTS.get(LATEST_CLUSTER_ID);
    }
    /**
     * 获取
     *
     * @param id 编号
     */
    public static RestHighLevelClient get(String id) {
        return CLIENTS.get(id);
    }

    /**
     * 删除
     *
     * @param id 编号
     */
    public static void remove(String id) {
        PROPERTY.remove(id);
        Optional.ofNullable(CLIENTS.get(id)).ifPresent(client -> {
            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            CLIENTS.remove(id);
        });
    }

    /**
     * 翻译 错误信息
     *
     * @param throwable 可投掷
     * @return {@link Throwable}
     */
    public static Throwable translate(Throwable throwable) {
        List<Throwable> list = new ArrayList<>();
        Throwable cause = throwable.getCause();
        while (cause != null) {
            list.add(cause);
            cause = cause.getCause();
        }
        for (Throwable t : list) {
            if (t instanceof TimeoutException) {
                return new RuntimeException(SettingClient.bundle().getString("config.connect.timeout"));
            }
        }
        return cause == null ? throwable : cause;
    }
}
