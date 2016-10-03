package org.hildan.fxlog.themes;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javafx.scene.Scene;

import org.hildan.fxlog.FXLog;

public enum Theme {

    DARK( "common.css", "dark_base.css", "dark_theme.css"),
    LIGHT("common.css", "light_theme.css");

    private static final String CSS_PATH = FXLog.BASE_PACKAGE + "/themes";

    private final List<String> cssFiles;

    Theme(String... cssFiles) {
        this.cssFiles = Arrays.asList(cssFiles);
    }

    /**
     * Applies this theme to the given scenes.
     *
     * @param scenes
     *         the scenes to set this theme for
     */
    public void apply(Scene... scenes) {
        Arrays.stream(scenes).map(Scene::getStylesheets).forEach(style -> {
            style.clear();
            cssFiles.stream().map(Theme::getCss).forEach(style::add);
        });
    }

    /**
     * Loads the given CSS file and returns it as a style string.
     *
     * @param cssFilename
     *         the name of the CSS file. It can be a path relative to the CSS package.
     * @return the CSS as a style string
     */
    private static String getCss(String cssFilename) {
        String path = CSS_PATH + '/' + cssFilename;
        URL url = FXLog.class.getResource(path);
        if (url == null) {
            throw new RuntimeException(String.format("Cannot find CSS stylesheet '%s'", path));
        }
        return url.toExternalForm();
    }
}
