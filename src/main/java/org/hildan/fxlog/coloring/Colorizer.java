package org.hildan.fxlog.coloring;

import javafx.scene.control.TableRow;
import javafx.scene.paint.Color;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.ColumnFilter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Colors a log entry based on a list of coloring rules.
 */
public class Colorizer {

    public static final Colorizer WEBLOGIC;

    static {
        Predicate<LogEntry> errorFilter = new ColumnFilter("severity", "[Ee]rror");
        Predicate<LogEntry> warnFilter = new ColumnFilter("severity", "[Ww]arn(ing)?");
        Predicate<LogEntry> infoFilter = new ColumnFilter("severity", "[Ii]nfo");
        Predicate<LogEntry> debugFilter = new ColumnFilter("severity", "[Dd]ebug");
        Predicate<LogEntry> noticeFilter = new ColumnFilter("severity", "[Nn]otice");
        StyleRule errorRule = new StyleRule("Error", errorFilter, Color.web("#DD0000"), Color.web("#DD0000"));
        StyleRule warnRule = new StyleRule("Warn", warnFilter, Color.web("#DD9900"), Color.web("#DD9900"));
        StyleRule infoRule = new StyleRule("Info", infoFilter, Color.web("#00DD00"), Color.web("#00DD00"));
        StyleRule debugRule = new StyleRule("Debug", debugFilter, Color.web("#0000DD"), null);
        StyleRule noticeRule = new StyleRule("Notice", noticeFilter, Color.web("#0000DD"), null);
        WEBLOGIC = new Colorizer("Weblogic", Arrays.asList(errorRule, warnRule, infoRule, debugRule, noticeRule));
    }

    private final String name;

    private final List<StyleRule> styleRules;

    public Colorizer(String name, List<StyleRule> styleRules) {
        this.name = name;
        this.styleRules = styleRules;
    }

    /**
     * @return the name of this colorizer
     */
    public String getName() {
        return name;
    }

    public void setStyle(TableRow row, LogEntry log) {
        for (StyleRule rule : styleRules) {
            if (rule.appliesFor(log)) {
                rule.setStyle(row);
            }
        }
    }
}
