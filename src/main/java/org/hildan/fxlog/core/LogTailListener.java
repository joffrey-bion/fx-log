package org.hildan.fxlog.core;

import java.util.List;

import javafx.application.Platform;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.errors.ErrorDialog;

/**
 * An implementation of {@link TailerListener} that columnizes logs and adds them to a list as they arrive.
 */
public class LogTailListener implements TailerListener {

    private final Columnizer columnizer;

    private final List<LogEntry> logs;

    private volatile boolean running;

    /**
     * Creates a new LogTailListener adding to the given log list using the given columnizer.
     *
     * @param columnizer
     *         the columnizer to use to columnized the raw logs
     * @param logs
     *         the list of logs to add to
     */
    public LogTailListener(Columnizer columnizer, List<LogEntry> logs) {
        this.columnizer = columnizer;
        this.logs = logs;
    }

    @Override
    public void init(Tailer tailer) {
        running = true;
    }

    @Override
    public void fileNotFound() {
        Platform.runLater(() -> ErrorDialog.selectedFileNotFound(null));
    }

    /**
     * Prevents this listener from modifying the logs list.
     */
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
        // avoid polluting vertical space with empty lines
        if (running && !line.isEmpty()) {
            LogEntry log = columnizer.parse(line);
            // needs to run on the main thread to avoid concurrent modifications
            Platform.runLater(() -> {
                // we need to check again here because the listener may have been stopped in the meantime
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
