package org.hildan.fxlog.controllers;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.apache.commons.io.input.Tailer;
import org.hildan.fxlog.coloring.ColorizedRowFactory;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.core.LogTailListener;
import org.hildan.fxlog.errors.ErrorDialog;
import org.hildan.fxlog.filtering.Filter;

public class MainController implements Initializable {

    private Config config;

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<LogEntry> logsTable;

    @FXML
    private ChoiceBox<Columnizer> columnizerSelector;

    @FXML
    private ChoiceBox<Colorizer> colorizerSelector;

    @FXML
    private TextField filterField;

    @FXML
    private Menu recentFilesMenu;

    private Property<Columnizer> columnizer;

    private Property<Colorizer> colorizer;

    private ObservableList<LogEntry> columnizedLogs;

    private FilteredList<LogEntry> filteredLogs;

    private Property<Predicate<LogEntry>> filter;

    private Tailer tailer;

    private LogTailListener logTailListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        columnizedLogs = FXCollections.observableArrayList();
        filteredLogs = new FilteredList<>(columnizedLogs);
        filter = new SimpleObjectProperty<>();
        colorizer = new SimpleObjectProperty<>();
        columnizer = new SimpleObjectProperty<>();
        configureColumnizerSelector();
        configureColorizerSelector();
        configureFiltering();
        configureLogsTable();
        configureRecentFilesMenu();
    }

    /**
     * Binds the colorizer selector to the current colorizer property and the colorizers of the config.
     */
    private void configureColorizerSelector() {
        ObservableList<Colorizer> colorizers = config.getColorizers();
        colorizerSelector.setItems(colorizers);
        colorizer.bindBidirectional(colorizerSelector.valueProperty());
        if (!colorizers.isEmpty()) {
            colorizer.setValue(colorizers.get(0));
        }
    }

    /**
     * Binds the columnizer selector to the current columnizer property and the columnizers of the config.
     */
    private void configureColumnizerSelector() {
        ObservableList<Columnizer> columnizers = config.getColumnizers();
        columnizerSelector.setItems(columnizers);
        columnizer.bindBidirectional(columnizerSelector.valueProperty());
        if (!columnizers.isEmpty()) {
            columnizer.setValue(columnizers.get(0));
        }
    }

    /**
     * Binds the filtered logs list predicate, the current filter, and the filter text field together.
     */
    private void configureFiltering() {
        filteredLogs.predicateProperty().bind(filter);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                filter.setValue(Filter.matchRawLog(".*?" + newValue + ".*"));
            } else {
                filter.setValue(log -> true);
            }
        });
        filter.setValue(log -> true);
    }

    /**
     * Binds the logs table to the current colorizer, columnizer, and filtered logs list.
     */
    private void configureLogsTable() {
        logsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (columnizer.getValue() != null) {
            logsTable.getColumns().addAll(columnizer.getValue().getColumns());
        }
        columnizer.addListener((observable, oldValue, newValue) -> {
            logsTable.getColumns().clear();
            logsTable.getColumns().addAll(newValue.getColumns());
        });
        logsTable.setItems(filteredLogs);
        ColorizedRowFactory colorizedRowFactory = new ColorizedRowFactory();
        colorizedRowFactory.colorizerProperty().bind(colorizer);
        logsTable.setRowFactory(colorizedRowFactory);
    }

    /**
     * Binds the recent files menu to the recent files in the config.
     */
    private void configureRecentFilesMenu() {
        ListChangeListener<String> updateRecentFilesMenu = change -> {
            ObservableList<MenuItem> items = recentFilesMenu.getItems();
            items.clear();
            if (config.getRecentFiles().isEmpty()) {
                MenuItem noItem = new MenuItem("No recent file");
                noItem.setDisable(true);
                items.add(noItem);
            } else {
                config.getRecentFiles().stream().map(path -> {
                    MenuItem menuItem = new MenuItem(path);
                    menuItem.setOnAction(event -> openRecentFile(path));
                    return menuItem;
                }).forEach(items::add);
            }
        };
        config.getRecentFiles().addListener(updateRecentFilesMenu);
        // manual trigger the first time for initialization
        updateRecentFilesMenu.onChanged(null);
    }

    /**
     * Opens a file chooser to choose the file to tail, and starts tailing the selected file.
     */
    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Log File");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Log files (*.txt, *.log)", "*.txt", "*.log"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("All files", "*.*"));
        File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (file != null) {
            try {
                startTailingFile(file);
            } catch (FileNotFoundException e) {
                ErrorDialog.selectedFileNotFound(file.getPath());
            }
        }
    }

    /**
     * Opens the given recent file and starts tailing it.
     */
    public void openRecentFile(String filename) {
        try {
            startTailingFile(filename);
        } catch (FileNotFoundException e) {
            config.removeFromRecentFiles(filename);
            ErrorDialog.recentFileNotFound(filename);
        }
    }

    /**
     * Starts tailing the given file, thus updating the log lines in the table.
     *
     * @param filename
     *         the path of the file to tail
     * @throws FileNotFoundException
     *         if the file was not found
     */
    public void startTailingFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        startTailingFile(file);
    }

    /**
     * Starts tailing the given file, thus updating the log lines in the table.
     *
     * @param file
     *         the file to tail
     * @throws FileNotFoundException
     *         if the file was not found
     */
    private void startTailingFile(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        closeCurrentFile();
        config.addToRecentFiles(file.getAbsolutePath());
        logTailListener = new LogTailListener(columnizer.getValue(), columnizedLogs);
        tailer = Tailer.create(file, logTailListener, 500);
    }

    /**
     * Closes the currently opened file.
     */
    public void closeCurrentFile() {
        if (tailer != null) {
            logTailListener.stop();
            tailer.stop();
        }
        columnizedLogs.clear();
    }

    /**
     * Exits the application.
     */
    public void quit() {
        Platform.exit();
    }

    /**
     * Copy to the clipboard the raw logs corresponding to the selected lines.
     */
    public void copyRaw() {
        copySelectedLogsToClipboard(LogEntry::rawLine);
    }

    /**
     * Copy to the clipboard the tab-separated columnized logs corresponding to the selected lines.
     */
    public void copyPretty() {
        copySelectedLogsToClipboard(LogEntry::toColumnizedString);
    }

    /**
     * Copy the selected logs to the clipboard using the given function to convert them to strings.
     *
     * @param logToLine
     *         the function to use to convert each log into a string
     */
    private void copySelectedLogsToClipboard(Function<LogEntry, String> logToLine) {
        String textLogs = logsTable.getSelectionModel()
                                   .getSelectedItems()
                                   .stream()
                                   .map(logToLine)
                                   .collect(Collectors.joining(String.format("%n")));
        ClipboardContent content = new ClipboardContent();
        content.putString(textLogs);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Selects all the logs in the table.
     */
    public void selectAll() {
        logsTable.getSelectionModel().selectAll();
    }

    /**
     * Unselects all the logs in the table.
     */
    public void unselectAll() {
        logsTable.getSelectionModel().clearSelection();
    }

    /**
     * Switches to the dark theme.
     */
    public void selectDarkTheme() {
        List<String> styles = mainPane.getScene().getStylesheets();
        styles.clear();
        styles.add(getClass().getResource("/org/hildan/fxlog/dark_theme.css").toExternalForm());
    }

    /**
     * Switches to the bright theme.
     */
    public void selectBrightTheme() {
        List<String> styles = mainPane.getScene().getStylesheets();
        styles.clear();
        styles.add(getClass().getResource("/org/hildan/fxlog/light_theme.css").toExternalForm());
    }

    /**
     * Opens the web page containing the user manual.
     */
    public void openUserManual() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/joffrey-bion/fx-log"));
        } catch (IOException | URISyntaxException e) {
            ErrorDialog.uncaughtException(e);
        }
    }
}
