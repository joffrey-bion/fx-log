package org.hildan.fxlog.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.commons.io.input.Tailer;
import org.controlsfx.control.textfield.CustomTextField;
import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.FXLog;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.core.LogTailListener;
import org.hildan.fxlog.errors.ErrorDialog;
import org.hildan.fxlog.filtering.Filter;
import org.hildan.fxlog.themes.Css;
import org.hildan.fxlog.themes.Theme;
import org.hildan.fxlog.version.VersionChecker;
import org.hildan.fxlog.view.StyledTableCell;
import org.hildan.fxlog.view.UIUtils;
import org.jetbrains.annotations.NotNull;

public class MainController implements Initializable {

    private Config config;

    private Stage columnizersStage;

    private Stage colorizersStage;

    private Stage preferencesStage;

    private Stage aboutStage;

    @FXML
    private BorderPane mainPane;

    @FXML
    private Pane searchPanel;

    @FXML
    private SearchController searchPanelController;

    @FXML
    private TableView<LogEntry> logsTable;

    @FXML
    private ChoiceBox<Columnizer> columnizerSelector;

    @FXML
    private ChoiceBox<Colorizer> colorizerSelector;

    @FXML
    private CustomTextField filterField;

    @FXML
    private CheckBox caseSensitiveFilterCheckbox;

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

    private StringProperty tailedFileName;

    private BooleanProperty followingTail;

    private BooleanProperty tailingFile;

    private Tailer tailer;

    private LogTailListener logTailListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        columnizedLogs = FXCollections.observableArrayList();
        filteredLogs = new FilteredList<>(columnizedLogs);
        colorizer = new SimpleObjectProperty<>();
        columnizer = new SimpleObjectProperty<>();
        followingTail = new SimpleBooleanProperty(false);
        tailingFile = new SimpleBooleanProperty(false);
        tailedFileName = new SimpleStringProperty();
        closeMenu.disableProperty().bind(tailingFile.not());

        configureTitleBinding();
        configureColumnizerSelector();
        configureColorizerSelector();
        configureFiltering();
        configureLogsTable();
        configureRecentFilesMenu();
        configureSecondaryStages();
        configureAutoScroll();

