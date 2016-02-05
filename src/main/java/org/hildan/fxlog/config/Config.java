package org.hildan.fxlog.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
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
        String normalizedPath = Paths.get(path).toString();
        recentFiles.removeIf(p -> p.equals(normalizedPath));
        recentFiles.add(0, normalizedPath);
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.remove(recentFiles.size() - 1);
        }
    }

    static Config readFrom(Reader source) throws JsonIOException, JsonSyntaxException {
        return FxGson.builder().create().fromJson(source, Config.class);
    }

    public void persist() throws IOException {
        writeTo(ConfigLoader.USER_CONFIG_PATH);
    }

    private void writeTo(String filename) throws IOException {
        Path filePath = Paths.get(filename);
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        // html escaping disabled to see <> in regexps in the JSON
        Gson gson = FxGson.builder().disableHtmlEscaping().setPrettyPrinting().create();
        List<String> lines = Collections.singletonList(gson.toJson(this));
        Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}
