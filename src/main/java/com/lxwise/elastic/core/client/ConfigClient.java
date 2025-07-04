package com.lxwise.elastic.core.client;


import cn.hutool.core.util.IdUtil;
import com.lxwise.elastic.entity.ConfigProperty;
import com.lxwise.elastic.utils.DatasourceUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author lstar
 * @create 2025-02
 * @description: 连接设置sql
 */
public class ConfigClient {
    private static final String INSERT = "insert into es_config (id,name, servers,security,username,password,type,parentId) values ('%s','%s', '%s',%b,'%s','%s','%s','%s')";
    private static final String UPDATE = "update es_config set name = '%s', servers = '%s',security=%b,username='%s',password='%s',type='%s',parentId='%s' where id = '%s'";

    private static final String DELETE = "delete from es_config where id = '%s'";

    private static final String SELECT = "select * from es_config order by name";
    private static final String SELECT_CHILDREN = "select * from es_config where parentId='%s'";


    public static List<ConfigProperty> query4List() {
        return DatasourceUtils.query4List(SELECT, ConfigProperty.class);
    }

    public static void save(ConfigProperty cluster) {
        if (StringUtils.hasText(cluster.getId())) {
            DatasourceUtils.execute(String.format(UPDATE,  cluster.getName(), cluster.getServers(),cluster.getSecurity(),cluster.getUsername(),cluster.getPassword(),
                    cluster.getType(),
                    cluster.getParentId(),
                    cluster.getId()));
        } else {
            cluster.setId(IdUtil.objectId());
            DatasourceUtils.execute(String.format(INSERT, cluster.getId(), cluster.getName(), cluster.getServers(),cluster.getSecurity(),cluster.getUsername(),cluster.getPassword(),cluster.getType(),
                    cluster.getParentId()));
        }
    }

    public static void deleteById(String id) {
        List<ConfigProperty> children = DatasourceUtils.query4List(String.format(SELECT_CHILDREN, id), ConfigProperty.class);
        if(!CollectionUtils.isEmpty(children)){
            for (ConfigProperty child : children) {
                deleteById(child.getId());
            }
        }
        DatasourceUtils.execute(String.format(DELETE, id));
    }
}
