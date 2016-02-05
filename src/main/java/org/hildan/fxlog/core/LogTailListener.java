package org.hildan.fxlog.core;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.hildan.fxlog.columns.Columnizer;

import java.util.List;

import javafx.application.Platform;

public class LogTailListener extends TailerListenerAdapter {

    private Columnizer columnizer;

    private List<LogEntry> logs;

    public LogTailListener(Columnizer columnizer, List<LogEntry> logs) {
        this.columnizer = columnizer;
        this.logs = logs;
    }

    @Override
    public void fileRotated() {
        // needs to run on the main thread to avoid concurrent modifications
        Platform.runLater(logs::clear);
    }

    @Override
    public void handle(String line) {
        LogEntry log =  columnizer.parse(line);
        // needs to run on the main thread to avoid concurrent modifications
        Platform.runLater(() -> logs.add(log));
    }

    @Override
    public void handle(Exception ex) {
        // will be handled as a dialog at top level
        throw new RuntimeException("Exception while reading the log file", ex);
    }
}
