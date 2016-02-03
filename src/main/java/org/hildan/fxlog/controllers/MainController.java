package org.hildan.fxlog.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.io.input.Tailer;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.core.LogTailListener;
import org.hildan.fxlog.filtering.RawFilter;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<LogEntry> logsTable;

    @FXML
    private ToggleButton autoScrollToggleButton;

    @FXML
    private TextField filterField;

    private Columnizer columnizer;

    private Colorizer colorizer;

    private ObservableList<LogEntry> columnizedLogs;

    private FilteredList<LogEntry> filteredLogs;

    private Tailer tailer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO make it customizable
        columnizer = Columnizer.WEBLOGIC;
        colorizer = Colorizer.WEBLOGIC;
        columnizedLogs = FXCollections.observableArrayList();
        configureLogsTable();
        updateFilter(null);
    }

    private void configureLogsTable() {
        logsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logsTable.getColumns().addAll(columnizer.getColumns());
        filteredLogs = new FilteredList<>(columnizedLogs);
        filteredLogs.setPredicate(null);
        logsTable.setItems(filteredLogs);
        logsTable.setRowFactory(table -> {
            final TableRow<LogEntry> row = new TableRow<LogEntry>() {
                @Override
                protected void updateItem(LogEntry log, boolean empty) {
                    super.updateItem(log, empty);
                    if (log != null && !empty) {
                        colorizer.setStyle(this, log);
                    } else {
                        setStyle(null);
                    }
                }
            };
            return row;
        });
    }

    public void openFile(@SuppressWarnings("unused") ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Log File");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Log files (*.txt, *.log)", "*.txt", "*.log"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Other files", "*.*"));
        File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (file != null) {
            closeFile(null);
            LogTailListener listener = new LogTailListener(columnizer, columnizedLogs);
            tailer = Tailer.create(file, listener, 500);
        }
    }

    public void closeFile(@SuppressWarnings("unused") ActionEvent event) {
        tailer.stop();
        columnizedLogs.clear();
    }

    public void openPreferences(@SuppressWarnings("unused") ActionEvent event) {
        // TODO handle preferences
    }

    public void quit(@SuppressWarnings("unused") ActionEvent event) {
        Platform.exit();
    }

    public void copyRaw(@SuppressWarnings("unused") ActionEvent event) {
        copySelectedLogsToClipboard(LogEntry::getInitialLog);
    }

    public void copyPretty(@SuppressWarnings("unused") ActionEvent event) {
        copySelectedLogsToClipboard(LogEntry::toColumnizedString);
    }

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

    public void selectAll(@SuppressWarnings("unused") ActionEvent event) {
        logsTable.getSelectionModel().selectAll();
    }

    public void unselectAll(@SuppressWarnings("unused") ActionEvent event) {
        logsTable.getSelectionModel().clearSelection();
    }

    public void toggleAutoscroll(@SuppressWarnings("unused") ActionEvent event) {
        // TODO handle auto-scroll
    }

    public void updateFilter(@SuppressWarnings("unused") ActionEvent event) {
        String filterText = filterField.getText();
        Predicate<LogEntry> filter = null;
        if (!filterText.isEmpty()) {
            filter = new RawFilter(".*?" + filterText + ".*");
        }
        filteredLogs.setPredicate(filter);
    }
}
