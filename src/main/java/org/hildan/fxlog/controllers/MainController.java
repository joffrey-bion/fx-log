package org.hildan.fxlog.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.apache.commons.io.input.Tailer;
import org.hildan.fxlog.FXLog;
import org.hildan.fxlog.coloring.ColorizedRowFactory;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.core.LogTailListener;
import org.hildan.fxlog.errors.ErrorDialog;
import org.hildan.fxlog.filtering.Filter;
import org.jetbrains.annotations.NotNull;

public class MainController implements Initializable {

    private Config config;

    private Stage columnizersStage;

    private Stage colorizersStage;

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<LogEntry> logsTable;

    @FXML
    private ChoiceBox<Columnizer> columnizerSelector;

    @FXML
    private ChoiceBox<Colorizer> colorizerSelector;

    @FXML
    private Button editColumnizersBtn;

    @FXML
    private Button editColorizersBtn;

    @FXML
    private TextField filterField;

    @FXML
    private Menu recentFilesMenu;

    @FXML
    private MenuItem closeMenu;

    @FXML
    private CheckMenuItem followTailMenu;

    @FXML
    private ToggleButton toggleFollowTailButton;

    private Property<Columnizer> columnizer;

    private Property<Colorizer> colorizer;

    private ObservableList<LogEntry> columnizedLogs;

    private FilteredList<LogEntry> filteredLogs;

    private Property<Predicate<LogEntry>> filter;

    private BooleanProperty followingTail;

    private BooleanProperty tailingFile;

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
        followingTail = new SimpleBooleanProperty(false);
        tailingFile = new SimpleBooleanProperty(false);
        closeMenu.disableProperty().bind(tailingFile.not());
        logsTable.setOnScroll(e -> followingTail.set(false));
        followTailMenu.selectedProperty().bindBidirectional(followingTail);
        toggleFollowTailButton.selectedProperty().bindBidirectional(followingTail);

