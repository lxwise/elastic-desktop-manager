package com.lxwise.elastic.entity;

import com.lxwise.elastic.core.event.ThemeChangeEvent;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.enums.Language;
import com.lxwise.elastic.enums.Themes;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Locale;

/**
 * @author lstar
 * @create 2024-12
 * @description: 设置属性
 */
public class SettingProperty {

    public SettingProperty() {

    }

    public void addListener() {
        this.language.addListener((observable, oldValue, newValue) -> {
            Locale.setDefault(newValue.locale());
            SettingClient.updateLocale();
            SettingClient.update(this);
        });
        this.theme.addListener((observable, oldValue, newValue) -> {
            SettingClient.update(this);
            new ThemeChangeEvent().publish();
        });
        this.timeout.addListener((observable, oldValue, newValue) -> {
            SettingClient.update(this);
        });
        this.autoTheme.addListener((observable, oldValue, newValue) -> {
            SettingClient.update(this);
            new ThemeChangeEvent().publish();
        });
        this.openDialog.addListener((observable, oldValue, newValue) -> {
            SettingClient.update(this);
        });
        this.downloadFolder.addListener((observable, oldValue, newValue) -> {
            SettingClient.update(this);
        });
        this.fontFamily.addListener((observable, oldValue, newValue) -> {
            SettingClient.update(this);
        });
        this.fontSize.addListener((observable, oldValue, newValue) -> {
            SettingClient.update(this);
        });
        this.autoUpdater.addListener((observable, oldValue, newValue) -> {
            SettingClient.update(this);
        });
        this.closeBehavior.addListener((observable, oldValue, newValue) -> {
            System.out.println("更新了"+oldValue+"========"+newValue);
            SettingClient.update(this);
        });
        this.closeRemember.addListener((observable, oldValue, newValue) -> {
            System.out.println("更新了123"+oldValue+"========"+newValue);
            SettingClient.update(this);
        });
    }

    private String id = "1";

    /**
     * 语言
     */
    private final SimpleObjectProperty<Language> language = new SimpleObjectProperty<>(Language.zh_cn);
    /**
     * 主题
     */
    private final SimpleObjectProperty<Themes> theme = new SimpleObjectProperty<>(Themes.primer_light);

    /**
     * 超时时间
     */
    private final SimpleIntegerProperty timeout = new SimpleIntegerProperty(60);

    /**
     * 自动主题
     */
    private final SimpleBooleanProperty autoTheme = new SimpleBooleanProperty(false);
    /**
     * 打开对话框
     */
    private final SimpleBooleanProperty openDialog = new SimpleBooleanProperty(false);
    /**
     * 下载文件夹
     */
    private final SimpleStringProperty downloadFolder = new SimpleStringProperty("");
    /**
     * 字体
     */
    private final SimpleStringProperty fontFamily = new SimpleStringProperty("");

    /**
     * 字体大小
     */
    private final SimpleIntegerProperty fontSize = new SimpleIntegerProperty(14);

    /**
     * 自动更新
     */
    private final SimpleBooleanProperty autoUpdater = new SimpleBooleanProperty(false);

    /**
     * 关闭行为  "minimize" or "exit" or "ask"
     */
    private final SimpleStringProperty closeBehavior = new SimpleStringProperty("");

    /**
     * 关闭记住
     */
    private final SimpleBooleanProperty closeRemember = new SimpleBooleanProperty(false);

    public Language getLanguage() {
        return language.get();
    }

    public void setLanguage(Language language) {
        this.language.set(language);
    }

    public SimpleObjectProperty<Language> language() {
        return this.language;
    }

    public Themes getTheme() {
        return theme.get();
    }

    public void setTheme(Themes theme) {
        this.theme.set(theme);
    }

    public SimpleObjectProperty<Themes> theme() {
        return this.theme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimeout(Integer timeout) {
        this.timeout.set(timeout);
    }

    public Integer getTimeout() {
        return timeout.get();
    }

    public SimpleIntegerProperty timeout() {
        return timeout;
    }


    public void setAutoTheme(Boolean autoTheme) {
        this.autoTheme.set(autoTheme);
    }

    public Boolean getAutoTheme() {
        return autoTheme.get();
    }

    public SimpleBooleanProperty autoTheme() {
        return autoTheme;
    }

    public void setOpenDialog(Boolean openDialog) {
        this.openDialog.set(openDialog);
    }

    public Boolean getOpenDialog() {
        return openDialog.get();
    }

    public SimpleBooleanProperty openDialog() {
        return openDialog;
    }

    public void setDownloadFolder(String downloadFolder) {
        this.downloadFolder.set(downloadFolder);
    }


    public String getDownloadFolder() {
        return downloadFolder.get();
    }

    public SimpleStringProperty downloadFolderProperty() {
        return downloadFolder;
    }


    public String getFontFamily() {
        return fontFamily.get();
    }

    public SimpleStringProperty fontFamilyProperty() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily.set(fontFamily);
    }

    public int getFontSize() {
        return fontSize.get();
    }

    public SimpleIntegerProperty fontSizeProperty() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize.set(fontSize);
    }

    public Boolean getAutoUpdater() {
        return autoUpdater.get();
    }

    public void setAutoUpdater(Boolean autoUpdater) {
        this.autoUpdater.set(autoUpdater);
    }

    public SimpleBooleanProperty autoUpdater() {
        return autoUpdater;
    }

    public String getCloseBehavior() {
        return closeBehavior.get();
    }

    public SimpleStringProperty closeBehaviorProperty() {
        return closeBehavior;
    }

    public void setCloseBehavior(String closeBehavior) {
        this.closeBehavior.set(closeBehavior);
    }

    public Boolean getCloseRemember() {
        return closeRemember.get();
    }

    public SimpleBooleanProperty closeRememberProperty() {
        return closeRemember;
    }

    public void setCloseRemember(Boolean closeRemember) {
        this.closeRemember.set(closeRemember);
    }
}
