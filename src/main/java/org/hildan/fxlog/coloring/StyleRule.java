package org.hildan.fxlog.coloring;

import javafx.geometry.Insets;
import javafx.scene.control.TableRow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.hildan.fxlog.core.LogEntry;

import java.util.function.Predicate;

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
        System.out.println("setting style");
        if (foreground != null) {
            row.setTextFill(foreground);
        }
        if (background != null) {
            row.setBackground(getBackground());
        }
    }

    private Background getBackground() {
        return new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public String toString() {
        return name;
    }
}