        searchPanelController.configure(config, filteredLogs, logsTable, columnizer);
    }

    private void configureTitleBinding() {
        UIUtils.whenWindowReady(mainPane, window -> {
            Stage stage = (Stage) window;
            StringBinding titleBinding = createTitleBinding(tailingFile, tailedFileName);
            stage.titleProperty().bind(titleBinding);
        });
    }

    private static StringBinding createTitleBinding(BooleanProperty appendFileName, StringProperty filename) {
        return Bindings.createStringBinding(() -> {
            String newTitle = FXLog.APP_NAME;
            if (appendFileName.get()) {
                newTitle += " - " + filename.get();
            }
            return newTitle;
        }, filename, appendFileName);
    }

    /**
     * Binds the colorizer selector to the current colorizer property and the colorizers of the config.
     */
    private void configureColorizerSelector() {
        IntegerProperty selectedIndexProp = config.getState().selectedColorizerIndexProperty();
        bindSelector(colorizerSelector, config.getColorizers(), colorizer, selectedIndexProp);
    }

    /**
     * Binds the columnizer selector to the current columnizer property and the columnizers of the config.
     */
    private void configureColumnizerSelector() {
        IntegerProperty selectedIndexProp = config.getState().selectedColumnizerIndexProperty();
        bindSelector(columnizerSelector, config.getColumnizers(), columnizer, selectedIndexProp);
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
    private static <T> void bindSelector(@NotNull ChoiceBox<T> selector, @NotNull ObservableList<T> items,
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
            try {
                filterField.pseudoClassStateChanged(Css.INVALID, false);
                if (filterField.getText().isEmpty()) {
                    return log -> true;
                }
                int flags = caseSensitiveFilterCheckbox.isSelected() ? 0 : Pattern.CASE_INSENSITIVE;
                return Filter.findInRawLog(filterField.getText(), flags);
            } catch (PatternSyntaxException e) {
                filterField.pseudoClassStateChanged(Css.INVALID, true);
                return log -> false;
            }
        };
        Binding<Predicate<LogEntry>> filterBinding =
                Bindings.createObjectBinding(createFilter, filterField.textProperty(),
                        caseSensitiveFilterCheckbox.selectedProperty());
        filterField.setText("");
        UIUtils.makeClearable(filterField);
        filteredLogs.predicateProperty().bind(filterBinding);
        filteredLogs.predicateProperty().addListener((obs, before, now) -> {
            if (followingTail.get()) {
                scrollToBottom();
            }
        });
    }

    @FXML
    public void search() {
        // stop tailing to be able to go from match to match
        followingTail.set(false);
        searchPanel.setVisible(true);
        searchPanelController.startSearch();
    }

    /**
     * Binds the logs table to the current colorizer, columnizer, and filtered logs list.
     */
    private void configureLogsTable() {
        logsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        EasyBind.subscribe(columnizer, c -> {
            logsTable.getColumns().clear();
            if (c != null) {
                logsTable.getColumns().addAll(getConfiguredColumns(c));
            }
        });
        logsTable.setItems(filteredLogs);
    }

    private Collection<TableColumn<LogEntry, String>> getConfiguredColumns(Columnizer columnizer) {
        Collection<TableColumn<LogEntry, String>> columns = columnizer.getColumns();
        columns.forEach(col -> col.setCellFactory(column -> {
            StyledTableCell cell = new StyledTableCell(column);
            cell.fontProperty().bind(config.getPreferences().logsFontProperty());
            cell.wrapTextProperty().bind(config.getPreferences().wrapLogsTextProperty());
            cell.colorizerProperty().bind(colorizer);
            cell.searchMatcherProperty().bind(searchPanelController.textMatcher());
            cell.searchHighlightStyleProperty().bind(config.getPreferences().searchHighlightStyleProperty());
            return cell;
        }));
        return columns;
    }

    /**
     * Binds the recent files menu to the recent files in the config.
     */
    private void configureRecentFilesMenu() {
        ListChangeListener<String> updateRecentFilesMenu = change -> {
            ObservableList<MenuItem> items = recentFilesMenu.getItems();
            items.clear();
            if (config.getState().getRecentFiles().isEmpty()) {
                MenuItem noItem = new MenuItem("No recent file");
                noItem.setDisable(true);
                items.add(noItem);
            } else {
                config.getState().getRecentFiles().stream().map(path -> {
                    MenuItem menuItem = new MenuItem(path);
                    menuItem.setOnAction(event -> openRecentFile(path));
                    return menuItem;
                }).forEach(items::add);
                MenuItem sep = new SeparatorMenuItem();
                items.add(sep);
                MenuItem clearItem = new MenuItem("Clear recent files");
                clearItem.setOnAction(event -> config.getState().getRecentFiles().clear());
                items.add(clearItem);
            }
        };
        config.getState().getRecentFiles().addListener(updateRecentFilesMenu);
        // manual trigger the first time for initialization
        updateRecentFilesMenu.onChanged(null);
    }

    /**
     * Configures the preferences and customization stages.
     */
    private void configureSecondaryStages() {
        Theme theme = config.getState().getCurrentTheme();
        colorizersStage = UIUtils.createStage("popups/colorizers.fxml", "Customize Colorizers", theme);
        columnizersStage = UIUtils.createStage("popups/columnizers.fxml", "Customize Columnizers", theme);
        preferencesStage = UIUtils.createStage("popups/preferences.fxml", "Preferences", theme);
        aboutStage = UIUtils.createStage("popups/about.fxml", "About FX Log", theme);
        aboutStage.initStyle(StageStyle.UNDECORATED);
        aboutStage.focusedProperty().addListener((observable, wasFocused, nowFocused) -> {
            if (!nowFocused) {
                aboutStage.hide();
            }
        });

        UIUtils.whenWindowReady(mainPane, scene -> {
            colorizersStage.initOwner(scene);
            columnizersStage.initOwner(scene);
            preferencesStage.initOwner(scene);
            aboutStage.initOwner(scene);
        });
    }

    private void configureAutoScroll() {
        followTailMenu.selectedProperty().bindBidirectional(followingTail);
        toggleFollowTailButton.selectedProperty().bindBidirectional(followingTail);
        logsTable.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.getDeltaY() > 0) {
                // scrolling up, stop following tail
                followingTail.set(false);
            } else if (event.getDeltaY() < 0 && UIUtils.getLastVisibleRowIndex(logsTable) == filteredLogs.size() - 1) {
                // scrolling down and reached the bottom
                // we can't prevent the stick effect, so we might as well consider the state changed
                followingTail.set(true);
            }
        });
        followingTail.addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                scrollToBottom();
            } else {
                // move back up 1 row to unstick the scroll
                int indexFirst = UIUtils.getFirstVisibleRowIndex(logsTable);
                if (indexFirst > 1 && !logsTable.getItems().isEmpty()) {
                    logsTable.scrollTo(indexFirst - 1);
                }
            }
        });
        logsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                followingTail.set(!followingTail.get());
            }
        });

        // keep scroll to bottom as logs are added
        // note: this is only necessary when the scrollbar appears, because being at the scrollbar maximum already
        // realizes the scroll-to-bottom feature naturally
        filteredLogs.addListener((Change<? extends LogEntry> c) -> {
            while (c.next()) {
                if ((c.wasAdded() || c.wasRemoved()) && followingTail.get()) {
                    scrollToBottom();
                }
            }
        });
    }

    private void scrollToBottom() {
        if (!filteredLogs.isEmpty()) {
            logsTable.scrollTo(filteredLogs.size() - 1);
        }
    }

    /**
     * Opens the custom colorizers window.
     */
    @FXML
    public void editColorizers() {
        if (!colorizersStage.isShowing()) {
            colorizersStage.showAndWait();
        } else {
            colorizersStage.toFront();
        }
    }

    /**
     * Opens the custom columnizers window.
     */
    @FXML
    public void editColumnizers() {
        if (!columnizersStage.isShowing()) {
            columnizersStage.showAndWait();
        } else {
            columnizersStage.toFront();
        }
    }

    /**
     * Opens the preferences window.
     */
    @FXML
    public void editPreferences() {
        if (!preferencesStage.isShowing()) {
            preferencesStage.showAndWait();
        } else {
            preferencesStage.toFront();
        }
    }

    /**
     * Opens the about window.
     */
    @FXML
    public void about() {
        if (!aboutStage.isShowing()) {
            aboutStage.showAndWait();
        } else {
            aboutStage.toFront();
        }
    }

    /**
     * Opens a file chooser to choose the file to tail, and starts tailing the selected file.
     */
    @FXML
    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Log File");
        fileChooser.getExtensionFilters()
                   .add(new ExtensionFilter("Log files (*.txt, *.log, *.out)", "*.txt", "*.log", "*.out"));
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
     *
     * @param filename
     *         the recent file to tail
     */
    public void openRecentFile(String filename) {
        try {
            startTailingFile(new File(filename));
        } catch (FileNotFoundException e) {
            config.getState().removeFromRecentFiles(filename);
            ErrorDialog.recentFileNotFound(filename);
        }
    }

    /**
     * Starts tailing the given file, thus updating the log lines in the table.
     *
     * @param file
     *         the file to tail
     *
     * @throws FileNotFoundException
     *         if the file was not found
     */
    public void startTailingFile(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        closeCurrentFile();
        config.getState().addToRecentFiles(file.getAbsolutePath());
        logTailListener = new LogTailListener(columnizer.getValue(), columnizedLogs);
        logTailListener.skipEmptyLogsProperty().bind(config.getPreferences().skipEmptyLogsProperty());
        logTailListener.limitNumberOfLogsProperty().bind(config.getPreferences().limitNumberOfLogsProperty());
        logTailListener.maxNumberOfLogsProperty().bind(config.getPreferences().maxNumberOfLogsProperty());
        tailer = Tailer.create(file, logTailListener, config.getPreferences().getTailingDelayInMillis());
        tailingFile.set(true);
        tailedFileName.set(file.getAbsolutePath());
    }

    /**
     * Closes and re-opens the file being tailed. Useful to update the columnization for instance.
     */
    @FXML
    public void restartTailing() {
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
     * Clears the current logs.
     */
    @FXML
    public void clearLogs() {
        columnizedLogs.clear();
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
        tailedFileName.set("");
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
        copySelectedLogsToClipboard(log -> log.toColumnizedString(columnDefinitions, "\t"), headers + '\n');
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
        selectTheme(Theme.DARK);
    }

    /**
     * Switches to the bright theme.
     */
    @FXML
    public void selectBrightTheme() {
        selectTheme(Theme.LIGHT);
    }

    private void selectTheme(Theme theme) {
        theme.apply(mainPane.getScene(), colorizersStage.getScene(), columnizersStage.getScene());
        Config.getInstance().getState().setCurrentTheme(theme);
    }

    /**
     * Opens the web page containing the user manual.
     */
    @FXML
    public void openUserManual() {
        try {
            Desktop.getDesktop().browse(new URI(FXLog.GITHUB_URL));
        } catch (IOException | URISyntaxException e) {
            ErrorDialog.uncaughtException(e);
        }
    }

    /**
     * Checks for available updates of FX Log.
     */
    @FXML
    public void checkForUpdates() {
        VersionChecker.checkForUpdates(true);
    }
}
