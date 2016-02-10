package org.hildan.fxlog.filtering;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.hildan.fxlog.core.LogEntry;
import org.jetbrains.annotations.NotNull;

/**
 * A log filter based on a regexp matching the raw log entry or a column in a log.
 */
public class Filter implements Predicate<LogEntry> {

    private final StringProperty columnName;

    private final Property<Pattern> pattern;

    /**
     * Creates a new filter.
     *
     * @param columnName
     *         the capturing group name corresponding to the column to apply this filter to, or null to apply this
     *         filter to the whole raw log line
     * @param regex
     *         the regex that the given column (or whole line) should match
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    private Filter(String columnName, String regex) throws PatternSyntaxException {
        this.pattern = new SimpleObjectProperty<>(Pattern.compile(regex));
        this.columnName = new SimpleStringProperty(columnName);
    }

    /**
     * Creates a new filter which applies on the raw log line.
     *
     * @param regex
     *         the regex that the whole raw log line should match
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    @NotNull
    public static Filter findInRawLog(@NotNull String regex) {
        return new Filter(null, regex);
    }

    /**
     * Creates a new filter which applies on the value of the given column.
     *
     * @param columnName
     *         the capturing group name corresponding to the column to apply this filter to
     * @param regex
     *         the regex that the whole value of the given column should match
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    @NotNull
    public static Filter findInColumn(@NotNull String columnName, @NotNull String regex) {
        return new Filter(columnName, regex);
    }

    public String getColumnName() {
        return columnName.get();
    }

    public StringProperty columnNameProperty() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName.set(columnName);
    }

    public Pattern getPattern() {
        return pattern.getValue();
    }

    public Property<Pattern> patternProperty() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern.setValue(pattern);
    }

    @Override
    public boolean test(LogEntry log) {
        if (columnName.get() == null) {
            return pattern.getValue().matcher(log.rawLine()).find();
        } else {
            String columnValue = log.getColumnValues().get(columnName.get());
            return columnValue != null && pattern.getValue().matcher(columnValue).find();
        }
    }
}
