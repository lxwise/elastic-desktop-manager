CREATE TABLE es_setting
(
    id         varchar(64) primary key,
    language   varchar(32),
    theme      varchar(32),
    timeout    int,
    autoTheme  bool,
    openDialog bool,
    downloadFolder varchar(1024),
    fontFamily varchar(32),
    fontSize int
);

CREATE TABLE es_config
(
    id      varchar(64) primary key,
    name    varchar(128),
    servers varchar(128),
    security bool,
    username varchar(128),
    password varchar(128),
    type varchar(32),
    parentId varchar(64));


-- 创建命令历史表
CREATE TABLE es_command_history
(
    id         varchar(64) primary key,
    method   varchar(32),
    command      varchar(500),
    commandValue TEXT,
    createTime varchar(64)
);

-- 创建触发器
CREATE TRIGGER limit_command_history
    AFTER INSERT ON es_command_history
BEGIN
    DELETE FROM es_command_history
    WHERE id IN (
        SELECT id FROM es_command_history
        ORDER BY createTime DESC
        LIMIT -1 OFFSET 100 -- 100 为最大条数
    );
END;

INSERT INTO es_setting("id", "language", "theme", "timeout", "autoTheme", "openDialog", "downloadFolder", "fontFamily", "fontSize") VALUES ('1', 'zh_cn', 'dracula', 15, 1, 1, NULL, 'Inter', 14);


