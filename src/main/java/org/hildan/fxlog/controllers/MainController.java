package org.hildan.fxlog.controllers;

import javafx.application.Platform;
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
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.RawFilter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private Predicate<LogEntry> filter;

    private Colorizer colorizer;

    private Path currentFilePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO make it customizable
        columnizer = Columnizer.WEBLOGIC;
        colorizer = Colorizer.WEBLOGIC;
        configureLogsTable();
        refreshFilter();
        refreshLogsTable();
    }

    private void configureLogsTable() {
        logsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logsTable.getColumns().addAll(columnizer.getColumns());
    }

    private void refreshFilter() {
        String filterText = filterField.getText();
        if (!filterText.isEmpty()) {
            filter = new RawFilter(".*?" + filterText + ".*");
        } else {
            filter = log -> true;
        }
    }

    private void refreshLogsTable() {
        logsTable.getItems().clear();
        if (currentFilePath == null) {
            return;
        }
        try (Stream<String> lines = Files.lines(currentFilePath)) {
            lines.map(columnizer::parse).filter(filter).forEach(logsTable.getItems()::add);
        } catch (IOException e) {
            System.err.println("Error while reading the file " + currentFilePath);
        }
        logsTable.setRowFactory(table -> {
            final TableRow<LogEntry> row = new TableRow<LogEntry>() {
                @Override
                protected void updateItem(LogEntry log, boolean empty) {
                    super.updateItem(log, empty);
                    if (log != null && !empty) {
                        System.out.println("coloring!");
                        colorizer.setStyle(this, log);
                    } else {
                        System.out.println("empty again");
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
            // a file has indeed been chosen
            currentFilePath = Paths.get(file.toURI());
            refreshLogsTable();
        }
    }

    public void closeFile(@SuppressWarnings("unused") ActionEvent event) {
        if (currentFilePath != null) {
            currentFilePath = null;
            refreshLogsTable();
        }
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

    public void filterLogs(@SuppressWarnings("unused") ActionEvent event) {
        refreshFilter();
        refreshLogsTable();
    }
}
