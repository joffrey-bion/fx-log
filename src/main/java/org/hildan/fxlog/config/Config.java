package org.hildan.fxlog.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
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
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
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
        Gson gson = new GsonBuilder().registerTypeAdapter(ObservableList.class, new ObservableListCreator()).create();
        return gson.fromJson(new FileReader(sourceFilename), Config.class);
    }

    private void writeTo(String filename) throws IOException {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
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

    private static class ObservableListCreator implements InstanceCreator<ObservableList<?>> {

        public ObservableList<?> createInstance(Type type) {
            // No need to use a parametrized list since the actual instance will have the raw type anyway.
            return FXCollections.observableArrayList();
        }
    }
}
