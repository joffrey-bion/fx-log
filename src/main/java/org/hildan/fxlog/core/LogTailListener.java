package org.hildan.fxlog.core;

import java.util.List;

import javafx.application.Platform;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.hildan.fxlog.columns.Columnizer;

public class LogTailListener extends TailerListenerAdapter {

    private Columnizer columnizer;

    private List<LogEntry> logs;

    private volatile boolean running;

    public LogTailListener(Columnizer columnizer, List<LogEntry> logs) {
        this.columnizer = columnizer;
        this.logs = logs;
    }

    @Override
    public void init(Tailer tailer) {
        running = true;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void fileRotated() {
        if (running) {
            // needs to run on the main thread to avoid concurrent modifications
            Platform.runLater(() -> {
                if (running) {
                    logs.clear();
                }
            });
        }
    }

    @Override
    public void handle(String line) {
        if (running) {
            LogEntry log = columnizer.parse(line);
            // needs to run on the main thread to avoid concurrent modifications
            Platform.runLater(() -> {
                if (running) {
                    logs.add(log);
                }
            });
        }
    }

    @Override
    public void handle(Exception ex) {
        if (running) {
            // will be handled as a dialog at top level
            throw new RuntimeException("Exception while reading the log file", ex);
        }
    }
}
