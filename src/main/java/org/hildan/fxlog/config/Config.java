package org.hildan.fxlog.config;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.themes.Themes;
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

    private static final int MAX_RECENT_FILES = 10;

    private final IntegerProperty selectedColumnizerIndex;

    private final IntegerProperty selectedColorizerIndex;

    private final StringProperty currentTheme;

    private final ObservableList<String> recentFiles;

    private final ObservableList<Columnizer> columnizers;

    private final ObservableList<Colorizer> colorizers;

    /**
     * Creates an empty configuration.
     */
    Config() {
        this.selectedColumnizerIndex = new SimpleIntegerProperty(0);
        this.selectedColorizerIndex = new SimpleIntegerProperty(0);
        this.currentTheme = new SimpleStringProperty(Themes.LIGHT);
        this.recentFiles = FXCollections.observableArrayList();
        this.columnizers = FXCollections.observableArrayList();
        this.colorizers = FXCollections.observableArrayList();
    }

    public IntegerProperty selectedColorizerIndexProperty() {
        return selectedColorizerIndex;
    }

    public IntegerProperty selectedColumnizerIndexProperty() {
        return selectedColumnizerIndex;
    }

    public String getCurrentTheme() {
        return currentTheme.get();
    }

    public StringProperty currentThemeProperty() {
        return currentTheme;
    }

    public void setCurrentTheme(String currentTheme) {
        this.currentTheme.set(currentTheme);
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
