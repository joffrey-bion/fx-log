package org.hildan.fxlog.columns;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.hildan.fxlog.core.LogEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Uses columns definitions to split log lines into columns.
 */
public class Columnizer {

    private final List<Pattern> patterns;

    private final List<ColumnDefinition> columnDefinitions;

    /**
     * Creates a new Columnizer with the given definitions.
     *
     * @param columnDefinitions
     *         the column definitions to use
     * @param regexps
     *         the regexp to try and match when parsing logs
     * @throws PatternSyntaxException
     *         if one of the regexps' syntax is invalid
     */
    public Columnizer(List<ColumnDefinition> columnDefinitions, List<String> regexps) throws PatternSyntaxException {
        if (columnDefinitions.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one column definition");
        }
        this.columnDefinitions = columnDefinitions;
        this.patterns = regexps.stream().map(Pattern::compile).collect(Collectors.toList());
    }

    /**
     * Returns the columns associated to this columnizer. They can directly be added to a {@link TableView}.
     *
     * @return the columns associated to this columnizer
     */
    public List<TableColumn<LogEntry, String>> getColumns() {
        List<TableColumn<LogEntry, String>> columns = new ArrayList<>();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            TableColumn<LogEntry, String> col = new TableColumn<>(columnDefinition.getHeaderLabel());
            col.setCellValueFactory(data -> {
                LogEntry log = data.getValue();
                String cellValue = log.getColumnValues().get(columnDefinition.getCapturingGroupName());
                return new ReadOnlyStringWrapper(cellValue);
            });
            columns.add(col);
        }
        return columns;
    }

    /**
     * Parses the given input line to create a {@link LogEntry} following the rules of this Columnizer.
     * <p>
     * This method tries to match every regexp of this Columnizer in the order they were given to the constructor. The
     * column values are taken from the capturing groups of the first matched pattern. Missing capturing groups simply
     * yield empty strings.
     * <p>
     * If no regexp is matched, a LogEntry is still returned, containing the whole input string in the first column.
     *
     * @param inputLogLine
     *         the raw log string to parse
     * @return the parsed {@code LogEntry}
     */
    public LogEntry parse(String inputLogLine) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(inputLogLine.trim());
            if (matcher.matches()) {
                Map<String, String> columnValues = new HashMap<>(columnDefinitions.size());
                for (ColumnDefinition columnDefinition : columnDefinitions) {
                    String groupName = columnDefinition.getCapturingGroupName();
                    // we take all the values we can from this pattern
                    String value = getGroupValueOrEmptyString(matcher, groupName);
                    columnValues.put(groupName, value);
                }
                return new LogEntry(columnValues, inputLogLine);
            }
        }
        // no pattern matched, put everything in the first column
        Map<String, String> columnValues = new HashMap<>(columnDefinitions.size());
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            String groupName = columnDefinition.getCapturingGroupName();
            columnValues.put(groupName, "");
        }
        columnValues.put(columnDefinitions.get(0).getCapturingGroupName(), inputLogLine);
        return new LogEntry(columnValues, inputLogLine);
    }

    /**
     * Safely tries to get the value for a capturing group. If the group does not exist in the given matcher, this
     * method simply returns the empty string for display purposes.
     *
     * @param matcher
     *         the matcher to get the group value from
     * @param groupName
     *         the name of the capturing group for which to get the value
     * @return the value of the given capturing group, or the empty string if the group is missing
     */
    private static String getGroupValueOrEmptyString(Matcher matcher, String groupName) {
        try {
            return matcher.group(groupName);
        } catch (IllegalArgumentException e) {
            // case where the group name does not exist in the parent pattern
            return "";
        }
    }
}
