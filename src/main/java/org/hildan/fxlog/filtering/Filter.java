package org.hildan.fxlog.filtering;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.hildan.fxlog.core.LogEntry;
import org.jetbrains.annotations.NotNull;

/**
 * A log filter based on a regexp matching the raw log entry or a column in a log.
 */
public class Filter implements Predicate<LogEntry> {

    private final String column;

    private final Pattern pattern;

    /**
     * Creates a new filter.
     *
     * @param column
     *         the capturing group name corresponding to the column to apply this filter to, or null to apply this
     *         filter to the whole raw log line
     * @param regex
     *         the regex that the given column (or whole line) should match
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    private Filter(String column, String regex) throws PatternSyntaxException {
        this.pattern = Pattern.compile(regex);
        this.column = column;
    }

    /**
     * Creates a new raw log filter.
     *
     * @param regex
     *         the regex that the raw log line should match
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    @NotNull
    public static Filter matchRawLog(@NotNull String regex) {
        return new Filter(null, regex);
    }

    /**
     * Creates a new column filter.
     *
     * @param columnName
     *         the capturing group name corresponding to the column to apply this filter to
     * @param regex
     *         the regex that the given column should match
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    @NotNull
    public static Filter matchColumn(@NotNull String columnName, @NotNull String regex) {
        return new Filter(columnName, regex);
    }

    @Override
    public boolean test(LogEntry log) {
        if (column == null) {
            return pattern.matcher(log.rawLine()).matches();
        } else {
            String columnValue = log.getColumnValues().get(column);
            return columnValue != null && pattern.matcher(columnValue).matches();
        }
    }
}
