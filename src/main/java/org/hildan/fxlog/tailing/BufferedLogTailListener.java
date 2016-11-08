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
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link TailerListener} that columnizes logs and adds them to a list as they arrive.
 */
public class BufferedLogTailListener extends TailerListenerAdapter {

    private static final int MAX_LINES_PER_LOG = 200;

    private final Columnizer columnizer;

    private final List<LogEntry> logs;

    private final BooleanProperty skipEmptyLogs;

    private final BooleanProperty limitNumberOfLogs;

    private final ObjectProperty<Integer> maxNumberOfLogs;

    private volatile boolean running;

    private volatile boolean clearRequested;

    private final List<LogEntry> buffer;

    private final int bufferMaxSize;

    private final StringBuilder aggregatedLines;

    private int nbLinesInCurrentLog;

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
        this.aggregatedLines = new StringBuilder();
        this.nbLinesInCurrentLog = 0;
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
        if (running && !shouldSkipLog(line)) {
            aggregateLine(line);
            handleAggregatedLog(aggregatedLines.toString());
        }
    }

    private void aggregateLine(@NotNull String line) {
        if (nbLinesInCurrentLog > 0) {
            aggregatedLines.append("\n");
        }
        aggregatedLines.append(line);
        nbLinesInCurrentLog++;
    }

    private boolean shouldSkipLog(@NotNull String line) {
        return skipEmptyLogs.get() && line.isEmpty() && nbLinesInCurrentLog == 0;
    }

    private void handleAggregatedLog(@NotNull String logText) {
        LogEntry log = columnizer.parse(logText);
        if (log != null) {
            handleNewLog(log);
        } else if (nbLinesInCurrentLog >= MAX_LINES_PER_LOG) {
            System.out.println("Reached max lines for a single log:\n" + logText);
            handleNewLog(columnizer.createDefaultLogEntry(logText));
        }
    }

    private void handleNewLog(@NotNull LogEntry log) {
        addToBuffer(log);
        aggregatedLines.setLength(0);
        nbLinesInCurrentLog = 0;
    }

    @Override
    public void endOfFileReached() {
        requestBufferDumpIntoLogsList();
    }

    @Override
    public synchronized void fileRotated() {
        buffer.clear();
    }

    private synchronized void addToBuffer(@NotNull LogEntry log) {
        buffer.add(log);
        // limit batches size
        if (buffer.size() >= bufferMaxSize && !clearRequested) {
            requestBufferDumpIntoLogsList();
        }
    }

    private synchronized void requestBufferDumpIntoLogsList() {
        clearRequested = true;
        // needs to run on the main thread to avoid concurrent modifications
        Platform.runLater(() -> {
            // we need to check again here because the listener may have been stopped in the meantime
            if (running) {
                dumpBufferIntoLogsList();
            }
        });
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
