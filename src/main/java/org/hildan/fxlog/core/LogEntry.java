package org.hildan.fxlog.core;

import java.util.Map;

/**
 * Represents a columnized log entry.
 */
public class LogEntry {

    private final Map<String, String> columnValues;

    private final String initialLog;

    public LogEntry(Map<String, String> columnValues, String initialLog) {
        this.columnValues = columnValues;
        this.initialLog = initialLog;
    }

    public Map<String, String> getColumnValues() {
        return columnValues;
    }

    public String getInitialLog() {
        return initialLog;
    }
}
