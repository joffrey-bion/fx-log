package org.hildan.fxlog.coloring;

import javafx.scene.control.TableRow;
import javafx.scene.paint.Color;

import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.Filter;

/**
 * Colors a log line based on a predicate.
 */
public class StyleRule {

    private final String name;

    private final Filter filter;

    private final Color foreground;

    private final Color background;

    public StyleRule(String name, Filter filter, Color foreground, Color background) {
        this.name = name;
        this.filter = filter;
        this.foreground = foreground;
        this.background = background;
    }

    boolean appliesFor(LogEntry log) {
        return filter.test(log);
    }

    void setStyle(TableRow row) {
        String style = "";
        if (foreground != null) {
            style += "-fx-text-background-color: " + colorString(foreground) + "; ";
        }
        if (background != null) {
            style += "-fx-background-color: " + colorString(background) + "; ";
        }
        row.setStyle(style);
    }

    private static String colorString(Color color) {
        return "#" + color.toString().substring(2);
    }

    @Override
    public String toString() {
        return name;
    }
}
