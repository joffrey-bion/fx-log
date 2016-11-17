package org.hildan.fxlog.data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hildan.fxlog.columns.ColumnDefinition;

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
    public Map<String, String> getSections() {
        return columnValues;
    }

    /**
     * Gets collection of this log's column values for the currently visible columns.
     *
     * @param columnDefinitions
     *         the column definitions of this logs, to be able to give the values in proper order
     *
     * @return a collection of this log's column values for the currently visible columns
     */
    public List<String> getVisibleColumnValues(List<ColumnDefinition> columnDefinitions) {
        return columnDefinitions.stream()
                .filter(ColumnDefinition::isVisible)
                .flatMap(cd -> cd.getCapturingGroupNames().stream())
                .map(col -> getSections().get(col))
                .collect(Collectors.toList());
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
     * Gets a string representation of this log's column values.
     *
     * @param columnDefinitions
     *         the column definitions of this logs, to be able to give the values in proper order
     * @param delimiter
     *         the delimiter to place between the columns in the resulting string
     *
     * @return a string this log's column values separated by the given delimiter
     */
    public String toColumnizedString(List<ColumnDefinition> columnDefinitions, CharSequence delimiter) {
        return getVisibleColumnValues(columnDefinitions).stream().collect(Collectors.joining(delimiter));
    }

    @Override
    public String toString() {
        return rawLine();
    }
}
