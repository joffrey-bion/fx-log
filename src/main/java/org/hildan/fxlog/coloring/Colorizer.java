package org.hildan.fxlog.coloring;

import java.util.Arrays;
import java.util.List;

import javafx.scene.control.TableRow;
import javafx.scene.paint.Color;

import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.Filter;

/**
 * Colors a log entry based on a list of coloring rules.
 */
public class Colorizer {

    public static final Colorizer WEBLOGIC;

    static {
        Filter errorFilter = Filter.matchColumn("severity", "[Ee]rror");
        Filter warnFilter = Filter.matchColumn("severity", "[Ww]arn(ing)?");
        Filter infoFilter = Filter.matchColumn("severity", "[Ii]nfo");
        Filter debugFilter = Filter.matchColumn("severity", "[Dd]ebug");
        Filter noticeFilter = Filter.matchColumn("severity", "[Nn]otice");
        StyleRule errorRule = new StyleRule("Error", errorFilter, Color.web("#AA0000"), null);
        StyleRule warnRule = new StyleRule("Warn", warnFilter, Color.web("#AA8800"), null);
        StyleRule infoRule = new StyleRule("Info", infoFilter, Color.web("#00AA00"), null);
        StyleRule debugRule = new StyleRule("Debug", debugFilter, Color.web("#0000BB"), null);
        StyleRule noticeRule = new StyleRule("Notice", noticeFilter, Color.web("#000000"), null);
        WEBLOGIC = new Colorizer("Severity-based", Arrays.asList(errorRule, warnRule, infoRule, debugRule, noticeRule));
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
        row.setStyle(null);
        for (StyleRule rule : styleRules) {
            if (rule.appliesFor(log)) {
                rule.setStyle(row);
                return;
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
