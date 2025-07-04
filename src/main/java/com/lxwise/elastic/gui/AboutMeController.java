package com.lxwise.elastic.gui;

import com.lxwise.elastic.StateStore;
import com.lxwise.elastic.core.client.SettingClient;
import com.lxwise.elastic.core.model.ProjectInfoModel;
import com.lxwise.elastic.utils.ClipboardUtils;
import com.lxwise.elastic.utils.MessageUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ResourceBundle;

/**
 * @author lstar
 * @create 2025-03
 * @description: 关于我
 */
public class AboutMeController {
    private static Logger logger = LoggerFactory.getLogger(AboutMeController.class);

    @FXML private ImageView avatarImageView;
    @FXML private Label nameLabel;
    @FXML private Label titleLabel;
    @FXML private Hyperlink githubLink;
    @FXML private Hyperlink giteeLink;
    @FXML private Hyperlink csdnLink;
    @FXML private Hyperlink qqLink;
    @FXML public Hyperlink emailLink;
    @FXML public Hyperlink wechatLink;
    @FXML private VBox projectListBox;
    @FXML
    public void initialize() {
        nameLabel.setText("lxwise");
        titleLabel.setText(SettingClient.bundle().getString("about.me.author"));
        titleLabel.setTooltip(new Tooltip(SettingClient.bundle().getString("about.me.author")));

        avatarImageView.setImage(new Image("/images/author.jpg", true));
        avatarImageView.setClip(createCircularClip());

        githubLink.setOnAction(e -> openUrl("https://github.com/lxwise"));
        giteeLink.setOnAction(e -> openUrl("https://gitee.com/lxwise"));
        csdnLink.setOnAction(e -> openUrl("https://blog.csdn.net/qq_41940721"));
        qqLink.setOnAction(e -> copyAndMsg("1444073716"));
        wechatLink.setOnAction(e -> copyAndMsg("1444073716"));
        emailLink.setOnAction(e -> copyAndMsg("1444073716@qq.com"));

        Tooltip tip = new Tooltip(SettingClient.bundle().getString("about.me.jump"));
        githubLink.setTooltip(tip); giteeLink.setTooltip(tip); csdnLink.setTooltip(tip);
        String copyStr = SettingClient.bundle().getString("about.me.copy");
        qqLink.setTooltip(new Tooltip(copyStr));
        wechatLink.setTooltip(new Tooltip(copyStr));
        emailLink.setTooltip(new Tooltip(copyStr));

        initProjects();
    }

    private void initProjects() {
        ResourceBundle bundle = SettingClient.bundle();
        List<ProjectInfoModel> projects = List.of(
                new ProjectInfoModel("\uD83D\uDCD6 iris-blog", "https://gitee.com/lxwise/iris-blog_parent",
                        bundle.getString("project.iris.description")),
                new ProjectInfoModel("\uD83D\uDCE4 fx-updater", "https://gitee.com/lxwise/fx-updater",
                        bundle.getString("project.fxupdater.description")),
                new ProjectInfoModel("\uD83D\uDCE1 elastic-desktop-manager", "https://gitee.com/lxwise/elastic-desktop-manager",
                        bundle.getString("project.elastic.description")),
                new ProjectInfoModel("🔧 jfx-maven-plugin", "https://gitee.com/lxwise/jfx-maven-plugin",
                        bundle.getString("project.mavenplugin.description"))
        );

        for (ProjectInfoModel p : projects) {
            // 标题文本
            Label linkLabel = new Label(p.getName());
            linkLabel.getStyleClass().add("project-title");

            // 描述文本
            Label desc = new Label(p.getDesc());
            desc.getStyleClass().add("project-desc");
            desc.setWrapText(true);

            // 整个 VBox 卡片
            VBox projectBox = new VBox(5, linkLabel, desc);
            projectBox.getStyleClass().add("project-box");

            // 设置点击跳转
            projectBox.setOnMouseClicked(e -> openUrl(p.getUrl()));
            Tooltip.install(projectBox, new Tooltip(bundle.getString("about.me.jump")));
            projectBox.setCursor(Cursor.HAND); // 鼠标悬浮变手形

            projectListBox.getChildren().add(projectBox);
        }
    }

    private void openUrl(String url) {
        try {
            StateStore.hostServices.showDocument(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void copyAndMsg(String text) {
        ClipboardUtils.copy(text);
        MessageUtils.success(text+SettingClient.bundle().getString("action.alert.copy.success"));

    }

    private Circle createCircularClip() {
        Circle clip = new Circle(50, 50, 50);
        return clip;
    }
}
