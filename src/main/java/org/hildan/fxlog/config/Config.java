package org.hildan.fxlog.config;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.themes.Theme;
import org.jetbrains.annotations.NotNull;

/**
 * Contains the configuration of the app. This includes preferences, recent files, custom columnizers, colorizers etc.
 * <p>
 * The config is a singleton, and can be accessed using {@link Config#getInstance}.
 */
public class Config {

    /**
     * Holder class for thread-safe singleton pattern.
     */
    private static class Holder {
        static final Config INSTANCE = ConfigLoader.getUserConfig();
    }

    /**
     * Returns the current configuration.
     *
     * @return the current configuration
     */
    @NotNull
    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * The version of the format of the config. This needs to be increased each time the serialization format of this
     * class is changed.
     */
    static final int FORMAT_VERSION = 2;

    private static final int MAX_RECENT_FILES = 10;

    /**
     * The version of the format of this config object. This value is changed during the JSON deserialization of the
     * config. It stays 0 if the JSON file does not contain the version field.
     */
    private final Integer version = 0;

    private final IntegerProperty selectedColumnizerIndex;

    private final IntegerProperty selectedColorizerIndex;

    private final Property<Theme> currentTheme;

    private final BooleanProperty openLastFileAtStartup;

    private final BooleanProperty skipEmptyLogs;

    private final BooleanProperty wrapLogsText;

    private final Property<Font> logsFont;

    private final ObservableList<String> recentFiles;

    private final ObservableList<Columnizer> columnizers;

    private final ObservableList<Colorizer> colorizers;

    /**
     * Creates an empty configuration.
     */
    Config() {
        this.selectedColumnizerIndex = new SimpleIntegerProperty(0);
        this.selectedColorizerIndex = new SimpleIntegerProperty(0);
        this.currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);
        this.openLastFileAtStartup = new SimpleBooleanProperty(true);
        this.skipEmptyLogs = new SimpleBooleanProperty(true);
        this.wrapLogsText = new SimpleBooleanProperty(false);
        this.logsFont = new SimpleObjectProperty<>(Font.getDefault());
        this.recentFiles = FXCollections.observableArrayList();
        this.columnizers = FXCollections.observableArrayList();
        this.colorizers = FXCollections.observableArrayList();
    }

    public Integer getVersion() {
        return version;
    }

    public IntegerProperty selectedColorizerIndexProperty() {
        return selectedColorizerIndex;
    }

    public IntegerProperty selectedColumnizerIndexProperty() {
        return selectedColumnizerIndex;
    }

    @NotNull
    public Theme getCurrentTheme() {
        return currentTheme.getValue();
    }

    public void setCurrentTheme(@NotNull Theme currentTheme) {
        this.currentTheme.setValue(currentTheme);
    }

    public boolean getOpenLastFileAtStartup() {
        return openLastFileAtStartup.get();
    }

    @NotNull
    public BooleanProperty openLastFileAtStartupProperty() {
        return openLastFileAtStartup;
    }

    public void setOpenLastFileAtStartup(boolean openLastFileAtStartup) {
        this.openLastFileAtStartup.set(openLastFileAtStartup);
    }

    public boolean getSkipEmptyLogs() {
        return skipEmptyLogs.get();
    }

    @NotNull
    public BooleanProperty skipEmptyLogsProperty() {
        return skipEmptyLogs;
    }

    public void setSkipEmptyLogs(boolean skipEmptyLogs) {
        this.skipEmptyLogs.set(skipEmptyLogs);
    }

    public boolean getWrapLogsText() {
        return wrapLogsText.get();
    }

    @NotNull
    public BooleanProperty wrapLogsTextProperty() {
        return wrapLogsText;
    }

    public void setWrapLogsText(boolean wrapLogsText) {
        this.wrapLogsText.set(wrapLogsText);
    }

    public Font getLogsFont() {
        return logsFont.getValue();
    }

    @NotNull
    public Property<Font> logsFontProperty() {
        return logsFont;
    }

    public void setLogsFont(Font logsFont) {
        this.logsFont.setValue(logsFont);
    }

    /**
     * Returns the list of recent files' absolute paths.
     *
     * @return the list of recent files' absolute paths, potentially empty.
     */
    @NotNull
    public ObservableList<String> getRecentFiles() {
        return recentFiles;
    }

    /**
     * Returns the list of available {@link Columnizer}s.
     *
     * @return the list of available {@link Columnizer}s, potentially empty.
     */
    @NotNull
    public ObservableList<Columnizer> getColumnizers() {
        return columnizers;
    }

    /**
     * Returns the list of available {@link Colorizer}s.
     *
     * @return the list of available {@link Colorizer}s, potentially empty.
     */
    @NotNull
    public ObservableList<Colorizer> getColorizers() {
        return colorizers;
    }

    /**
     * Adds the given path to the list of recent files. This method takes care of duplicates and maximum recent files
     * count.
     *
     * @param filename
     *         the path to add
     */
    public void addToRecentFiles(@NotNull String filename) {
        String absolutePath = Paths.get(filename).toAbsolutePath().toString();
        recentFiles.removeIf(p -> p.equals(absolutePath));
        recentFiles.add(0, absolutePath);
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.remove(recentFiles.size() - 1);
        }
    }

    /**
     * Removes the given path from the list of recent files. This is useful when a path is not valid anymore, for
     * instance.
     *
     * @param filename
     *         the path to remove
     */
    public void removeFromRecentFiles(@NotNull String filename) {
        String absolutePath = Paths.get(filename).toAbsolutePath().toString();
        recentFiles.remove(absolutePath);
    }

    /**
     * Writes this config to the default user config file.
     *
     * @throws IOException
     *         if an I/O error occurs while writing to the file
     */
    public void persist() throws IOException {
        ConfigLoader.writeUserConfig(this);
    }
}
