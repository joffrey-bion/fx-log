package org.hildan.fxlog.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    private ObservableList<Columnizer> columnizers;

    private ObservableList<Colorizer> colorizers;

    private ObservableList<String> recentFiles;

    Config() {
        this.columnizers = FXCollections.observableArrayList();
        this.colorizers = FXCollections.observableArrayList();
        this.recentFiles = FXCollections.observableArrayList();
    }

    public ObservableList<Columnizer> getColumnizers() {
        return columnizers;
    }

    public ObservableList<Colorizer> getColorizers() {
        return colorizers;
    }

    public ObservableList<String> getRecentFiles() {
        return recentFiles;
    }

    static Config readFrom(String sourceFilename) throws FileNotFoundException, JsonIOException, JsonSyntaxException {
        Gson gson = FxGson.builder().create();
        return gson.fromJson(new FileReader(sourceFilename), Config.class);
    }

    private void writeTo(String filename) throws IOException {
        Gson gson = FxGson.builder().disableHtmlEscaping().setPrettyPrinting().create();
        List<String> lines = Collections.singletonList(gson.toJson(this));
        Path filePath = Paths.get(filename);
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void persist() throws IOException {
        writeTo(ConfigLoader.USER_CONFIG_PATH);
    }
}
