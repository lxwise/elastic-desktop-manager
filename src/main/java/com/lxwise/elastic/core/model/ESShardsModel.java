package com.lxwise.elastic.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lstar
 * @create 2025-03
 * @description: es分片模型
 */
public class ESShardsModel {

    private String index;
    private String shard;
    private String prirep;
    private String state;
    private String docs;
    private String store;
    private String ip;
    private String node;

    private List<ShardType> types = new ArrayList<>();


    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getShard() {
        return shard;
    }

    public void setShard(String shard) {
        this.shard = shard;
    }

    public String getPrirep() {
        return prirep;
    }

    public void setPrirep(String prirep) {
        this.prirep = prirep;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDocs() {
        return docs;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public List<ShardType> getTypes() {
        return types;
    }

    public void setTypes(List<ShardType> types) {
        this.types = types;
    }

    public static class ShardType {
        private String prirep; // p 或 r
        private String shard;
        private String state;

        public String getPrirep() {
            return prirep;
        }

        public void setPrirep(String prirep) {
            this.prirep = prirep;
        }

        public String getShard() {
            return shard;
        }

        public void setShard(String shard) {
            this.shard = shard;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