        configureColumnizerSelector();
        configureColorizerSelector();
        configureFiltering();
        configureLogsTable();
        configureRecentFilesMenu();
        configureColumnizersStage();
        configureColorizersStage();
        configureAutoScroll();
    }

    /**
     * Binds the colorizer selector to the current colorizer property and the colorizers of the config.
     */
    private void configureColorizerSelector() {
        bindSelector(colorizerSelector, config.getColorizers(), colorizer, config.selectedColorizerIndexProperty());
    }

    /**
     * Binds the columnizer selector to the current columnizer property and the columnizers of the config.
     */
    private void configureColumnizerSelector() {
        bindSelector(columnizerSelector, config.getColumnizers(), columnizer, config.selectedColumnizerIndexProperty());
        columnizer.addListener(change -> {
            // re-columnizes the logs
            restartTailing();
        });
    }

    /**
     * Configures the given selector with the given {@code items}, and binds it to the given properties.
     * <p>
     * The initial values for the selector and the selectedItem are set based on {@code selectedItemIndexProperty}.
     *
     * @param selector
     *         the selector to configure
     * @param items
     *         the items to put in the selector
     * @param selectedItemProperty
     *         the property to bind for the selected item
     * @param selectedItemIndexProperty
     *         the property to bind for the index of the selected item
     * @param <T>
     *         the type of items in the selector
     */
    private <T> void bindSelector(@NotNull ChoiceBox<T> selector, @NotNull ObservableList<T> items,
                                  @NotNull Property<T> selectedItemProperty,
                                  @NotNull IntegerProperty selectedItemIndexProperty) {
        selector.setItems(items);
        if (!items.isEmpty()) {
            int selectedIndex = selectedItemIndexProperty.get();
            selectedItemProperty.setValue(items.get(selectedIndex));
            selector.getSelectionModel().select(selectedIndex);
        }
        selectedItemProperty.bindBidirectional(selector.valueProperty());
        selectedItemIndexProperty.bind(selector.getSelectionModel().selectedIndexProperty());
    }

    /**
     * Binds the filtered logs list predicate, the current filter, and the filter text field together.
     */
    private void configureFiltering() {
        Callable<Predicate<LogEntry>> createFilter = () -> {
            PseudoClass errorClass = PseudoClass.getPseudoClass("error");
            try {
                filterField.pseudoClassStateChanged(errorClass, false);
                if (filterField.getText().isEmpty()) {
                    return log -> true;
                }
                return Filter.matchRawLog(".*?" + filterField.getText() + ".*");
            } catch (PatternSyntaxException e) {
                filterField.pseudoClassStateChanged(errorClass, true);
                return log -> false;
            }
        };
        Binding<Predicate<LogEntry>> filterBinding =
                Bindings.createObjectBinding(createFilter, filterField.textProperty());
        filterField.setText("");
        filter.setValue(log -> true);
        filter.bind(filterBinding);
        filteredLogs.predicateProperty().bind(filter);
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
                MenuItem sep = new SeparatorMenuItem();
                items.add(sep);
                MenuItem clearItem = new MenuItem("Clear recent files");
                clearItem.setOnAction(event -> config.getRecentFiles().clear());
                items.add(clearItem);
            }
        };
        config.getRecentFiles().addListener(updateRecentFilesMenu);
        // manual trigger the first time for initialization
        updateRecentFilesMenu.onChanged(null);
    }

    /**
     * Configures the stage in which the colorizers customization UI is started.
     */
    private void configureColorizersStage() {
        colorizersStage = new Stage();
        try {
            Parent root = FXLog.loadView("colorizers.fxml");
            Scene scene = new Scene(root);
            colorizersStage.setTitle("Customize Colorizers");
            colorizersStage.setScene(scene);
            editColorizersBtn.disableProperty().bind(colorizersStage.showingProperty());
            List<String> styles = scene.getStylesheets();
            styles.clear();
            styles.add(FXLog.getCss("light_theme.css"));
        } catch (IOException e) {
            ErrorDialog.uncaughtException(e);
        }
    }

    /**
     * Configures the stage in which the columnizers customization UI is started.
     */
    private void configureColumnizersStage() {
        columnizersStage = new Stage();
        try {
            Parent root = FXLog.loadView("columnizers.fxml");
            Scene scene = new Scene(root);
            columnizersStage.setTitle("Customize Columnizers");
            columnizersStage.setScene(scene);
            editColumnizersBtn.disableProperty().bind(columnizersStage.showingProperty());
            List<String> styles = scene.getStylesheets();
            styles.clear();
            styles.add(FXLog.getCss("light_theme.css"));
        } catch (IOException e) {
            ErrorDialog.uncaughtException(e);
        }
    }

    private void configureAutoScroll() {
        followingTail.addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                scrollToBottom();
            } else {
                if (columnizedLogs.size() > 10) {
                    logsTable.scrollTo(columnizedLogs.size() - 10);
                }
            }
        });
        logsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                followingTail.set(!followingTail.get());
            }
        });
    }

    private void scrollToBottom() {
        if (!columnizedLogs.isEmpty()) {
            logsTable.scrollTo(columnizedLogs.size() - 1);
        }
    }

    /**
     * Opens the custom colorizers window.
     */
    @FXML
    public void editColorizers() {
        colorizersStage.showAndWait();
    }

    /**
     * Opens the custom columnizers window.
     */
    @FXML
    public void editColumnizers() {
        columnizersStage.showAndWait();
    }

    /**
     * Opens a file chooser to choose the file to tail, and starts tailing the selected file.
     */
    @FXML
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
        tailingFile.set(true);
    }

    /**
     * Closes and re-opens the file being tailed. Useful to update the columnization for instance.
     */
    private void restartTailing() {
        if (!tailingFile.getValue()) {
            System.err.println("Can't RE-start if we're not tailing");
            return;
        }
        File file = tailer.getFile();
        closeCurrentFile();
        try {
            startTailingFile(file);
        } catch (FileNotFoundException e) {
            ErrorDialog.recentFileNotFound(file.getAbsolutePath());
        }
    }

    /**
     * Closes the currently opened file.
     */
    @FXML
    public void closeCurrentFile() {
        if (tailer != null) {
            logTailListener.stop();
            tailer.stop();
        }
        columnizedLogs.clear();
        tailingFile.set(false);
    }

    /**
     * Exits the application.
     */
    @FXML
    public void quit() {
        Platform.exit();
    }

    /**
     * Copy to the clipboard the raw logs corresponding to the selected lines.
     */
    @FXML
    public void copyRaw() {
        copySelectedLogsToClipboard(LogEntry::rawLine, "");
    }

    /**
     * Copy to the clipboard the tab-separated columnized logs corresponding to the selected lines.
     */
    @FXML
    public void copyPretty() {
        List<ColumnDefinition> columnDefinitions = columnizer.getValue().getColumnDefinitions();
        String headers = columnDefinitions.stream()
                                          .filter(ColumnDefinition::isVisible)
                                          .map(ColumnDefinition::getHeaderLabel)
                                          .collect(Collectors.joining("\t"));
        copySelectedLogsToClipboard(log -> log.toColumnizedString(columnDefinitions), headers + '\n');
    }

    /**
     * Copy the selected logs to the clipboard using the given function to convert them to strings.
     *
     * @param logToLine
     *         the function to use to convert each log into a string
     * @param prefix
     *         some extra content to put before the logs
     */
    private void copySelectedLogsToClipboard(Function<LogEntry, String> logToLine, String prefix) {
        String textLogs = logsTable.getSelectionModel()
                                   .getSelectedItems()
                                   .stream()
                                   .map(logToLine)
                                   .collect(Collectors.joining("\n"));
        ClipboardContent content = new ClipboardContent();
        content.putString(prefix + textLogs);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Selects all the logs in the table.
     */
    @FXML
    public void selectAll() {
        logsTable.getSelectionModel().selectAll();
    }

    /**
     * Unselects all the logs in the table.
     */
    @FXML
    public void unselectAll() {
        logsTable.getSelectionModel().clearSelection();
    }

    /**
     * Switches to the dark theme.
     */
    @FXML
    public void selectDarkTheme() {
        List<List<String>> styles =
                Arrays.asList(mainPane.getScene().getStylesheets(), colorizersStage.getScene().getStylesheets());
        for (List<String> style : styles) {
            style.clear();
            style.add(FXLog.getCss("dark_theme.css"));
        }
    }

    /**
     * Switches to the bright theme.
     */
    @FXML
    public void selectBrightTheme() {
        List<List<String>> styles =
                Arrays.asList(mainPane.getScene().getStylesheets(), colorizersStage.getScene().getStylesheets());
        for (List<String> style : styles) {
            style.clear();
            style.add(FXLog.getCss("light_theme.css"));
        }
    }

    /**
     * Opens the web page containing the user manual.
     */
    @FXML
    public void openUserManual() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/joffrey-bion/fx-log"));
        } catch (IOException | URISyntaxException e) {
            ErrorDialog.uncaughtException(e);
        }
    }
}
