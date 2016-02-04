package org.hildan.fxlog.coloring;

import java.util.function.Predicate;

import javafx.scene.control.TableRow;
import javafx.scene.paint.Color;

import org.hildan.fxlog.core.LogEntry;

/**
 * Colors a log line based on a predicate.
 */
public class StyleRule {

    private final String name;

    private final Predicate<LogEntry> predicate;

    private final Color foreground;

    private final Color background;

    public StyleRule(String name, Predicate<LogEntry> predicate, Color foreground, Color background) {
        this.name = name;
        this.predicate = predicate;
        this.foreground = foreground;
        this.background = background;
    }

    /**
     * @return the name of this style rule
     */
    public String getName() {
        return name;
    }

    boolean appliesFor(LogEntry log) {
        return predicate.test(log);
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
