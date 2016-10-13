package org.hildan.fxlog.tailing;

import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.data.LogEntry;
import org.hildan.fxlog.errors.ErrorDialog;

/**
 * An implementation of {@link TailerListener} that columnizes logs and adds them to a list as they arrive.
 */
public class LogTailListener implements TailerListener {

    protected final Columnizer columnizer;

    protected final List<LogEntry> logs;

    protected final BooleanProperty skipEmptyLogs;

    protected final BooleanProperty limitNumberOfLogs;

    protected final Property<Integer> maxNumberOfLogs;

    protected volatile boolean running;

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
        this.skipEmptyLogs = new SimpleBooleanProperty(false);
        this.limitNumberOfLogs = new SimpleBooleanProperty(false);
        this.maxNumberOfLogs = new SimpleObjectProperty<>(0);
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
        if (running && !(skipEmptyLogs.get() && line.isEmpty())) {
            LogEntry log = columnizer.parse(line);
            // needs to run on the main thread to avoid concurrent modifications
            Platform.runLater(() -> {
                // we need to check again here because the listener may have been stopped in the meantime
                if (running) {
                    appendLog(log);
                }
            });
        }
    }

    private void appendLog(LogEntry log) {
        if (limitNumberOfLogs.get()) {
            while (logs.size() >= maxNumberOfLogs.getValue()) {
                logs.remove(0);
            }
        }
        logs.add(log);
    }

    @Override
    public void handle(Exception ex) {
        if (running) {
            // will be handled as a dialog at top level
            throw new RuntimeException("Exception while reading the log file", ex);
        }
    }

    public boolean getSkipEmptyLogs() {
        return skipEmptyLogs.get();
    }

    public BooleanProperty skipEmptyLogsProperty() {
        return skipEmptyLogs;
    }

    public void setSkipEmptyLogs(boolean skipEmptyLogs) {
        this.skipEmptyLogs.set(skipEmptyLogs);
    }

    public boolean isLimitNumberOfLogs() {
        return limitNumberOfLogs.get();
    }

    public BooleanProperty limitNumberOfLogsProperty() {
        return limitNumberOfLogs;
    }

    public void setLimitNumberOfLogs(boolean limitNumberOfLogs) {
        this.limitNumberOfLogs.set(limitNumberOfLogs);
    }

    public Integer getMaxNumberOfLogs() {
        return maxNumberOfLogs.getValue();
    }

    public Property<Integer> maxNumberOfLogsProperty() {
        return maxNumberOfLogs;
    }

    public void setMaxNumberOfLogs(Integer maxNumberOfLogs) {
        this.maxNumberOfLogs.setValue(maxNumberOfLogs);
    }
}
