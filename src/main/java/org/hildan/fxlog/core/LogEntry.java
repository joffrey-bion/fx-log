package org.hildan.fxlog.core;

import java.util.Map;

/**
 * Represents a columnized log entry.
 */
public class LogEntry {

    private final Map<String, String> columnValues;

    private final String initialLog;

    /**
     * Creates a new log entry with the given values for each column.
     *
     * @param columnValues
     *         the values for each column. The keys are the names of the capturing groups corresponding to the columns,
     *         and the values are the content of each column for this log.
     * @param rawLine
     *         the initial raw log line before being parsed into these columns. This allows later raw filtering.
     */
    public LogEntry(Map<String, String> columnValues, String rawLine) {
        this.columnValues = columnValues;
        this.initialLog = rawLine;
    }

    /**
     * Gets this log's content for each column. The keys are the names of the capturing groups corresponding to the
     * columns, and the values are the contents of each column for this log.
     *
     * @return this log's content for each column
     */
    public Map<String, String> getColumnValues() {
        return columnValues;
    }

    /**
     * Gets the initial raw log line before being parsed into columns.
     *
     * @return the raw log line
     */
    public String rawLine() {
        return initialLog;
    }

    /**
     * Gets a tab-separated representation of this log used its columns values.
     *
     * @return a tab-separated string of this log's column values
     */
    public String toColumnizedString() {
        return String.join("\t", columnValues.values());
    }
}
