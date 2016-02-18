package org.hildan.fxlog.core;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.hildan.fxlog.columns.Columnizer;

/**
 * An implementation of {@link TailerListener} that columnizes logs and adds them to a list as they arrive.
 */
public class BufferedLogTailListener extends LogTailListener {

    private static final int DEFAULT_BUF_SIZE = 10;

    private static final int DEFAULT_TIMEOUT_MS = 100;

    private final List<LogEntry> buffer;

    private Instant lastBufferClear;

    private final int bufferMaxSize;

    private final int bufferClearTimeout;

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
        this(columnizer, logs, DEFAULT_BUF_SIZE, DEFAULT_TIMEOUT_MS);
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
     * @param bufferClearTimeout
     *         the max time to wait before sending new logs to the UI
     */
    public BufferedLogTailListener(Columnizer columnizer, List<LogEntry> logs, int logBufferSize,
                                   int bufferClearTimeout) {
        super(columnizer, logs);
        this.buffer = new ArrayList<>(logBufferSize);
        this.bufferMaxSize = logBufferSize;
        this.bufferClearTimeout = bufferClearTimeout;
    }

    @Override
    public void init(Tailer tailer) {
        super.init(tailer);
        lastBufferClear = Instant.now();
    }

    @Override
    public void fileRotated() {
        lastBufferClear = Instant.now();
        buffer.clear();
        super.fileRotated();
    }

    @Override
    public void handle(String line) {
        // avoid polluting vertical space with empty lines
        if (running && !(skipEmptyLogs.get() && line.isEmpty())) {
            LogEntry log = columnizer.parse(line);
            buffer.add(log);
            if (needsToClearBuffer()) {
                synchronized (buffer) {
                    List<LogEntry> toSendToUI = new ArrayList<>(buffer);
                    buffer.clear();
                    lastBufferClear = Instant.now();
                    // needs to run on the main thread to avoid concurrent modifications
                    Platform.runLater(() -> {
                        // we need to check again here because the listener may have been stopped in the meantime
                        if (running) {
                            logs.addAll(toSendToUI);
                        }
                    });
                }
            }
        }
    }

    private boolean needsToClearBuffer() {
        boolean tooManyLogs = buffer.size() == bufferMaxSize;
        boolean waitedForTooLong = Duration.between(lastBufferClear, Instant.now()).toMillis() > bufferClearTimeout;
        return tooManyLogs || waitedForTooLong;
    }
}
