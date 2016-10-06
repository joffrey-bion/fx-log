package org.hildan.fxlog.config;

import java.nio.file.Paths;

import org.hildan.fxlog.themes.Theme;
import org.jetbrains.annotations.NotNull;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Holds the saved state for application restart. This includes the recent files, the last selected columnizer,
 * colorizer etc.
 */
@SuppressWarnings("unused")
public class State {

    private static final int MAX_RECENT_FILES = 10;

    private final ObservableList<String> recentFiles;

    private final IntegerProperty selectedColumnizerIndex;

    private final IntegerProperty selectedColorizerIndex;

    private final Property<Theme> currentTheme;

    State() {
        this.recentFiles = FXCollections.observableArrayList();
        this.selectedColumnizerIndex = new SimpleIntegerProperty(0);
        this.selectedColorizerIndex = new SimpleIntegerProperty(0);
        this.currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);
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
     * Returns the list of recent files' absolute paths.
     *
     * @return the list of recent files' absolute paths, potentially empty.
     */
    @NotNull
    public ObservableList<String> getRecentFiles() {
        return recentFiles;
    }

    public int getSelectedColumnizerIndex() {
        return selectedColumnizerIndex.get();
    }

    public IntegerProperty selectedColumnizerIndexProperty() {
        return selectedColumnizerIndex;
    }

    public void setSelectedColumnizerIndex(int selectedColumnizerIndex) {
        this.selectedColumnizerIndex.set(selectedColumnizerIndex);
    }

    public int getSelectedColorizerIndex() {
        return selectedColorizerIndex.get();
    }

    public IntegerProperty selectedColorizerIndexProperty() {
        return selectedColorizerIndex;
    }

    public void setSelectedColorizerIndex(int selectedColorizerIndex) {
        this.selectedColorizerIndex.set(selectedColorizerIndex);
    }

    public Theme getCurrentTheme() {
        return currentTheme.getValue();
    }

    public Property<Theme> currentThemeProperty() {
        return currentTheme;
    }

    public void setCurrentTheme(Theme currentTheme) {
        this.currentTheme.setValue(currentTheme);
    }
}
