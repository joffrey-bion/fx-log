package org.hildan.fxlog.columns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.hildan.fx.components.list.Named;
import org.hildan.fxlog.data.LogEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Uses regexps to split log lines into columns.
 */
public class Columnizer implements Named {

    private final StringProperty name;

    private final ObservableList<Pattern> patterns;

    private final ObservableList<ColumnDefinition> columnDefinitions;

    /**
     * Creates a new Columnizer with the no columns and no patterns.
     *
     * @param name
     *         a name for this columnizer
     */
    public Columnizer(@NotNull String name) {
        this(name, FXCollections.observableArrayList(), Collections.emptyList());
    }

    /**
     * Creates a new Columnizer with the given definitions.
     *
     * @param name
     *         a name for this columnizer
     * @param columnDefinitions
     *         the column definitions to use
     * @param regexps
     *         the regexps to try and match when parsing logs. The order matters: the first matched regexp determines
     *         the capturing groups used to put parts of the log into the columns.
     * @throws PatternSyntaxException
     *         if one of the regexps' syntax is invalid
     */
    public Columnizer(@NotNull String name, @NotNull ObservableList<ColumnDefinition> columnDefinitions,
                      @NotNull Collection<String> regexps) throws PatternSyntaxException {
        this.name = new SimpleStringProperty(name);
        this.columnDefinitions = columnDefinitions;
        List<Pattern> patterns = regexps.stream().map(Pattern::compile).collect(Collectors.toList());
        this.patterns = FXCollections.observableArrayList(patterns);
    }

    /**
     * Creates a copy of the given Columnizer.
     *
     * @param source
     *         the rule to copy
     */
    public Columnizer(@NotNull Columnizer source) {
        this.name = new SimpleStringProperty(source.getName());
        this.columnDefinitions = FXCollections.observableArrayList(source.columnDefinitions);
        this.patterns = FXCollections.observableArrayList(source.patterns);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @SuppressWarnings("unused")
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    public ObservableList<Pattern> getPatterns() {
        return patterns;
    }

    public ObservableList<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    /**
     * Returns the columns associated to this columnizer. They can directly be added to a {@link TableView}.
     * <p>
     * The visibility and width of the returned columns are bound to the column definitions of this Columnizer, so that
     * they are stored in the config.
     *
     * @return the columns associated to this columnizer
     */
    @NotNull
    public List<TableColumn<LogEntry, String>> createColumns() {
        return columnDefinitions.stream().map(ColumnDefinition::createColumn).collect(Collectors.toList());
    }

    /**
     * Parses the given input log text to create a {@link LogEntry} following the rules of this Columnizer.
     * <p>
     * This method tries to match every regexp of this Columnizer in the order they were given to the constructor. The
     * column values are taken from the capturing groups of the first matched pattern. Missing capturing groups simply
     * yield empty strings.
     * <p>
     * If no regexp is matched, null is returned.
     *
     * @param inputLogLine
     *         the raw log string to parse
     * @return the parsed {@code LogEntry}, or null if no regex of this columnizer matched the input line
     */
    @Nullable
    public LogEntry parse(@NotNull String inputLogLine) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(inputLogLine);
            if (matcher.matches()) {
                return createMatchingLogEntry(inputLogLine, matcher);
            }
        }
        return null;
    }

    @NotNull
    private LogEntry createMatchingLogEntry(@NotNull String inputLogLine, @NotNull Matcher matcher) {
        Map<String, String> columnValues = new HashMap<>(columnDefinitions.size());
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            String groupName = columnDefinition.getCapturingGroupName();
            String value = getGroupValueOrEmptyString(matcher, groupName);
            columnValues.put(groupName, value);
        }
        return new LogEntry(columnValues, inputLogLine);
    }

    @NotNull
    public LogEntry createDefaultLogEntry(@NotNull String inputLogLine) {
        Map<String, String> columnValues = createEmptyColumnsMap();
        // put the whole line in the default column as a fallback, if possible
        if (!columnDefinitions.isEmpty()) {
            columnValues.put(getDefaultColumnCapturingGroup(), inputLogLine);
        }
        return new LogEntry(columnValues, inputLogLine);
    }

    @NotNull
    private Map<String, String> createEmptyColumnsMap() {
        Map<String, String> columnValues = new HashMap<>(columnDefinitions.size());
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            String groupName = columnDefinition.getCapturingGroupName();
            columnValues.put(groupName, "");
        }
        return columnValues;
    }

    private String getDefaultColumnCapturingGroup() {
        return columnDefinitions.get(0).getCapturingGroupName();
    }

    /**
     * Safely tries to get the input subsequence captured by the given capturing group. If the group does not exist in
     * the given matcher, this method simply returns the empty string for display purposes.
     *
     * @param matcher
     *         the matcher to get the group value from
     * @param groupName
     *         the name of the capturing group for which to get the value
     * @return the input subsequence captured by the given capturing group during the previous match, or the empty
     * string if the group is missing
     */
    @NotNull
    private static String getGroupValueOrEmptyString(@NotNull Matcher matcher, @NotNull String groupName) {
        try {
            String capturedValue = matcher.group(groupName);
            return capturedValue != null ? capturedValue : "";
        } catch (IllegalArgumentException e) {
            // case where the group name does not exist in the parent pattern
            return "";
        }
    }

    @Override
    public String toString() {
        return name.get();
    }
}
