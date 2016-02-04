package org.hildan.fxlog.filtering;

import org.hildan.fxlog.core.LogEntry;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A log filter based on a regexp matching a column in a log.
 */
public class ColumnFilter implements Predicate<LogEntry> {

    private final String column;

    private final Pattern pattern;

    public ColumnFilter(String column, String regex) throws PatternSyntaxException {
        this.column = column;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean test(LogEntry logEntry) {
        String columnValue = logEntry.getColumnValues().get(column);
        if (columnValue == null) {
            return false;
        }
        return pattern.matcher(columnValue).matches();
    }
}
