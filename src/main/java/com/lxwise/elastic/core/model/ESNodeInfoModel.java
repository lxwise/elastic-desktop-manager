package com.lxwise.elastic.core.model;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author lstar
 * @create 2025-03
 * @description: 节点信息模型
 */
public class ESNodeInfoModel {
    // 常规字段
    public String name;
    public String httpAddress;
    public String version;
    public String master;
    public String role;
    public String load1m;
    public String load5m;
    public String load15m;
    public String cpu;
    public String ramCurrent;
    public String ramMax;
    public String ramPercent;
    public String heapCurrent;
    public String heapMax;
    public String heapPercent;
    public String diskUsed;
    public String diskTotal;
    public String diskUsedPercent;

    /** 存储完整原始数据 */
    private Map<String, String> rawData = new LinkedHashMap<>();


    /** 简要展示数据，用于表格 */
    public Map<String, String> asMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("名称", name);
        map.put("地址", httpAddress);
        map.put("版本", version);
        map.put("主节点", master);
        map.put("角色", role);
        map.put("负载", load1m + "/" + load5m + "/" + load15m);
        map.put("CPU", cpu + "%");
        map.put("内存", ramCurrent + "/" + ramMax + " (" + ramPercent + "%)");
        map.put("堆内存", heapCurrent + "/" + heapMax + " (" + heapPercent + "%)");
        map.put("磁盘", diskUsed + "/" + diskTotal + " (" + diskUsedPercent + "%)");
        return map;
    }

    public String getName() {
        return name;
    }

    public String getHttpAddress() {
        return httpAddress;
    }

    public String getVersion() {
        return version;
    }

    public String getMaster() {
        return master;
    }

    public String getRole() {
        return role;
    }

    public String getLoad1m() {
        return load1m;
    }

    public String getLoad5m() {
        return load5m;
    }

    public String getLoad15m() {
        return load15m;
    }

    public String getCpu() {
        return cpu;
    }

    public String getRamCurrent() {
        return ramCurrent;
    }

    public String getRamMax() {
        return ramMax;
    }

    public String getRamPercent() {
        return ramPercent;
    }

    public String getHeapCurrent() {
        return heapCurrent;
    }

    public String getHeapMax() {
        return heapMax;
    }

    public String getHeapPercent() {
        return heapPercent;
    }

    public String getDiskUsed() {
        return diskUsed;
    }

    public String getDiskTotal() {
        return diskTotal;
    }

    public String getDiskUsedPercent() {
        return diskUsedPercent;
    }

    public void setRawData(Map<String, String> rawData) {
        this.rawData = rawData;
    }

    public Map<String, String> getRawData() {
        return rawData;
    }
}
