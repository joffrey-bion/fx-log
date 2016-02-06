package org.hildan.fxlog.config;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;

public class Config {

    private static class Holder {
        static final Config INSTANCE = ConfigLoader.getUserConfig();
    }

    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    private static final int MAX_RECENT_FILES = 10;

    private ObservableList<String> recentFiles;

    private ObservableList<Columnizer> columnizers;

    private ObservableList<Colorizer> colorizers;

    Config() {
        this.recentFiles = FXCollections.observableArrayList();
        this.columnizers = FXCollections.observableArrayList();
        this.colorizers = FXCollections.observableArrayList();
    }

    public ObservableList<String> getRecentFiles() {
        return recentFiles;
    }

    public ObservableList<Columnizer> getColumnizers() {
        return columnizers;
    }

    public ObservableList<Colorizer> getColorizers() {
        return colorizers;
    }

    public void addToRecentFiles(String path) {
        String absolutePath = Paths.get(path).toAbsolutePath().toString();
        recentFiles.removeIf(p -> p.equals(absolutePath));
        recentFiles.add(0, absolutePath);
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.remove(recentFiles.size() - 1);
        }
    }

    public void removeFromRecentFiles(String path) {
        String absolutePath = Paths.get(path).toAbsolutePath().toString();
        recentFiles.remove(absolutePath);
    }

    public void persist() throws IOException {
        ConfigLoader.writeUserConfig(this);
    }
}
