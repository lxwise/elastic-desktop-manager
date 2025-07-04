package com.lxwise.elastic.core.model;


import com.alibaba.fastjson2.annotation.JSONField;

import java.util.Objects;

/**
 * @author lstar
 * @create 2025-03
 * @description: es索引信息模型
 */
public class ESIndicesModel {

    private String index;
    private String health;
    private String pri;
    private String rep;

    @JSONField(name = "docs.count")
    private String docsCount;

    private String status;
    private String tm;
    private String uuid;

    @JSONField(name = "store.size")
    private String storeSize;

    @JSONField(name = "memory.total")
    private String memoryTotal;

    @JSONField(name = "creation.date")
    private String creationDate;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getPri() {
        return pri;
    }

    public void setPri(String pri) {
        this.pri = pri;
    }

    public String getRep() {
        return rep;
    }

    public void setRep(String rep) {
        this.rep = rep;
    }

    public String getDocsCount() {
        return docsCount;
    }

    public void setDocsCount(String docsCount) {
        this.docsCount = docsCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTm() {
        return tm;
    }

    public void setTm(String tm) {
        this.tm = tm;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStoreSize() {
        return storeSize;
    }

    public void setStoreSize(String storeSize) {
        this.storeSize = storeSize;
    }

    public String getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(String memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ESIndicesModel that = (ESIndicesModel) o;

        if (!Objects.equals(index, that.index)) return false;
        if (!Objects.equals(health, that.health)) return false;
        if (!Objects.equals(pri, that.pri)) return false;
        if (!Objects.equals(rep, that.rep)) return false;
        if (!Objects.equals(docsCount, that.docsCount)) return false;
        if (!Objects.equals(status, that.status)) return false;
        if (!Objects.equals(tm, that.tm)) return false;
        if (!Objects.equals(uuid, that.uuid)) return false;
        if (!Objects.equals(storeSize, that.storeSize)) return false;
        if (!Objects.equals(memoryTotal, that.memoryTotal)) return false;
        return Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        int result = index != null ? index.hashCode() : 0;
        result = 31 * result + (health != null ? health.hashCode() : 0);
        result = 31 * result + (pri != null ? pri.hashCode() : 0);
        result = 31 * result + (rep != null ? rep.hashCode() : 0);
        result = 31 * result + (docsCount != null ? docsCount.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (tm != null ? tm.hashCode() : 0);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + (storeSize != null ? storeSize.hashCode() : 0);
        result = 31 * result + (memoryTotal != null ? memoryTotal.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ESIndicesModel{" +
                "index='" + index + '\'' +
                ", health='" + health + '\'' +
                ", pri='" + pri + '\'' +
                ", rep='" + rep + '\'' +
                ", docsCount='" + docsCount + '\'' +
                ", status='" + status + '\'' +
                ", tm='" + tm + '\'' +
                ", uuid='" + uuid + '\'' +
                ", storeSize='" + storeSize + '\'' +
                ", memoryTotal='" + memoryTotal + '\'' +
                ", creationDate='" + creationDate + '\'' +
                '}';
    }
}
