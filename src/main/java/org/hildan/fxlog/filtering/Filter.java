package org.hildan.fxlog.filtering;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import org.hildan.fx.bindings.rulesets.Matcher;
import org.hildan.fxlog.data.LogEntry;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A log filter based on a regexp matching the raw log entry or a column in a log.
 */
public class Filter implements Matcher<LogEntry> {

    public static final Filter MATCH_ALL = findInRawLog(".*");

    public static final Filter ERROR_SEVERITY = findInColumn("severity", "[Ee]rror|ERROR");

    public static final Filter WARN_SEVERITY = findInColumn("severity", "[Ww]arn(ing)?|WARN(ING)?");

    public static final Filter INFO_SEVERITY = findInColumn("severity", "[Ii]nfo|INFO");

    public static final Filter DEBUG_SEVERITY = findInColumn("severity", "[Dd]ebug|DEBUG");

    public static final Filter NOTICE_SEVERITY = findInColumn("severity", "[Nn]otice|NOTICE");

    public static final Filter STACKTRACE_HEAD = findInColumn("msg", "(^\\s*Caused By.*)|^((\\S+\\.)+\\S*Exception.*)");

    public static final Filter STACKTRACE_BODY = findInColumn("msg", "^\\s*at \\S.*");

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
     * @param flags
     *         the match flags, a bit mask that may include {@link Pattern#CASE_INSENSITIVE}, {@link Pattern#MULTILINE},
     *         {@link Pattern#DOTALL}, {@link Pattern#UNICODE_CASE}, {@link Pattern#CANON_EQ}, {@link
     *         Pattern#UNIX_LINES}, {@link Pattern#LITERAL}, {@link Pattern#UNICODE_CHARACTER_CLASS} and {@link
     *         Pattern#COMMENTS}
     *
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    private Filter(String columnName, @RegExp String regex, int flags) throws PatternSyntaxException {
        this.pattern = new SimpleObjectProperty<>(Pattern.compile(regex, flags));
        this.columnName = new SimpleStringProperty(columnName);
    }

    /**
     * Creates a copy of the given Filter.
     *
     * @param source
     *         the filter to copy
     *
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    public Filter(Filter source) throws PatternSyntaxException {
        this.pattern = new SimpleObjectProperty<>(source.pattern.getValue());
        this.columnName = new SimpleStringProperty(source.getColumnName());
    }

    /**
     * Creates a new filter which applies on the raw log line.
     *
     * @param regex
     *         the regex that the whole raw log line should match
     *
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    @NotNull
    public static Filter findInRawLog(@NotNull @RegExp String regex) throws PatternSyntaxException {
        return new Filter(null, regex, 0);
    }

    /**
     * Creates a new filter which applies on the raw log line.
     *
     * @param regex
     *         the regex that the whole raw log line should match
     * @param flags
     *         the match flags, a bit mask that may include {@link Pattern#CASE_INSENSITIVE}, {@link Pattern#MULTILINE},
     *         {@link Pattern#DOTALL}, {@link Pattern#UNICODE_CASE}, {@link Pattern#CANON_EQ}, {@link
     *         Pattern#UNIX_LINES}, {@link Pattern#LITERAL}, {@link Pattern#UNICODE_CHARACTER_CLASS} and {@link
     *         Pattern#COMMENTS}
     *
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    @NotNull
    public static Filter findInRawLog(@NotNull @RegExp String regex, int flags) throws PatternSyntaxException {
        return new Filter(null, regex, flags);
    }

    /**
     * Creates a new filter which applies on the value of the given column.
     *
     * @param columnName
     *         the capturing group name corresponding to the column to apply this filter to
     * @param regex
     *         the regex that the whole value of the given column should match
     *
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    @NotNull
    public static Filter findInColumn(@NotNull String columnName, @NotNull @RegExp String regex) throws
            PatternSyntaxException {
        return findInColumn(columnName, regex, 0);
    }

    /**
     * Creates a new filter which applies on the value of the given column.
     *
     * @param columnName
     *         the capturing group name corresponding to the column to apply this filter to
     * @param regex
     *         the regex that the whole value of the given column should match
     * @param flags
     *         the match flags, a bit mask that may include {@link Pattern#CASE_INSENSITIVE}, {@link Pattern#MULTILINE},
     *         {@link Pattern#DOTALL}, {@link Pattern#UNICODE_CASE}, {@link Pattern#CANON_EQ}, {@link
     *         Pattern#UNIX_LINES}, {@link Pattern#LITERAL}, {@link Pattern#UNICODE_CHARACTER_CLASS} and {@link
     *         Pattern#COMMENTS}
     *
     * @throws PatternSyntaxException
     *         if the given regex is not well formed
     */
    @NotNull
    private static Filter findInColumn(@NotNull String columnName, @NotNull @RegExp String regex, int flags) throws
            PatternSyntaxException {
        return new Filter(columnName, regex, flags);
    }

    public String getColumnName() {
        return columnName.get();
    }

    public StringProperty columnNameProperty() {
        return columnName;
    }

    @SuppressWarnings("unused")
    public void setColumnName(String columnName) {
        this.columnName.set(columnName);
    }

    public Pattern getPattern() {
        return pattern.getValue();
    }

    public Property<Pattern> patternProperty() {
        return pattern;
    }

    @SuppressWarnings("unused")
    public void setPattern(Pattern pattern) {
        this.pattern.setValue(pattern);
    }

    @Override
    public boolean test(@Nullable LogEntry log) {
        if (log == null) {
            return false;
        }
        if (columnName.get() == null) {
            return pattern.getValue().matcher(log.rawLine()).find();
        } else {
            String columnValue = log.getColumnValues().get(columnName.get());
            return columnValue != null && pattern.getValue().matcher(columnValue).find();
        }
    }

    @Override
    public Binding<Boolean> matches(ObservableValue<LogEntry> logObservable) {
        return Bindings.createBooleanBinding(() -> test(logObservable.getValue()), logObservable, pattern, columnName);
    }
}
