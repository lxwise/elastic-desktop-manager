package com.lxwise.elastic.store;
import com.lxwise.elastic.core.event.EventBus;
import com.lxwise.elastic.core.event.ThemeEvent;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.text.Font;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author lstar
 * @create 2025-02
 * @description: 主题管理器
 */
public final class ThemeManager {

    private static final PseudoClass USER_CUSTOM = PseudoClass.getPseudoClass("user-custom");
    private static final EventBus EVENT_BUS = EventBus.getInstance();

    public static final String DEFAULT_FONT_FAMILY_NAME = Font.getDefault().getName();
    public static final int DEFAULT_FONT_SIZE = 14;
    public static final int DEFAULT_ZOOM = 100;
    public static final List<Integer> SUPPORTED_FONT_SIZE = IntStream.range(8, 29).boxed().collect(Collectors.toList());
    public static final List<Integer> SUPPORTED_ZOOM = List.of(50, 75, 80, 90, 100, 110, 125, 150, 175, 200);

    private final Map<String, String> customCSSDeclarations = new LinkedHashMap<>(); // -fx-property | value;
    private final Map<String, String> customCSSRules = new LinkedHashMap<>(); // .foo | -fx-property: value;
    private Scene scene;
    private String fontFamily = DEFAULT_FONT_FAMILY_NAME;
    private int fontSize = DEFAULT_FONT_SIZE;
    private int zoom = DEFAULT_ZOOM;


    public Scene getScene() {
        return scene;
    }

    // MUST BE SET ON STARTUP
    // (this is supposed to be a constructor arg, but since app don't use DI..., sorry)
    public void setScene(Scene scene) {
        this.scene = Objects.requireNonNull(scene);
    }


    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        Objects.requireNonNull(fontFamily);
        setCustomDeclaration("-fx-font-family", "\"" + fontFamily + "\"");

        this.fontFamily = fontFamily;

        reloadCustomCSS();
        EVENT_BUS.publish(new ThemeEvent(ThemeEvent.EventType.FONT_CHANGE));
    }

    public boolean isDefaultFontFamily() {
        return Objects.equals(DEFAULT_FONT_FAMILY_NAME, getFontFamily());
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int size) {
        if (!SUPPORTED_FONT_SIZE.contains(size)) {
            throw new IllegalArgumentException(
                    String.format("Font size must in the range %d-%dpx. Actual value is %d.",
                            SUPPORTED_FONT_SIZE.get(0),
                            SUPPORTED_FONT_SIZE.get(SUPPORTED_FONT_SIZE.size() - 1),
                            size
                    ));
        }

        setCustomDeclaration("-fx-font-size", size + "px");
        setCustomRule(".ikonli-font-icon", String.format("-fx-icon-size: %dpx;", size + 2));

        this.fontSize = size;

        var rawZoom = (int) Math.ceil((size * 1.0 / DEFAULT_FONT_SIZE) * 100);
        this.zoom = SUPPORTED_ZOOM.stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - rawZoom)))
                .orElseThrow(NoSuchElementException::new);

        reloadCustomCSS();
        EVENT_BUS.publish(new ThemeEvent(ThemeEvent.EventType.FONT_CHANGE));
    }

    public boolean isDefaultSize() {
        return DEFAULT_FONT_SIZE == fontSize;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if (!SUPPORTED_ZOOM.contains(zoom)) {
            throw new IllegalArgumentException(
                    String.format("Zoom value must one of %s. Actual value is %d.", SUPPORTED_ZOOM, zoom)
            );
        }

        setFontSize((int) Math.ceil(zoom != 100 ? (DEFAULT_FONT_SIZE * zoom) / 100.0f : DEFAULT_FONT_SIZE));
        this.zoom = zoom;
    }



    ///////////////////////////////////////////////////////////////////////////

    private void setCustomDeclaration(String property, String value) {
        customCSSDeclarations.put(property, value);
    }


    private void setCustomRule(String selector, String rule) {
        customCSSRules.put(selector, rule);
    }

    @SuppressWarnings("unused")
    private void removeCustomRule(String selector) {
        customCSSRules.remove(selector);
    }


    private void reloadCustomCSS() {
        Objects.requireNonNull(scene);
        StringBuilder css = new StringBuilder();

        css.append(".root:");
        css.append(USER_CUSTOM.getPseudoClassName());
        css.append(" {\n");
        customCSSDeclarations.forEach((k, v) -> {
            css.append("\t");
            css.append(k);
            css.append(":\s");
            css.append(v);
            css.append(";\n");
        });
        css.append("}\n");

        customCSSRules.forEach((k, v) -> {
            // custom CSS is applied to the body,
            // thus it has a preference over accent color
            css.append(".body:");
            css.append(USER_CUSTOM.getPseudoClassName());
            css.append(" ");
            css.append(k);
            css.append(" {");
            css.append(v);
            css.append("}\n");
        });

        getScene().getRoot().getStylesheets().removeIf(uri -> uri.startsWith("data:text/css"));
        getScene().getRoot().getStylesheets().add(
                "data:text/css;base64," + Base64.getEncoder().encodeToString(css.toString().getBytes(UTF_8))
        );
        getScene().getRoot().pseudoClassStateChanged(USER_CUSTOM, true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Singleton                                                             //
    ///////////////////////////////////////////////////////////////////////////

    private ThemeManager() {
    }

    private static class InstanceHolder {

        private static final ThemeManager INSTANCE = new ThemeManager();
    }

    public static ThemeManager getInstance() {
        return InstanceHolder.INSTANCE;
    }
}
