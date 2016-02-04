package org.hildan.fxlog.controllers;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    private ChoiceBox<Columnizer> columnizerSelector;

    @FXML
    private ChoiceBox<Colorizer> colorizerSelector;

    @FXML
    private TextField filterField;

    private Property<Columnizer> columnizer;

    private Property<Colorizer> colorizer;

    private ObservableList<LogEntry> columnizedLogs;

    private FilteredList<LogEntry> filteredLogs;

    private Property<Predicate<LogEntry>> filter;

    private Tailer tailer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        columnizedLogs = FXCollections.observableArrayList();
        filteredLogs = new FilteredList<>(columnizedLogs);
        filter = new SimpleObjectProperty<>(log -> true);
        colorizer = new SimpleObjectProperty<>(Colorizer.WEBLOGIC);
        columnizer = new SimpleObjectProperty<>(Columnizer.WEBLOGIC);
        configureColumnizerSelector();
        configureColorizerSelector();
        configureFiltering();
        configureLogsTable();
    }

    private void configureColorizerSelector() {
        colorizerSelector.setItems(FXCollections.observableArrayList(Colorizer.WEBLOGIC));
//        colorizerSelector.getSelectionModel().selectFirst();
        colorizer.bindBidirectional(colorizerSelector.valueProperty());
        colorizer.setValue(Colorizer.WEBLOGIC);
    }

    private void configureColumnizerSelector() {
        columnizerSelector.setItems(FXCollections.observableArrayList(Columnizer.WEBLOGIC, Columnizer.TEST));
//        colorizerSelector.getSelectionModel().selectFirst();
        columnizer.bindBidirectional(columnizerSelector.valueProperty());
        columnizer.setValue(Columnizer.WEBLOGIC);
    }

    private void configureFiltering() {
        filteredLogs.predicateProperty().bind(filter);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                filter.setValue(new RawFilter(".*?" + newValue + ".*"));
            } else {
                filter.setValue(log -> true);
            }
        });
    }

    private void configureLogsTable() {
        logsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logsTable.getColumns().addAll(columnizer.getValue().getColumns());
        columnizer.addListener((observable, oldValue, newValue) -> {
            logsTable.getColumns().clear();
            logsTable.getColumns().addAll(newValue.getColumns());
        });
        logsTable.setItems(filteredLogs);
        logsTable.setRowFactory(table -> {
            final TableRow<LogEntry> row = new TableRow<LogEntry>() {
                @Override
                protected void updateItem(LogEntry log, boolean empty) {
                    super.updateItem(log, empty);
                    if (log != null && !empty) {
                        colorizer.getValue().setStyle(this, log);
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
            closeFile();
            LogTailListener listener = new LogTailListener(columnizer.getValue(), columnizedLogs);
            tailer = Tailer.create(file, listener, 500);
        }
    }

    public void closeFile() {
        if (tailer != null) {
            tailer.stop();
        }
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
}
