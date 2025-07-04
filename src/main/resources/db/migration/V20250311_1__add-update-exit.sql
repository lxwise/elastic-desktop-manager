alter table es_setting add column autoUpdater bool;
update es_setting set autoUpdater=1;

alter table es_setting add column closeRemember bool;
update es_setting set closeRemember=0;

alter table es_setting add column closeBehavior varchar(32);
update es_setting set closeBehavior='ask';
