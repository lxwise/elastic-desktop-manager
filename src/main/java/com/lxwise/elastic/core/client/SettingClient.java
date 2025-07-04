package com.lxwise.elastic.core.client;

import com.lxwise.elastic.core.thread.Debounce;
import com.lxwise.elastic.entity.SettingProperty;
import com.lxwise.elastic.enums.Language;
import com.lxwise.elastic.enums.Themes;
import com.lxwise.elastic.utils.DatasourceUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author lstar
 * @create 2025-02
 * @description: 应用设置sql
 */
public class SettingClient {
    private final static String INSERT = "insert into es_setting(id,language,theme,timeout,autoTheme,openDialog,downloadFolder,fontFamily,fontSize) values ('1','%s','%s',%d,%b,%b,'%s','%s','%s')";
    private final static String UPDATE = "update es_setting set language='%s',theme='%s',timeout=%d,autoTheme=%b,openDialog=%b,downloadFolder='%s',fontFamily='%s',fontSize='%s',autoUpdater=%b,closeBehavior='%s',closeRemember=%b where id='1'";
    private final static Logger logger = LoggerFactory.getLogger(SettingClient.class);
    private final static Debounce DEBOUNCE = new Debounce(Duration.ofMillis(100));
    public static ObservableList<Themes> THEMES = FXCollections.observableArrayList(
            Themes.primer_light,
            Themes.primer_dark,
            Themes.nord_light,
            Themes.nord_dark,
            Themes.cupertino_light,
            Themes.cupertino_dark,
            Themes.dracula
    );


    private static ResourceBundle RESOURCE_BUNDLE;

    public static ObservableList<Language> LANGUAGES = FXCollections.observableArrayList(
            Language.zh_cn,
            Language.english,
            Language.zh_TW
    );
    private static SettingProperty INSTANCE=null;

    static {
        SettingProperty property = DatasourceUtils.query4Object("select * from es_setting where id='1'", SettingProperty.class);
        if (property == null) {
            property = new SettingProperty();
            DatasourceUtils.execute(String.format(INSERT, property.getLanguage().name(), property.getTheme().name(),property.getTimeout(),property.getAutoTheme(),property.getOpenDialog(),property.getDownloadFolder()));
        }
        INSTANCE = property;
        Locale.setDefault(INSTANCE.getLanguage().locale());
        RESOURCE_BUNDLE = ResourceBundle.getBundle("messages", Locale.getDefault());
        INSTANCE.addListener();
    }

    public static void updateLocale() {
        RESOURCE_BUNDLE = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    public static ResourceBundle bundle() {
        return RESOURCE_BUNDLE;
    }

    public static Debounce debounce() {
        return DEBOUNCE;
    }

    /**
     * 写
     *
     * @param setting 设置
     */
    public static void update(SettingProperty setting) {
        DEBOUNCE.execute(() -> {
            String language = setting.getLanguage().name();
            String theme = setting.getTheme().name();
            DatasourceUtils.execute(String.format(UPDATE, language, theme,setting.getTimeout(),setting.getAutoTheme(),setting.getOpenDialog(),setting.getDownloadFolder(),setting.getFontFamily(),setting.getFontSize(),setting.getAutoUpdater(),setting.getCloseBehavior(),setting.getCloseRemember()));
            logger.info("更新配置:{}",setting);
        });
    }

    public static SettingProperty get() {
        return INSTANCE;
    }
}
