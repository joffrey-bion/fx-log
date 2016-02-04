package org.hildan.fxlog.filtering;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.hildan.fxlog.core.LogEntry;

/**
 * A log filter based on a regexp matching the raw log entry or a column in a log.
 */
public class Filter implements Predicate<LogEntry> {

    private final String column;

    private final Pattern pattern;

    private Filter(String column, String regex) throws PatternSyntaxException {
        this.pattern = Pattern.compile(regex);
        this.column = column;
    }

    public static Filter matchRawLog(String regex) {
        return new Filter(null, regex);
    }

    public static Filter matchColumn(String columnName, String regex) {
        return new Filter(columnName, regex);
    }

    @Override
    public boolean test(LogEntry log) {
        if (column == null) {
            return matchesRawLog(log);
        } else {
            return matchesColumn(log);
        }
    }

    private boolean matchesRawLog(LogEntry log) {
        return pattern.matcher(log.getInitialLog()).matches();
    }

    private boolean matchesColumn(LogEntry log) {
        String columnValue = log.getColumnValues().get(column);
        if (columnValue == null) {
            return false;
        }
        return pattern.matcher(columnValue).matches();
    }
}
