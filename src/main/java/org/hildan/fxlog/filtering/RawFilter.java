package org.hildan.fxlog.filtering;

import org.hildan.fxlog.core.LogEntry;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A log filter based on a regexp matching the entire initial raw log.
 */
public class RawFilter implements Predicate<LogEntry> {

    private final Pattern pattern;

    public RawFilter(String regex) throws PatternSyntaxException {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean test(LogEntry logEntry) {
        return pattern.matcher(logEntry.getInitialLog()).matches();
    }
}
