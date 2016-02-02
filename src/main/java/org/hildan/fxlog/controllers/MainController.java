package org.hildan.fxlog.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.core.LogEntry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MainController implements Initializable {

    // TODO externalize this
    private static final Columnizer TEST_COLUMNIZER;

    static {
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(5);
        columnDefinitions.add(new ColumnDefinition("Date", "date"));
        columnDefinitions.add(new ColumnDefinition("Level", "level"));
        columnDefinitions.add(new ColumnDefinition("Class", "class"));
        columnDefinitions.add(new ColumnDefinition("Message", "msg"));
        columnDefinitions.add(new ColumnDefinition("JSessionID", "sid"));
        List<String> regexps =
                Arrays.asList("<(?<date>.*?)> <(?<level>.*?)> <(?<class>.*?)> <> <(?<msg>.*?);jsessionid=(?<sid>.*?)>",
                        "(?<msg>.*)");
        TEST_COLUMNIZER = new Columnizer(columnDefinitions, regexps);
    }

    // TODO externalize this
    private static final Columnizer AMADEUS_BE_COLUMNIZER;

    static {
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(5);
        columnDefinitions.add(new ColumnDefinition("Date", "date"));
        columnDefinitions.add(new ColumnDefinition("Level", "level"));
        columnDefinitions.add(new ColumnDefinition("Group", "group"));
        columnDefinitions.add(new ColumnDefinition("Host", "host"));
        columnDefinitions.add(new ColumnDefinition("Server Name", "server"));
        columnDefinitions.add(new ColumnDefinition("Thread", "thread"));
        columnDefinitions.add(new ColumnDefinition("Timestamp?", "timestamp"));
        columnDefinitions.add(new ColumnDefinition("???", "xxxx"));
        columnDefinitions.add(new ColumnDefinition("Class", "class"));
        columnDefinitions.add(new ColumnDefinition("Message", "msg"));
        columnDefinitions.add(new ColumnDefinition("JSessionID", "sid"));
        List<String> regexps = Arrays.asList(
                "####<(?<date>.*?)> <(?<level>.*?)> <(?<group>.*?)> <(?<host>.*?)> <(?<server>.*?)> <(?<thread>.*?)> <<anonymous>> <> <> <(?<timestamp>.*?)> <(?<xxxx>.*?)> <(?<class>.*?)> <(?<msg>.*?);jsessionid=(?<sid>.*?)>",
                "(?<msg>.*)");
        AMADEUS_BE_COLUMNIZER = new Columnizer(columnDefinitions, regexps);
    }

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<LogEntry> logsTable;

    private Columnizer columnizer;

    private Predicate<LogEntry> filter;

    private Path filePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO make it customizable
        columnizer = AMADEUS_BE_COLUMNIZER;
        filter = log -> true;
        initColumns();
    }

    private void initColumns() {
        logsTable.getColumns().addAll(columnizer.getColumns());
    }

    public void openFile(@SuppressWarnings("unused") ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Log files (*.txt, *.log)", "*.txt", "*.log"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Other files", "*.*"));
        File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (file != null) {
            // a file has indeed been chosen
            filePath = Paths.get(file.toURI());
            refreshLogsTable();
        }
    }

    private void refreshLogsTable() {
        logsTable.getItems().clear();
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.map(columnizer::parse).filter(filter).forEach(logsTable.getItems()::add);
        } catch (IOException e) {
            System.err.println("Error while reading the file " + filePath);
        }
    }
}
