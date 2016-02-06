package org.hildan.fxlog.config;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;
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

    private final ObservableList<String> recentFiles;

    private final ObservableList<Columnizer> columnizers;

    private final ObservableList<Colorizer> colorizers;

    /**
     * Creates an empty configuration.
     */
    Config() {
        this.recentFiles = FXCollections.observableArrayList();
        this.columnizers = FXCollections.observableArrayList();
        this.colorizers = FXCollections.observableArrayList();
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
