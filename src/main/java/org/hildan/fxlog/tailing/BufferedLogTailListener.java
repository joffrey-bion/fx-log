package org.hildan.fxlog.tailing;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.data.LogEntry;

/**
 * An implementation of {@link TailerListener} that columnizes logs and adds them to a list as they arrive.
 */
public class BufferedLogTailListener extends TailerListenerAdapter {

    private static final int DEFAULT_BUF_SIZE = 1000;

    private final Columnizer columnizer;

    private final List<LogEntry> logs;

    private final BooleanProperty skipEmptyLogs;

    private final BooleanProperty limitNumberOfLogs;

    private final ObjectProperty<Integer> maxNumberOfLogs;

    private volatile boolean running;

    private volatile boolean clearRequested;

    private final List<LogEntry> buffer;

    private final int bufferMaxSize;

    /**
     * Creates a new BufferedLogTailListener adding to the given log list using the given columnizer, with default
     * buffer size and timeout.
     *
     * @param columnizer
     *         the columnizer to use to columnized the raw logs
     * @param logs
     *         the list of logs to add to
     */
    public BufferedLogTailListener(Columnizer columnizer, List<LogEntry> logs) {
        this(columnizer, logs, DEFAULT_BUF_SIZE);
    }

    /**
     * Creates a new BufferedLogTailListener adding to the given log list using the given columnizer, with the given
     * buffer size and timeout.
     *
     * @param columnizer
     *         the columnizer to use to columnized the raw logs
     * @param logs
     *         the list of logs to add to
     * @param logBufferSize
     *         the max number of logs in the buffer before sending them to the UI
     */
    public BufferedLogTailListener(Columnizer columnizer, List<LogEntry> logs, int logBufferSize) {
        this.columnizer = columnizer;
        this.logs = logs;
        this.skipEmptyLogs = new SimpleBooleanProperty(false);
        this.limitNumberOfLogs = new SimpleBooleanProperty(false);
        this.maxNumberOfLogs = new SimpleObjectProperty<>(Integer.MAX_VALUE);
        this.buffer = new ArrayList<>(logBufferSize);
        this.bufferMaxSize = logBufferSize;
    }

    @Override
    public void init(Tailer tailer) {
        running = true;
    }

    /**
     * Prevents this listener from modifying the logs list.
     */
    public void stop() {
        running = false;
    }

    @Override
    public void handle(String line) {
        if (running && !(skipEmptyLogs.get() && line.isEmpty())) {
            LogEntry log = columnizer.parse(line);
            addToBuffer(log);
        }
    }

    @Override
    public synchronized void endOfFileReached() {
        clearRequested = true;
        // needs to run on the main thread to avoid concurrent modifications
        Platform.runLater(() -> {
            // we need to check again here because the listener may have been stopped in the meantime
            if (running) {
                dumpBufferIntoLogsList();
            }
        });
    }

    @Override
    public synchronized void fileRotated() {
        buffer.clear();
    }

    private synchronized void addToBuffer(LogEntry log) {
        buffer.add(log);
        // limit batches size
        if (buffer.size() >= bufferMaxSize && !clearRequested) {
            endOfFileReached();
        }
    }

    private synchronized void dumpBufferIntoLogsList() {
        if (limitNumberOfLogs.get()) {
            int nbExtraLogs = logs.size() + buffer.size() - maxNumberOfLogs.get();
            if (nbExtraLogs > 0) {
                logs.subList(0, nbExtraLogs).clear();
            }
        }
        logs.addAll(buffer);
        clearRequested = false;
        buffer.clear();
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
        return maxNumberOfLogs.get();
    }

    public ObjectProperty<Integer> maxNumberOfLogsProperty() {
        return maxNumberOfLogs;
    }

    public void setMaxNumberOfLogs(Integer maxNumberOfLogs) {
        this.maxNumberOfLogs.set(maxNumberOfLogs);
    }
}
